package com.example.springboot.controller;

import com.example.springboot.common.Result;
import com.example.springboot.entity.CommentVO;
import com.example.springboot.service.CommentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/comment")
@Tag(name = "评论管理")
public class CommentController {

    @Resource
    private CommentService commentService;

    @GetMapping("/list/{videoId}")
    @Operation(summary = "获取视频一级评论列表")
    public Result listComments(
            @PathVariable String videoId,
            @RequestParam(value = "limit", defaultValue = "20") int limit,
            @RequestParam(value = "offset", defaultValue = "0") int offset) {
        List<CommentVO> list = commentService.listTopLevelComments(videoId, limit, offset);
        return Result.success(list);
    }

    @GetMapping("/replies/{commentId}")
    @Operation(summary = "获取评论回复列表")
    public Result listReplies(
            @PathVariable String commentId,
            @RequestParam(value = "limit", defaultValue = "20") int limit,
            @RequestParam(value = "offset", defaultValue = "0") int offset) {
        List<CommentVO> list = commentService.listReplies(commentId, limit, offset);
        return Result.success(list);
    }

    @GetMapping("/count/{videoId}")
    @Operation(summary = "获取视频评论总数")
    public Result getCommentCount(@PathVariable String videoId) {
        int count = commentService.getCommentCount(videoId);
        return Result.success(Map.of("count", count));
    }

    @PostMapping
    @Operation(summary = "发表评论或回复")
    public Result addComment(@RequestBody Map<String, String> body) {
        String videoId = body.get("videoId");
        String content = body.get("content");
        String parentId = body.get("parentId");
        CommentVO comment = commentService.addComment(videoId, content, parentId);
        return Result.success(comment, "评论成功");
    }

    @DeleteMapping("/{commentId}")
    @Operation(summary = "删除自己的评论")
    public Result deleteComment(@PathVariable String commentId) {
        Map<String, Object> data = commentService.deleteComment(commentId);
        return Result.success(data, "删除成功");
    }

    @PostMapping("/like/{commentId}")
    @Operation(summary = "点赞评论")
    public Result likeComment(@PathVariable String commentId) {
        commentService.likeComment(commentId);
        return Result.success(null, "点赞成功");
    }

    @PostMapping("/unlike/{commentId}")
    @Operation(summary = "取消点赞评论")
    public Result unlikeComment(@PathVariable String commentId) {
        commentService.unlikeComment(commentId);
        return Result.success(null, "已取消点赞");
    }

    @PostMapping("/fold/{commentId}")
    @Operation(summary = "折叠评论（心碎）")
    public Result foldComment(@PathVariable String commentId) {
        commentService.foldComment(commentId);
        return Result.success(null, "已折叠");
    }

    @PostMapping("/unfold/{commentId}")
    @Operation(summary = "取消折叠评论")
    public Result unfoldComment(@PathVariable String commentId) {
        commentService.unfoldComment(commentId);
        return Result.success(null, "已取消折叠");
    }
}
