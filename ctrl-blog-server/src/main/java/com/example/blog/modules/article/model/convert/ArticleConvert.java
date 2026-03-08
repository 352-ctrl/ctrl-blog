package com.example.blog.modules.article.model.convert;

import com.example.blog.common.base.BizStatusTransform;
import com.example.blog.modules.article.model.bo.ArticleExtraContext;
import com.example.blog.modules.article.model.vo.*;
import com.example.blog.common.base.BaseConvert;
import com.example.blog.modules.article.model.dto.ArticleAddDTO;
import com.example.blog.modules.article.model.dto.ArticleUpdateDTO;
import com.example.blog.modules.article.model.entity.Article;
import com.example.blog.common.utils.MarkdownUtil;
import org.mapstruct.*;

import java.util.List;

/**
 * 文章专属转换接口（继承通用接口）
 */
@Mapper(
        componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.IGNORE,
        uses = {BaseConvert.class, BizStatusTransform.class, MarkdownUtil.class}
)
public interface ArticleConvert extends BaseConvert<Article, ArticleAddDTO, ArticleUpdateDTO, ArticleDetailVO> {

    @Override
    @Mapping(target = "contentHtml", source = "content", qualifiedByName = "markdownToHtml")
    Article addDtoToEntity(ArticleAddDTO addDTO);

    @Override
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "contentHtml", source = "content", qualifiedByName = "markdownToHtml")
    void updateEntityFromDto(ArticleUpdateDTO updateDTO, @MappingTarget Article article);

    @Override
    ArticleDetailVO entityToVo(Article article);

    ArticleCarouselVO entityToCarouselVo(Article article);

    List<ArticleCarouselVO> entitiesToCarouseVos(List<Article> articles);

    ArticleArchiveVO entityToArchiveVo(Article article);

    List<ArticleArchiveVO> entitiesToArchiveVos(List<Article> articles);

    /**
     * 单个文章实体转换为后台文章VO
     */
    @Mappings({
            @Mapping(target = "userNickname", expression = "java(context.getUserNickname(article.getUserId()))"),
            @Mapping(target = "categoryName", expression = "java(context.getCategoryName(article.getCategoryId()))"),
            @Mapping(target = "tags", expression = "java(context.getArticleTags(article.getId()))")
    })
    AdminArticleVO entityToAdminVo(Article article, @Context ArticleExtraContext context);

    List<AdminArticleVO> entitiesToAdminVos(List<Article> articles, @Context ArticleExtraContext context);

    /**
     * 单个文章实体转换为前台文章列表VO
     */
    @Mappings({
            @Mapping(target = "userNickname", expression = "java(context.getUserNickname(article.getUserId()))"),
            @Mapping(target = "userAvatar", expression = "java(context.getUserAvatar(article.getUserId()))"),
            @Mapping(target = "categoryName", expression = "java(context.getCategoryName(article.getCategoryId()))"),
            @Mapping(target = "tags", expression = "java(context.getArticleTags(article.getId()))")
    })
    ArticleCardVO entityToListVo(Article article, @Context ArticleExtraContext context);

    List<ArticleCardVO> entitiesToListVos(List<Article> articles, @Context ArticleExtraContext context);

    /**
     * 单个文章实体转换为文章搜索索引VO
     */
    @Mappings({
            @Mapping(target = "categoryName", expression = "java(context.getCategoryName(article.getCategoryId()))"),
            @Mapping(target = "tags", expression = "java(context.getArticleTags(article.getId()))")
    })
    ArticleSearchVO entityToSearchVo(Article article, @Context ArticleExtraContext context);

    List<ArticleSearchVO> entitiesToSearchVos(List<Article> articles, @Context ArticleExtraContext context);

    ArticleSimpleVO entityToSimpleVo(Article article);

    List<ArticleSimpleVO> entitiesToSimpleVos(List<Article> articles);

}