package com.hsbc.wechat.service;

import java.io.File;

public interface SftpService {

    /**
     * 文件上传到SFTP服务器
     * @param localPath 本地文件地址
     * @param isRecursion 是否递归上传子目录中的文件和文件夹
     * @param mode 上传模式：1-覆盖，2-断点续传，3-追加，默认是覆盖
     * @return 是否全部上传成功
     */
    public boolean uploadPath(String localPath, boolean isRecursion, int mode);

    /**
     * 文件上传到SFTP服务器
     * @param localFile 本地文件地址对应的File对象
     * @param isRecursion 是否递归上传子目录中的文件和文件夹
     * @param mode 上传模式：1-覆盖，2-断点续传，3-追加
     * @return 是否全部上传成功
     */
    public boolean uploadPath(File localFile, boolean isRecursion, int mode);

    /**
     * 单个文件上传到SFTP服务器，默认覆盖同名文件，暂不支持跳过
     * @param targetPath SFTP目标地址
     * @param localFile 本地文件地址对应的File对象
     * @param mode 上传模式：1-覆盖，2-断点续传，3-追加
     * @return 是否上传成功
     */
    public boolean uploadFile(String targetPath, File localFile, int mode);

    /**
     * 单个文件上传到SFTP服务器，默认覆盖同名文件，暂不支持跳过
     * @param targetPath SFTP目标地址
     * @param localFilePath 本地文件地址
     * @param mode 上传模式：1-覆盖，2-断点续传，3-追加
     * @return 是否上传成功
     */
    public boolean uploadFile(String targetPath, String localFilePath, int mode);

    /**
     * 文件上传到SFTP服务器，默认断点续传的方式
     * @param localPath 本地文件地址
     * @param isRecursion 是否递归上传子目录中的文件和文件夹
     * @return 是否全部上传成功
     */
    public boolean uploadPath(String localPath, boolean isRecursion);

    /**
     * 文件上传到SFTP服务器，默认断点续传的方式
     * @param localFile 本地文件地址对应的File对象
     * @param isRecursion 是否递归上传子目录中的文件和文件夹
     * @return 是否全部上传成功
     */
    public boolean uploadPath(File localFile, boolean isRecursion);

    /**
     * 单个文件上传到SFTP服务器，默认断点续传的方式
     * @param targetPath SFTP目标地址
     * @param localFile 本地文件地址对应的File对象
     * @return 是否上传成功
     */
    public boolean uploadFile(String targetPath, File localFile);

}
