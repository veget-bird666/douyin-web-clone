package com.example.springboot.mapper;

import com.example.springboot.entity.Comment;
import com.example.springboot.entity.CommentVO;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface CommentMapper {

    void insert(Comment comment);

    Comment selectByCommentId(@Param("commentId") String commentId);

    List<CommentVO> selectTopLevelByVideoId(@Param("videoId") String videoId,
                                            @Param("limit") int limit,
                                            @Param("offset") int offset);

    List<CommentVO> selectRepliesByParentId(@Param("parentId") String parentId,
                                              @Param("limit") int limit,
                                              @Param("offset") int offset);

    int countRepliesByParentId(@Param("parentId") String parentId);

    int countByVideoId(@Param("videoId") String videoId);

    int incrementLikeCount(@Param("commentId") String commentId);

    int decrementLikeCount(@Param("commentId") String commentId);

    int foldComment(@Param("commentId") String commentId);

    int unfoldComment(@Param("commentId") String commentId);

    int deleteByCommentId(@Param("commentId") String commentId);

    int deleteRepliesByParentId(@Param("parentId") String parentId);
}
