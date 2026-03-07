package com.example.blog.modules.article.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.example.blog.modules.article.model.dto.*;
import com.example.blog.modules.article.model.entity.Article;
import com.example.blog.modules.article.model.vo.*;

import java.time.LocalDateTime;
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
    List<ArticleSimpleVO> listHotArticles();

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
     * 根据 ID 列表批量获取文章卡片列表（并保持传入 ID 的顺序）
     *
     * @param ids 文章ID列表
     * @return 组装好的文章卡片列表
     */
    List<ArticleSimpleVO> listArticleCardsByIds(List<Long> ids);

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

    /**
     * 清理文章回收站（包含级联清理标签关联和评论）
     * @param recycleLimitDate 过期时间阈值
     * @return 成功删除的文章数量
     */
    int clearArticleTrash(LocalDateTime recycleLimitDate);

    /**
     * 聚合操作：文章点赞 (落盘关联记录、增加计数、发消息)
     */
    Long likeArticle(Long articleId);

    /**
     * 聚合操作：取消文章点赞
     */
    Long cancelLikeArticle(Long articleId);

    /**
     * 聚合操作：文章收藏
     */
    Long favoriteArticle(Long articleId);

    /**
     * 聚合操作：取消文章收藏
     */
    Long cancelFavoriteArticle(Long articleId);

}