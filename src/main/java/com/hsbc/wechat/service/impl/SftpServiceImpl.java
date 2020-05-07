package com.hsbc.wechat.service.impl;

import com.hsbc.wechat.service.SftpService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import com.hsbc.wechat.util.SftpFileUtil;

import java.io.File;

/**
 * SFTP文件上传服务，单向上传，不做下载
 */
@Service
public class SftpServiceImpl implements SftpService {

    //本地文件根目录，存放聊天记录、从微信下载的媒体文件等
    @Value("${wechat.fileroot}")
    private String localRootFilePath;

    //本地备份文件的根目录，上传成功后的文件，迁移到备份目录
    @Value("${wechat.bakfileroot}")
    private String localBakRootFilePath;

    /**
     * 文件上传到SFTP服务器
     *
     * @param localPath   本地文件地址
     * @param isRecursion 是否递归上传子目录中的文件和文件夹
     * @param mode 上传模式：1-覆盖，2-断点续传，3-追加
     * @return 是否全部上传成功
     */
    @Override
    public boolean uploadPath(File localPath, boolean isRecursion, int mode) {
        boolean flag = SftpFileUtil.uploadPath(localPath, isRecursion, mode);
        if (flag) {
            movePath(localPath);
        }
        return flag;
    }

    /**
     * 文件上传到SFTP服务器
     *
     * @param localPath   本地文件地址
     * @param isRecursion 是否递归上传子目录中的文件和文件夹
     * @param mode 上传模式：1-覆盖，2-断点续传，3-追加
     * @return 是否全部上传成功
     */
    @Override
    public boolean uploadPath(String localPath, boolean isRecursion, int mode) {
        return uploadPath(new File(localPath), isRecursion, mode);
    }

    /**
     * 单个文件上传到SFTP服务器，默认覆盖同名文件，暂不支持跳过
     *
     * @param targetPath SFTP目标地址
     * @param localFile  本地文件地址对应的File对象
     * @param mode 上传模式：1-覆盖，2-断点续传，3-追加
     * @return 是否上传成功
     */
    @Override
    public boolean uploadFile(String targetPath, File localFile, int mode) {
        try {
            //上传
            SftpFileUtil.uploadFile(targetPath, localFile, mode);
            //已上传文件的迁移
            moveFile(localFile);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    /**
     * 单个文件上传到SFTP服务器，默认覆盖同名文件，暂不支持跳过
     *
     * @param targetPath SFTP目标地址
     * @param localFilePath  本地文件地址
     * @param mode 上传模式：1-覆盖，2-断点续传，3-追加
     * @return 是否上传成功
     */
    @Override
    public boolean uploadFile(String targetPath, String localFilePath, int mode) {
        return uploadFile(targetPath, new File(localFilePath), mode);
    }

    /**
     * 文件夹剪切，已上传成功的文件夹，挪到备份文件夹。
     * 全部上传完成后，统一迁移。（已经在SftpFileUtil中实现，该方法实际只删除空文件夹）
     * 如果需要单个文件上传完成马上迁移，需修改文件上传公用类
     * @param srcFile
     */
    private void movePath(File srcFile) {
        //文件则移动，文件夹则递归
        if (srcFile.isFile()) {
            moveFile(srcFile);
        } else {
            File[] childrenFile = srcFile.listFiles();
            if (childrenFile != null) {
                for (File f : childrenFile) {
                    movePath(f);
                }
            }
        }
        /*
        //剩下一堆空文件夹怎么处理？删除？删除会误删其他正在从微信下载的文件。
        try {
            srcFile.delete();
            System.out.println("delete file=" + srcFile.getAbsolutePath());
        } catch(Exception e) {
            e.printStackTrace();
        }
        */
    }

    /**
     * 文件剪切，已上传成功的文件，挪到备份文件夹
     * @param srcFile
     */
    private void moveFile(File srcFile) {
        //转换根目录为标准目录，消除路径中正反斜杠的混乱问题
        String localRoot = localRootFilePath.replace("\\", "/");
        String localBakRoot = localBakRootFilePath.replace("\\", "/");
        //获取原文件的绝对路径和相对路径
        String srcPath = srcFile.getAbsolutePath();
        String subPath = srcPath.substring(localRoot.length());
        //目标文件
        File destFile = new File((localBakRoot + "/" + subPath).replace("//", "/"));
        File parent = destFile.getParentFile();
        if (!parent.exists()) {
            parent.mkdirs();
        }
        //文件剪切（重命名）
        try {
            srcFile.renameTo(destFile);
            System.out.println("move srcFile=" + srcFile.getAbsolutePath());
            System.out.println("move destFile=" + destFile.getAbsolutePath());
        } catch(Exception e) {
            e.printStackTrace();
        }

    }

    /**
     * 文件上传到SFTP服务器，默认断点续传的方式
     * @param localPath 本地文件地址
     * @param isRecursion 是否递归上传子目录中的文件和文件夹
     * @return 是否全部上传成功
     */
    public boolean uploadPath(String localPath, boolean isRecursion) {
        return uploadPath(localPath, isRecursion, 2);
    }

    /**
     * 文件上传到SFTP服务器，默认断点续传的方式
     * @param localFile 本地文件地址对应的File对象
     * @param isRecursion 是否递归上传子目录中的文件和文件夹
     * @return 是否全部上传成功
     */
    public boolean uploadPath(File localFile, boolean isRecursion) {
        return uploadPath(localFile, isRecursion, 2);
    }

    /**
     * 单个文件上传到SFTP服务器，默认断点续传的方式
     * @param targetPath SFTP目标地址
     * @param localFile 本地文件地址对应的File对象
     * @return 是否上传成功
     */
    public boolean uploadFile(String targetPath, File localFile) {
        return uploadFile(targetPath, localFile, 2);
    }

    /**
     * 单个文件上传到SFTP服务器，默认断点续传的方式
     * @param targetPath SFTP目标地址
     * @param localFilePath 本地文件地址
     * @return 是否上传成功
     */
    public boolean uploadFile(String targetPath, String localFilePath) {
        return uploadFile(targetPath, localFilePath, 2);
    }

}
