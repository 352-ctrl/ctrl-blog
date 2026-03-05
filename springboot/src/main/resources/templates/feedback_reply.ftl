<html>
<body>
<div style="border: 1px solid #ddd; padding: 20px; max-width: 600px;">
    <h2 style="color: #409EFF;">【博客反馈处理结果】</h2>
    <p>您好！非常感谢您对本站的关注与支持。您之前提交的反馈状态已更新为：<strong style="color: ${replyColor};">【${statusText}】</strong></p>

    <div style="background-color: #f5f5f5; padding: 15px; border-radius: 4px; margin: 10px 0;">
        <p style="margin-top: 0;"><strong>您的反馈内容：</strong><br/>${content}</p>
        <hr style="border: 0; border-top: 1px dashed #ccc; margin: 15px 0;">
        <p style="margin-bottom: 0;"><strong>管理员回复：</strong><br/>
            <span style="color: ${replyColor}; font-weight: bold;">${adminReply}</span>
        </p>
    </div>

    <p style="font-size: 12px; color: #999; margin-top: 20px;">此邮件为系统自动发送，请勿直接回复本邮件。</p>
</div>
</body>
</html>