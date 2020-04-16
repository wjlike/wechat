package com.hsbc.wechat.service.impl;

import com.hsbc.wechat.service.AsycWechatApiService;
import com.hsbc.wechat.service.WechatApiService;
import com.hsbc.wechat.tempalte.WeChatAPITemplate;
import com.hsbc.wechat.util.ThreadLocalUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class WechatApiServiceImpl implements WechatApiService {

    @Autowired
    AsycWechatApiService asycWechatApiService;
    private final int QUERY_LIMIST = 10;

    @Value("${wechat.data.limit}")
    private long chatdata_limit;
    @Override
    public void get(long seq) {
        for (int i = 1; i <= QUERY_LIMIST ; i++) {
            asycWechatApiService.get(seq+(QUERY_LIMIST-1)*chatdata_limit);
        }
    }
}
