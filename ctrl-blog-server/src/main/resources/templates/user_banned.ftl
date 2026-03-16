<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
</head>
<body style="margin: 0; padding: 0; background-color: #f4f6f8; font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, Helvetica, Arial, sans-serif;">
<div style="padding: 40px 20px;">
    <div style="max-width: 600px; margin: 0 auto; background-color: #ffffff; border-radius: 8px; overflow: hidden; box-shadow: 0 4px 12px rgba(0,0,0,0.05); border-top: 4px solid #F56C6C;">

        <div style="padding: 24px 30px; border-bottom: 1px solid #fde2e2;">
            <span style="font-size: 24px; font-weight: 700; letter-spacing: -0.5px;">
                <span style="color: #409EFF;">Ctrl</span><span style="color: #303133;">Blog</span>
            </span>
            <span style="color: #dcdfe6; margin: 0 12px; font-size: 18px;">|</span>
            <span style="color: #F56C6C; font-size: 16px; font-weight: 600;">账号违规处理通知</span>
        </div>

        <div style="padding: 30px 40px;">
            <p style="margin: 0 0 15px 0; color: #303133; font-size: 16px; font-weight: 600;">尊敬的用户 ${nickname}，您好：</p>
            <p style="margin: 0 0 20px 0; color: #606266; font-size: 15px; line-height: 1.6;">系统检测到您的账号存在违规行为，为了维护良好的社区环境，我们对您的账号进行了限制处理。</p>

            <div style="background-color: #fef0f0; border: 1px solid #fde2e2; border-left: 4px solid #F56C6C; border-radius: 4px; padding: 20px; margin: 0 0 25px 0;">
                <p style="margin: 0 0 12px 0; color: #606266; font-size: 14px;">
                    <strong style="color: #909399; font-weight: normal; display: inline-block; width: 75px;">处理类型：</strong>
                    <span style="color: #303133; font-weight: 600;">${banType}</span>
                </p>
                <p style="margin: 0 0 12px 0; color: #606266; font-size: 14px;">
                    <strong style="color: #909399; font-weight: normal; display: inline-block; width: 75px;">违规原因：</strong>
                    <span style="color: #F56C6C; font-weight: 600;">${banReason}</span>
                </p>
                <p style="margin: 0; color: #606266; font-size: 14px;">
                    <strong style="color: #909399; font-weight: normal; display: inline-block; width: 75px;">解封时间：</strong>
                    <span style="color: #303133;">${endTime!'永久封禁'}</span>
                </p>
            </div>

            <p style="margin: 0; color: #909399; font-size: 13px; line-height: 1.6;">
                如果您对本次处理有异议，请在 7 个工作日内直接回复本邮件或联系管理员进行申诉 (<a href="mailto:${adminEmail}" style="color: #409EFF; text-decoration: none;">${adminEmail}</a>)。
            </p>
        </div>

        <div style="background-color: #fafbfc; border-top: 1px solid #ebeef5; padding: 20px 30px; text-align: center;">
            <p style="margin: 0; font-size: 12px; color: #a8abb2;">此邮件为系统自动发送，请勿直接回复本邮件。</p>
        </div>
    </div>
</div>
</body>
</html>