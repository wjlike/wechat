package com.hsbc.wechat.task;

import com.hsbc.wechat.service.WeChatContentService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class WeChatTask {
    @Autowired
    private WeChatContentService weChatContentService;
   // @Scheduled(cron = "${wechat.corn}")
    public void doWeChatContentTask(){
        log.info("START TO WECHAT TASK");
        try {
            weChatContentService.doWeChatContent();

            log.info("WECHAT TASK  END");
        }catch (Exception e){
            e.printStackTrace();
            log.error("WECHAT TASK ERRPT:{}",e.getMessage());
        }

    }
}
