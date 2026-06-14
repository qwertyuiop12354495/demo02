package com.example.activity.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.example.activity.common.scope.ScopedData;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("sys_user")
public class SysUser implements ScopedData {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String username;

    private String password;

    private String nickname;

    private String role;

    private String provinceName;

    private String cityName;

    private String districtName;

    private String schoolName;

    private Integer status;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
