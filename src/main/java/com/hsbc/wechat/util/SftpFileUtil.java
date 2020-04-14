package com.hsbc.wechat.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Arrays;

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

    //上传单个文件
    public static boolean uploadFile(String targetPath, InputStream inputStream) throws Exception {
        ChannelSftp sftp = createSftp();
        try {
            sftp.cd(config.getRoot());
            logger.info("Change path to {}", config.getRoot());

            int index = targetPath.lastIndexOf("/");
            String fileDir = targetPath.substring(0, index);
            String fileName = targetPath.substring(index + 1);
            boolean dirs = createDirs(fileDir, sftp);
            if (!dirs) {
                logger.error("Remote path error. path:{}", targetPath);
                throw new Exception("Upload File failure");
            }
            sftp.put(inputStream, fileName);
            return true;
        } catch (Exception e) {
            logger.error("Upload file failure. TargetPath: {}", targetPath, e);
            throw new Exception("Upload File failure");
        } finally {
            disconnect(sftp);
        }
    }

    //上传单个文件
    public static boolean uploadFile(String targetPath, File file) throws Exception {
        return uploadFile(targetPath, new FileInputStream(file));
    }

    /**
     * 递归上传整个目录及子目录下的所有文件，该方法暴露给外部使用
     * @param targetPath 服务器目标目录
     * @param localFilePath 本地目录
     * @return
     * @throws Exception
     */
    public static void uploadFilePath(String targetPath, String localFilePath) throws Exception {
        uploadFilePath(targetPath, new File(localFilePath));
    }

    /**
     * 递归上传整个目录及子目录下的所有文件，该方法暴露给外部使用
     * @param targetPath 服务器目标目录
     * @param localFilePath 本地目录
     * @return
     * @throws Exception
     */
    public static void uploadFilePath(String targetPath, File localFilePath) throws Exception {
        ChannelSftp sftp = createSftp();
        uploadFilePathSub(targetPath, localFilePath, sftp);
        disconnect(sftp);
    }

    /**
     * 递归上传整个目录及子目录下的所有文件，工具类内部使用
     * @param targetPath 服务器目标目录
     * @param localFilePath 本地目录
     * @param sftp 可使用的sftp连接，需要调用者自己创建连接，并在处理完成后主动释放
     * @return
     * @throws Exception
     */
    private static void uploadFilePathSub(String targetPath, File localFilePath, ChannelSftp sftp) throws Exception {
        //递归出口：如果是文件，则直接上传
        if (localFilePath.isFile()) {
            sftp.put(new FileInputStream(localFilePath), localFilePath.getName());
        } else { //如果是目录，则进入sftp服务器的目录（进入失败则创建目录）
            String dir = localFilePath.getName();
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
            //循环本地目录下的所有文件，上传到sftp服务器的对应目录下
            File[] files = localFilePath.listFiles();
            if (files == null) {
                return;
            }
            for (File file : files) {
                uploadFilePathSub(targetPath + "/" + dir, file, sftp);
            }
        }
    }

    //创建多级目录
    private static boolean createDirs(String dirPath, ChannelSftp sftp) {
        if (dirPath != null && !dirPath.isEmpty()
                && sftp != null) {
            String[] dirs = Arrays.stream(dirPath.split("/"))
                    .filter(SftpFileUtil::isNotBlank)
                    .toArray(String[]::new);

            for (String dir : dirs) {
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
            return true;
        }
        return false;
    }

    //是否非空字符串
    private static boolean isNotBlank(String str) {
        return str != null && str.trim().length() > 0;
    }

}