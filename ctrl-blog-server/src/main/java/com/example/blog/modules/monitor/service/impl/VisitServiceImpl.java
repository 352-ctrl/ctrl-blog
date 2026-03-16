package com.example.blog.modules.monitor.service.impl;

import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.blog.common.constants.RedisConstants;
import com.example.blog.common.utils.RedisUtil;
import com.example.blog.modules.monitor.mapper.VisitMapper;
import com.example.blog.modules.monitor.model.entity.DailyVisit;
import com.example.blog.modules.monitor.model.vo.VisitTrendVO;
import com.example.blog.modules.monitor.service.VisitService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
public class VisitServiceImpl extends ServiceImpl<VisitMapper, DailyVisit> implements VisitService {

    @Resource
    private RedisUtil redisUtil;

    @Override
    public void recordVisit(String ip) {
        // 构建当前 IP 的防刷限制 Key (例如: blog:view:day:lock:192.168.1.1)
        String lockKey = RedisConstants.VIEW_COUNT_DAY_PREFIX + "lock:" + ip;

        // 如果 Redis 中不存在这个 IP 的锁，说明是新访客或冷却期已过，才执行真实的 +1 操作
        if (redisUtil.get(lockKey) == null) {
            // 1. 增加总访问量 +1
            redisUtil.increment(RedisConstants.VIEW_COUNT_KEY, 1);

            // 2. 今日访问量 +1
            String todayKey = RedisConstants.VIEW_COUNT_DAY_PREFIX + LocalDate.now();
            redisUtil.increment(todayKey, 1);
            // 设置过期时间，防止长期堆积占用内存
            redisUtil.expire(todayKey, RedisConstants.EXPIRE_VIEW_COUNT_DAY, TimeUnit.DAYS);

            // 3. 核心机制：给该 IP 加上冷却期锁（30 分钟内同一个 IP 重复访问不增加总访问量）
            redisUtil.set(lockKey, "1", 30, TimeUnit.MINUTES);
        }
    }

    @Override
    public Long countTotalVisits() {
        Object countObj = redisUtil.get(RedisConstants.VIEW_COUNT_KEY);

        if (ObjectUtil.isNotNull(countObj)) {
            return Long.parseLong(countObj.toString());
        }

        QueryWrapper<DailyVisit> queryWrapper = new QueryWrapper<>();
        // IFNULL 保证如果表是空的，返回 0 而不是 null
        queryWrapper.select("IFNULL(SUM(views), 0)");

        Object result = this.getObj(queryWrapper, o -> o);
        Long totalFromDb = result != null ? Long.parseLong(result.toString()) : 0L;

        // 回写 Redis，保持数据热度
        redisUtil.set(RedisConstants.VIEW_COUNT_KEY, totalFromDb, RedisConstants.EXPIRE_VIEW_COUNT_MINUTES, TimeUnit.HOURS);

        return totalFromDb;
    }

    @Override
    public VisitTrendVO getVisitTrendStats() {
        LocalDate today = LocalDate.now();
        LocalDate startDate = today.minusDays(6);

        List<DailyVisit> dbList = this.lambdaQuery()
                .ge(DailyVisit::getDate, startDate)
                .le(DailyVisit::getDate, today)
                .list();

        // 转为 Map<String, Integer> 方便后续查找，key是日期字符串
        Map<String, Long> dbMap = dbList.stream()
                .collect(Collectors.toMap(
                        v -> v.getDate().toString(),
                        DailyVisit::getViews,
                        (v1, v2) -> v1
                ));

        List<String> dateList = new ArrayList<>();
        List<Long> pvCountList = new ArrayList<>();

        // 循环组装数据
        for (int i = 6; i >= 0; i--) {
            LocalDate date = today.minusDays(i);
            String dateStr = date.toString();
            dateList.add(dateStr);

            // 优先查 Redis (因为“今天”的数据在 DB 里可能是不准的/空的，Redis 才是实时的)
            String redisKey = RedisConstants.VIEW_COUNT_DAY_PREFIX + dateStr;
            Object redisCount = redisUtil.get(redisKey);

            if (ObjectUtil.isNotNull(redisCount)) {
                // 如果 Redis 有，直接用 Redis 的 (实时性最高)
                pvCountList.add(Long.valueOf((redisCount.toString())));
            } else {
                // Redis 没有 (过期了)，用 DB Map 里的缓存 (内存匹配，不查库)
                pvCountList.add(dbMap.getOrDefault(dateStr, 0L));
            }
        }

        return VisitTrendVO.builder()
                .dates(dateList)
                .pvCounts(pvCountList)
                .build();
    }

    @Override
    public void syncVisitDataToDb() {
        // 1. 获取今天的 Redis Key
        LocalDate today = LocalDate.now();
        String todayKey = RedisConstants.VIEW_COUNT_DAY_PREFIX + today;

        // 2. 从 Redis 取出今天的访问量
        Object countObj = redisUtil.get(todayKey);
        if (countObj == null) {
            // 如果 Redis 里没有（比如刚过零点，还没人访问），直接返回，不操作数据库
            return;
        }
        Long redisCount = Long.valueOf(((countObj.toString())));

        // 3. 查询数据库里有没有今天的记录
        DailyVisit visit = this.lambdaQuery()
                .eq(DailyVisit::getDate, today)
                .one();

        // 4. 数据落库 (Upsert 逻辑)
        if (visit == null) {
            // 场景 A：今天第一次同步（数据库还没记录） -> 执行插入
            visit = new DailyVisit();
            visit.setDate(today);
            visit.setViews(redisCount);
            this.save(visit);
        } else {
            // 场景 B：数据库已有记录 -> 执行更新
            // 只有当 Redis 的数据比数据库大时才更新（防止 Redis 意外丢数据导致覆盖了 DB 的正确数据）
            if (redisCount > visit.getViews()) {
                visit.setViews(redisCount);
                this.updateById(visit);
            }
        }
    }
}