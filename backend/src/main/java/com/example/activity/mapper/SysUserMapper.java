package com.example.activity.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.activity.entity.SysUser;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

public interface SysUserMapper extends BaseMapper<SysUser> {

    @Select("<script>"
            + "SELECT COUNT(*) FROM sys_user WHERE role = #{role} AND status = 1 "
            + "<if test='provinceName != null'> AND province_name = #{provinceName}</if>"
            + "<if test='cityName != null'> AND city_name = #{cityName}</if>"
            + "<if test='districtName != null'> AND district_name = #{districtName}</if>"
            + "</script>")
    int countEnabledScorersByScope(@Param("role") String role,
                                   @Param("provinceName") String provinceName,
                                   @Param("cityName") String cityName,
                                   @Param("districtName") String districtName);
}
