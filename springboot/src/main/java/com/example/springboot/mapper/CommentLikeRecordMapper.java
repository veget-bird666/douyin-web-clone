package com.example.springboot.mapper;

import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

public interface CommentLikeRecordMapper {

    @Insert("INSERT IGNORE INTO comment_like_record (user_id, comment_id) VALUES (#{userId}, #{commentId})")
    int insertIgnore(@Param("userId") String userId, @Param("commentId") String commentId);

    @Delete("DELETE FROM comment_like_record WHERE user_id = #{userId} AND comment_id = #{commentId}")
    int delete(@Param("userId") String userId, @Param("commentId") String commentId);

    @Select("SELECT COUNT(*) FROM comment_like_record WHERE user_id = #{userId} AND comment_id = #{commentId}")
    int countByUserAndComment(@Param("userId") String userId, @Param("commentId") String commentId);
}
