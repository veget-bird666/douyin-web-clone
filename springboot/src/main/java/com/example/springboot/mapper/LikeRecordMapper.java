package com.example.springboot.mapper;

import com.example.springboot.entity.Video;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

public interface LikeRecordMapper {

    @Insert("INSERT IGNORE INTO like_record (user_id, video_id) VALUES (#{userId}, #{videoId})")
    int insertIgnore(@Param("userId") String userId, @Param("videoId") String videoId);

    @Delete("DELETE FROM like_record WHERE user_id = #{userId} AND video_id = #{videoId}")
    int delete(@Param("userId") String userId, @Param("videoId") String videoId);

    @Delete("DELETE FROM like_record WHERE user_id = #{userId}")
    int deleteByUserId(@Param("userId") String userId);

    @Select("SELECT COUNT(*) FROM like_record WHERE user_id = #{userId} AND video_id = #{videoId}")
    int countByUserAndVideo(@Param("userId") String userId, @Param("videoId") String videoId);

    @Select("SELECT v.* FROM video v INNER JOIN like_record l ON v.video_id = l.video_id " +
            "WHERE l.user_id = #{userId} AND v.status = 1 ORDER BY l.create_time DESC")
    List<Video> selectLikesByUserId(@Param("userId") String userId);
}
