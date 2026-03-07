package com.example.blog.modules.article.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.example.blog.modules.article.model.dto.CategoryAddDTO;
import com.example.blog.modules.article.model.dto.CategoryQueryDTO;
import com.example.blog.modules.article.model.dto.CategoryUpdateDTO;
import com.example.blog.modules.article.model.entity.Category;
import com.example.blog.modules.article.model.vo.CategoryVO;

import java.util.List;

/**
 * 文章分类业务服务接口
 * 定义分类相关的业务操作方法
 */
public interface CategoryService extends IService<Category> {
    /**
     * 获取单条分类详情
     *
     * @param id 分类ID
     * @return 分类VO
     */
    CategoryVO getCategoryById(Long id);

    /**
     * 获取前台展示用的分类列表
     *
     * @return 分类列表
     */
    List<CategoryVO> listPortalCategories();

    /**
     * 获取所有分类
     *
     * @return 分类列表
     */
    List<CategoryVO> listAdminCategories();

    /**
     * 分页查询分类
     *
     * @param categoryQueryDTO 查询条件DTO
     * @return 分页结果
     */
    IPage<CategoryVO> pageAdminCategories(CategoryQueryDTO categoryQueryDTO);

    /**
     * 保存分类（包含重复校验）
     *
     * @param categoryAddDTO 分类DTO
     */
    void addCategory(CategoryAddDTO categoryAddDTO);

    /**
     * 更新分类（包含重复校验）
     *
     * @param categoryUpdateDTO 分类DTO
     */
    void updateCategory(CategoryUpdateDTO categoryUpdateDTO);

    /**
     * 删除分类
     *
     * @param id 分类ID
     */
    void deleteCategoryById(Long id);

    /**
     * 批量删除分类
     *
     * @param ids 分类ID列表
     */
    void batchDeleteCategories(List<Long> ids);
}