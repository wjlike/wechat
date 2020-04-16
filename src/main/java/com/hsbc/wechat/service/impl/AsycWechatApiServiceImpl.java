package com.hsbc.wechat.service.impl;

import com.hsbc.wechat.service.AsycWechatApiService;
import com.hsbc.wechat.tempalte.WeChatAPITemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class AsycWechatApiServiceImpl implements AsycWechatApiService {
    @Override
    @Async
    public void get(long seq) {
        WeChatAPITemplate template = new WeChatAPITemplate();
        template.getChatData(seq);
        template.DestroySdk();
    }
}
