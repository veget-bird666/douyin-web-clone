package com.example.springboot.mapper;

import com.example.springboot.entity.Video;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

public interface FavoriteRecordMapper {

    @Insert("INSERT IGNORE INTO favorite_record (user_id, video_id) VALUES (#{userId}, #{videoId})")
    int insertIgnore(@Param("userId") String userId, @Param("videoId") String videoId);

    @Delete("DELETE FROM favorite_record WHERE user_id = #{userId} AND video_id = #{videoId}")
    int delete(@Param("userId") String userId, @Param("videoId") String videoId);

    @Delete("DELETE FROM favorite_record WHERE user_id = #{userId}")
    int deleteByUserId(@Param("userId") String userId);

    @Select("SELECT COUNT(*) FROM favorite_record WHERE user_id = #{userId} AND video_id = #{videoId}")
    int countByUserAndVideo(@Param("userId") String userId, @Param("videoId") String videoId);

    @Select("SELECT COUNT(*) FROM favorite_record WHERE video_id = #{videoId}")
    int countByVideoId(@Param("videoId") String videoId);

    @Select("SELECT v.* FROM video v INNER JOIN favorite_record f ON v.video_id = f.video_id " +
            "WHERE f.user_id = #{userId} AND v.status = 1 ORDER BY f.create_time DESC")
    List<Video> selectFavoritesByUserId(@Param("userId") String userId);
}
