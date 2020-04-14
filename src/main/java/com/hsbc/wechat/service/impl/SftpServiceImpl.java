package com.hsbc.wechat.service.impl;

import com.hsbc.wechat.service.SftpService;
import com.hsbc.wechat.util.SftpFileUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

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
     * @param targetPath  SFTP目标地址
     * @param localPath   本地文件地址
     * @param isRecursion 是否递归上传子目录中的文件和文件夹
     * @param isCover     如果服务器已有同名文件，是否覆盖（默认覆盖，暂不支持跳过）
     * @return 是否全部上传成功
     */
    @Override
    public boolean uploadPath(String targetPath, String localPath, boolean isRecursion, boolean isCover) {
        try {
            //上传
            SftpFileUtil.uploadFilePath(targetPath, localPath);
            //已上传文件的迁移
            movePath(new File(localPath));
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    /**
     * 文件上传到SFTP服务器
     *
     * @param targetPath  SFTP目标地址
     * @param localFile   本地文件地址对应的File对象
     * @param isRecursion 是否递归上传子目录中的文件和文件夹
     * @param isCover     如果服务器已有同名文件，是否覆盖（默认覆盖，暂不支持跳过）
     * @return 是否全部上传成功
     */
    @Override
    public boolean uploadPath(String targetPath, File localFile, boolean isRecursion, boolean isCover) {
        try {
            //上传
            SftpFileUtil.uploadFilePath(targetPath, localFile);
            //已上传文件的迁移
            movePath(localFile);
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
     * @param localFile  本地文件地址对应的File对象
     * @return 是否上传成功
     */
    @Override
    public boolean uploadFile(String targetPath, File localFile) {
        try {
            //上传
            SftpFileUtil.uploadFile(targetPath, localFile);
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
     * @return 是否上传成功
     */
    @Override
    public boolean uploadFile(String targetPath, String localFilePath) {
        return uploadFile(targetPath, new File(localFilePath));
    }

    /**
     * 文件夹剪切，已上传成功的文件夹，挪到备份文件夹
     * @param srcFile
     */
    private void movePath(File srcFile) {
        //文件则移动，文件夹则递归
        if (srcFile.isFile()) {
            moveFile(srcFile);
        } else {
            File[] childrenFile = srcFile.listFiles();
            for (File f : childrenFile) {
                movePath(f);
            }
        }
        //剩下一堆空文件夹怎么处理？删除？
        srcFile.delete();
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
        srcFile.renameTo(destFile);
    }

}
