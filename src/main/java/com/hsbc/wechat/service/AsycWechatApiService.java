package com.hsbc.wechat.service;

import org.springframework.scheduling.annotation.AsyncResult;

public interface AsycWechatApiService {
    void get(long seq,String strNow);
}
