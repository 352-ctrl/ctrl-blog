<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
</head>
<body style="margin: 0; padding: 0; background-color: #f4f6f8; font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, Helvetica, Arial, sans-serif;">
<div style="padding: 40px 20px;">
    <div style="max-width: 600px; margin: 0 auto; background-color: #ffffff; border-radius: 8px; overflow: hidden; box-shadow: 0 4px 12px rgba(0,0,0,0.05); border-top: 4px solid #409EFF;">

        <div style="padding: 24px 30px; border-bottom: 1px solid #ebeef5;">
            <span style="font-size: 24px; font-weight: 700; letter-spacing: -0.5px;">
                <span style="color: #409EFF;">Ctrl</span><span style="color: #303133;">Blog</span>
            </span>
            <span style="color: #dcdfe6; margin: 0 12px; font-size: 18px;">|</span>
            <span style="color: #606266; font-size: 16px;">反馈处理进度更新</span>
        </div>

        <div style="padding: 30px 40px;">
            <p style="margin: 0 0 15px 0; color: #303133; font-size: 16px; font-weight: 600;">您好！</p>
            <p style="margin: 0 0 20px 0; color: #606266; font-size: 15px; line-height: 1.6;">非常感谢您对本站的关注与支持。您之前提交的反馈状态已更新为：<strong style="color: ${replyColor!'#409EFF'}; background-color: #f4f4f5; padding: 2px 8px; border-radius: 4px;">${statusText}</strong></p>

            <div style="background-color: #f8f9fa; border: 1px solid #ebeef5; border-left: 4px solid #dcdfe6; border-radius: 4px; padding: 20px; margin: 0 0 25px 0;">
                <p style="margin: 0 0 8px 0; color: #909399; font-size: 13px;">您的反馈内容：</p>
                <p style="margin: 0 0 20px 0; color: #606266; font-size: 14px; line-height: 1.6; background: #ffffff; padding: 10px; border-radius: 4px; border: 1px solid #ebeef5;">${content}</p>

                <p style="margin: 0 0 8px 0; color: #909399; font-size: 13px;">管理员回复：</p>
                <p style="margin: 0; color: #303133; font-size: 15px; line-height: 1.6; font-weight: 500; background: #ffffff; padding: 12px; border-radius: 4px; border: 1px solid #ebeef5; border-left: 3px solid ${replyColor!'#409EFF'};">${adminReply}</p>
            </div>
        </div>

        <div style="background-color: #fafbfc; border-top: 1px solid #ebeef5; padding: 20px 30px; text-align: center;">
            <p style="margin: 0; font-size: 12px; color: #a8abb2;">此邮件为系统自动发送，请勿直接回复本邮件。</p>
        </div>
    </div>
</div>
</body>
</html>