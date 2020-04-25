package com.hsbc.wechat.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Arrays;

import com.hsbc.wechat.bean.SftpLog;
import com.hsbc.wechat.service.SftpService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.SftpException;
import com.hsbc.wechat.config.SftpConfig;

@Component
public class SftpFileUtil {

    private static SftpConfig config;

    private static final Logger logger = LoggerFactory.getLogger(SftpFileUtil.class);

    // 设置第一次登陆的时候提示，可选值：(ask | yes | no)
    private static final String SESSION_CONFIG_STRICT_HOST_KEY_CHECKING = "StrictHostKeyChecking";

    @Autowired
    public void init(SftpConfig config) {
        SftpFileUtil.config = config;
    }

    /**
     * 创建SFTP连接
     * @return
     * @throws Exception
     */
    private static ChannelSftp createSftp() throws Exception {
        JSch jsch = new JSch();
        logger.info("Try to connect sftp[" + config.getUsername() + "@" + config.getHost() + "], use password[" + config.getPassword() + "]");

        Session session = createSession(jsch, config.getHost(), config.getUsername(), config.getPort());
        session.setPassword(config.getPassword());
        session.connect(config.getSessionConnectTimeout());

        logger.info("Session connected to {}.", config.getHost());

        Channel channel = session.openChannel(config.getProtocol());
        channel.connect(config.getChannelConnectedTimeout());

        logger.info("Channel created to {}.", config.getHost());

        return (ChannelSftp) channel;
    }

    /**
     * 加密秘钥方式登陆
     * @return
     */
    private static ChannelSftp connectByKey() throws Exception {
        JSch jsch = new JSch();

        // 设置密钥和密码 ,支持密钥的方式登陆
        if (isNotBlank(config.getPrivateKey())) {
            if (isNotBlank(config.getPassphrase())) {
                // 设置带口令的密钥
                jsch.addIdentity(config.getPrivateKey(), config.getPassphrase());
            } else {
                // 设置不带口令的密钥
                jsch.addIdentity(config.getPrivateKey());
            }
        }
        logger.info("Try to connect sftp[" + config.getUsername() + "@" + config.getHost() + "], use private key[" + config.getPrivateKey()
                + "] with passphrase[" + config.getPassphrase() + "]");

        Session session = createSession(jsch, config.getHost(), config.getUsername(), config.getPort());
        // 设置登陆超时时间
        session.connect(config.getSessionConnectTimeout());
        logger.info("Session connected to " + config.getHost() + ".");

        // 创建sftp通信通道
        Channel channel = session.openChannel(config.getProtocol());
        channel.connect(config.getChannelConnectedTimeout());
        logger.info("Channel created to " + config.getHost() + ".");
        return (ChannelSftp) channel;
    }

    /**
     * 创建session
     * @param jsch
     * @param host
     * @param username
     * @param port
     * @return
     * @throws Exception
     */
    private static Session createSession(JSch jsch, String host, String username, Integer port) throws Exception {
        Session session = null;

        if (port <= 0) {
            session = jsch.getSession(username, host);
        } else {
            session = jsch.getSession(username, host, port);
        }

        if (session == null) {
            throw new Exception(host + " session is null");
        }

        session.setConfig(SESSION_CONFIG_STRICT_HOST_KEY_CHECKING, config.getSessionStrictHostKeyChecking());
        return session;
    }

    /**
     * 关闭连接
     * @param sftp
     */
    private static void disconnect(ChannelSftp sftp) {
        try {
            if (sftp != null) {
                if (sftp.isConnected()) {
                    sftp.disconnect();
                } else if (sftp.isClosed()) {
                    logger.info("sftp is closed already");
                }
                if (null != sftp.getSession()) {
                    sftp.getSession().disconnect();
                }
            }
        } catch (JSchException e) {
            e.printStackTrace();
        }
    }

    /**
     * 不再开放此API，因为记录日志时无法获取文件大小
     * 上传单个文件，以断点续传的方式
     * @param targetPath 上传到的目录，服务器的相对目录
     * @param inputStream 文件流
     * @return 上传是否成功
     * @throws Exception
     */
//    public static boolean uploadFile(String targetPath, InputStream inputStream) throws Exception {
//        return uploadFile(targetPath, inputStream, 2);
//    }

    /**
     * 不再开放此API，因为记录日志时无法获取文件大小
     * 上传单个文件
     * @param targetPath 上传到的目录，服务器的相对目录
     * @param inputStream 文件流
     * @param mode 上传模式：1-覆盖，2-断点续传，3-追加
     * @return 上传是否成功
     * @throws Exception
     */
//    public static boolean uploadFile(String targetPath, InputStream inputStream, int mode) throws Exception {
//        ChannelSftp sftp = createSftp();
//        boolean flag = false;
//        try {
//            createDirs(config.getRoot(), sftp);
//            sftp.cd(config.getRoot());
//            logger.info("Change path to {}", config.getRoot());
//
//            int index = targetPath.lastIndexOf("/");
//            String fileDir = targetPath.substring(0, index);
//            String fileName = targetPath.substring(index + 1);
//            boolean dirs = createDirs(fileDir, sftp);
//            if (!dirs) {
//                logger.error("Remote path error. path:{}", targetPath);
//                throw new Exception("Upload File failure");
//            }
//            sftp.put(inputStream, fileName, mode);
//            flag = true;
//        } catch (Exception e) {
//            flag = false;
//            logger.error("Upload file failure. TargetPath: {}", targetPath, e);
//            throw new Exception("Upload File failure");
//        } finally {
//            if (inputStream != null) {
//                inputStream.close();
//            }
//            disconnect(sftp);
//            afterUploadOne(targetPath, flag);
//            return flag;
//        }
//    }

    /**
     * 上传单个文件，以断点续传方式上传
     * @param targetPath 上传到的目录，服务器的相对目录
     * @param file 待上传的文件
     * @return 上传是否成功
     * @throws Exception
     */
    public static boolean uploadFile(String targetPath, File file) throws Exception {
        return uploadFile(targetPath, file, 2);
    }

    /**
     * 上传单个文件
     * @param targetPath 上传到的目录，服务器的相对目录
     * @param file 待上传的文件
     * @param mode 上传模式：1-覆盖，2-断点续传，3-追加
     * @return 上传是否成功
     * @throws Exception
     */
    public static boolean uploadFile(String targetPath, File file, int mode) throws Exception {
        return uploadFile(targetPath, file, mode);
    }

    /**
     * 不再开放此API，因为不方便日志记录，无法获悉文件大小。
     * 上传单个文件，该方法用于批量上传，因此中间不会申请新的sftp连接，也不会关闭连接，
     * 请调用方在批量任务开始时自己申请连接，批量任务结束后主动关闭连接
     * @param sftp sftp服务器的连接
     * @param targetPath 上传目录，服务器的相对目录
     * @param inputStream 待上传的文件流
     * @param mode 上传模式：1-覆盖，2-断点续传，3-追加
     * @return 上传是否成功
     * @throws Exception
     */
//    private static boolean uploadFile(ChannelSftp sftp, String targetPath, InputStream inputStream, int mode) throws Exception {
//        boolean flag = false;
//        try {
//            int index = targetPath.lastIndexOf("/");
//            String fileDir = targetPath.substring(0, index);
//            String fileName = targetPath.substring(index + 1);
//            boolean dirs = createDirs(fileDir, sftp);
//            if (!dirs) {
//                logger.error("Remote path error. path:{}", targetPath);
//                throw new Exception("Upload File failure");
//            }
//            //切换到目标目录
//            fileDir = fileDir.startsWith("/") ? fileDir.substring(1) : fileDir;
//            sftp.cd(fileDir);
//            sftp.put(inputStream, fileName, mode);
//            //切换回原目录
//            int len = fileDir.split("/").length;
//            String back = "";
//            for (int i=0; i<len; i++) {
//                back += "../";
//            }
//            sftp.cd(back);
//            flag = true;
//        } catch (Exception e) {
//            flag = false;
//            logger.error("Upload file failure. TargetPath: {}", targetPath, e);
//            throw new Exception("Upload File failure");
//        } finally {
//            if (inputStream != null) {
//                inputStream.close();
//            }
//            afterUploadOne(targetPath, flag);
//            return flag;
//        }
//    }

    /**
     * 上传单个文件，该方法用于批量上传，因此中间不会申请新的sftp连接，也不会关闭连接，
     * 请调用方在批量任务开始时自己申请连接，批量任务结束后主动关闭连接
     * @param sftp sftp服务器的连接
     * @param targetPath 上传目录，服务器的相对目录
     * @param file 待上传的文件
     * @param mode 上传模式：1-覆盖，2-断点续传，3-追加
     * @return 上传是否成功
     * @throws Exception
     */
    private static boolean uploadFile(ChannelSftp sftp, String targetPath, File file, int mode) throws Exception {
        //return uploadFile(sftp, targetPath, new FileInputStream(file), mode);
        boolean flag = false;
        InputStream inputStream = null;
        long startTime = System.currentTimeMillis();
        try {
            int index = targetPath.lastIndexOf("/");
            String fileDir = targetPath.substring(0, index);
            String fileName = targetPath.substring(index + 1);
            boolean dirs = createDirs(fileDir, sftp);
            if (!dirs) {
                logger.error("Remote path error. path:{}", targetPath);
                throw new Exception("Upload File failure");
            }
            //切换到目标目录
            fileDir = fileDir.startsWith("/") ? fileDir.substring(1) : fileDir;
            sftp.cd(fileDir);
            inputStream = new FileInputStream(file);
            sftp.put(inputStream, fileName, mode);
            //切换回原目录
            int len = fileDir.split("/").length;
            String back = "";
            for (int i=0; i<len; i++) {
                back += "../";
            }
            sftp.cd(back);
            flag = true;
        } catch (Exception e) {
            flag = false;
            logger.error("Upload file failure. TargetPath: {}", targetPath, e);
            throw new Exception("Upload File failure");
        } finally {
            if (inputStream != null) {
                inputStream.close();
            }
            long costTime = System.currentTimeMillis() - startTime;
            afterUploadOne(targetPath, flag, file.length(), costTime);
            return flag;
        }
    }

    /**
     * 上传文件夹，默认断点续传的方式
     * @param localPath 本地文件
     * @param isRecursion 是否递归
     * @return
     */
    public static boolean uploadPath(File localPath, boolean isRecursion) {
        return uploadPath(localPath, isRecursion, 2);
    }
    /**
     * 上传文件夹
     * @param localPath 本地文件
     * @param isRecursion 是否递归
     * @param mode 上传模式：1-覆盖，2-断点续传，3-追加
     * @return
     */
    public static boolean uploadPath(File localPath, boolean isRecursion, int mode) {
        ChannelSftp sftp = null;
        try {
            //是否存在根目录，没有则创建
            sftp = createSftp();
            String rootPath = config.getRoot();
            rootPath = rootPath.startsWith("/") ? rootPath.substring(1) : rootPath;
            try {
                sftp.cd(rootPath);
            } catch (Exception e) {
                createDirs(rootPath, sftp);
                sftp.cd(rootPath);
            }
            logger.info("Change path to {}", config.getRoot());
            //开始上传
            uploadPathSub(sftp, localPath, isRecursion, mode);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        } finally {
            disconnect(sftp);
        }
        return true;
    }


    /**
     * 上传文件夹，该方法用于批量上传，因此中间不会申请新的sftp连接，也不会关闭连接，
     * 请调用方在批量任务开始时自己申请连接，批量任务结束后主动关闭连接
     * @param sftp sftp服务器的连接
     * @param localPath 本地文件目录
     * @param mode 上传模式：1-覆盖，2-断点续传，3-追加
     * @return 上传是否成功
     * @throws Exception
     */
    private static boolean uploadPathSub(ChannelSftp sftp, File localPath, boolean isRecursion, int mode) {
        try {
            if (localPath.isFile()) {
                //获取相对目录
                String subPath = localPath.getAbsolutePath().replace("\\", "/")
                        .substring(config.getLocalRootPath().length());
                subPath = subPath.startsWith("/") ? subPath.substring(1) : subPath;
                //上传
                SftpFileUtil.uploadFile(sftp, subPath, localPath, mode);
            }
            //遍历子文件夹和文件
            else if (isRecursion && localPath.isDirectory()) {
                File[] children = localPath.listFiles();
                if (children == null) {
                    return true;
                }
                for (File file : children) {
                    uploadPathSub(sftp, file, isRecursion, mode);
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    //创建多级目录
    private static boolean createDirs(String dirPath, ChannelSftp sftp) {
        if (dirPath != null && !dirPath.isEmpty()
                && sftp != null) {
            String[] dirs = Arrays.stream(dirPath.split("/"))
                    .filter(SftpFileUtil::isNotBlank)
                    .toArray(String[]::new);

            String back = "";

            for (String dir : dirs) {
                if (dir == null || dir.trim().length() == 0) {
                    continue;
                }
                //创建完目录再返回当前目录
                back += "../";
                try {
                    sftp.cd(dir);
                    logger.info("Change directory {}", dir);
                } catch (Exception e) {
                    try {
                        sftp.mkdir(dir);
                        logger.info("Create directory {}", dir);
                    } catch (SftpException e1) {
                        logger.error("Create directory failure, directory:{}", dir, e1);
                        e1.printStackTrace();
                    }
                    try {
                        sftp.cd(dir);
                        logger.info("Change directory {}", dir);
                    } catch (SftpException e1) {
                        logger.error("Change directory failure, directory:{}", dir, e1);
                        e1.printStackTrace();
                    }
                }
            }
            //返回当前目录
            try {
                sftp.cd(back);
            } catch (Exception e) {
                e.printStackTrace();
            }

            return true;
        }
        return false;
    }

    //是否非空字符串
    private static boolean isNotBlank(String str) {
        return str != null && str.trim().length() > 0;
    }

    /**
     * 记录上传日志，并迁移文件
     * @param targetPath SFTP上传的目标地址
     * @param flag 是否上传成功
     * @return void
     */
    private static void afterUploadOne(String targetPath, boolean flag, long fileSize, long costTime) {
        SftpLogUtil.afterUploadOne(targetPath, flag, fileSize, costTime);
    }

}