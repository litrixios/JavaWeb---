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

//    @Value("${file.temp.path}")
//    private String localTempPath;
//
//    public byte[] downloadFileBytes(String remoteFilePath) {
//        ChannelSftp sftp = null;
//        ByteArrayOutputStream outputStream = null;
//        try {
//            sftp = connect();
//            outputStream = new ByteArrayOutputStream();
//
//            // 检查文件是否存在
//            try {
//                sftp.stat(remoteFilePath);
//            } catch (SftpException e) {
//                if (e.id == ChannelSftp.SSH_FX_NO_SUCH_FILE) {
//                    throw new FileNotFoundException("远程文件不存在: " + remoteFilePath);
//                } else {
//                    throw new RuntimeException("检查文件存在性失败: " + e.getMessage(), e);
//                }
//            }
//
//            sftp.get(remoteFilePath, outputStream);
//            System.out.println("✅ 文件下载成功: " + remoteFilePath);
//            return outputStream.toByteArray();
//
//        } catch (Exception e) {
//            e.printStackTrace();
//            throw new RuntimeException("文件下载失败: " + e.getMessage());
//        } finally {
//            if (outputStream != null) {
//                try { outputStream.close(); } catch (IOException e) { e.printStackTrace(); }
//            }
//            close(sftp);
//        }
//    }
//
//    // 新增：检查文件是否存在
//    public boolean fileExists(String remoteFilePath) {
//        ChannelSftp sftp = null;
//        try {
//            sftp = connect();
//            sftp.stat(remoteFilePath);
//            return true;
//        } catch (SftpException e) {
//            if (e.id == ChannelSftp.SSH_FX_NO_SUCH_FILE) {
//                return false;
//            }
//            throw new RuntimeException("检查文件存在性失败: " + e.getMessage(), e);
//        } catch (Exception e) {
//            throw new RuntimeException("检查文件失败: " + e.getMessage(), e);
//        } finally {
//            close(sftp);
//        }
//    }
    public String uploadAvatar(InputStream inputStream, Integer userId) {
        // 1. 定义头像存储目录（按用户ID分目录）
        String remoteDir = remoteBaseDir + "/avatars/" + userId; // 如：/root/cms/files/avatars/123
        // 2. 生成唯一文件名（时间戳+扩展名）
        String fileExt = getFileExtensionFromStream(inputStream);
        String fileName = System.currentTimeMillis() + "." + fileExt;
        String remoteFilePath = remoteDir + "/" + fileName;

        ChannelSftp sftp = null;
        try {
            sftp = connect();
            createRemoteDir(sftp, remoteDir); // 自动创建多级目录
            sftp.cd(remoteDir);
            sftp.put(inputStream, fileName); // 上传文件
            System.out.println("头像上传成功: " + remoteFilePath);
            return remoteFilePath;
        } catch (Exception e) {
            throw new RuntimeException("头像上传失败: " + e.getMessage());
        } finally {
            close(sftp);
            try { inputStream.close(); } catch (IOException e) { e.printStackTrace(); }
        }
    }

    // 【辅助方法】从文件头判断扩展名（JPG/PNG/GIF）
    private String getFileExtensionFromStream(InputStream inputStream) {
        try {
            if (inputStream.markSupported()) {
                inputStream.mark(8); // 标记流位置
                byte[] header = new byte[8];
                inputStream.read(header);
                inputStream.reset(); // 重置流，保证后续上传能正常读取

                if (isJpg(header)) return "jpg";
                if (isPng(header)) return "png";
                if (isGif(header)) return "gif";
            }
            return "jpg"; // 默认扩展名
        } catch (IOException e) {
            return "jpg";
        }
    }

    // 【辅助判断】JPG文件头（FF D8）
    private boolean isJpg(byte[] header) {
        return header.length >= 2 && header[0] == (byte) 0xFF && header[1] == (byte) 0xD8;
    }

    // 【辅助判断】PNG文件头（89 50 4E 47 0D 0A 1A 0A）
    private boolean isPng(byte[] header) {
        return header.length >= 8 &&
                header[0] == (byte) 0x89 && header[1] == (byte) 0x50 &&
                header[2] == (byte) 0x4E && header[3] == (byte) 0x47 &&
                header[4] == (byte) 0x0D && header[5] == (byte) 0x0A &&
                header[6] == (byte) 0x1A && header[7] == (byte) 0x0A;
    }

    // 【辅助判断】GIF文件头（47 49 46 38 39/37 61）
    private boolean isGif(byte[] header) {
        return header.length >= 6 &&
                header[0] == (byte) 0x47 && header[1] == (byte) 0x49 &&
                header[2] == (byte) 0x46 && header[3] == (byte) 0x38 &&
                (header[4] == (byte) 0x39 || header[4] == (byte) 0x37) &&
                header[5] == (byte) 0x61;
    }

    // 获取连接
    public ChannelSftp connect() throws JSchException {
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

    public void close(ChannelSftp sftp) {
        if (sftp != null) {
            try {
                if (sftp.isConnected()) sftp.disconnect();
                if (sftp.getSession() != null) sftp.getSession().disconnect();
            } catch (Exception e) { e.printStackTrace(); }
        }
    }
}