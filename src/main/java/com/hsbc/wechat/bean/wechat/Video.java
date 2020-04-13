package com.hsbc.wechat.bean.wechat;

import lombok.Data;

import java.io.Serializable;

/**
 * 
 * 语音消息bean
 * @author like
 * @date : 2020年4月13日10:19:42
 */
@Data
public class Video implements Serializable {

    private static final long serialVersionUID = -6420174414349845713L;

    /**
     * 媒体资源的id信息
     */
    private String sdkfileid;

    /**
     * 资源的md5值，供进行校验
     */
    private String md5sum;

    /**
     * 资源的文件大小
     */
    private Long filesize;

    /**
     * 视频播放长度
     */
    private Long play_length;


    /**
     * local url
     */
    private String url;
}
