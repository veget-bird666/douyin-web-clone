package com.example.springboot.mapper;

import com.example.springboot.entity.Video;
import org.apache.ibatis.annotations.*;

@Mapper
public interface VideoMapper {
    @Insert("INSERT INTO video (video_id, user_id, title, description, like_count, comment_count, view_count, status, object_name, cover_object_name, duration, size, width, height, format) " +
            "VALUES (#{videoId}, #{userId}, #{title}, #{description}, #{likeCount}, #{commentCount}, #{viewCount}, #{status}, #{objectName}, #{coverObjectName}, #{duration}, #{size}, #{width}, #{height}, #{format})")
    void insert(Video video);

    @Select("SELECT * FROM video WHERE video_id = #{videoId}")
    Video selectByVideoId(String videoId);

    @Select("SELECT * FROM video WHERE user_id = #{userId} ORDER BY create_time DESC")
    java.util.List<Video> selectByUserId(String userId);

    @Select("SELECT * FROM video WHERE status = 1 ORDER BY create_time DESC LIMIT #{limit} OFFSET #{offset}")
    java.util.List<Video> selectFeed(@Param("limit") int limit, @Param("offset") int offset);
}