package com.example.blog.modules.article.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.example.blog.modules.article.model.dto.TagAddDTO;
import com.example.blog.modules.article.model.dto.TagQueryDTO;
import com.example.blog.modules.article.model.dto.TagUpdateDTO;
import com.example.blog.modules.article.model.entity.Tag;
import com.example.blog.modules.article.model.vo.TagVO;

import java.util.List;

/**
 * 文章标签业务服务接口
 * 定义标签相关的业务操作方法
 */
public interface TagService extends IService<Tag> {

    /**
     * 获取单条标签详情
     *
     * @param id 标签ID
     * @return 标签VO
     */
    TagVO getTagById(Long id);

    /**
     * 获取前台展示用的标签列表
     *
     * @return 经过筛选的标签列表
     */
    List<TagVO> listPortalTags();

    /**
     * 获取标签列表
     *
     * @return 标签列表
     */
    List<TagVO> listAdminTags();

    /**
     * 根据文章ID获取关联的标签列表
     * (用于 ArticleService 的详情页等需要聚合标签数据的场景)
     *
     * @param articleId 文章ID
     * @return 关联的标签VO列表
     */
    List<TagVO> listTagsByArticleId(Long articleId);

    /**
     * 分页查询标签
     *
     * @param queryDTO 查询条件DTO
     * @return 分页结果
     */
    IPage<TagVO> pageAdminTags(TagQueryDTO queryDTO);

    /**
     * 保存标签（包含重复校验）
     *
     * @param addDTO 标签DTO
     */
    void addTag(TagAddDTO addDTO);

    /**
     * 更新标签（包含重复校验）
     *
     * @param updateDTO 标签DTO
     */
    void updateTag(TagUpdateDTO updateDTO);

    /**
     * 删除标签
     *
     * @param id 标签ID
     */
    void deleteTagById(Long id);

    /**
     * 批量删除标签
     *
     * @param ids 标签ID列表
     */
    void batchDeleteTags(List<Long> ids);
}