package com.bjfu.cms.service;

import com.jcraft.jsch.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.Properties;

@Service
public class SftpService {

    @Value("${aliyun.ecs.ip}")
    private String host;

    @Value("${aliyun.ecs.user}")
    private String username;

    @Value("${aliyun.ecs.password}")
    private String password;

    @Value("${aliyun.ecs.port}")
    private int port;

    // 获取连接
    private ChannelSftp connect() throws JSchException {
        JSch jsch = new JSch();
        Session session = jsch.getSession(username, host, port);
        session.setPassword(password);

        Properties config = new Properties();
        config.put("StrictHostKeyChecking", "no");
        session.setConfig(config);

        session.connect();
        Channel channel = session.openChannel("sftp");
        channel.connect();
        return (ChannelSftp) channel;
    }

    /**
     * 上传文件
     * @param localPath 本地文件绝对路径
     * @param remoteDir 服务器目标目录 (如 /root)
     */
    public void upload(String localPath, String remoteDir) {
        ChannelSftp sftp = null;
        try {
            sftp = connect();
            try { sftp.cd(remoteDir); } catch (SftpException e) {
                // 如果目录不存在，这里可以做创建目录的逻辑，或者报错
                System.err.println("目录不存在: " + remoteDir);
                return;
            }
            File file = new File(localPath);
            sftp.put(new FileInputStream(file), file.getName());
            System.out.println("✅ 上传成功: " + file.getName());
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            close(sftp);
        }
    }

    /**
     * 下载文件
     * @param remoteFile 服务器文件全路径 (如 /root/test.txt)
     * @param localDir 本地保存目录
     */
    public void download(String remoteFile, String localDir) {
        ChannelSftp sftp = null;
        try {
            sftp = connect();
            String fileName = remoteFile.substring(remoteFile.lastIndexOf("/") + 1);
            sftp.get(remoteFile, new FileOutputStream(localDir + File.separator + fileName));
            System.out.println("✅ 下载成功: " + fileName);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            close(sftp);
        }
    }

    private void close(ChannelSftp sftp) {
        if (sftp != null) {
            try {
                if (sftp.isConnected()) sftp.disconnect();
                if (sftp.getSession() != null) sftp.getSession().disconnect();
            } catch (Exception e) { e.printStackTrace(); }
        }
    }
}