package com.example.springboot.mapper;

import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

public interface LikeRecordMapper {

    @Insert("INSERT IGNORE INTO like_record (user_id, video_id) VALUES (#{userId}, #{videoId})")
    int insertIgnore(@Param("userId") String userId, @Param("videoId") String videoId);

    @Delete("DELETE FROM like_record WHERE user_id = #{userId} AND video_id = #{videoId}")
    int delete(@Param("userId") String userId, @Param("videoId") String videoId);

    @Delete("DELETE FROM like_record WHERE user_id = #{userId}")
    int deleteByUserId(@Param("userId") String userId);

    @Select("SELECT COUNT(*) FROM like_record WHERE user_id = #{userId} AND video_id = #{videoId}")
    int countByUserAndVideo(@Param("userId") String userId, @Param("videoId") String videoId);
}
