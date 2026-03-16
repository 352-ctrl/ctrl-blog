<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
</head>
<body style="margin: 0; padding: 0; background-color: #f4f6f8; font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, Helvetica, Arial, sans-serif;">
<div style="padding: 40px 20px;">
    <div style="max-width: 600px; margin: 0 auto; background-color: #ffffff; border-radius: 8px; overflow: hidden; box-shadow: 0 4px 12px rgba(0,0,0,0.05); border-top: 4px solid #E6A23C;">

        <div style="padding: 24px 30px; border-bottom: 1px solid #faecd8;">
            <span style="font-size: 24px; font-weight: 700; letter-spacing: -0.5px;">
                <span style="color: #409EFF;">Ctrl</span><span style="color: #303133;">Blog</span>
            </span>
            <span style="color: #dcdfe6; margin: 0 12px; font-size: 18px;">|</span>
            <span style="color: #E6A23C; font-size: 16px; font-weight: 600;">待处理反馈提醒</span>
        </div>

        <div style="padding: 30px 40px;">
            <p style="margin: 0 0 15px 0; color: #303133; font-size: 16px; font-weight: 600;">管理员您好：</p>
            <p style="margin: 0 0 20px 0; color: #606266; font-size: 15px; line-height: 1.6;">系统刚刚收到了一条新的用户反馈，请及时登录后台跟进处理。</p>

            <div style="background-color: #fdf6ec; border: 1px solid #faecd8; border-left: 4px solid #E6A23C; border-radius: 4px; padding: 20px; margin: 0 0 25px 0;">
                <p style="margin: 0 0 12px 0; color: #606266; font-size: 14px; line-height: 1.6;">
                    <strong style="color: #909399; font-weight: normal;">联系方式：</strong><span style="color: #303133;">${contact!'未留联系方式'}</span>
                </p>
                <p style="margin: 0; color: #606266; font-size: 14px; line-height: 1.6;">
                    <strong style="color: #909399; font-weight: normal;">反馈内容：</strong><br/>
                    <span style="display: block; margin-top: 8px; color: #303133; background: #ffffff; padding: 12px; border-radius: 4px; border: 1px solid #faecd8;">${content}</span>
                </p>
            </div>

            <p style="margin: 0; color: #909399; font-size: 13px; line-height: 1.6;">请尽快登录后台管理系统进行回复与操作。</p>
        </div>

        <div style="background-color: #fafbfc; border-top: 1px solid #ebeef5; padding: 20px 30px; text-align: center;">
            <p style="margin: 0; font-size: 12px; color: #a8abb2;">此邮件为系统自动发送，请勿直接回复本邮件。</p>
        </div>
    </div>
</div>
</body>
</html>