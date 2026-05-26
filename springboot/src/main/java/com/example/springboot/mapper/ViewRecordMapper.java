package com.example.springboot.mapper;

import com.example.springboot.entity.ViewRecord;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

public interface ViewRecordMapper {

    @Insert("INSERT IGNORE INTO view_record (user_id, video_id) VALUES (#{userId}, #{videoId})")
    void insertIgnore(@Param("userId") String userId, @Param("videoId") String videoId);

    @Select("SELECT COUNT(*) FROM view_record WHERE user_id = #{userId} AND video_id = #{videoId}")
    int countByUserAndVideo(@Param("userId") String userId, @Param("videoId") String videoId);
}
