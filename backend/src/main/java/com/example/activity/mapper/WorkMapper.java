package com.example.activity.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.activity.entity.Work;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

public interface WorkMapper extends BaseMapper<Work> {

    @Select("SELECT * FROM work WHERE id = #{id} AND deleted = 0 FOR UPDATE")
    Work selectByIdForUpdate(@Param("id") Long id);
}
