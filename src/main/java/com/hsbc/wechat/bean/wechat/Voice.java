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
public class Voice implements Serializable {

    private static final long serialVersionUID = 7324643844902425243L;

    /**
     * 语音消息大小
     */
    private Long voice_size;

    /**
     * 播放长度
     */
    private Long play_length;

    /**
     * 媒体资源的id信息
     */
    private String sdkfileid;

    /**
     * 图片资源的md5值，供进行校验
     */
    private String md5sum;


    /**
     * local url
     */
    private String url;

}
