<html>
<body>
<div style="border: 1px solid #ddd; padding: 20px; max-width: 600px;">
    <h2 style="color: #F56C6C;">【CtrlBlog】账号违规处理通知</h2>
    <p>尊敬的用户 <strong>${nickname}</strong>，您好：</p>
    <p>系统检测到您的账号存在违规行为，为了维护良好的社区环境，我们对您的账号进行了限制处理。</p>

    <div style="background-color: #fcfcfc; border-left: 4px solid #F56C6C; padding: 15px; margin: 15px 0;">
        <p style="margin: 0 0 10px 0;"><strong>处理类型：</strong>${banType}</p>
        <p style="margin: 0 0 10px 0;"><strong>违规原因：</strong>${banReason}</p>
        <p style="margin: 0;"><strong>解封时间：</strong>${endTime!'永久封禁'}</p>
    </div>

    <p style="font-size: 14px; color: #666;">
        如果您对本次处理有异议，请在 7 个工作日内回复本邮件或联系管理员 (${adminEmail}) 进行申诉。
    </p>
    <hr style="border: 0; border-top: 1px solid #eee; margin: 20px 0;">
    <p style="font-size: 12px; color: #999;">此邮件为系统自动发送，请勿直接回复。</p>
</div>
</body>
</html>