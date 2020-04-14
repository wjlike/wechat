package com.hsbc.wechat.util;

import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.hsbc.wechat.bean.WxLogBean;
import com.hsbc.wechat.config.BussinessConfig;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.Signature;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

/**
 * 记录与微信服务器之间的请求日志。
 * 此类将以文件的形式记录程序与微信服务器的请求日志。
 * 如需要用数据库记录请求日志，请参考此类实现。
 */
@Component
public class FileLogUtil {

    //待写入文件的日志信息队列
    public static List<WxLogBean> wxLogBeanList = new LinkedList<WxLogBean>();
    //日志文件文件名前缀，实际的日志文件名形式举例：request_log_20200413.log
    private static final String PRE_LOG_NAME = "request_log_";

    //对内接口，准备将对象写入日志文件，只需要传入一个LogBean实例即可
    private static void writeLog(WxLogBean wxLogBean) {
        add(wxLogBean);
        writeToFile();
    }

    //内部使用，线程安全
    private synchronized static void add(WxLogBean wxLogBean) {
        wxLogBeanList.add(wxLogBean);
    }

    //实际的日志处理逻辑，循环队列中所有的对象，依次写入文件
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
            String content = null;
            WxLogBean wxLogBean = new WxLogBean();
            //写入表头
            if (!fileExists) {
                content = wxLogBean.getTableHead();
                pw.println(content);
            }
            //循环队列，写入日志
            for (WxLogBean o : wxLogBeanList) {
                wxLogBean = o;
                content = wxLogBean.getTableContent();
                pw.println(content);
                wxLogBeanList.remove(wxLogBean);
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

    /**
     * 记录微信请求日志
     * @param result 微信返回的json内容，也可以为空，也可以为Exception或Throwable
     * @param startTimeMillis 请求的开始时间System.currentTimeMills
     * @param seq 如果请求参数有seq，请传实际的seq，否则可以传0
     * @param sdkFileId 如果请求参数有文件id，请传实际的sdkFileId，否则可以传0
     */
    @Async
    public static void writeLog(Object result, long startTimeMillis, long seq, String sdkFileId) {
        //初始化日志对象
        WxLogBean logbean = new WxLogBean();
        logbean.setStartTimeMillis(startTimeMillis);
        String id = System.currentTimeMillis() + "_" + lpad((int)(Math.random()*1000), 3);
        logbean.setId(id);
        logbean.setStartSeq(seq);
        logbean.setSdkFileId(sdkFileId);
        logbean.setErrCode("0");
        logbean.setErrMsg("ok");
        //出现异常
        if (result instanceof Exception || result instanceof Throwable) {
            logbean.setErrCode("1");
            logbean.setErrMsg(result.toString());
        }
        //正常结束
        else if (result != null) {
            String returnStr = result.toString();
            //尝试解析json
            if (returnStr != null && returnStr.trim().length() > 0) {
                //防止json转换失败而记录不到日志
                try {
                    JSONObject jsonObj = JSONObject.parseObject(returnStr);
                    logbean.setErrCode(jsonObj.getString("errcode"));
                    logbean.setErrMsg(jsonObj.getString("errmsg"));
                    JSONArray chatDatas = jsonObj.getJSONArray("chatDatas");
                    int rows = (chatDatas == null ? 0 : chatDatas.size());
                    if (rows > 0) {
                        //认为最后一条数据的seq是最大的，如果不是，要修改为循环判断
                        JSONObject chatData = (JSONObject)chatDatas.get(chatDatas.size() - 1);
                        int seqMax = chatData.getIntValue("seq");
                        logbean.setMaxSeq(seqMax);
                    }
                    logbean.setDataRows(rows);
                } catch(Exception e) {
                    e.printStackTrace();
                }
            }
        }
        //记录截止时间、耗时
        long endTime = System.currentTimeMillis();
        logbean.setEndTimeMillis(endTime);
        logbean.setCostTimeMillis(endTime - logbean.getStartTimeMillis());
        String makeTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
        logbean.setMakeTime(makeTime);
        System.out.println(logbean);
        writeLog(logbean);
    }

    //给整数的前面补零
    private static String lpad(int num, int len) {
        String s = num + "";
        int lenS = s.length();
        if (lenS >= len) {
            return s.substring(lenS - len);
        }
        String zero = "00000000000000000000";
        return zero.substring(0, len - lenS) + s;
    }

}

