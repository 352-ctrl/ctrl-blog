package com.example.blog.modules.operation.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.example.blog.modules.operation.model.dto.DanmakuAddDTO;
import com.example.blog.modules.operation.model.dto.DanmakuQueryDTO;
import com.example.blog.modules.operation.model.entity.Danmaku;
import com.example.blog.modules.operation.model.vo.DanmakuVO;

import java.util.List;

public interface DanmakuService extends IService<Danmaku> {

    /**
     * 前台：获取所有有效弹幕（用于初始化屏幕）
     */
    List<DanmakuVO> listDanmakus();

    /**
     * 前台/通用：新增弹幕
     */
    void addDanmaku(DanmakuAddDTO addDTO);

    /**
     * 后台：分页查询弹幕
     */
    IPage<DanmakuVO> pageAdminDanmakus(DanmakuQueryDTO queryDTO);

    /**
     * 后台：单个逻辑删除
     */
    void deleteDanmakuById(Long id);

    /**
     * 后台：批量逻辑删除
     */
    void batchDeleteDanmakus(List<Long> ids);
}