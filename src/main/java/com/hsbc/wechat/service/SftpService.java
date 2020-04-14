package com.hsbc.wechat.service;

import java.io.File;

public interface SftpService {

    /**
     * 文件上传到SFTP服务器
     * @param targetPath SFTP目标地址
     * @param localPath 本地文件地址
     * @param isRecursion 是否递归上传子目录中的文件和文件夹
     * @param isCover 如果服务器已有同名文件，是否覆盖（默认覆盖，暂不支持跳过）
     * @return 是否全部上传成功
     */
    public boolean uploadPath(String targetPath, String localPath, boolean isRecursion, boolean isCover);

    /**
     * 文件上传到SFTP服务器
     * @param targetPath SFTP目标地址
     * @param localFile 本地文件地址对应的File对象
     * @param isRecursion 是否递归上传子目录中的文件和文件夹
     * @param isCover 如果服务器已有同名文件，是否覆盖（默认覆盖，暂不支持跳过）
     * @return 是否全部上传成功
     */
    public boolean uploadPath(String targetPath, File localFile, boolean isRecursion, boolean isCover);

    /**
     * 单个文件上传到SFTP服务器，默认覆盖同名文件，暂不支持跳过
     * @param targetPath SFTP目标地址
     * @param localFile 本地文件地址对应的File对象
     * @return 是否上传成功
     */
    public boolean uploadFile(String targetPath, File localFile);

    /**
     * 单个文件上传到SFTP服务器，默认覆盖同名文件，暂不支持跳过
     * @param targetPath SFTP目标地址
     * @param localFilePath 本地文件地址
     * @return 是否上传成功
     */
    public boolean uploadFile(String targetPath, String localFilePath);

}
