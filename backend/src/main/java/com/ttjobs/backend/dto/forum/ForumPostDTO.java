package com.ttjobs.backend.dto.forum;

import com.ttjobs.backend.dto.forum.ForumCommentDTO;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
public class ForumPostDTO {
    private Long id;
    private Long authorId;
    private String author;
    private String role;
    private String title;
    private String body;
    private String tag;
    private String imageUrl;
    private List<String> hashtags = new ArrayList<>();
    private long likes;
    private long commentCount;
    private boolean liked;
    private boolean hidden;
    private boolean editable;
    private List<ForumCommentDTO> comments = new ArrayList<>();
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

