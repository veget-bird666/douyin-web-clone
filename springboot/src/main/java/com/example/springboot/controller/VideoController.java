package com.example.springboot.controller;

import com.example.springboot.common.Result;
import com.example.springboot.entity.Video;
import com.example.springboot.service.VideoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/video")
@Tag(name = "视频管理")
public class VideoController {

    private static final Logger log = LoggerFactory.getLogger(VideoController.class);

    @Resource
    private VideoService videoService;

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "上传视频")
    public Result upload(
            @RequestPart("file") MultipartFile file,
            @RequestParam("title") String title,
            @RequestParam(value = "description", required = false) String description) {
        log.info("上传视频请求 - title: {}, fileSize: {}", title, file.getSize());
        Video video = videoService.uploadVideo(file, title, description);
        log.info("视频上传成功 - videoId: {}", video.getVideoId());
        return Result.success(video, "上传成功");
    }

    @GetMapping("/recommend")
    @Operation(summary = "推荐视频流（按点赞数排序，排除已看过的）")
    public Result getRecommended(
            @RequestParam(value = "limit", defaultValue = "10") int limit,
            @RequestParam(value = "offset", defaultValue = "0") int offset) {
        List<Video> videos = videoService.getRecommendedFeed(limit, offset);
        return Result.success(videos);
    }

    @GetMapping("/list")
    @Operation(summary = "全部视频流（按时间排序）")
    public Result getFeed(
            @RequestParam(value = "limit", defaultValue = "10") int limit,
            @RequestParam(value = "offset", defaultValue = "0") int offset) {
        List<Video> videos = videoService.getFeed(limit, offset);
        return Result.success(videos);
    }

    @GetMapping("/user/{userId}")
    @Operation(summary = "用户视频列表")
    public Result getUserVideos(@PathVariable String userId) {
        List<Video> videos = videoService.getVideoListByUser(userId);
        return Result.success(videos);
    }

    @GetMapping("/info/{videoId}")
    @Operation(summary = "视频详情（自动记录浏览）")
    public Result getVideoInfo(@PathVariable String videoId) {
        Video video = videoService.getVideoInfo(videoId);
        return Result.success(video);
    }

    @GetMapping("/url/{videoId}")
    @Operation(summary = "获取视频播放地址")
    public Result getVideoUrl(@PathVariable String videoId) {
        String url = videoService.getVideoUrl(videoId);
        return Result.success(new UrlResponse(url));
    }

    @PostMapping("/view/{videoId}")
    @Operation(summary = "记录浏览")
    public Result recordView(@PathVariable String videoId) {
        videoService.recordView(videoId);
        return Result.success(null, "已记录浏览");
    }

    @GetMapping("/viewed/{videoId}")
    @Operation(summary = "查询当前用户是否已浏览过")
    public Result isViewed(@PathVariable String videoId) {
        boolean viewed = videoService.isViewed(videoId);
        return Result.success(Map.of("viewed", viewed));
    }

    @PostMapping("/like/{videoId}")
    @Operation(summary = "点赞视频")
    public Result likeVideo(@PathVariable String videoId) {
        videoService.likeVideo(videoId);
        return Result.success(null, "点赞成功");
    }

    @PostMapping("/unlike/{videoId}")
    @Operation(summary = "取消点赞")
    public Result unlikeVideo(@PathVariable String videoId) {
        videoService.unlikeVideo(videoId);
        return Result.success(null, "已取消点赞");
    }

    @GetMapping("/liked/{videoId}")
    @Operation(summary = "查询当前用户是否已点赞")
    public Result isLiked(@PathVariable String videoId) {
        boolean liked = videoService.isLiked(videoId);
        return Result.success(Map.of("liked", liked));
    }

    @PostMapping("/favorite/{videoId}")
    @Operation(summary = "收藏视频")
    public Result favoriteVideo(@PathVariable String videoId) {
        videoService.favoriteVideo(videoId);
        return Result.success(null, "收藏成功");
    }

    @PostMapping("/unfavorite/{videoId}")
    @Operation(summary = "取消收藏")
    public Result unfavoriteVideo(@PathVariable String videoId) {
        videoService.unfavoriteVideo(videoId);
        return Result.success(null, "已取消收藏");
    }

    @GetMapping("/favorited/{videoId}")
    @Operation(summary = "查询当前用户是否已收藏")
    public Result isFavorited(@PathVariable String videoId) {
        boolean favorited = videoService.isFavorited(videoId);
        return Result.success(Map.of("favorited", favorited));
    }

    @GetMapping("/favorites")
    @Operation(summary = "获取我的收藏列表")
    public Result getFavorites() {
        return Result.success(videoService.getFavoriteList());
    }

    @GetMapping("/favorite-count/{videoId}")
    @Operation(summary = "获取视频收藏数")
    public Result getFavoriteCount(@PathVariable String videoId) {
        return Result.success(Map.of("count", videoService.getFavoriteCount(videoId)));
    }

    @DeleteMapping("/{videoId}")
    @Operation(summary = "删除视频（仅作者可删）")
    public Result deleteVideo(@PathVariable String videoId) {
        videoService.deleteVideo(videoId);
        return Result.success(null, "删除成功");
    }

    public record UrlResponse(String url) {}
}
