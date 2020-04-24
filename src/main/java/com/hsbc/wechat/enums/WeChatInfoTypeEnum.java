package com.hsbc.wechat.enums;

/**
 * 聊天内容类型枚举
 */
public enum WeChatInfoTypeEnum {
    TEXT("text"),
    AGREE("agree"),
    CARD("card"),
    EMOTION("emotion"),
    FILE("file"),
    IMAGE("image"),
    LINK("link"),
    LOCATION("location"),
    REVOKE("revoke"),
    VIDEO("video"),
    VOICE("voice"),
    WEAPP("weapp");

    private String value;

    WeChatInfoTypeEnum(String value){
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
