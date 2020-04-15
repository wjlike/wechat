package com.hsbc.wechat.tempalte;

import com.alibaba.fastjson.JSONObject;
import com.hsbc.wechat.bean.wechat.ChatData;
import com.hsbc.wechat.bean.wechat.ChatInfo;
import com.hsbc.wechat.bean.wechat.ContentInfo;
import com.hsbc.wechat.config.BussinessConfig;
import com.hsbc.wechat.util.FileUtil;
import com.hsbc.wechat.util.ThreadLocalUtil;
import com.tencent.wework.Finance;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import javax.crypto.Cipher;
import java.io.File;
import java.io.FileOutputStream;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.text.SimpleDateFormat;
import java.util.Base64;
import java.util.Date;
import java.util.List;

@Slf4j
public class WeChatMediaAPI extends WeChatAPITemplateAbstract implements Runnable{

    private ChatInfo chatInfo;

    public void setChatInfo(ChatInfo chatInfo) {
        this.chatInfo = chatInfo;
    }

    @Override
    public void run() {
        doDecryptChatInfo(chatInfo);

    }
    /**
     * 拉取媒体消息方法
     * @return
     */
    public String handleMediaData(String sdkField,String msgType,String fileExt,long  seq){

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

        log.info("文件路径:" + outputFilePath);
        File outputFile = new File(outputFilePath);

        //如果已经存在文件,清空原有文件以便重新追加
        FileUtil.clearFile(outputFile);

        long ret = 0;
        String indexbuf = "";
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
        return outputFilePath;

    }

    /**
     *
     * @param contentInfo 聊天内容
     */
    private  void parseMediaDataTocal(ContentInfo contentInfo, Long seq){
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
    private void parseContentToLocal(ContentInfo contentInfo, long seq) {
        String basefilepath = BussinessConfig.getDownloadpath();
        String outputFilePath = "";
        String[] strNow = new SimpleDateFormat("yyyy-MM-dd").format(new Date()).toString().split("-");
        String year = strNow[0];
        String month = strNow[1];
        String day = strNow[2];
        outputFilePath = basefilepath + "/" + year + "/" + month + "/" + day + "/content/" + contentInfo.getMsgType() + "/" + seq;
        FileOutputStream outputStream = null;
        try {
            File file = new File(outputFilePath);
            if (!file.exists()) {
                file.mkdirs();
            }
            outputStream = new FileOutputStream(file);
            outputStream.write(JSONObject.toJSONString(contentInfo).getBytes());
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (outputStream != null) {
                try {
                    outputStream.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

    }

    /**
     * 处理具体聊天内容
     * @param contentInfo
     */
    private void generateContentInfo(ContentInfo contentInfo,Long seq) {
        parseMediaDataTocal(contentInfo,seq);

        parseContentToLocal(contentInfo,seq);

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
                PrivateKey privateKey = keyFactory.generatePrivate(keySpec);

                byte[] contentbyte = Base64.getDecoder().decode(chatData.getEncrypt_random_key());
                Cipher cipher = Cipher.getInstance("RSA");
                cipher.init(Cipher.DECRYPT_MODE,privateKey);

                byte[] decodeContent = cipher.doFinal(contentbyte);
                String contentKey = new String(decodeContent);

                String encrypt_chat_msg = chatData.getEncrypt_chat_msg();
                ContentInfo contentInfo = DecryptData(contentKey,encrypt_chat_msg);

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

}
