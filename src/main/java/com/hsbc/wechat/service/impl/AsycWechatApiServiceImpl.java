package com.hsbc.wechat.service.impl;

import com.hsbc.wechat.service.AsycWechatApiService;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class AsycWechatApiServiceImpl implements AsycWechatApiService {
    @Override
    @Async
    public long get(long seq) {
        return 0;
    }
}
