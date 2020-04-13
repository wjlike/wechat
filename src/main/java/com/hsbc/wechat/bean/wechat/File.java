package com.hsbc.wechat.bean.wechat;

import lombok.Data;

import java.io.Serializable;

/**
 * 
 * 文件bean
 * @author like
 * @date : 2020年4月13日10:19:42
 */
@Data
public class File implements Serializable {

    private static final long serialVersionUID = -2135900779871468796L;

    /**
     * 媒体资源的id信息
     */
    private String sdkfileid;

    /**
     * 资源的md5值
     */
    private String md5sum;

    /**
     * 文件名称
     */
    private String filename;

    /**
     * 文件类型后缀
     */
    private String fileext;

    /**
     * 文件大小
     */
    private String filesize;


    /**
     * local url
     */
    private String url;

}
