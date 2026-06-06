package com.example.springboot.mapper;

import com.example.springboot.entity.Video;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

public interface VideoMapper {

    @Insert("INSERT INTO video (video_id, user_id, title, description, like_count, comment_count, view_count, " +
            "status, object_name, cover_object_name, duration, size, width, height, format) " +
            "VALUES (#{videoId}, #{userId}, #{title}, #{description}, #{likeCount}, #{commentCount}, #{viewCount}, " +
            "#{status}, #{objectName}, #{coverObjectName}, #{duration}, #{size}, #{width}, #{height}, #{format})")
    void insert(Video video);

    @Select("SELECT * FROM video WHERE video_id = #{videoId}")
    Video selectByVideoId(@Param("videoId") String videoId);

    @Select("SELECT * FROM video WHERE user_id = #{userId} ORDER BY create_time DESC")
    List<Video> selectByUserId(@Param("userId") String userId);

    @Select("SELECT * FROM video WHERE status = 1 ORDER BY create_time DESC LIMIT #{limit} OFFSET #{offset}")
    List<Video> selectFeed(@Param("limit") int limit, @Param("offset") int offset);

    @Select("SELECT * FROM video WHERE status = 1 AND video_id NOT IN " +
            "(SELECT video_id COLLATE utf8mb4_unicode_ci FROM view_record WHERE user_id = #{userId}) " +
            "ORDER BY like_count DESC, create_time DESC LIMIT #{limit} OFFSET #{offset}")
    List<Video> selectRecommended(@Param("userId") String userId, @Param("limit") int limit, @Param("offset") int offset);

    @Update("UPDATE video SET like_count = like_count + 1 WHERE video_id = #{videoId}")
    int incrementLikeCount(@Param("videoId") String videoId);

    @Update("UPDATE video SET like_count = GREATEST(like_count - 1, 0) WHERE video_id = #{videoId}")
    int decrementLikeCount(@Param("videoId") String videoId);

    @Update("UPDATE video SET status = 2 WHERE video_id = #{videoId} AND user_id = #{userId}")
    int softDelete(@Param("videoId") String videoId, @Param("userId") String userId);

    @Update("UPDATE video SET status = 2 WHERE user_id = #{userId}")
    int softDeleteAllByUserId(@Param("userId") String userId);
}
