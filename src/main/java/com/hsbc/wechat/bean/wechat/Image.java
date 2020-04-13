package com.hsbc.wechat.bean.wechat;

import lombok.Data;

import java.io.Serializable;

/**
 * 
 * 图片信息Bean
 * @author like
 * @date : 2020年4月13日10:19:42
 */
@Data
public class Image implements Serializable {

    private static final long serialVersionUID = -8484226026138823187L;

    /**
     * 媒体资源的id信息
     */
    private String sdkfileid;

    /**
     * 图片资源的md5值，供进行校验
     */
    private String md5sum;

    /**
     * 图片资源的文件大小
     */
    private String filesize;

}
