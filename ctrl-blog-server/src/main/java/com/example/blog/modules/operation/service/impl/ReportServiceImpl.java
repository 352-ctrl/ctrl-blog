package com.example.blog.modules.operation.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.blog.common.constants.MessageConstants;
import com.example.blog.common.enums.BizStatus;
import com.example.blog.core.exception.CustomerException;
import com.example.blog.core.security.UserContext;
import com.example.blog.modules.article.model.entity.Article;
import com.example.blog.modules.article.service.ArticleService;
import com.example.blog.modules.operation.builder.ReportContextBuilder;
import com.example.blog.modules.operation.event.ReportProcessedEvent;
import com.example.blog.modules.operation.mapper.ReportMapper;
import com.example.blog.modules.operation.model.bo.ReportExtraContext;
import com.example.blog.modules.operation.model.dto.ReportAddDTO;
import com.example.blog.modules.operation.model.dto.ReportProcessDTO;
import com.example.blog.modules.operation.model.dto.ReportQueryDTO;
import com.example.blog.modules.operation.model.entity.Comment;
import com.example.blog.modules.operation.model.entity.Report;
import com.example.blog.modules.operation.model.vo.AdminReportVO;
import com.example.blog.modules.operation.service.CommentService;
import com.example.blog.modules.operation.service.ReportService;
import com.example.blog.modules.user.event.UserBannedEvent;
import com.example.blog.modules.user.model.dto.UserPayloadDTO;
import com.example.blog.modules.user.model.entity.User;
import com.example.blog.modules.user.service.AuthService;
import com.example.blog.modules.user.service.UserService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
public class ReportServiceImpl extends ServiceImpl<ReportMapper, Report> implements ReportService {

    @Resource
    private UserService userService;

    @Resource
    private ArticleService articleService;

    @Resource
    private CommentService commentService;

    @Resource
    private AuthService authService;

    @Resource
    private ApplicationEventPublisher eventPublisher;

    @Resource
    private ReportContextBuilder reportContextBuilder;

    @Override
    public void addReport(ReportAddDTO addDTO) {
        Assert.notNull(addDTO, "举报参数不能为空");

        UserPayloadDTO currentUser = UserContext.get();
        Assert.notNull(currentUser, "请先登录后再进行举报");

        // 1. DTO 转 Entity
        Report report = BeanUtil.copyProperties(addDTO, Report.class);
        report.setUserId(currentUser.getId()); // 设置当前登录用户ID

        // 2. 默认状态设为待处理
        report.setStatus(BizStatus.ReportStatus.PENDING);
        this.save(report);
    }

    @Override
    public IPage<AdminReportVO> pageAdminReports(ReportQueryDTO queryDTO) {
        Page<Report> page = new Page<>(queryDTO.getPageNum(), queryDTO.getPageSize());
        LambdaQueryWrapper<Report> queryWrapper = new LambdaQueryWrapper<>();

        queryWrapper.eq(queryDTO.getStatus() != null, Report::getStatus, queryDTO.getStatus())
                .eq(StrUtil.isNotBlank(queryDTO.getTargetType()), Report::getTargetType, queryDTO.getTargetType())
                .orderByAsc(Report::getStatus)
                .orderByDesc(Report::getCreateTime);

        // 1. 查询实体分页数据
        Page<Report> entityPage = this.page(page, queryWrapper);
        Page<AdminReportVO> voPage = new Page<>(entityPage.getCurrent(), entityPage.getSize(), entityPage.getTotal());
        List<Report> records = entityPage.getRecords();

        if (CollUtil.isEmpty(records)) {
            return voPage;
        }

        // 2. 调用 Builder 构建上下文环境
        ReportExtraContext context = reportContextBuilder.buildContext(records);

        // 3. 组装 VO 列表 (现在这里的逻辑极其精简)
        List<AdminReportVO> voList = records.stream().map(report -> {
            AdminReportVO vo = BeanUtil.copyProperties(report, AdminReportVO.class);
            // 从上下文中智能获取昵称和摘要
            vo.setUserNickname(context.getReporterNickname(vo.getUserId()));
            vo.setTargetSummary(context.getTargetSummary(report.getTargetType(), report.getTargetId()));
            return vo;
        }).toList();

        voPage.setRecords(voList);
        return voPage;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void processReport(ReportProcessDTO processDTO) {
        Assert.notNull(processDTO, "处理参数不能为空");

        // 1. 获取原始的举报记录，确认它是存在且需要获取它关联的 targetType 和 targetId
        Report originalReport = this.getById(processDTO.getId());
        Assert.notNull(originalReport, "未找到该举报记录");

        // 2. 更新举报处理状态
        BizStatus.ReportStatus newStatus = BizStatus.ReportStatus.getByCode(processDTO.getStatus());
        Report updateReport = new Report();
        updateReport.setId(processDTO.getId());
        updateReport.setAdminNote(processDTO.getAdminNote());
        updateReport.setStatus(newStatus);
        this.updateById(updateReport);

        // 为了传给 Event 完整的数据，合并一下最新状态
        originalReport.setStatus(newStatus);
        originalReport.setAdminNote(processDTO.getAdminNote());

        Long targetUserId = null;

        // 3. 自动化联动惩罚机制：如果管理员判定举报属实
        if (BizStatus.ReportStatus.VALID.equals(newStatus)) {
            BizStatus.ReportTargetType targetType = originalReport.getTargetType();
            Long targetId = originalReport.getTargetId();

            if (BizStatus.ReportTargetType.COMMENT.equals(targetType)) {
                // 删除违规评论并获取评论作者ID
                Comment comment = commentService.getById(targetId);
                if (comment != null) targetUserId = comment.getUserId();
                commentService.deleteCommentById(targetId);
                log.info("已自动隐藏违规评论，ID: {}", targetId);

            } else if (BizStatus.ReportTargetType.ARTICLE.equals(targetType)) {
                // 下架文章并获取文章作者ID
                Article article = articleService.getById(targetId);
                if (article != null) targetUserId = article.getUserId();
                articleService.lambdaUpdate()
                        .eq(Article::getId, targetId)
                        .set(Article::getStatus, BizStatus.Article.DRAFT)
                        .update();
                log.info("已自动下架违规文章，ID: {}", targetId);

            } else if (BizStatus.ReportTargetType.USER.equals(targetType)) {
                // 封禁用户并设置状态为 DISABLE
                targetUserId = targetId;

                Integer disableDays = processDTO.getDisableDays();
                LocalDateTime endTime = null;

                if (disableDays != null && disableDays > 0) {
                    endTime = LocalDateTime.now().plusDays(disableDays);
                } else if (disableDays != null && disableDays == -1) {
                    // 永久封禁，给个极大值
                    endTime = LocalDateTime.of(2099, 12, 31, 23, 59, 59);
                }

                String banReason = StrUtil.isNotBlank(processDTO.getAdminNote()) ? processDTO.getAdminNote() : MessageConstants.MSG_DEFAULT_BAN_REASON;

                // 获取被封禁用户的完整信息
                User bannedUser = userService.getById(targetId);

                userService.lambdaUpdate()
                        .eq(User::getId, targetId)
                        .set(User::getStatus, BizStatus.User.DISABLE)
                        .set(User::getDisableEndTime, endTime)
                        .set(User::getDisableReason, StrUtil.isNotBlank(processDTO.getAdminNote()) ? processDTO.getAdminNote() : MessageConstants.MSG_DEFAULT_BAN_REASON)
                        .update();

                authService.forceLogoutByUserId(targetId);
                log.info("已自动封禁违规用户，ID: {}，封禁至: {}", targetId, endTime);

                if (bannedUser != null && StrUtil.isNotBlank(bannedUser.getEmail())) {
                    eventPublisher.publishEvent(new UserBannedEvent(
                            this,
                            bannedUser.getId(),
                            bannedUser.getEmail(),
                            bannedUser.getNickname(),
                            banReason,
                            endTime
                    ));
                }
            }
        }

        // 4. 发布事件，交由 MessageService 处理后续发信通知
        eventPublisher.publishEvent(new ReportProcessedEvent(this, originalReport, targetUserId));

    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteReportById(Long id) {
        Assert.notNull(id, "举报ID不能为空");

        boolean success = this.removeById(id);

        if (!success) {
            throw new CustomerException(MessageConstants.MSG_REPORT_NOT_EXIST);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void batchDeleteReports(List<Long> ids) {
        Assert.notEmpty(ids, "举报ID列表不能为空");

        boolean success = this.removeBatchByIds(ids);

        if (!success) {
            throw new CustomerException(MessageConstants.MSG_BATCH_DELETE_FAILED);
        }
    }
}