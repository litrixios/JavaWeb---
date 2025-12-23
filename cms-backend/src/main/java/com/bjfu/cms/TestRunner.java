package com.bjfu.cms;

import com.bjfu.cms.service.SftpService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;


@Component
public class TestRunner implements CommandLineRunner {

    @Autowired
    private SftpService sftpService;

    @Override
    public void run(String... args) throws Exception {
        System.out.println("--- 开始测试 SFTP ---");

//         1. 上传测试 (请确保本地有这个文件)
        sftpService.upload("D:\\Wang 等 - 2024 - User Behavior Simulation with Large Language Model based Agents.pdf", "/root/cms/files");
        // 2. 下载测试 (请确保服务器有这个文件)
//         sftpService.download("/root/test.txt", "D:/");
    }
}
