package com.hsbc.wechat.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class BussinessConfig {

    private static String corpid;
    private static String secret;
    private static String prikey;
    private static String downloadpath;
    private static String seqFilepPth;
    private static String logPath;

    public static String getCorpid() {
        return corpid;
    }

    public static String getSecret() {
        return secret;
    }

    public static String getPrikey() {
        return prikey;
    }


    public static String getDownloadpath() {
        return downloadpath;
    }

    public static String getSeqFilepPth() {
        return seqFilepPth;
    }
    public static String getLogPath() {
        return logPath;
    }

    @Value("${wechat.corpid}")
    public  void setCorpid(String corpid) {
        BussinessConfig.corpid = corpid;
    }
    @Value("${wechat.secret}")
    public  void setSecret(String secret) {
        BussinessConfig.secret = secret;
    }
    @Value("${wechat.prikey}")
    public  void setPrikey(String prikey) {
        BussinessConfig.prikey = prikey;
    }
    @Value("${wechat.downloadpath}")
    public  void setDownloadpath(String downloadpath) {
        BussinessConfig.downloadpath = downloadpath;
    }
    @Value("${wechat.seqfileppth}")
    public  void setSeqFilepPth(String seqFilepPth) {
        BussinessConfig.seqFilepPth = seqFilepPth;
    }
    @Value("${wechat.logpath}")
    public  void setLogPath(String logPath) {
        BussinessConfig.logPath = logPath;
    }
}
