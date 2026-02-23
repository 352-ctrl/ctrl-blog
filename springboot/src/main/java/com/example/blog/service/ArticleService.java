package com.example.blog.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.example.blog.dto.article.*;
import com.example.blog.entity.Article;
import com.example.blog.vo.article.*;

import java.util.List;

/**
 * 文章业务服务接口
 * 定义文章相关的业务操作方法
 */
public interface ArticleService extends IService<Article> {

    /**
     * 获取侧边栏热门文章列表
     *
     * @return 热门文章列表
     */
    List<ArticleHotVO> listHotArticles();

    /**
     * 获取全站搜索索引数据
     *
     * @return 文章搜索索引列表
     */
    List<ArticleSearchVO> listSearchIndexes();

    /**
     * 前台归档获取文章
     *
     * @return 文章列表
     */
    List<ArchiveAggregateVO> listArchives();

    /**
     * 获取前台首页轮播图列表
     *
     * @return 轮播图文章列表
     */
    List<ArticleCarouselVO> listCarousel();

    /**
     * 前台列表分页查询文章
     *
     * @param articleQueryDTO 查询条件DTO
     * @return 分页结果
     */
    IPage<ArticleCardVO> pageArticles(ArticleQueryDTO articleQueryDTO);

    /**
     * 后台分页查询文章
     *
     * @param articleQueryDTO 查询条件DTO
     * @return 分页结果
     */
    IPage<AdminArticleVO> pageAdminArticles(ArticleQueryDTO articleQueryDTO);

    /**
     * 保存文章（包含标签关联处理）
     *
     * @param articleAddDTO 文章DTO
     */
    void addArticle(ArticleAddDTO articleAddDTO);

    /**
     * 更新文章（包含标签关联处理）
     *
     * @param articleUpdateDTO 文章DTO
     */
    void updateArticle(ArticleUpdateDTO articleUpdateDTO);

    /**
     * 删除文章（包含标签关联处理）
     *
     * @param id 文章ID
     */
    void deleteArticleById(Long id);

    /**
     * 批量删除文章（包含标签关联处理）
     *
     * @param ids 文章ID列表
     */
    void batchDeleteArticles(List<Long> ids);

    /**
     * 增加文章浏览量
     *
     * @param visitorDTO 文章访问者DTO
     */
    void incrementViewCount(ArticleVisitorDTO visitorDTO);

    /**
     * 前台获取文章详情
     *
     * @param id 文章ID
     * @return 文章详情
     */
    ArticleDetailVO getArticleDetail(Long id);

    /**
     * 后台获取文章详情
     *
     * @param id 文章ID
     * @return 文章详情
     */
    AdminArticleVO getArticleForEdit(Long id);

    /**
     * 获取按分类分组的文章数量统计
     *
     * @return 统计列表
     */
    List<ArticleCategoryCountDTO> countArticleByCategoryId();

    /**
     * 同步 Redis 中的文章阅读数到数据库
     * 该方法由 Quartz 定时任务调用
     */
    void syncArticleViewsToDb();

}