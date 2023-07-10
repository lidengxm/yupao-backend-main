package com.lmeng.yupao.model.enums;

/**
 * @version 1.0
 * @learner Lmeng
 * @date 2023/7/9
 */
public enum TeamStatusEnum {

    PUBLIC(0,"公开"),
    PRIVATE(1,"私有"),
    SECRET(2,"加密");

    public static TeamStatusEnum getEnumByValue(Integer value){
        if (value == null){
            return null;
        }
        //获取TeamStatusEnum的所有枚举数组
        TeamStatusEnum[] values = TeamStatusEnum.values();
        //遍历数组中的每个枚举值teamStatusEnum
        for (TeamStatusEnum teamStatusEnum: values){
            if (teamStatusEnum.getValue()==value) {
                return teamStatusEnum;
            }
        }
        return null;
    }

    public int getValue() {
        return value;
    }

    public void setValue(int value) {
        this.value = value;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    private int value;
    private String text;

    TeamStatusEnum(int value, String text) {
        this.value = value;
        this.text = text;
    }
}
