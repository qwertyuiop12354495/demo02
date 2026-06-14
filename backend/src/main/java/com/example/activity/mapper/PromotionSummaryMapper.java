package com.example.activity.mapper;

import com.example.activity.mapper.dto.PromotionSummaryRow;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface PromotionSummaryMapper {

    @Select("<script>"
            + "SELECT w.id AS work_id, w.title AS work_title, w.school_name, w.district_name, "
            + "w.city_name, w.province_name, AVG(rr.final_score) AS average_score, "
            + "MAX(rr.created_at) AS published_at "
            + "FROM review_record rr "
            + "INNER JOIN work w ON w.id = rr.work_id AND w.deleted = 0 "
            + "WHERE rr.review_level = #{reviewLevel} AND rr.result = #{result} "
            + "AND rr.final_score IS NOT NULL "
            + "<if test='activityId != null'> AND w.activity_id = #{activityId} </if>"
            + "<if test='provinceName != null'> AND w.province_name = #{provinceName} </if>"
            + "<if test='cityName != null'> AND w.city_name = #{cityName} </if>"
            + "<if test='districtName != null'> AND w.district_name = #{districtName} </if>"
            + "<if test='schoolName != null'> AND w.school_name = #{schoolName} </if>"
            + "GROUP BY w.id, w.title, w.school_name, w.district_name, w.city_name, w.province_name "
            + "ORDER BY published_at DESC "
            + "LIMIT #{offset}, #{pageSize}"
            + "</script>")
    List<PromotionSummaryRow> selectPromotionSummary(
            @Param("reviewLevel") String reviewLevel,
            @Param("result") String result,
            @Param("activityId") Long activityId,
            @Param("provinceName") String provinceName,
            @Param("cityName") String cityName,
            @Param("districtName") String districtName,
            @Param("schoolName") String schoolName,
            @Param("offset") long offset,
            @Param("pageSize") long pageSize);

    @Select("<script>"
            + "SELECT COUNT(DISTINCT w.id) "
            + "FROM review_record rr "
            + "INNER JOIN work w ON w.id = rr.work_id AND w.deleted = 0 "
            + "WHERE rr.review_level = #{reviewLevel} AND rr.result = #{result} "
            + "AND rr.final_score IS NOT NULL "
            + "<if test='activityId != null'> AND w.activity_id = #{activityId} </if>"
            + "<if test='provinceName != null'> AND w.province_name = #{provinceName} </if>"
            + "<if test='cityName != null'> AND w.city_name = #{cityName} </if>"
            + "<if test='districtName != null'> AND w.district_name = #{districtName} </if>"
            + "<if test='schoolName != null'> AND w.school_name = #{schoolName} </if>"
            + "</script>")
    long countPromotionSummary(
            @Param("reviewLevel") String reviewLevel,
            @Param("result") String result,
            @Param("activityId") Long activityId,
            @Param("provinceName") String provinceName,
            @Param("cityName") String cityName,
            @Param("districtName") String districtName,
            @Param("schoolName") String schoolName);
}
