package com.hsbc.wechat.util;

import com.hsbc.wechat.bean.SftpLog;
import com.hsbc.wechat.config.BussinessConfig;
import org.springframework.scheduling.annotation.Async;

import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

public class SftpLogUtil {

    //待写入文件的日志信息队列
    public static List<SftpLog> logList = new LinkedList<SftpLog>();
    //日志文件文件名前缀，实际的日志文件名形式举例：request_log_20200413.log
    private static final String PRE_LOG_NAME = "sftp_log_";

    public static void afterUploadOne(String path, boolean isSuccess) {
        SftpLog sftpLog = new SftpLog();
        sftpLog.setPath(path);
        sftpLog.setSuccessFlag(isSuccess);
        //同步迁移文件
        String srcFilePath = (sftpLog.getLocalRootFilePath() + "/" + path).replace("//", "/");
        String destFilePath = (sftpLog.getLocalBakRootFilePath() + "/" + path).replace("//", "/");
        File srcFile = new File(srcFilePath);
        if (srcFile.isFile()) {
            File destFile = new File(destFilePath);
            File destParent = destFile.getParentFile();
            if (!destParent.exists()) {
                destParent.mkdirs();
            }
            srcFile.renameTo(destFile);
        }
        //异步写日志
        writeLog(sftpLog);
    }

    //对内接口，准备将对象写入日志文件，只需要传入一个LogBean实例即可
    private static void writeLog(SftpLog sftpLog) {
        add(sftpLog);
        writeToFile();
    }

    //内部使用，线程安全
    private synchronized static void add(SftpLog sftpLog) {
        logList.add(sftpLog);
    }

    //实际的日志处理逻辑，循环队列中所有的对象，依次写入文件
    @Async
    private static void writeToFile() {
        Date d = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
        String fileName = PRE_LOG_NAME + sdf.format(d) + ".log";
        String filePath = BussinessConfig.getLogPath(); //"D:/tmp/test/logs";
        File file = new File(filePath, fileName);
        PrintWriter pw = null;
        boolean fileExists = true;
        try {
            File path = new File(filePath);
            //创建目录
            if (!path.exists()) {
                path.mkdirs();
                fileExists = false;
            }
            //创建文件
            if (!file.exists()) {
                file.createNewFile();
                fileExists = false;
            }
            //追加方式写入文件
            pw = new PrintWriter(new FileWriter(file, true));
            String content = "";
            for (SftpLog o : logList) {
                sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
                content = sdf.format("datetime: " + new Date()) + "\t status: " + o.isSuccessFlag() + "\t to path: " + o.getPath();
                pw.println(content);
                logList.remove(o);
            }
            pw.flush();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (pw != null) {
                pw.close();
            }
        }
    }

}
