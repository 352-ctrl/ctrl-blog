package com.example.blog.modules.operation.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.blog.common.enums.BizStatus;
import com.example.blog.modules.operation.model.dto.MessageSendDTO;
import com.example.blog.modules.operation.model.entity.Message;
import com.example.blog.modules.operation.model.vo.MessageVO;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 系统消息业务服务接口
 * 定义消息中心相关的业务操作方法，如获取未读数、标记已读、消息分页等
 */
public interface MessageService extends IService<Message> {

    /**
     * 获取当前用户的消息列表（无分页，按类型获取，最多返回最近的 200 条）
     *
     * @param type 消息类型，为空则查询全部
     * @return 消息VO列表
     */
    List<MessageVO> listMessages(BizStatus.MessageType type);

    /**
     * 获取当前登录用户的未读消息总数
     * 用于前端顶部导航栏的“小红点”展示
     *
     * @return 未读消息数量
     */
    Integer getUnreadMessageCount();

    /**
     * 标记单条消息为已读
     *
     * @param messageId 消息ID
     */
    void markMessageAsRead(Long messageId);

    /**
     * 一键将当前登录用户的所有未读消息标记为已读
     *
     * @param type 消息类型（可选）。如果传入类型，则只标记该类型；如果为null，则标记所有类型
     */
    void markAllAsRead(BizStatus.MessageType type);

    /**
     * 发送系统通知（后台管理员主动发送，或系统自动触发的反馈回复等）
     *
     * @param sendDTO 消息发送参数对象
     */
    void sendSystemNotice(MessageSendDTO sendDTO);

    /**
     * 发送互动消息（用户点赞、评论时系统自动异步触发）
     *
     * @param sendDTO 消息发送参数对象
     */
    void sendInteractiveMessage(MessageSendDTO sendDTO);

    /**
     * 清理过期的系统消息垃圾数据
     * 用于定时任务物理删除早期无用的系统消息
     *
     * @param msgLimitDate 过期时间阈值
     * @return 成功删除的条数
     */
    int clearMessageTrash(LocalDateTime msgLimitDate);

}