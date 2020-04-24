package com.hsbc.wechat.tempalte;

import com.alibaba.fastjson.JSONObject;
import com.hsbc.wechat.bean.wechat.ChatData;
import com.hsbc.wechat.bean.wechat.ChatInfo;
import com.hsbc.wechat.bean.wechat.ContentInfo;
import com.hsbc.wechat.config.BussinessConfig;
import com.hsbc.wechat.util.AESKeyUtil;
import com.hsbc.wechat.util.WxLogUtil;
import com.hsbc.wechat.util.FileUtil;
import com.tencent.wework.Finance;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;


import javax.crypto.Cipher;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.security.PrivateKey;
import java.text.SimpleDateFormat;
import java.util.Base64;
import java.util.Date;
import java.util.List;


/**
 * @author like
 * WeChat API Util
 */
@Slf4j
public class WeChatAPITemplate extends Finance{

    private  long sdk = 0;

    //seq 默认为 0
    private long seq = 0;
    //每次获取的数量，最大值为1000
    @Value("${wechat.data.limit}")
    private int limit=100;
    //初始化默认超长时间为60s
    private long  timeout  = 15;
    //初始化下载媒体超长时间
    private long  mediaTimeOut = 30;

    private String corpid = BussinessConfig.getCorpid();

    private String secret =BussinessConfig.getSecret();

    private String priKey = BussinessConfig.getPrikey();

    private String proxy = "";
    private String paswd = "";

    private String mediaPath = BussinessConfig.getMediapath();

    private String downloadPath = BussinessConfig.getDownloadpath();


    /**
     *
     * @param limit 每次获取的数量，最大值为1000
     * @param timeout 超时设置
     */
    public  WeChatAPITemplate(int limit,long  timeout){
        this.timeout = timeout<=0?this.timeout:timeout;
        this.limit = limit;
        init();
    }

    /**
     * 每次获取的数量，最大值为1000
     * 默认超时15s
     */
    public  WeChatAPITemplate(){
        init();
    }

    /**
     * 初始化
     * @return true:初始化成功
     *         false：初始化失败
     */
    private boolean init(){
        sdk = NewSdk();
        int status  = Init(sdk, corpid,secret);
        return status == 0 ? true:false;
    }

    /**
     * 注：获取会话记录内容不能超过3天
     * @param seq 本次请求获取消息记录开始的seq值。默认从0开始，非首次使用上次企业微信返回的最大seq。允许从任意seq重入拉取。Uint64类型，范围0-pow(2,64)-1
     */
    public void getChatData(long seq){
        log.info("Start get ChatDate  seq:{}",seq);
        long startTimeMillis = System.currentTimeMillis();
        seq = Math.max(seq, 0);
        ChatInfo chatInfo = null;
        //返回本次拉取消息的数据.密文消息
        long slice = NewSlice();
        int ret = GetChatData(sdk, seq, limit, proxy, paswd, timeout, slice);
        if (ret != 0) {
            // log.error("getchatdata ret :()",ret);
            RuntimeException e = new RuntimeException("get WeChat Content Error: "+ret);
            //记录微信请求日志
            WxLogUtil.writeLog(e, "下载聊天记录", startTimeMillis, seq);
            throw new RuntimeException(e);
        }
        String data = GetContentFromSlice(slice);
        //记录微信请求日志
        WxLogUtil.writeLog(data, "下载聊天记录", startTimeMillis, seq);

        chatInfo = JSONObject.parseObject(data,ChatInfo.class);

        if(chatInfo.getChatData().size()<=0){
            DestroySdk();
            log.info("No ChatDate ! seq:{}",seq);
            return;
        }

        getAndWriteMaxSeq(chatInfo);

        doDecryptChatInfo(chatInfo);


    }
    private void getAndWriteMaxSeq(ChatInfo chatInfo){
        chatInfo.getChatData().forEach(chatData->{
            this.seq = Math.max(chatData.getSeq(),this.seq);
        });
        long localseq = getLocatSeq();
        this.seq = Math.max(localseq,this.seq);
        FileUtil.writeStringToFile(BussinessConfig.getSeqFilepPth(),this.seq+"");
    }
    /**
     * 解密聊天内容
     * @param chatInfo
     */
    private void doDecryptChatInfo(ChatInfo chatInfo) {
        List<ChatData> chatDatas = chatInfo.getChatData();
        chatDatas.forEach(chatData -> {
            try {
                //重新解密
                PrivateKey privateKey = AESKeyUtil.getPrivateKey(priKey);
                //RSA/ECB/PKCS1Padding
                Cipher cipher = Cipher.getInstance("RSA");
                cipher.init(Cipher.DECRYPT_MODE, privateKey);

                byte[] randomkeybyte = Base64.getDecoder().decode(chatData.getEncrypt_random_key());
                byte[] finalrandomkeybyte = cipher.doFinal(randomkeybyte);
                String finalrandomkey = new String(finalrandomkeybyte);

//                byte[] keyBytes = Base64.getDecoder().decode(priKey);
//                PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(keyBytes);
//                KeyFactory keyFactory = KeyFactory.getInstance("RSA");
//                PrivateKey privateKey = keyFactory.generatePrivate(keySpec);
//
//                byte[] contentbyte = Base64.getDecoder().decode(chatData.getEncrypt_random_key());
//                Cipher cipher = Cipher.getInstance("RSA");
//                cipher.init(Cipher.DECRYPT_MODE,privateKey);
//
//                byte[] decodeContent = cipher.doFinal(contentbyte);
//                String contentKey = new String(decodeContent);

                String encrypt_chat_msg = chatData.getEncrypt_chat_msg();
                ContentInfo contentInfo = DecryptData(finalrandomkey,encrypt_chat_msg);

                generateContentInfo(contentInfo,chatData.getSeq());


            } catch (Exception e) {
                e.printStackTrace();
            }
        });


    }

    /**
     * 解密
     * @param encrypt_key
     * @param encrypt_msg
     */
    private ContentInfo DecryptData(String encrypt_key, String encrypt_msg){
        long startTimeMillis = System.currentTimeMillis();
        long msg = NewSlice();
        int ret = DecryptData(sdk,encrypt_key,encrypt_msg,msg);
        if (ret != 0) {
            //  log.error("SDK 解密失败",ret);
            FreeSlice(msg);
            //记录微信请求日parseContentToLocal志
            RuntimeException e = new RuntimeException("SDK WeChat DecryptData Error: "+ret);
            WxLogUtil.writeLog(e, "聊天记录解密", startTimeMillis);
            throw e;
        }
        String data = GetContentFromSlice(msg);
        FreeSlice(msg);
        //记录微信请求日志
        WxLogUtil.writeLog(data, "聊天记录解密", startTimeMillis);
        return JSONObject.parseObject(data,ContentInfo.class);

    }


    public void DestroySdk(){
        DestroySdk(sdk);
    }

    /**
     * 处理具体聊天内容
     * @param contentInfo
     */
    private void generateContentInfo(ContentInfo contentInfo,Long seq) {
        parseMediaDataTocal(contentInfo,seq);

        parseContentToLocal(contentInfo,seq);

    }

    private void parseContentToLocal(ContentInfo contentInfo,long seq) {
        String basefilepath = BussinessConfig.getDownloadpath();
        String outputFilePath = "";
        String[] strNow = new SimpleDateFormat("yyyy-MM-dd").format(new Date()).toString().split("-");
        String year = strNow[0];
        String month = strNow[1];
        String day = strNow[2];
        outputFilePath = basefilepath + "/" + year + "/" + month + "/" + day + "/content/" + contentInfo.getMsgType() + "/" ;
        log.info("parseContentToLocal文件路径:{}",outputFilePath);
        FileWriter fileWriter = null;
        try {
            FileUtil.CreateDir(outputFilePath);
            File file = new File(outputFilePath+seq+".json");
            if(!file.exists()){file.createNewFile();}
            fileWriter =new FileWriter(file, true);
            fileWriter.write(JSONObject.toJSONString(contentInfo));
            fileWriter.flush();
        }catch ( Exception e){
            e.printStackTrace();
        }finally {
            if(fileWriter!=null){
                try {
                    fileWriter.close();
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        }

    }

    /**
     * 拉取媒体消息方法
     * @return
     */
    private String handleMediaData(String sdkField,String msgType,String fileExt,long seq){

        String basefilepath = BussinessConfig.getDownloadpath();
        String outputFilePath = "";
        String[] strNow = new SimpleDateFormat("yyyy-MM-dd").format(new Date()).toString().split("-");
        String year = strNow[0];
        String month = strNow[1];
        String day = strNow[2];
        //获取媒体文件生成路径
        if (StringUtils.isNotBlank(msgType)) {
            outputFilePath = basefilepath + "/" + year + "/" + month + "/" + day + "/media/" + msgType + "/" + seq + "."
                    + fileExt;
        } else {
            outputFilePath = basefilepath + "/" + year + "/" + month + "/" + day + "/media/" + msgType + "/" + seq;
        }

        log.info("handleMediaData文件路径:" + outputFilePath);
        File outputFile = new File(outputFilePath);

        //如果已经存在文件,清空原有文件以便重新追加
        FileUtil.clearFile(outputFile);

        long ret = 0;
        String indexbuf = "";
        long startTimeMillis = System.currentTimeMillis();
        while (true) {
            long media_data = Finance.NewMediaData();
            ret = GetMediaData(sdk, indexbuf, sdkField, null, null, mediaTimeOut, media_data);
            if (ret != 0) {
                return outputFilePath;
            }
            try {
                //需要持续追加媒体内容,设置append参数为true
                FileOutputStream outputStream = new FileOutputStream(outputFile, true);
                outputStream.write(Finance.GetData(media_data));
                outputStream.close();
            } catch (Exception e) {
                //记录微信请求日志
                WxLogUtil.writeLog(e, "下载媒体文件", startTimeMillis, sdkField);
                throw new RuntimeException("导出媒体文件失败:" + e.getMessage());
            }

            if (Finance.IsMediaDataFinish(media_data) == 1) {
                Finance.FreeMediaData(media_data);
                break;
            } else {
                indexbuf = Finance.GetOutIndexBuf(media_data);
                Finance.FreeMediaData(media_data);
            }
        }
        //记录微信请求日志
        WxLogUtil.writeLog(outputFilePath, "下载媒体文件", startTimeMillis, sdkField);
        return outputFilePath;

    }

    /**
     *
     * @param contentInfo 聊天内容
     */
    private  void parseMediaDataTocal(ContentInfo contentInfo,Long seq){
        String msgType = "";
        String mediaPath = "";

        if (StringUtils.isNotBlank(contentInfo.getMsgType())) {
            msgType = contentInfo.getMsgType();
        }

        if ("text".equals(msgType)) {
        } else if ("agree".equals(msgType)) {

        } else if ("card".equals(msgType)) {

        } else if ("emotion".equals(msgType)) {

            mediaPath = handleMediaData(contentInfo.getEmotion().getSdkfileid(),msgType,
                    contentInfo.getEmotion().getType() == 1 ? "gif" : "png",seq);
            contentInfo.getEmotion().setUrl(mediaPath);

        } else if ("file".equals(msgType)) {

            mediaPath = handleMediaData(contentInfo.getFile().getSdkfileid(),msgType,
                    contentInfo.getFile().getFileext(),seq);
            contentInfo.getFile().setUrl(mediaPath);
        } else if ("image".equals(msgType)) {

            mediaPath = handleMediaData(contentInfo.getImage().getSdkfileid(),msgType,
                    "jpg",seq);
            contentInfo.getImage().setUrl(mediaPath);

        } else if ("link".equals(msgType)) {

        } else if ("location".equals(msgType)) {

        } else if ("revoke".equals(msgType)) {

        } else if ("video".equals(msgType)) {

            mediaPath = handleMediaData(contentInfo.getVideo().getSdkfileid(),msgType,
                    null,seq);
            contentInfo.getVideo().setUrl(mediaPath);

        } else if ("voice".equals(msgType)) {

            mediaPath = handleMediaData(contentInfo.getVoice().getSdkfileid(),msgType,
                    null,seq);
            contentInfo.getVoice().setUrl(mediaPath);
        } else if ("weapp".equals(msgType)) {

        } else {
            log.info("不支持的消息类型msgType={}" ,msgType);
        }

    }
    private long getLocatSeq(){
        String str =  FileUtil.readStringFromFile(BussinessConfig.getSeqFilepPth());
        if(str==null || "".equals(str)){
            str = "0";
        }
        return Long.decode(str);
    }

    public boolean hasNextBySeq(long seq){
        long startTimeMillis = System.currentTimeMillis();
        seq = Math.max(seq, 0);
        ChatInfo chatInfo = null;
        //返回本次拉取消息的数据.密文消息
        long slice = NewSlice();
        int ret = GetChatData(sdk, seq, limit, proxy, paswd, timeout, slice);

        String data = GetContentFromSlice(slice);

        chatInfo = JSONObject.parseObject(data,ChatInfo.class);

        if(chatInfo.getChatData().size()<=0){
            DestroySdk();
            log.info("No ChatDate ! seq:{}",seq);
            return false;
        }
        return true;
    }

}