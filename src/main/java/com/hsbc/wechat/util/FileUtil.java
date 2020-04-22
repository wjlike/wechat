package com.hsbc.wechat.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FileUtil {

    protected static Logger logger = LoggerFactory.getLogger(FileUtil.class);

    /**
     *
     * @Description：读取远程服务，并写入本地服务
     * @param url
     * @param filePath: 返回结果描述
     * @return void: 返回值类型
     * @throws
     */
    public static void ReadAndWriteFile(String pathUrl, String filePath) {
        URL url = null;
        int byteread = 0;
        try {
            url = new URL(pathUrl);
            URLConnection conn = url.openConnection();
            InputStream inputStream = conn.getInputStream();

            File file = new File(filePath.substring(0, filePath.lastIndexOf('/')));
            file.mkdirs();
            FileOutputStream fs = new FileOutputStream(new File(filePath));

            byte[] buffer = new byte[1024];
            while ((byteread = inputStream.read(buffer)) != -1) {
                fs.write(buffer, 0, byteread);
            }
            inputStream.close();
            fs.close();

        } catch (Exception e) {
            logger.error("从远程服务器读取并写入失败！" + e.getMessage());
        }
    }

    /**
     *
     * @Description：从远程服务器读取文件
     * @param pathUrl
     * @return: 返回结果描述
     * @return InputStream: 返回值类型
     * @throws
     */
    public static InputStream readRemoteFile(String pathUrl) {
        URL url = null;
        InputStream inputStream = null;
        try {
            url = new URL(pathUrl);
            URLConnection conn = url.openConnection();
            inputStream = conn.getInputStream();

        } catch (Exception e) {
            logger.error("从远程服务器读取并写入失败！" + e.getMessage());
        }
        return inputStream;
    }

    /**
     *
     * @Description：向文件写入数据
     * @param filePath
     * @param countent: 返回结果描述
     * @return void: 返回值类型
     * @throws
     */
    public static void writeStringToFile(String filePath, String countent) {
        try {
            File file = new File(filePath.substring(0, filePath.lastIndexOf('/')));
            if(!file.exists()){file.createNewFile();}
            FileOutputStream outputStream = new FileOutputStream(new File(filePath));
            outputStream.write(countent.getBytes());
            outputStream.close();
        } catch (Exception e) {
            logger.error("写入文件失败:" + e.getMessage());
        }
    }

    /**
     *
     * @Description：读取文件中的字符串
     * @param filePath
     * @return: 返回结果描述
     * @return String: 返回值类型
     * @throws
     */
    public static String readStringFromFile(String filePath) {
        String encoding = "UTF-8";
        File file = new File(filePath);
        Long filelength = file.length();
        byte[] filecontent = new byte[filelength.intValue()];
        try {
            FileInputStream in = new FileInputStream(file);
            in.read(filecontent);
            in.close();
        } catch (FileNotFoundException e) {
            logger.error("文件不存在:" + e.getMessage());
        } catch (IOException e) {
            logger.error("读取文件失败:" + e.getMessage());
        }
        try {
            return new String(filecontent, encoding);
        } catch (UnsupportedEncodingException e) {
            logger.error("The OS does not support " + encoding);
            return null;
        }
    }

    /**
     *
     * @Description:将文件移动到目标路径下
     * @param srcFile:待移动的源文件文件指针
     * @param destPath:目标路径
     * @return: 返回结果描述
     * @return boolean: 返回值类型
     * @throws
     */
    public static boolean move(File srcFile, String destPath) {
        File dir = new File(destPath);
        boolean success = srcFile.renameTo(new File(dir, srcFile.getName()));
        return success;
    }

    /**
     *
     * @Description:将文件移动到目标路径下
     * @param srcFile:待移动的源文件文件指针
     * @param destPath：目标路径
     * @return: 返回结果描述
     * @return boolean: 返回值类型
     * @throws
     */
    public static boolean Move(String srcFile, String destPath) {
        File file = new File(srcFile);
        File dir = new File(destPath);
        boolean success = file.renameTo(new File(dir, file.getName()));
        return success;
    }

    /**
     *
     * @Description：列出某个目录下的文件列表并且按照时间排序
     * @param filePath
     * @return: 返回结果描述
     * @return ArrayList<File>: 返回值类型
     * @throws
     */
    public static ArrayList<File> getSortedFile(String filePath) {
        File[] fileArray = new File(filePath).listFiles();

        ArrayList<File> fileList = new ArrayList<File>();
        if (fileArray == null || fileArray.length == 0) {
            return null;
        }
        for (File f : fileArray) {
            fileList.add(f);
        }

        Collections.sort(fileList, new Comparator<File>() {
            @Override
            public int compare(File f1, File f2) {
                long diff = f1.lastModified() - f2.lastModified();
                if (diff > 0)
                    return 1;
                else if (diff == 0)
                    return 0;
                else
                    return -1;
            }
        });

        return fileList;
    }

    /**
     *
     * @Description：清除文件内容
     * @param file: 返回结果描述
     * @return void: 返回值类型
     * @throws
     */
    public static void clearFile(File file) {
        try {
            if (!file.exists()) {
                String filePathStr = file.getPath();
                logger.info("filePath", filePathStr);
                File filePath = new File(filePathStr.substring(0, filePathStr.lastIndexOf('/')));

                filePath.mkdirs();
                file.createNewFile();
            }
            FileWriter fileWriter = new FileWriter(file);
            fileWriter.write("");
            fileWriter.flush();
            fileWriter.close();
        } catch (Exception e) {
            logger.error("清除文件失败:" + e.getMessage());
        }
    }

    /**
     *
     * @Description：创建相关文件目录
     * @param file: 返回结果描述
     * @return void: 返回值类型
     * @throws
     */
    public static void CreateDir(String filePath) {
        File file = new File(filePath);
        if (file.exists()) {
        } else {
            try {
                file.mkdirs();
            } catch (Exception e) {
                logger.error("创建文件目录失败:" + e.getMessage());
            }
        }

    }

}
