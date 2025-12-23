package com.bjfu.cms.service;

import com.jcraft.jsch.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.*;
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

    //注入远程目录配置
    @Value("${aliyun.ecs.remote-dir}")
    private String remoteBaseDir;
    @Value("${file.temp.path}")
    private String localTempPath;  // 本地临时目录


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

            // 检查远程文件是否存在
            try {
                sftp.stat(remoteFile); // 调用stat方法检查文件是否存在
            } catch (SftpException e) {
                if (e.id == ChannelSftp.SSH_FX_NO_SUCH_FILE) {
                    throw new FileNotFoundException("远程文件不存在: " + remoteFile);
                } else {
                    throw new RuntimeException("检查文件存在性失败: " + e.getMessage(), e);
                }
            }

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

    // 新增：创建远程目录（支持多级目录）
    private void createRemoteDir(ChannelSftp sftp, String remoteDir) throws SftpException {
        String[] dirs = remoteDir.split("/");
        String currentDir = "";
        for (String dir : dirs) {
            if (dir.isEmpty()) continue;
            currentDir += "/" + dir;
            try {
                sftp.cd(currentDir);
            } catch (SftpException e) {
                sftp.mkdir(currentDir);
                sftp.cd(currentDir);
            }
        }
    }

    // 优化上传方法：自动创建目录，支持通过输入流上传
    public void upload(InputStream inputStream, String remoteFileName, String remoteDir) {
        ChannelSftp sftp = null;
        try {
            sftp = connect();
            // 自动创建远程目录（如果不存在）
            createRemoteDir(sftp, remoteDir);
            sftp.cd(remoteDir);
            // 上传文件
            sftp.put(inputStream, remoteFileName);
            System.out.println("✅ 上传成功: " + remoteDir + "/" + remoteFileName);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("文件上传失败: " + e.getMessage());
        } finally {
            close(sftp);
            try {
                inputStream.close(); // 关闭输入流
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    // 新增：删除远程文件
    public void deleteRemoteFile(String remoteFilePath) {
        ChannelSftp sftp = null;
        try {
            sftp = connect();
            sftp.rm(remoteFilePath);
            System.out.println("✅ 删除远程文件成功: " + remoteFilePath);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("删除远程文件失败: " + e.getMessage());
        } finally {
            close(sftp);
        }
    }

    /**
     * [新增] 下载文件到输出流（用于Web下载）
     * @param remoteFile 服务器文件全路径
     * @param outputStream 响应输出流
     */
    public void downloadToStream(String remoteFile, OutputStream outputStream) {
        ChannelSftp sftp = null;
        try {
            sftp = connect();
            // 检查文件是否存在
            try {
                sftp.stat(remoteFile);
            } catch (SftpException e) {
                if (e.id == ChannelSftp.SSH_FX_NO_SUCH_FILE) {
                    throw new FileNotFoundException("远程文件不存在: " + remoteFile);
                }
                throw e;
            }
            // 将文件内容写入输出流
            sftp.get(remoteFile, outputStream);
            System.out.println("✅ 文件流传输成功: " + remoteFile);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("文件下载失败: " + e.getMessage());
        } finally {
            close(sftp);
        }
    }

}