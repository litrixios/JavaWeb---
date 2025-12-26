package com.bjfu.cms.entity;

import lombok.Data;
import java.util.Date;

@Data
public class User {
    // 对应 UserID (INT IDENTITY)
    private Integer userId;

    // 对应 Username
    private String username;

    // 对应 Password
    private String password;

    // 对应 Role
    private String role;

    private String email;
    private String fullName;
    private String affiliation;
    private String researchDirection;

    // 对应 RegisterTime (DATETIME)
    private Date registerTime;

    // 对应 Status (INT)
    private Integer status;
    private String avatarUrl;
    // 🔥 扩展字段：为了方便业务，把权限对象直接放这里，虽然数据库是分表的
    private UserPermission permissions;

    private Integer activeTasks;

    public Integer getActiveTasks() {
        return activeTasks;
    }

    public void setActiveTasks(Integer activeTasks) {
        this.activeTasks = activeTasks;
    }

    public void setAvatarUrl(String avatarUrl) {this.avatarUrl = avatarUrl;}
    private Double recommendScore;
}