package com.hsbc.wechat.bean;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;

@Data
public class SftpLog {

    private String path;

    private boolean successFlag;

    private long fileSize;

    private long costTime;

    //本地文件根目录，存放聊天记录、从微信下载的媒体文件等
    @Value("${wechat.fileroot}")
    private String localRootFilePath;

    //本地备份文件的根目录，上传成功后的文件，迁移到备份目录
    @Value("${wechat.bakfileroot}")
    private String localBakRootFilePath;

}
