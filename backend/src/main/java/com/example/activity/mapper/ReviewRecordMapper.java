package com.example.activity.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.activity.entity.ReviewRecord;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.math.BigDecimal;
import java.util.List;

public interface ReviewRecordMapper extends BaseMapper<ReviewRecord> {

    @Select("SELECT COUNT(*) FROM review_record "
            + "WHERE work_id = #{workId} AND review_level = #{reviewLevel} AND final_score IS NOT NULL")
    int countScoredByWorkAndLevel(@Param("workId") Long workId, @Param("reviewLevel") String reviewLevel);

    @Select("SELECT final_score FROM review_record "
            + "WHERE work_id = #{workId} AND review_level = #{reviewLevel} AND final_score IS NOT NULL")
    List<BigDecimal> listFinalScoresByWorkAndLevel(@Param("workId") Long workId,
                                                   @Param("reviewLevel") String reviewLevel);

    @Select("SELECT COUNT(*) FROM review_record "
            + "WHERE work_id = #{workId} AND review_level = #{reviewLevel} AND reviewer_id = #{reviewerId}")
    int countByWorkLevelAndReviewer(@Param("workId") Long workId,
                                    @Param("reviewLevel") String reviewLevel,
                                    @Param("reviewerId") Long reviewerId);

    @org.apache.ibatis.annotations.Update("UPDATE review_record SET result = #{result} "
            + "WHERE work_id = #{workId} AND review_level = #{reviewLevel} AND final_score IS NOT NULL")
    int updateResultByWorkAndLevel(@Param("workId") Long workId,
                                   @Param("reviewLevel") String reviewLevel,
                                   @Param("result") String result);
}
