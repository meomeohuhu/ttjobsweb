package com.ttjobs.backend.controller;

import com.ttjobs.backend.dto.forum.ForumModerationRequest;
import com.ttjobs.backend.dto.common.AdminActionRequest;
import com.ttjobs.backend.dto.forum.ForumCommentDTO;
import com.ttjobs.backend.dto.forum.ForumPostDTO;
import com.ttjobs.backend.dto.forum.ForumReportDTO;
import com.ttjobs.backend.dto.forum.ForumUserViolationDTO;
import com.ttjobs.backend.service.ForumService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/admin/forum")
public class AdminForumController {

    @Autowired
    private ForumService forumService;

    @GetMapping("/reports")
    public List<ForumReportDTO> getOpenReports(
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size,
            @RequestParam(required = false) String status) {
        return forumService.getReports(status, page, size);
    }

    @PutMapping("/reports/{reportId}")
    public ForumReportDTO moderateReport(@PathVariable Long reportId, @RequestBody ForumModerationRequest request) {
        return forumService.moderateReport(reportId, request);
    }

    @PostMapping("/reports/{reportId}/review")
    public ForumReportDTO reviewReport(@PathVariable Long reportId, @RequestBody ForumModerationRequest request) {
        return forumService.moderateReport(reportId, request);
    }

    @PutMapping("/posts/{postId}/hide")
    public ForumPostDTO hidePost(@PathVariable Long postId) {
        return forumService.hidePost(postId);
    }

    @DeleteMapping("/posts/{postId}")
    public void deletePost(@PathVariable Long postId) {
        forumService.deletePost(postId);
    }

    @PostMapping("/posts/{postId}/restore")
    public ForumPostDTO restorePost(@PathVariable Long postId) {
        return forumService.restorePost(postId);
    }

    @PostMapping("/comments/{commentId}/restore")
    public ForumCommentDTO restoreComment(@PathVariable Long commentId) {
        return forumService.restoreComment(commentId);
    }

    @GetMapping("/violations")
    public List<ForumUserViolationDTO> getViolations() {
        return forumService.getViolations();
    }

    @PostMapping("/users/{userId}/warn")
    public ForumUserViolationDTO warnUser(@PathVariable Long userId, @RequestBody(required = false) AdminActionRequest request) {
        return forumService.warnUser(userId, request == null ? null : request.getReason());
    }

    @PostMapping("/users/{userId}/mute")
    public ForumUserViolationDTO muteUser(@PathVariable Long userId, @RequestBody(required = false) AdminActionRequest request) {
        return forumService.muteUser(userId, request);
    }
}

