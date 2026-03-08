package com.example.blog.modules.operation.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.blog.common.constants.Constants;
import com.example.blog.common.constants.MessageConstants;
import com.example.blog.common.constants.RedisConstants;
import com.example.blog.common.enums.BizStatus;
import com.example.blog.common.utils.GravatarUtils;
import com.example.blog.common.utils.RedisUtil;
import com.example.blog.core.security.UserContext;
import com.example.blog.modules.operation.mapper.DanmakuMapper;
import com.example.blog.modules.operation.model.dto.DanmakuAddDTO;
import com.example.blog.modules.operation.model.dto.DanmakuQueryDTO;
import com.example.blog.modules.operation.model.entity.Danmaku;
import com.example.blog.modules.operation.model.vo.DanmakuVO;
import com.example.blog.modules.operation.service.DanmakuService;
import com.example.blog.modules.system.validator.SensitiveWordManager;
import com.example.blog.modules.user.model.dto.UserPayloadDTO;
import com.example.blog.modules.user.model.entity.User;
import com.example.blog.modules.user.service.UserService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
import org.springframework.web.util.HtmlUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
public class DanmakuServiceImpl extends ServiceImpl<DanmakuMapper, Danmaku> implements DanmakuService {

    @Resource
    private SensitiveWordManager sensitiveWordManager;

    @Resource
    private UserService userService;

    @Resource
    private RedisUtil redisUtil;

    @Override
    @SuppressWarnings("unchecked")
    public List<DanmakuVO> listDanmakus() {
        // 1. 优先尝试从 Redis 缓存中读取
        Object cachedDanmakus = redisUtil.get(RedisConstants.REDIS_DANMAKU_LIST_KEY);
        if (cachedDanmakus != null) {
            return (List<DanmakuVO>) cachedDanmakus;
        }

        // 2. 如果 Redis 中没有，则从 MySQL 查出最新的 100 条
        List<Danmaku> list = this.list(new LambdaQueryWrapper<Danmaku>()
                .orderByDesc(Danmaku::getCreateTime)
                .last("LIMIT 100"));

        List<DanmakuVO> voList = list.stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());

        // 3. 将查出的结果回填到 Redis 缓存
        redisUtil.set(RedisConstants.REDIS_DANMAKU_LIST_KEY, voList, RedisConstants.EXPIRE_DANMAKU_LIST, TimeUnit.HOURS);

        return voList;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addDanmaku(DanmakuAddDTO addDTO) {
        Assert.notNull(addDTO, "内容不能为空");

        String safeContent = HtmlUtils.htmlEscape(addDTO.getContent());
        safeContent = sensitiveWordManager.replace(safeContent, Constants.SENSITIVE_REPLACE_CHAR);

        UserPayloadDTO currentUser = UserContext.get();
        Danmaku danmaku = Danmaku.builder()
                .content(safeContent)
                .build();

        if (currentUser != null) {
            // 2. 登录用户：从数据库获取最新用户信息
            User user = userService.getById(currentUser.getId());
            danmaku.setUserId(currentUser.getId());

            // 优先用数据库昵称，其次用 Token 里的昵称
            String nickname = (user != null && StrUtil.isNotBlank(user.getNickname()))
                    ? user.getNickname()
                    : currentUser.getNickname();
            danmaku.setNickname(nickname);

            // 优先用数据库头像，没有则根据昵称生成复古像素头像兜底
            String avatar = (user != null && StrUtil.isNotBlank(user.getAvatar()))
                    ? user.getAvatar()
                    : GravatarUtils.getRetroAvatar(nickname + Constants.GRAVATAR_DUMMY_DOMAIN);
            danmaku.setAvatar(avatar);
        } else {
            // 3. 匿名用户：使用 "匿名游客_随机字符" 兜底，并生成随机像素头像
            danmaku.setNickname(Constants.DEFAULT_GUEST_NICKNAME + StrUtil.UNDERLINE + RandomUtil.randomString(5));
            danmaku.setAvatar(GravatarUtils.getRetroAvatar(null));
        }

        // 4. 保存到 MySQL
        this.save(danmaku);

        // 5. 删除缓存！让下次刷新时重新从数据库拉取最新的数据
        redisUtil.delete(RedisConstants.REDIS_DANMAKU_LIST_KEY);
    }

    @Override
    public IPage<DanmakuVO> pageAdminDanmakus(DanmakuQueryDTO queryDTO) {
        Page<Danmaku> page = new Page<>(queryDTO.getPageNum(), queryDTO.getPageSize());
        LambdaQueryWrapper<Danmaku> wrapper = new LambdaQueryWrapper<>();

        wrapper.like(StrUtil.isNotBlank(queryDTO.getContent()), Danmaku::getContent, queryDTO.getContent())
                .like(StrUtil.isNotBlank(queryDTO.getNickname()), Danmaku::getNickname, queryDTO.getNickname())
                .orderByDesc(Danmaku::getCreateTime);

        return this.page(page, wrapper).convert(this::convertToVO);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteDanmakuById(Long id) {
        Assert.notNull(id, "ID不能为空");
        this.lambdaUpdate()
                .eq(Danmaku::getId, id)
                .set(Danmaku::getIsDeleted, BizStatus.DeleteStatus.DELETED)
                .set(Danmaku::getDeleteTime, LocalDateTime.now())
                .update();

        redisUtil.delete(RedisConstants.REDIS_DANMAKU_LIST_KEY);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void batchDeleteDanmakus(List<Long> ids) {
        if (CollUtil.isEmpty(ids)) return;

        boolean success = this.lambdaUpdate()
                .in(Danmaku::getId, ids)
                .set(Danmaku::getIsDeleted, BizStatus.DeleteStatus.DELETED)
                .set(Danmaku::getDeleteTime, LocalDateTime.now())
                .update();

        if (!success) {
            throw new RuntimeException(MessageConstants.MSG_BATCH_DELETE_FAILED);
        }

        redisUtil.delete(RedisConstants.REDIS_DANMAKU_LIST_KEY);
    }

    private DanmakuVO convertToVO(Danmaku entity) {
        return DanmakuVO.builder()
                .id(entity.getId())
                .content(entity.getContent())
                .nickname(entity.getNickname())
                .avatar(entity.getAvatar())
                .createTime(entity.getCreateTime())
                .build();
    }
}