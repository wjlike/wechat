package com.hsbc.wechat.config;

import com.alibaba.fastjson.JSONObject;
import com.hsbc.wechat.util.HttpUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;

import java.io.IOException;

@Configuration
public class BussinessConfig {

    private static String corpid;
    private static String secret;
    private static String prikey;
    private static String prikeyURL;
    private static String downloadpath;
    private static String seqFilepPth;
    private static String logPath;

    private static String httpsUploadUrl;
    private static String zipPath;
    private static String bakFilePath;

    //简单的处理
    public static String JASYPT_ENCRYPTOR_PASSWORD="hfworkwx";

    public static String getCorpid() {
        return corpid;
    }

    public static String getSecret() {
        return secret;
    }

    public static String getPrikey() {
        JSONObject jsonObject = null;
        try {
            jsonObject = HttpUtil.doGetJsonByUrl(prikeyURL);
            prikey = (String)jsonObject.get("prikey");
        } catch (IOException e) {
            e.printStackTrace();
        }
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
    public static String getHttpsUploadUrl() {
        return httpsUploadUrl;
    }
    public static String getZipPath() {
        return zipPath;
    }
    public static String getBakFilePath() {
        return bakFilePath;
    }


    @Value("${wechat.corpid}")
    public  void setCorpid(String corpid) {
        BussinessConfig.corpid = corpid;
    }
    @Value("${wechat.secret}")
    public  void setSecret(String secret) {
        BussinessConfig.secret = secret;
    }

  //  @Value("${wechat.prikey}")
    public  void setPrikey(String prikey) {
        BussinessConfig.prikey = prikey;
    }

    @Value("${wechat.prikeyurl}")
    public  void setPrikeyURL(String prikeyURL) {
        BussinessConfig.prikeyURL = prikeyURL;
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
    @Value("${wechat.https.upload.url}")
    public void setHttpsUploadUrl(String url) {
        BussinessConfig.httpsUploadUrl = url;
    }
    @Value("${wechat.zip.path}")
    public void setZipPath(String zipPath) {
        BussinessConfig.zipPath = zipPath;
    }
    @Value("${wechat.bakfileroot}")
    public void setBakFilePath(String bakFilePath) {
        BussinessConfig.bakFilePath = bakFilePath;
    }
}
