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
        log.info("upload请求 - title: {}, description: {}, fileSize: {}", title, description, file.getSize());
        try {
            String videoId = videoService.uploadVideo(file, title, description);
            Video video = videoService.getVideoInfo(videoId);
            log.info("上传成功 - videoId: {}", videoId);
            return Result.success(video);
        } catch (Exception e) {
            log.error("上传失败: {}", e.getMessage(), e);
            return Result.error("上传失败: " + e.getMessage());
        }
    }

    @GetMapping("/list")
    @Operation(summary = "视频流列表")
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
    @Operation(summary = "视频详情")
    public Result getVideoInfo(@PathVariable String videoId) {
        Video video = videoService.getVideoInfo(videoId);
        if (video == null) {
            return Result.error("视频不存在");
        }
        return Result.success(video);
    }

    @GetMapping("/url/{videoId}")
    @Operation(summary = "获取视频URL")
    public Result getVideoUrl(@PathVariable String videoId) {
        try {
            String url = videoService.getVideoUrl(videoId);
            return Result.success(new UrlResult(url));
        } catch (Exception e) {
            return Result.error("获取视频URL失败: " + e.getMessage());
        }
    }

    public static class UrlResult {
        private String url;
        public UrlResult(String url) { this.url = url; }
        public String getUrl() { return url; }
    }
}