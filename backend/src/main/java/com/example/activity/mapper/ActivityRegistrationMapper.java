package com.example.activity.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.activity.entity.ActivityRegistration;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

public interface ActivityRegistrationMapper extends BaseMapper<ActivityRegistration> {

    @Select("SELECT * FROM activity_registration WHERE id = #{id} FOR UPDATE")
    ActivityRegistration selectByIdForUpdate(@Param("id") Long id);
}
