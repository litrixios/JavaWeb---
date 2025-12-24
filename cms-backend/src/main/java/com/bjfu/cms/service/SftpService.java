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

    @Value("${aliyun.ecs.remote-dir}")
    private String remoteBaseDir;

    @Value("${file.temp.path}")
    private String localTempPath;

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
     * [修改] 上传文件
     * 逻辑更新：如果远程目录不存在，则自动递归创建，而不是直接报错返回
     * @param localPath 本地文件绝对路径
     * @param remoteDir 服务器目标目录 (如 /root/data)
     */
    public void upload(String localPath, String remoteDir) {
        ChannelSftp sftp = null;
        try {
            sftp = connect();

            // --- 修改开始 ---
            try {
                // 尝试进入目录
                sftp.cd(remoteDir);
            } catch (SftpException e) {
                // 如果目录不存在 (或其他进入失败原因)，尝试递归创建该目录
                // createRemoteDir 内部逻辑会最终 cd 到该目录
                createRemoteDir(sftp, remoteDir);
            }
            // --- 修改结束 ---

            File file = new File(localPath);
            // 此时 sftp 已经位于 remoteDir 目录下
            sftp.put(new FileInputStream(file), file.getName());
            System.out.println("✅ 上传成功: " + remoteDir + "/" + file.getName());
        } catch (Exception e) {
            e.printStackTrace();
            // 建议抛出运行时异常以便上层捕获
            throw new RuntimeException("文件上传失败: " + e.getMessage());
        } finally {
            close(sftp);
        }
    }

    /**
     * 递归创建远程目录 (辅助方法)
     * 该方法会逐级检查目录，不存在则创建，并且最终会停留在 remoteDir 目录下
     */
    private void createRemoteDir(ChannelSftp sftp, String remoteDir) throws SftpException {
        // 处理路径分隔符，防止双斜杠
        String path = remoteDir.replaceAll("//", "/");
        if (remoteDir.startsWith("/")) {
            // 如果是绝对路径，先回退到根目录，防止相对路径叠加错误
            sftp.cd("/");
        }

        String[] dirs = path.split("/");
        for (String dir : dirs) {
            if (dir == null || dir.trim().isEmpty()) continue;

            try {
                sftp.cd(dir);
            } catch (SftpException e) {
                // 进入失败，尝试创建
                try {
                    sftp.mkdir(dir);
                    sftp.cd(dir);
                } catch (SftpException ex) {
                    System.err.println("创建目录失败: " + dir);
                    throw ex;
                }
            }
        }
    }

    /**
     * 下载文件
     */
    public void download(String remoteFile, String localDir) {
        ChannelSftp sftp = null;
        try {
            sftp = connect();
            String fileName = remoteFile.substring(remoteFile.lastIndexOf("/") + 1);

            try {
                sftp.stat(remoteFile);
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

    // 优化上传方法：通过输入流上传 (保留你原有的重载方法)
    public void upload(InputStream inputStream, String remoteFileName, String remoteDir) {
        ChannelSftp sftp = null;
        try {
            sftp = connect();
            createRemoteDir(sftp, remoteDir); // 复用创建目录逻辑
            // 此时已在目标目录
            sftp.put(inputStream, remoteFileName);
            System.out.println("✅ 上传成功: " + remoteDir + "/" + remoteFileName);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("文件上传失败: " + e.getMessage());
        } finally {
            close(sftp);
            try {
                inputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    // 删除远程文件
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

    // 下载文件到输出流
    public void downloadToStream(String remoteFile, OutputStream outputStream) {
        ChannelSftp sftp = null;
        try {
            sftp = connect();
            try {
                sftp.stat(remoteFile);
            } catch (SftpException e) {
                if (e.id == ChannelSftp.SSH_FX_NO_SUCH_FILE) {
                    throw new FileNotFoundException("远程文件不存在: " + remoteFile);
                }
                throw e;
            }
            sftp.get(remoteFile, outputStream);
            System.out.println("✅ 文件流传输成功: " + remoteFile);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("文件下载失败: " + e.getMessage());
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