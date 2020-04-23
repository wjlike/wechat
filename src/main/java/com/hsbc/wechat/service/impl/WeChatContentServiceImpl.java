package com.hsbc.wechat.service.impl;

import com.hsbc.wechat.config.BussinessConfig;
import com.hsbc.wechat.service.WeChatContentService;
import com.hsbc.wechat.service.WechatApiService;
import com.hsbc.wechat.tempalte.WeChatAPITemplate;
import com.hsbc.wechat.util.FileUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


@Slf4j
@Service
public class WeChatContentServiceImpl implements WeChatContentService {
    @Autowired
    WechatApiService wechatApiService;
    @Override
    public void doWeChatContent() {

        while (true){
            try {
                long seq = getLocatSeq();
                wechatApiService.get(seq);
                //一小时后查看是否还存在数据
                Thread.sleep(10000);
                boolean hasNext = wechatApiService.hasNext(getLocatSeq());
                if(!hasNext){
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
    public void test() {
        long seq = getLocatSeq();
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
