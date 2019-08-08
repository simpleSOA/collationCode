package com.springboot.core.excel;

import cn.afterturn.easypoi.excel.annotation.Excel;

import java.io.Serializable;

public class ExcelModel implements Serializable {
    public ExcelModel() {
    }

    public ExcelModel(String name, String username, String phoneNumber) {
        this.name = name;
        this.username = username;
        this.phoneNumber = phoneNumber;
    }

    @Excel(name = "姓名", width = 15)
    private String name;
    /**
     * 登录用户名
     */
    @Excel(name = "用户名", orderNum = "1", width = 15)
    private String username;

    @Excel(name = "手机号码", orderNum = "2", width = 15)
    private String phoneNumber;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("ExcelModel{");
        sb.append("name='").append(name).append('\'');
        sb.append(", username='").append(username).append('\'');
        sb.append(", phoneNumber='").append(phoneNumber).append('\'');
        sb.append('}');
        return sb.toString();
    }
}
