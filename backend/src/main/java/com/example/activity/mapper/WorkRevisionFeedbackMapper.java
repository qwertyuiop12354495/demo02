package com.example.activity.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.activity.entity.WorkRevisionFeedback;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

public interface WorkRevisionFeedbackMapper extends BaseMapper<WorkRevisionFeedback> {

    @Select("SELECT COALESCE(MAX(round_no), 0) FROM work_revision_feedback "
            + "WHERE work_id = #{workId} AND review_step = #{reviewStep}")
    int selectMaxRoundNo(@Param("workId") Long workId, @Param("reviewStep") String reviewStep);
}
