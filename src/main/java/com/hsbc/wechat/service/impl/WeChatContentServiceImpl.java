package com.hsbc.wechat.service.impl;

import com.hsbc.wechat.config.BussinessConfig;
import com.hsbc.wechat.service.SftpService;
import com.hsbc.wechat.service.WeChatContentService;
import com.hsbc.wechat.service.WechatApiService;
import com.hsbc.wechat.tempalte.WeChatAPITemplate;
import com.hsbc.wechat.util.FileUtil;
import com.hsbc.wechat.util.SftpFileUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;


@Slf4j
@Service
public class WeChatContentServiceImpl implements WeChatContentService {
    @Autowired
    WechatApiService wechatApiService;
    @Autowired
    SftpService sftpService;

    private static String basefilepath= BussinessConfig.getDownloadpath();
    private static String separator = File.separator;

    @Override
    public void doWeChatContent() {

        while (true){
            try {
                long seq = getLocatSeq();
                wechatApiService.get(seq);
                //五分钟后查看是否还存在数据
                Thread.sleep(1000*60*5);
                boolean hasNext = wechatApiService.hasNext(getLocatSeq());
                if(!hasNext){
                    String[] strNow = new SimpleDateFormat("yyyy-MM-dd").format(new Date()).toString().split("-");
                    String year = strNow[0];
                    String month = strNow[1];
                    String day = strNow[2];
                    String path = basefilepath + separator + year + separator + month + separator + day + separator ;
                    File file = new File("");
                    sftpService.uploadPath(file,true);
                    break;
                }

            }catch (Exception e){
                e.printStackTrace();
                log.error("WeChatContentServiceImpl doWeChatContent error:{}",e.getMessage());
                break;
            }

        }

    }

    @Override
    public void test(long seq) {
       // long seq = getLocatSeq();
        WeChatAPITemplate template = new WeChatAPITemplate();
        template.getChatData(seq);
        template.DestroySdk();
    }

    /**
     * 读取本地seq
     * @return
     */
    private long getLocatSeq(){
       String str =  FileUtil.readStringFromFile(BussinessConfig.getSeqFilepPth());
       if(str==null || "".equals(str)){
           str = "0";
       }
       return Long.decode(str);
    }
}
