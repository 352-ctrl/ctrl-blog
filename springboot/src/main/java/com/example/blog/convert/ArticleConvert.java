package com.example.blog.convert;

import com.example.blog.dto.article.ArticleAddDTO;
import com.example.blog.dto.article.ArticleUpdateDTO;
import com.example.blog.entity.Article;
import com.example.blog.utils.MarkdownUtil;
import com.example.blog.vo.TagVO;
import com.example.blog.vo.UserVO;
import com.example.blog.vo.article.*;
import org.mapstruct.*;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * 文章专属转换接口（继承通用接口）
 */
@Mapper(
        componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.IGNORE,
        uses = {BaseConvert.class, BizStatusTransform.class, MarkdownUtil.class} // 关联通用转换接口
)
public interface ArticleConvert extends BaseConvert<Article, ArticleAddDTO, ArticleUpdateDTO, ArticleDetailVO> {

    /**
     * 新增DTO转换为文章实体
     *
     * @param addDTO 文章新增DTO，为null时返回null
     * @return 文章实体
     */
    @Override
    @Mapping(target = "contentHtml", source = "content", qualifiedByName = "markdownToHtml")
    Article addDtoToEntity(ArticleAddDTO addDTO);

    /**
     * 修改DTO转换为文章实体
     *
     * @param updateDTO 文章修改DTO，为null时返回null
     * @param article   已存在的数据库实体（目标数据）
     */
    @Override
    // 当源属性为null时，跳过映射，保持目标属性的原值
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "contentHtml", source = "content", qualifiedByName = "markdownToHtml")
    void updateEntityFromDto(ArticleUpdateDTO updateDTO, @MappingTarget Article article);

    /**
     * 文章实体转换为文章详情VO
     *
     * @param article 文章实体，为null时返回null
     * @return 文章详情VO（ArticleApiDetailVO）
     */
    @Override
    ArticleDetailVO entityToVo(Article article);

    /**
     * 单个文章实体转换为前台轮播图专用-文章VO
     *
     * @param article    文章实体，为null时返回null
     * @return 前台轮播图专用-文章VO（entityToCarouselVo），实体为null时返回null
     */
    ArticleCarouselVO entityToCarouselVo(Article article);

    /**
     * 文章实体列表转换为前台轮播图专用-文章VO列表
     * <p>内部复用{@link #entityToCarouselVo(Article)}方法实现单个实体的转换逻辑</p>
     *
     * @param articles   文章实体列表，为null/空列表时返回空列表（非null）
     * @return 前台轮播图专用-文章VO列表（entityToCarouselVo），入参为null/空时返回空列表
     */
    List<ArticleCarouselVO> entitiesToCarouseVos(List<Article> articles);

    /**
     * 单个文章实体转换为前台归档专用-文章VO
     *
     * @param article    文章实体，为null时返回null
     * @return 前台归档专用-文章VO（entityToArchiveVo），实体为null时返回null
     */
    ArticleArchiveVO entityToArchiveVo(Article article);

    /**
     * 文章实体列表转换为前台归档专用-文章VO列表
     * <p>内部复用{@link #entityToArchiveVo(Article)}方法实现单个实体的转换逻辑</p>
     *
     * @param articles   文章实体列表，为null/空列表时返回空列表（非null）
     * @return 前台归档专用-文章VO列表（entityToArchiveVo），入参为null/空时返回空列表
     */
    List<ArticleArchiveVO> entitiesToArchiveVos(List<Article> articles);

    /**
     * 单个文章实体转换为后台文章VO
     *
     * @param article    文章实体，为null时返回null
     * @param extraMaps  上下文参数-文章附加信息Map（来源于ArticleInfoFillUtil批量查询），核心key说明：
     *                   <ul>
     *                   <li>userIdToUserMap：Map<Long, UserVO> - 用户ID -> UserVO(包含昵称/头像)</li>
     *                   <li>categoryIdToNameMap: Map<Long, String> - 分类ID映射分类名称</li>
     *                   <li><strong>articleIdToTagsMap</strong> : {@code Map<Long, List<TagVO>>} - 文章ID -> 标签VO列表(包含ID和Name)</li>
     *                   </ul>
     * @return 后台文章VO（ArticleAdminVO），实体为null时返回null
     */
    @Mappings({
            @Mapping(target = "userNickname", expression = "java(this.getUserNickname(article, extraMaps))"),
            @Mapping(target = "categoryName", expression = "java(this.getCategoryName(article, extraMaps))"),
            @Mapping(target = "tags", expression = "java(this.getArticleTags(article, extraMaps))")
    })
    AdminArticleVO entityToAdminVo(Article article, @Context Map<String, Object> extraMaps);

    /**
     * 文章实体列表转换为后台文章VO列表
     * <p>内部复用{@link #entityToAdminVo(Article, Map)}方法实现单个实体的转换逻辑</p>
     *
     * @param articles   文章实体列表，为null/空列表时返回空列表（非null）
     * @param extraMaps  上下文参数-文章附加信息Map，同{@link #entityToAdminVo(Article, Map)}的extraMaps参数说明
     * @return 后台文章VO列表（ArticleAdminVO），入参为null/空时返回空列表
     */
    List<AdminArticleVO> entitiesToAdminVos(List<Article> articles, @Context Map<String, Object> extraMaps);

    /**
     * 单个文章实体转换为前台文章列表VO
     *
     * @param article    文章实体，为null时返回null
     * @param extraMaps  上下文参数-文章附加信息Map（来源于ArticleInfoFillUtil批量查询），核心key说明：
     *                   <ul>
     *                   <li>userIdToUserMap：Map<Long, UserVO> - 用户ID -> UserVO(包含昵称/头像)</li>
     *                   <li>categoryIdToNameMap: Map<Long, String> - 分类ID映射分类名称</li>
     *                   <li><strong>articleIdToTagsMap</strong> : {@code Map<Long, List<TagVO>>} - 文章ID -> 标签VO列表(包含ID和Name)</li>
     *                   </ul>
     * @return 前台文章列表VO（ArticleApiListVO），实体为null时返回null
     */
    @Mappings({
            @Mapping(target = "userNickname", expression = "java(this.getUserNickname(article, extraMaps))"),
            @Mapping(target = "userAvatar", expression = "java(this.getUserAvatar(article, extraMaps))"),
            @Mapping(target = "categoryName", expression = "java(this.getCategoryName(article, extraMaps))"),
            @Mapping(target = "tags", expression = "java(this.getArticleTags(article, extraMaps))")
    })
    ArticleCardVO entityToListVo(Article article, @Context Map<String, Object> extraMaps);

    /**
     * 文章实体列表转换为前台文章列表VO列表
     * <p>内部复用{@link #entityToListVo(Article, Map)}方法实现单个实体的转换逻辑，避免重复映射配置</p>
     *
     * @param articles   文章实体列表，为null/空列表时返回空列表（非null），无NPE风险
     * @param extraMaps  上下文参数-文章附加信息Map，参数说明同{@link #entityToListVo(Article, Map)}
     * @return 前台文章列表VO列表（ArticleApiListVO），入参为null/空时返回空列表
     */
    List<ArticleCardVO> entitiesToListVos(List<Article> articles, @Context Map<String, Object> extraMaps);

    /**
     * 单个文章实体转换为文章搜索索引VO
     *
     * @param article    文章实体，为null时返回null
     * @param extraMaps  上下文参数-文章附加信息Map（来源于ArticleInfoFillUtil批量查询），核心key说明：
     *                   <ul>
     *                   <li>categoryIdToNameMap: Map<Long, String> - 分类ID映射分类名称</li>
     *                   <li><strong>articleIdToTagsMap</strong> : {@code Map<Long, List<TagVO>>} - 文章ID -> 标签VO列表(包含ID和Name)</li>
     *                   </ul>
     * @return 文章搜索索引VO（ArticleSearchVO），实体为null时返回null
     */
    @Mappings({
            @Mapping(target = "categoryName", expression = "java(this.getCategoryName(article, extraMaps))"),
            @Mapping(target = "tags", expression = "java(this.getArticleTags(article, extraMaps))")
    })
    ArticleSearchVO entityToSearchVo(Article article, @Context Map<String, Object> extraMaps);

    /**
     * 文章实体列表转换为文章搜索索引VO列表
     * <p>内部复用{@link #entityToListVo(Article, Map)}方法实现单个实体的转换逻辑，避免重复映射配置</p>
     *
     * @param articles   文章实体列表，为null/空列表时返回空列表（非null），无NPE风险
     * @param extraMaps  上下文参数-文章附加信息Map，参数说明同{@link #entityToListVo(Article, Map)}
     * @return 文章搜索索引VO列表（ArticleSearchVO），入参为null/空时返回空列表
     */
    List<ArticleSearchVO> entitiesToSearchVos(List<Article> articles, @Context Map<String, Object> extraMaps);

    /**
     * 从附加信息Map中安全获取文章作者昵称
     *
     * @param article   文章实体（不可为null）
     * @param extraMaps 文章附加信息Map，为null时返回空字符串
     * @return 作者昵称，无匹配用户时返回空字符串
     */
    @SuppressWarnings("unchecked")
    default String getUserNickname(Article article, @Context Map<String, Object> extraMaps) {
        if (extraMaps == null || article.getUserId() == null) return "";

        Map<Long, UserVO> userIdToUserMap = (Map<Long, UserVO>) extraMaps.get("userIdToUserMap");
        if (userIdToUserMap == null) return "";

        return Optional.ofNullable(userIdToUserMap.get(article.getUserId()))
                .map(UserVO::getNickname)
                .orElse("");
    }

    /**
     * 从附加信息Map中安全获取文章作者头像
     *
     * @param article 文章实体（不可为null）
     * @param extraMaps 文章附加信息Map，为null时返回空字符串
     * @return 作者头像，无匹配用户时返回空字符串
     */
    @SuppressWarnings("unchecked")
    default String getUserAvatar(Article article, @Context Map<String, Object> extraMaps) {
        if (extraMaps == null || article.getUserId() == null) return "";

        Map<Long, UserVO> userIdToUserMap = (Map<Long, UserVO>) extraMaps.get("userIdToUserMap");
        if (userIdToUserMap == null) return "";

        return Optional.ofNullable(userIdToUserMap.get(article.getUserId()))
                .map(UserVO::getAvatar)
                .orElse("");
    }

    /**
     * 从附加信息Map中安全获取文章分类名称
     *
     * @param article   文章实体（不可为null）
     * @param extraMaps 文章附加信息Map，为null时返回空字符串
     * @return 分类名称，无匹配分类时返回空字符串
     */
    @SuppressWarnings("unchecked")
    default String getCategoryName(Article article, @Context Map<String, Object> extraMaps) {
        if (extraMaps == null || article.getCategoryId() == null) return "";
        Map<Long, String> categoryIdToNameMap = (Map<Long, String>) extraMaps.get("categoryIdToNameMap");
        if (categoryIdToNameMap == null) return "";
        return categoryIdToNameMap.getOrDefault(article.getCategoryId(), "");
    }

    /**
     * 从附加信息Map中安全获取文章标签列表
     *
     * @param article   文章实体（不可为null）
     * @param extraMaps 文章附加信息Map，为null时返回空列表
     * @return 标签名称列表，无匹配标签时返回空列表（不可变集合）
     */
    @SuppressWarnings("unchecked")
    default List<TagVO> getArticleTags(Article article, @Context Map<String, Object> extraMaps) {
        if (extraMaps == null || article.getId() == null) {
            return Collections.emptyList();
        }
        Map<Long, List<TagVO>> articleIdToTagsMap =
                (Map<Long, List<TagVO>>) extraMaps.get("articleIdToTagsMap");
        if (articleIdToTagsMap == null) {
            return Collections.emptyList();
        }
        return articleIdToTagsMap.getOrDefault(article.getId(), Collections.emptyList());
    }

}
