package com.hsbc.wechat.task;

import com.hsbc.wechat.util.HttpUtil;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class HttpTask {

    @Scheduled(cron = "0 * * * * ?")
    public void run() {
        HttpUtil.zipAndUpload("D:\\tmp\\test\\wechat\\2020");
    }
}
