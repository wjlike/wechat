package com.hsbc.wechat.service.impl;

import com.hsbc.wechat.service.AsycWechatApiService;
import com.hsbc.wechat.tempalte.WeChatAPITemplate;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class AsycWechatApiServiceImpl implements AsycWechatApiService {
    @Override
    @Async
    public void get(long seq) {
        try {
            WeChatAPITemplate template = new WeChatAPITemplate();
            template.getChatData(seq);
            template.DestroySdk();
        }catch (Exception e){
            log.error("GET CHAT DATA SERVICE ERROR:{}"+e.getMessage());
        }
    }
}
