package com.hsbc.wechat.bean.wechat;

import lombok.Data;

import java.io.Serializable;

/**
 * 
 * 地址位置信息bean
 * @author like
 * @date : 2020年4月13日10:19:42
 */
@Data
public class Location implements Serializable {

    private static final long serialVersionUID = -236218480074059145L;

    /**
     * 经度
     */
    private Double longitude;

    /**
     * 纬度
     */
    private Double latitude;

    /**
     * 不同公司是openid
     */
    private String address;

    /**
     * 位置信息
     */
    private String title;

    /**
     * 缩放比例
     */
    private Long zoom;
}
