package com.ttjobs.backend.controller;

import com.ttjobs.backend.dto.forum.ForumCommentDTO;
import com.ttjobs.backend.dto.forum.ForumCommentRequest;
import com.ttjobs.backend.dto.forum.ForumPostDTO;
import com.ttjobs.backend.dto.forum.ForumPostRequest;
import com.ttjobs.backend.dto.forum.ForumReportDTO;
import com.ttjobs.backend.dto.forum.ForumReportRequest;
import com.ttjobs.backend.service.ForumService;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/forum")
public class ForumController {

    @Autowired
    private ForumService forumService;

    @GetMapping("/posts")
    public List<ForumPostDTO> getPosts(
            @RequestParam(required = false) String tag,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size) {
        return forumService.getPosts(tag, page, size);
    }

    @GetMapping("/posts/{postId}")
    public ForumPostDTO getPost(@PathVariable Long postId) {
        return forumService.getPost(postId);
    }

    @PostMapping("/posts")
    public ForumPostDTO createPost(@Valid @RequestBody ForumPostRequest request) {
        return forumService.createPost(request);
    }

    @PostMapping(value = "/posts", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ForumPostDTO createPostWithImage(
            @RequestPart("title") String title,
            @RequestPart("body") String body,
            @RequestPart("tag") String tag,
            @RequestPart(value = "image", required = false) MultipartFile image) {
        ForumPostRequest request = new ForumPostRequest();
        request.setTitle(title);
        request.setBody(body);
        request.setTag(tag);
        return forumService.createPost(request, image);
    }

    @PutMapping("/posts/{postId}")
    public ForumPostDTO updatePost(@PathVariable Long postId, @Valid @RequestBody ForumPostRequest request) {
        return forumService.updatePost(postId, request);
    }

    @DeleteMapping("/posts/{postId}")
    public void deleteOwnPost(@PathVariable Long postId) {
        forumService.deleteOwnPost(postId);
    }

    @PostMapping("/posts/{postId}/likes")
    public ForumPostDTO toggleLike(@PathVariable Long postId) {
        return forumService.toggleLike(postId);
    }

    @PostMapping("/posts/{postId}/comments")
    public ForumCommentDTO createComment(@PathVariable Long postId, @Valid @RequestBody ForumCommentRequest request) {
        return forumService.createComment(postId, request);
    }

    @PutMapping("/comments/{commentId}")
    public ForumCommentDTO updateComment(@PathVariable Long commentId, @Valid @RequestBody ForumCommentRequest request) {
        return forumService.updateComment(commentId, request);
    }

    @DeleteMapping("/comments/{commentId}")
    public void deleteOwnComment(@PathVariable Long commentId) {
        forumService.deleteOwnComment(commentId);
    }

    @PostMapping("/reports")
    public ForumReportDTO report(@Valid @RequestBody ForumReportRequest request) {
        return forumService.report(request);
    }
}

