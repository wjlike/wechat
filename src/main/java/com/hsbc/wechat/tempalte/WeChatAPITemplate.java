package com.hsbc.wechat.tempalte;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.JSONPObject;
import com.hsbc.wechat.bean.wechat.ChatData;
import com.hsbc.wechat.bean.wechat.ChatInfo;
import com.hsbc.wechat.bean.wechat.ContentInfo;
import com.hsbc.wechat.config.BussinessConfig;
import com.tencent.wework.Finance;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;

import javax.crypto.Cipher;
import javax.crypto.NoSuchPaddingException;
import java.io.File;
import java.io.FileOutputStream;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Base64;
import java.util.List;


/**
 * @author like
 * WeChat API Util
 */
public class WeChatAPITemplate extends Finance{

    private  long sdk = 0;

    //seq 默认为 0
    private int seq = 0;
    //每次获取的数量，最大值为1000
    private int limit=1000;
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
    public void getChatData(int seq){
        seq = seq<=0?0:seq;

        ChatInfo chatInfo = null;
        //返回本次拉取消息的数据.密文消息
        long slice = NewSlice();
        int ret = GetChatData(sdk, seq, limit, proxy, paswd, timeout, slice);
        if (ret != 0) {
            // log.error("getchatdata ret :()",ret);
            throw new RuntimeException("get WeChat Content Error: "+ret);
        }
        String data = GetContentFromSlice(slice);

        chatInfo = JSONObject.parseObject(data,ChatInfo.class);

        doDecryptChatInfo(chatInfo);


    }

    /**
     * 解密聊天内容
     * @param chatInfo
     */
    private void doDecryptChatInfo(ChatInfo chatInfo) {
        List<ChatData> chatDatas = chatInfo.getChatData();
        chatDatas.forEach(chatData -> {

            try {
                byte[] decode = Base64.getDecoder().decode(priKey);
                PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(decode);
                KeyFactory keyFactory = KeyFactory.getInstance("RSA");
                PrivateKey privateKey = KeyFactory.generateprivate(keySpec);

                byte[] contentbyte = Base64.getDecoder().decode(chatData.getEncrypt_random_key());
                Cipher cipher = Cipher.getInstance("RSA");
                cipher.init(Cipher.DECRYPT_MODE,privateKey);

                byte[] decodeContent = cipher.doFinal(contentbyte);
                String contentKey = new String(decodeContent);

                String encrypt_chat_msg = chatData.getEncrypt_chat_msg();
                ContentInfo contentInfo = DecryptData(contentKey,encrypt_chat_msg);

                generateContentInfo(contentInfo);


            } catch (Exception e) {
                e.printStackTrace();
            }
        });


    }

    /**
     * 处理具体聊天内容
     * @param contentInfo
     */
    private void generateContentInfo(ContentInfo contentInfo) {
    }

    /**
     * 拉取媒体信息
     */
    private void handleMediaData(){

    }

    /**
     * 解密
     * @param encrypt_key
     * @param encrypt_msg
     */
    public ContentInfo DecryptData(String encrypt_key, String encrypt_msg){
        long msg = 0;
        int ret = DecryptData(sdk,encrypt_key,encrypt_msg,msg);
        if (ret != 0) {
            //  log.error("SDK 解密失败",ret);
            FreeSlice(msg);
            throw new RuntimeException("SDK WeChat DecryptData Error: "+ret);
        }
        String data = GetContentFromSlice(msg);
        FreeSlice(msg);
        return JSONObject.parseObject(data,ContentInfo.class);

    }

    /**
     * 拉取媒体消息方法
     * @param indexbuf 媒体消息分片拉取，需要填入每次拉取的索引信息。首次不需要填写，默认拉取512k，后续每次调用只需要将上次调用返回的outindexbuf填入即可。
     * @param sdkField
     * @return
     */
    public String GetMediaData(String indexbuf, String sdkField){
        File f = null;
        FileOutputStream outputStream = null;
        long media_data = NewMediaData();
        int ret = GetMediaData(sdk, indexbuf, sdkField, proxy, paswd, mediaTimeOut, media_data);
        if (ret != 0) {
            //  log.error("SDK WeChat DecryptData Error: ",ret);
            FreeMediaData(media_data);
            throw new RuntimeException("SDK WeChat GetMediaData Error: "+ret);
        }
        try {
            f = new File(mediaPath);
            outputStream  =new FileOutputStream(f);
            outputStream.write(GetData(media_data));
            outputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        //IsMediaDataFinish = 1 拉取完成
        if(Finance.IsMediaDataFinish(media_data) == 1)
        {
            FreeMediaData(media_data);
        }
        else
        {
            indexbuf = Finance.GetOutIndexBuf(media_data);
            Finance.FreeMediaData(media_data);
            GetMediaData(indexbuf,sdkField);
        }
        return f.getPath();
    }

    public void DestroySdk(){
        DestroySdk(sdk);
    }

    /**
     *
     * @param chatObj 根据seq获取得到的聊天内容
     */
    public  void parseMediaDataTocal(JSONObject chatObj){
        List<JSONObject> msgList = (List)chatObj.get("chatdata");
        msgList.forEach(msg->{
        });

    }


}
