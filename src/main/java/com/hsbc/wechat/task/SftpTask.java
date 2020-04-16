package com.hsbc.wechat.task;

import com.hsbc.wechat.service.SftpService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.File;

@Component
public class SftpTask {

    //本地文件根目录，存放聊天记录、从微信下载的媒体文件等
    @Value("${wechat.fileroot}")
    private String localRootFilePath;

    @Autowired
    private SftpService sftpService;

    //@Scheduled(cron = "${wechat.cornsftp}")
    @Scheduled(cron = "0 * * * * ?")
    public void upload() {
        System.out.println("start task");
        String subPath = "2020";
        String localPath = (localRootFilePath + "/" + subPath).replace("//", "/");
        sftpService.uploadPath(new File(localPath), true);
        System.out.println("end task");
    }

}
