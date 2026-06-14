package com.example.activity.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.activity.entity.Activity;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

public interface ActivityMapper extends BaseMapper<Activity> {

    /**
     * 行级锁：并发报名同一活动时串行等待。
     */
    @Select("SELECT * FROM activity WHERE id = #{id} FOR UPDATE")
    Activity selectByIdForUpdate(@Param("id") Long id);

    /**
     * 原子预占名额：仅当已上架且未满员时 current_count + 1。
     *
     * @return 受影响行数，0 表示名额已满或活动不可报名
     */
    @Update("UPDATE activity SET current_count = current_count + 1, updated_at = NOW(3) "
            + "WHERE id = #{id} AND status = 'PUBLISHED' AND current_count < max_count")
    int reserveQuota(@Param("id") Long id);

    /**
     * 原子释放名额：current_count - 1，防止减到负数。
     *
     * @return 受影响行数，0 表示数据异常
     */
    @Update("UPDATE activity SET current_count = current_count - 1, updated_at = NOW(3) "
            + "WHERE id = #{id} AND current_count > 0")
    int releaseQuota(@Param("id") Long id);
}
