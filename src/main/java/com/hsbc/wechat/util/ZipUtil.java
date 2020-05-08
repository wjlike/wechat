package com.hsbc.wechat.util;

import lombok.extern.slf4j.Slf4j;

import java.io.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Slf4j
public class ZipUtil {

    /**
     * 文件或文件夹压缩入口方法
     * @param zipFileName 压缩后的目标文件名，例如：test.zip
     * @param dir 待压缩的文件目录，字符串格式
     * @throws Exception 抛出的异常
     * @return 压缩是否成功，如果压缩失败，不建议使用该压缩文件
     */
    public static boolean zip(String zipFileName, String dir) {
        // 创建ZipOutputStream类对象
        File zipFile = new File(zipFileName);
        log.info("zipFile=" + zipFile.getAbsolutePath());
        File parent = zipFile.getParentFile();
        if (!parent.exists()) {
            parent.mkdirs();
        }
        ZipOutputStream out = null;
        // 输出信息
        log.info("压缩中…");
        // 调用方法，递归压缩
        try {
            out = new ZipOutputStream(new FileOutputStream(zipFile));
            zipCore(out,  dir, "");
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            log.info("文件压缩失败：" + e.toString());
            // 失败了删除压缩文件，以免被后来者使用到
            new File(zipFileName).delete();
            return false;
        } finally {
            try {
                if (out != null) {
                    out.close();
                }
            } catch (Exception e2) {}
            log.info("压缩完成…");
        }
    }

    /**
     * 实际的压缩方法，递归文件夹下的文件进行压缩
     * @param outputStream zip文件的输出流
     * @param filePath 当前文件路径
     * @param baseDir 相对与压缩根目录的文件路径
     * @throws Exception
     */
    private static void zipCore(ZipOutputStream outputStream, String filePath, String baseDir)
            throws Exception {
        //log.info("filePath=" + filePath);
        File file = new File(filePath);
        // 如果是目录，递归压缩
        if (file.isDirectory()) {
            // 获取目录内的所有文件名
            String[] subFilePath = file.list();
            // 写入此目录的entry
            outputStream.putNextEntry(new ZipEntry(baseDir + "/"));
            // 递归
            baseDir = baseDir.length() == 0 ? "" : baseDir + "/";
            for (int i = 0; i < subFilePath.length; i++) {
                zipCore(outputStream, filePath + "/" + subFilePath[i], baseDir + subFilePath[i]);
            }
        } else {
            // 创建新的进入点
            outputStream.putNextEntry(new ZipEntry(baseDir));
            log.info("压缩：" + filePath);
            // 创建FileInputStream对象
            FileInputStream in = null;
            try {
                // 文件写入zip中
                in = new FileInputStream(filePath);
                int b;
                while ((b = in.read()) != -1) {
                    outputStream.write(b);
                }
            } catch (Exception e) {
                throw e;
            } finally {
                if (in != null) {
                    in.close();
                }
            }
        }
    }

    public static void main(String[] args) {
        try {
            boolean flag = ZipUtil.zip("123.zip", "C:\\Users\\lanruijin\\Desktop\\wechat-like");
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

}
