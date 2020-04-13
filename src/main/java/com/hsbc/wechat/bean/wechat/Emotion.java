package com.hsbc.wechat.bean.wechat;

import lombok.Data;

import java.io.Serializable;

/**
 * 
 * 表情信息bean
 * @author like
 * @date : 2020年4月13日10:19:42
 */
@Data
public class Emotion implements Serializable {

    private static final long serialVersionUID = -236218480074059145L;

    /**
     * 表情类型，png或者gif 1表示gif 2表示png
     */
    private Long type;

    /**
     * 表情图片宽度
     */
    private Long width;

    /**
     * 表情图片高度
     */
    private Long height;

    /**
     * 媒体资源的id信息
     */
    private String sdkfileid;

    /**
     * 资源的md5值
     */
    private String md5sum;

    /**
     * 资源的文件大小
     */
    private Long imagesize;

}
