package com.hsbc.wechat.util;
import java.io.*;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.CollectionUtils;

/**
 * @Description: CSV生成工具类
 * @Author: hunger.zhu
 * @CreateDate: 2019/4/10 15:21
 */
public class CsvUtil {
    private static final Logger logger = LoggerFactory.getLogger(CsvUtil.class);

    /**
     * 写出CSV文件，每一行数据为一个Object对象
     * @param data  List<Object>
     * @param header    第一行标题
     * @param filePath  写出文件的绝对路径
     * @param split     CSV文件分隔符，一般为","
     * @throws Exception
     */
    public static void writeCSV(List<?> data, List<String> header, String filePath, String split) throws Exception {
        BufferedWriter bw = null;
        try {
            bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(filePath), "UTF-8"));
            // 填充对象属性值
            if (!CollectionUtils.isEmpty(header)) {
                // 添加头部（首行）
                for (int i = 0; i < header.size(); i++) {
                    bw.append(header.get(i));
                    if (i != header.size() - 1) {
                        bw.append(split);
                    }
                }
                bw.newLine();
            }
            // 填充数据
            if (!CollectionUtils.isEmpty(data)) {
                Class<? extends Object> objClass = data.get(0).getClass();
                Field[] fields = objClass.getDeclaredFields();
                for (int i = 0; i < data.size(); i++) {
                    for (int j = 0; j < fields.length; j++) {
                        Method[] methods = objClass.getDeclaredMethods();
                        for (Method method : methods) {
                            if (method.getName().equalsIgnoreCase("get" + fields[j].getName())) {
                                String property = (String) method.invoke(data.get(i), null);
                                bw.append(property == null ? "" : property);
                                break;
                            }
                        }
                        if (j != fields.length - 1) {
                            bw.append(split);
                        }
                    }
                    if (i != data.size() - 1) {
                        bw.newLine();
                    }
                }
            }
        } catch (Exception e) {
            logger.error("Write CSV exception------", e);
            throw e;
        } finally {
            if (bw != null) {
                try {
                    bw.close();
                } catch (IOException e) {
                    logger.error("Close OutputStream exception------", e);
                }
            }
        }
    }

    /**
     * 写出CSV文件，每一行数据为一个List<String>集合
     * @param data  List<String>
     * @param header    第一行标题
     * @param filePath  写出文件的绝对路径
     * @param split     CSV文件分隔符，一般为","
     * @throws Exception
     */
    public static void writeCSVFromList(List<List<String>> data, List<String> header, String filePath,
                                        String split) throws Exception {
        BufferedWriter bw = null;
        try {
            bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(filePath), "UTF-8"));
            // 填充对象属性值
            if (!CollectionUtils.isEmpty(header)) {
                // 添加头部（首行）
                for (int i = 0; i < header.size(); i++) {
                    bw.append(header.get(i));
                    if (i != header.size() - 1) {
                        bw.append(split);
                    }
                }
                bw.newLine();
            }
            // 填充数据
            if (!CollectionUtils.isEmpty(data)) {
                for (int i = 0; i < data.size(); i++) {
                    List<String> list = data.get(i);
                    for (int j = 0; j < list.size(); j++) {
                        bw.append(list.get(j));
                        if (j != list.size() - 1) {
                            bw.append(split);
                        }
                    }
                    if (i != data.size() - 1) {
                        bw.newLine();
                    }
                }
            }
        } catch (Exception e) {
            logger.error("Write CSV exception------", e);
            throw e;
        } finally {
            if (bw != null) {
                try {
                    bw.close();
                } catch (IOException e) {
                    logger.error("Close OutputStream exception------", e);
                }
            }
        }
    }

    /**
     * 像文件中追加字符串
     * @param data 字符串
     * @param fileName 目录+文件名
     * @throws Exception
     */
    public static void writeCsvFromString(String data,String fileName) throws Exception {
        synchronized (CsvUtil.class){
            data = handleCsvComma(data);
            RandomAccessFile randomFile = null;
            try {
                // 打开一个随机访问文件流，按读写方式
                File file = new File(fileName+".csv");
                if (!file.exists()){
                    file.createNewFile();
                }
                randomFile = new RandomAccessFile(fileName+".csv", "rw");

                // 将写文件指针移到文件尾。
                long fileLength = randomFile.length();
                randomFile.seek(fileLength);
                randomFile.writeBytes(data);
                randomFile.writeBytes("\r\n");
            } catch (IOException e) {
                e.printStackTrace();
            } finally{
                if(randomFile != null){
                    try {
                        randomFile.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    /**
     * 转义csv内逗号双引号等
     * @param str
     * @return
     */
    public static String handleCsvComma(String str) {
        StringBuilder sb = new StringBuilder();
        String handleStr=str;
        //先判断字符里是否含有逗号
        if(str.contains(",")){
            //如果还有双引号，先将双引号转义，避免两边加了双引号后转义错误
            if(str.contains("\"")){
                handleStr=str.replace("\"", "\"\"");
            }
            //将逗号转义
            handleStr="\""+handleStr+"\"";
        }

        return sb.append(handleStr).append(",").toString();
    }

}
