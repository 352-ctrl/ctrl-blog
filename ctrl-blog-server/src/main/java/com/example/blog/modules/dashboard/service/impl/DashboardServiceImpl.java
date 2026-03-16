package com.example.blog.modules.dashboard.service.impl;

import com.example.blog.modules.article.model.dto.ArticleCategoryCountDTO;
import com.example.blog.modules.article.service.ArticleService;
import com.example.blog.modules.dashboard.model.vo.DashboardVO;
import com.example.blog.modules.dashboard.service.DashboardService;
import com.example.blog.modules.monitor.model.vo.VisitTrendVO;
import com.example.blog.modules.monitor.service.VisitService;
import com.example.blog.modules.operation.service.CommentService;
import com.example.blog.modules.user.service.UserService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
public class DashboardServiceImpl implements DashboardService {

    @Resource
    private ArticleService articleService;

    @Resource
    private UserService userService;

    @Resource
    private CommentService commentService;

    @Resource
    private VisitService visitService;

    @Override
    @SuppressWarnings("unchecked")
    public DashboardVO getDashboardData() {
        DashboardVO vo = new DashboardVO();

        // 获取基础数据
        Long articleCount = articleService.count();
        Long userCount = userService.count();
        Long commentCount = commentService.count();
        Long visitCount = visitService.countTotalVisits();

        // 获取趋势图数据 (折线图)
        VisitTrendVO trendVO = visitService.getVisitTrendStats();

        DashboardVO.VisitTrend visitTrend = DashboardVO.VisitTrend.builder()
                .dates(trendVO.getDates())
                .pvCounts(trendVO.getPvCounts())
                .build();

        // 获取分类占比数据 (饼图)
        List<ArticleCategoryCountDTO> categoryStats = articleService.countArticleByCategoryId();

        // DTO 转换逻辑
        List<DashboardVO.CategoryPie> categoryPieList = Optional.ofNullable(categoryStats)
                .orElse(Collections.emptyList())
                .stream()
                .map(dto -> DashboardVO.CategoryPie.builder()
                        // 假设 ArticleService 已经填充了分类名称
                        // 如果没有填充，这里可能是 "未知分类" 或者 null
                        .name(dto.getCategoryName() != null ? dto.getCategoryName() : null)
                        .value(dto.getCount() != null ? dto.getCount() : 0L)
                        .build())
                .filter(pie -> pie.getValue() > 0) // 过滤掉文章数为0的（可选）
                .collect(Collectors.toList());

        // 组装最终 DashboardVO
        return DashboardVO.builder()
                .articleCount(articleCount)
                .userCount(userCount)
                .commentCount(commentCount)
                .visitCount(visitCount)
                .visitTrend(visitTrend)
                .categoryPie(categoryPieList)
                .build();
    }
}