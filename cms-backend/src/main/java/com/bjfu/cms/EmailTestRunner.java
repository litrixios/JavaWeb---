package com.bjfu.cms;

import com.bjfu.cms.service.EmailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class EmailTestRunner implements CommandLineRunner {

    @Autowired
    private EmailService emailService;

    @Override
    public void run(String... args) throws Exception {
        // 为了避免每次启动项目都发邮件，这里可以注释掉，需要测试时再打开
        // 或者设置为只在特定参数下运行

        System.out.println("--- 准备进行邮件发送测试 ---");

        // 1. 设置收件人 (建议改成你自己的另一个邮箱进行测试)
        String to = "2662775049@qq.com"; // 这里用了你SeedData里的一个邮箱，建议修改

        // 2. 主题和内容
        String subject = "CMS系统邮件功能测试";
        String content = "<h1>这是测试邮件</h1><p>如果看到这句话，说明系统邮件配置正常（SSL/465端口）。</p>";

        // 3. 调用发送
        // 注意：因为是 @Async 异步方法，控制台可能会先打印 "测试指令已发出"，后打印 "邮件发送成功"
        emailService.sendHtmlMail(to, null, subject, content);

        System.out.println("--- 邮件发送指令已发出，请留意后续日志输出 ---");
    }
}