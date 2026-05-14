package com.ttjobs.backend.service;

import com.ttjobs.backend.dto.forum.ForumCommentDTO;
import com.ttjobs.backend.dto.forum.ForumCommentRequest;
import com.ttjobs.backend.dto.forum.ForumModerationRequest;
import com.ttjobs.backend.dto.forum.ForumPostDTO;
import com.ttjobs.backend.dto.forum.ForumPostRequest;
import com.ttjobs.backend.dto.forum.ForumReportDTO;
import com.ttjobs.backend.dto.forum.ForumReportRequest;
import com.ttjobs.backend.entity.ForumComment;
import com.ttjobs.backend.entity.ForumLike;
import com.ttjobs.backend.entity.ForumPost;
import com.ttjobs.backend.entity.ForumReport;
import com.ttjobs.backend.entity.ForumUserViolation;
import com.ttjobs.backend.entity.User;
import com.ttjobs.backend.repository.ForumCommentRepository;
import com.ttjobs.backend.repository.ForumLikeRepository;
import com.ttjobs.backend.repository.ForumPostRepository;
import com.ttjobs.backend.repository.ForumReportRepository;
import com.ttjobs.backend.repository.ForumUserViolationRepository;
import com.ttjobs.backend.repository.UserRepository;
import com.ttjobs.backend.dto.common.AdminActionRequest;
import com.ttjobs.backend.dto.forum.ForumUserViolationDTO;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

@Service
public class ForumService {

    private static final Pattern HASHTAG_PATTERN = Pattern.compile("#[\\p{L}\\p{N}_-]+");
    private static final Set<String> ALLOWED_TAGS = Set.of(
            "Hỏi đáp",
            "CV",
            "Phỏng vấn",
            "Lương thưởng",
            "Tin tuyển dụng"
    );

    @Autowired
    private ForumPostRepository forumPostRepository;

    @Autowired
    private ForumCommentRepository forumCommentRepository;

    @Autowired
    private ForumLikeRepository forumLikeRepository;

    @Autowired
    private ForumReportRepository forumReportRepository;
    @Autowired
    private ForumUserViolationRepository forumUserViolationRepository;

    @Autowired
    private AuthContextService authContextService;

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private ImageUploadService imageUploadService;

    @Autowired
    private RealtimeEventPublisher realtimeEventPublisher;
    @Autowired
    private AdminAuditLogService adminAuditLogService;
    @Autowired
    private UserRepository userRepository;

    @Transactional(readOnly = true)
    public List<ForumPostDTO> getPosts(String tag, Integer page, Integer size) {
        Pageable pageable = PageRequest.of(safePage(page), safeSize(size));
        Optional<User> currentUser = authContextService.getCurrentUserOptional();
        boolean filterByTag = tag != null && !tag.isBlank() && !"all".equalsIgnoreCase(tag);

        return (filterByTag
                ? forumPostRepository.findByTagAndDeletedAtIsNullAndHiddenFalseOrderByCreatedAtDesc(tag.trim(), pageable)
                : forumPostRepository.findByDeletedAtIsNullAndHiddenFalseOrderByCreatedAtDesc(pageable))
                .stream()
                .map(post -> toPostDto(post, currentUser, true))
                .toList();
    }

    @Transactional(readOnly = true)
    public ForumPostDTO getPost(Long postId) {
        Optional<User> currentUser = authContextService.getCurrentUserOptional();
        return toPostDto(getVisiblePost(postId), currentUser, true);
    }

    @Transactional
    public ForumPostDTO createPost(ForumPostRequest request) {
        return createPost(request, null);
    }

    @Transactional
    public ForumPostDTO createPost(ForumPostRequest request, MultipartFile image) {
        User currentUser = authContextService.requireCurrentUser();
        requireNotMuted(currentUser);
        String tag = normalizeTag(request.getTag());

        ForumPost post = new ForumPost();
        post.setAuthor(currentUser);
        applyPostContent(post, request, tag);
        if (image != null && !image.isEmpty()) {
            // Ảnh bài viết lưu ở storage tập trung để feed không phụ thuộc file local của browser.
            post.setImageUrl(imageUploadService.uploadImage(image, "ttjobs/forum", "forum-" + currentUser.getId()));
        }

        ForumPost saved = forumPostRepository.save(post);
        ForumPostDTO dto = toPostDto(saved, Optional.of(currentUser), true);
        publishForumEvent("post_created", saved.getId(), dto);
        realtimeEventPublisher.publish("/topic/forum/posts", dto);
        return dto;
    }

    @Transactional
    public ForumPostDTO updatePost(Long postId, ForumPostRequest request) {
        User currentUser = authContextService.requireCurrentUser();
        requireNotMuted(currentUser);
        ForumPost post = getVisiblePost(postId);
        requireOwnerOrAdmin(post.getAuthor(), currentUser);

        String tag = normalizeTag(request.getTag());
        applyPostContent(post, request, tag);
        ForumPost saved = forumPostRepository.save(post);
        ForumPostDTO dto = toPostDto(saved, Optional.of(currentUser), true);
        publishForumEvent("post_updated", saved.getId(), dto);
        realtimeEventPublisher.publish("/topic/forum/posts", dto);
        return dto;
    }

    @Transactional
    public void deleteOwnPost(Long postId) {
        User currentUser = authContextService.requireCurrentUser();
        ForumPost post = forumPostRepository.findByIdAndDeletedAtIsNull(postId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy bài viết"));
        requireOwnerOrAdmin(post.getAuthor(), currentUser);
        softDeletePost(post);
        publishForumEvent("post_deleted", post.getId(), Map.of("id", post.getId()));
    }

    @Transactional
    public ForumPostDTO toggleLike(Long postId) {
        User currentUser = authContextService.requireCurrentUser();
        ForumPost post = getVisiblePost(postId);
        Optional<ForumLike> existing = forumLikeRepository.findByPostIdAndUserId(post.getId(), currentUser.getId());

        if (existing.isPresent()) {
            forumLikeRepository.delete(existing.get());
        } else {
            ForumLike like = new ForumLike();
            like.setPost(post);
            like.setUser(currentUser);
            forumLikeRepository.save(like);
            notifyPostAuthor(post, currentUser, "Có lượt thích mới", currentUser.getName() + " đã thích bài viết của bạn.");
        }

        post.setLikeCount((int) forumLikeRepository.countByPostId(post.getId()));
        ForumPostDTO dto = toPostDto(forumPostRepository.save(post), Optional.of(currentUser), true);
        publishForumEvent("post_liked", post.getId(), dto);
        realtimeEventPublisher.publish("/topic/forum/posts/" + post.getId() + "/likes", dto);
        return dto;
    }

    @Transactional
    public ForumCommentDTO createComment(Long postId, ForumCommentRequest request) {
        User currentUser = authContextService.requireCurrentUser();
        ForumPost post = getVisiblePost(postId);

        ForumComment comment = new ForumComment();
        comment.setPost(post);
        comment.setAuthor(currentUser);
        comment.setBody(cleanBody(request.getBody()));
        ForumComment saved = forumCommentRepository.save(comment);

        post.setCommentCount(post.getCommentCount() + 1);
        forumPostRepository.save(post);
        notifyPostAuthor(post, currentUser, "Có bình luận mới", currentUser.getName() + " đã bình luận trong bài viết của bạn.");

        ForumCommentDTO dto = toCommentDto(saved, Optional.of(currentUser));
        publishForumEvent("comment_created", post.getId(), dto);
        realtimeEventPublisher.publish("/topic/forum/posts/" + post.getId() + "/comments", dto);
        return dto;
    }

    @Transactional
    public ForumCommentDTO updateComment(Long commentId, ForumCommentRequest request) {
        User currentUser = authContextService.requireCurrentUser();
        ForumComment comment = forumCommentRepository.findByIdAndDeletedAtIsNull(commentId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy bình luận"));
        if (Boolean.TRUE.equals(comment.getHidden())) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy bình luận");
        }
        requireOwnerOrAdmin(comment.getAuthor(), currentUser);
        comment.setBody(cleanBody(request.getBody()));
        ForumComment saved = forumCommentRepository.save(comment);
        ForumCommentDTO dto = toCommentDto(saved, Optional.of(currentUser));
        publishForumEvent("comment_updated", saved.getPost().getId(), dto);
        return dto;
    }

    @Transactional
    public void deleteOwnComment(Long commentId) {
        User currentUser = authContextService.requireCurrentUser();
        ForumComment comment = forumCommentRepository.findByIdAndDeletedAtIsNull(commentId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy bình luận"));
        requireOwnerOrAdmin(comment.getAuthor(), currentUser);
        softDeleteComment(comment);
        publishForumEvent("comment_deleted", comment.getPost().getId(), Map.of(
                "id", comment.getId(),
                "postId", comment.getPost().getId()
        ));
    }

    @Transactional
    public ForumReportDTO report(ForumReportRequest request) {
        User currentUser = authContextService.requireCurrentUser();
        if (request.getPostId() == null && request.getCommentId() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cần chọn bài viết hoặc bình luận để báo cáo");
        }

        ForumReport report = new ForumReport();
        report.setReporter(currentUser);
        report.setReason(cleanBody(request.getReason()));
        report.setDetails(request.getDetails() == null ? null : request.getDetails().trim());
        if (request.getPostId() != null) {
            report.setPost(forumPostRepository.findByIdAndDeletedAtIsNull(request.getPostId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy bài viết")));
        }
        if (request.getCommentId() != null) {
            report.setComment(forumCommentRepository.findByIdAndDeletedAtIsNull(request.getCommentId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy bình luận")));
        }
        return toReportDto(forumReportRepository.save(report));
    }

    @Transactional(readOnly = true)
    public List<ForumReportDTO> getReports(String status, Integer page, Integer size) {
        ForumReport.Status reportStatus = parseReportStatus(status);
        return forumReportRepository
                .findByStatusOrderByCreatedAtDesc(reportStatus, PageRequest.of(safePage(page), safeSize(size)))
                .stream()
                .map(this::toReportDto)
                .toList();
    }

    @Transactional
    public ForumReportDTO moderateReport(Long reportId, ForumModerationRequest request) {
        ForumReport report = forumReportRepository.findById(reportId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy báo cáo"));
        ForumModerationRequest safeRequest = request == null ? new ForumModerationRequest() : request;
        User admin = authContextService.requireCurrentUser();
        String action = safeRequest.getAction() == null || safeRequest.getAction().isBlank()
                ? "review"
                : safeRequest.getAction().trim().toLowerCase();
        String reason = safeRequest.getReason() == null ? null : safeRequest.getReason().trim();

        if (("hide_post".equals(action) || Boolean.TRUE.equals(safeRequest.getHidePost())) && report.getPost() != null) {
            report.getPost().setHidden(true);
            forumPostRepository.save(report.getPost());
            publishForumEvent("post_hidden", report.getPost().getId(), Map.of("id", report.getPost().getId()));
        }
        if (("hide_comment".equals(action) || Boolean.TRUE.equals(safeRequest.getHideComment())) && report.getComment() != null) {
            report.getComment().setHidden(true);
            forumCommentRepository.save(report.getComment());
            publishForumEvent("comment_hidden", report.getComment().getPost().getId(), Map.of(
                    "id", report.getComment().getId(),
                    "postId", report.getComment().getPost().getId()
            ));
        }
        if ("restore".equals(action)) {
            if (report.getPost() != null) {
                report.getPost().setHidden(false);
                forumPostRepository.save(report.getPost());
                publishForumEvent("post_restored", report.getPost().getId(), toPostDto(report.getPost(), Optional.empty(), false));
            }
            if (report.getComment() != null) {
                report.getComment().setHidden(false);
                forumCommentRepository.save(report.getComment());
                publishForumEvent("comment_restored", report.getComment().getPost().getId(), toCommentDto(report.getComment(), Optional.empty()));
            }
        }
        if ("warn_user".equals(action)) {
            User target = report.getComment() != null ? report.getComment().getAuthor()
                    : report.getPost() != null ? report.getPost().getAuthor() : null;
            if (target != null) {
                warnUser(target.getId(), reason == null ? "Forum report warning" : reason);
            }
        }

        ForumReport.Status nextStatus = parseReportStatus(safeRequest.getStatus());
        report.setStatus(nextStatus);
        report.setModerationAction(action);
        report.setModerationReason(reason);
        report.setResolvedBy(admin);
        report.setResolvedAt((nextStatus == ForumReport.Status.RESOLVED || nextStatus == ForumReport.Status.REJECTED) ? LocalDateTime.now() : null);
        ForumReportDTO dto = toReportDto(forumReportRepository.save(report));
        adminAuditLogService.log("FORUM_REPORT_" + nextStatus.name(), "FORUM_REPORT", reportId, reason, action);
        return dto;
    }

    @Transactional
    public ForumPostDTO restorePost(Long postId) {
        ForumPost post = forumPostRepository.findByIdAndDeletedAtIsNull(postId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "KhÃ´ng tÃ¬m tháº¥y bÃ i viáº¿t"));
        post.setHidden(false);
        ForumPost saved = forumPostRepository.save(post);
        adminAuditLogService.log("FORUM_POST_RESTORED", "FORUM_POST", postId, null, null);
        publishForumEvent("post_restored", saved.getId(), toPostDto(saved, Optional.empty(), true));
        return toPostDto(saved, Optional.empty(), true);
    }

    @Transactional
    public ForumCommentDTO restoreComment(Long commentId) {
        ForumComment comment = forumCommentRepository.findByIdAndDeletedAtIsNull(commentId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "KhÃ´ng tÃ¬m tháº¥y bÃ¬nh luáº­n"));
        comment.setHidden(false);
        ForumComment saved = forumCommentRepository.save(comment);
        adminAuditLogService.log("FORUM_COMMENT_RESTORED", "FORUM_COMMENT", commentId, null, null);
        publishForumEvent("comment_restored", saved.getPost().getId(), toCommentDto(saved, Optional.empty()));
        return toCommentDto(saved, Optional.empty());
    }

    @Transactional
    public ForumUserViolationDTO warnUser(Long userId, String reason) {
        User target = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
        ForumUserViolation violation = forumUserViolationRepository.findByUserId(userId).orElseGet(() -> {
            ForumUserViolation created = new ForumUserViolation();
            created.setUser(target);
            return created;
        });
        violation.setWarningCount((violation.getWarningCount() == null ? 0 : violation.getWarningCount()) + 1);
        violation.setLastReason(reason);
        violation.setLastActionAt(LocalDateTime.now());
        ForumUserViolation saved = forumUserViolationRepository.save(violation);
        adminAuditLogService.log("FORUM_USER_WARNED", "USER", userId, reason, null);
        return toViolationDto(saved);
    }

    @Transactional
    public ForumUserViolationDTO muteUser(Long userId, AdminActionRequest request) {
        String reason = request == null ? null : request.getReason();
        int days = request == null || request.getDays() == null ? 7 : Math.max(1, request.getDays());
        ForumUserViolationDTO dto = warnUser(userId, reason == null ? "Muted by admin" : reason);
        ForumUserViolation violation = forumUserViolationRepository.findByUserId(userId).orElseThrow();
        violation.setMutedUntil(LocalDateTime.now().plusDays(days));
        ForumUserViolation saved = forumUserViolationRepository.save(violation);
        adminAuditLogService.log("FORUM_USER_MUTED", "USER", userId, reason, "days=" + days);
        return toViolationDto(saved);
    }

    @Transactional(readOnly = true)
    public List<ForumUserViolationDTO> getViolations() {
        return forumUserViolationRepository.findAllByOrderByLastActionAtDesc().stream().map(this::toViolationDto).toList();
    }

    @Transactional
    public ForumPostDTO hidePost(Long postId) {
        ForumPost post = forumPostRepository.findByIdAndDeletedAtIsNull(postId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy bài viết"));
        post.setHidden(true);
        ForumPost saved = forumPostRepository.save(post);
        publishForumEvent("post_hidden", saved.getId(), Map.of("id", saved.getId()));
        return toPostDto(saved, Optional.empty(), true);
    }

    @Transactional
    public void deletePost(Long postId) {
        ForumPost post = forumPostRepository.findByIdAndDeletedAtIsNull(postId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy bài viết"));
        softDeletePost(post);
        publishForumEvent("post_deleted", post.getId(), Map.of("id", post.getId()));
    }

    private void applyPostContent(ForumPost post, ForumPostRequest request, String tag) {
        String title = cleanBody(request.getTitle());
        String body = cleanBody(request.getBody());
        post.setTitle(title);
        post.setBody(body);
        post.setTag(tag);
        post.setHashtags(String.join("\n", extractHashtags(title + " " + body, tag)));
    }

    private ForumPost getVisiblePost(Long postId) {
        ForumPost post = forumPostRepository.findByIdAndDeletedAtIsNull(postId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy bài viết"));
        if (Boolean.TRUE.equals(post.getHidden())) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy bài viết");
        }
        return post;
    }

    private void softDeletePost(ForumPost post) {
        post.setDeletedAt(LocalDateTime.now());
        forumPostRepository.save(post);
    }

    private void softDeleteComment(ForumComment comment) {
        comment.setDeletedAt(LocalDateTime.now());
        forumCommentRepository.save(comment);
        ForumPost post = comment.getPost();
        if (post != null) {
            post.setCommentCount(Math.max(0, (post.getCommentCount() == null ? 0 : post.getCommentCount()) - 1));
            forumPostRepository.save(post);
        }
    }

    private void requireOwnerOrAdmin(User owner, User currentUser) {
        if (currentUser == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Unauthorized");
        }
        boolean isOwner = owner != null && owner.getId() != null && owner.getId().equals(currentUser.getId());
        if (!isOwner && !authContextService.isAdmin(currentUser)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Bạn không có quyền thực hiện thao tác này");
        }
    }

    private String normalizeTag(String tag) {
        String cleanTag = tag == null ? "" : tag.trim();
        if (!ALLOWED_TAGS.contains(cleanTag)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Chủ đề không hợp lệ");
        }
        return cleanTag;
    }

    private String cleanBody(String value) {
        String cleanValue = value == null ? "" : value.trim();
        if (cleanValue.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Nội dung không được để trống");
        }
        return cleanValue;
    }

    private ForumReport.Status parseReportStatus(String status) {
        if (status == null || status.isBlank()) {
            return ForumReport.Status.PENDING;
        }
        if ("OPEN".equalsIgnoreCase(status.trim())) {
            return ForumReport.Status.PENDING;
        }
        try {
            return ForumReport.Status.valueOf(status.trim().toUpperCase());
        } catch (IllegalArgumentException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Trạng thái báo cáo không hợp lệ");
        }
    }

    private List<String> extractHashtags(String text, String tag) {
        Set<String> hashtags = new LinkedHashSet<>();
        Matcher matcher = HASHTAG_PATTERN.matcher(text == null ? "" : text);
        while (matcher.find() && hashtags.size() < 6) {
            hashtags.add(matcher.group());
        }
        if (hashtags.isEmpty()) {
            hashtags.add("#" + tag.replaceAll("\\s+", ""));
        }
        return hashtags.stream().toList();
    }

    private ForumPostDTO toPostDto(ForumPost post, Optional<User> currentUser, boolean includeComments) {
        ForumPostDTO dto = new ForumPostDTO();
        dto.setId(post.getId());
        dto.setAuthorId(post.getAuthor() == null ? null : post.getAuthor().getId());
        dto.setAuthor(post.getAuthor() == null ? "Thành viên TTJobSocial" : post.getAuthor().getName());
        dto.setRole(toVietnameseRole(post.getAuthor()));
        dto.setTitle(post.getTitle());
        dto.setBody(post.getBody());
        dto.setTag(post.getTag());
        dto.setImageUrl(post.getImageUrl());
        dto.setHashtags(parseHashtags(post.getHashtags()));
        dto.setLikes(post.getLikeCount() == null ? 0 : post.getLikeCount());
        dto.setCommentCount(post.getCommentCount() == null ? 0 : post.getCommentCount());
        dto.setHidden(Boolean.TRUE.equals(post.getHidden()));
        dto.setCreatedAt(post.getCreatedAt());
        dto.setUpdatedAt(post.getUpdatedAt());
        currentUser.ifPresent(user -> {
            dto.setLiked(forumLikeRepository.existsByPostIdAndUserId(post.getId(), user.getId()));
            dto.setEditable(canEdit(post.getAuthor(), user));
        });
        if (includeComments) {
            dto.setComments(forumCommentRepository
                    .findByPostIdAndDeletedAtIsNullAndHiddenFalseOrderByCreatedAtAsc(post.getId())
                    .stream()
                    .map(comment -> toCommentDto(comment, currentUser))
                    .toList());
        }
        return dto;
    }

    private ForumCommentDTO toCommentDto(ForumComment comment, Optional<User> currentUser) {
        ForumCommentDTO dto = new ForumCommentDTO();
        dto.setId(comment.getId());
        dto.setPostId(comment.getPost() == null ? null : comment.getPost().getId());
        dto.setAuthorId(comment.getAuthor() == null ? null : comment.getAuthor().getId());
        dto.setAuthor(comment.getAuthor() == null ? "Thành viên TTJobSocial" : comment.getAuthor().getName());
        dto.setAuthorRole(toVietnameseRole(comment.getAuthor()));
        dto.setBody(comment.getBody());
        dto.setCreatedAt(comment.getCreatedAt());
        dto.setUpdatedAt(comment.getUpdatedAt());
        currentUser.ifPresent(user -> dto.setEditable(canEdit(comment.getAuthor(), user)));
        return dto;
    }

    private ForumReportDTO toReportDto(ForumReport report) {
        ForumReportDTO dto = new ForumReportDTO();
        dto.setId(report.getId());
        Long postId = report.getPost() == null ? null : report.getPost().getId();
        if (postId == null && report.getComment() != null && report.getComment().getPost() != null) {
            postId = report.getComment().getPost().getId();
        }
        dto.setPostId(postId);
        dto.setCommentId(report.getComment() == null ? null : report.getComment().getId());
        dto.setReporterId(report.getReporter() == null ? null : report.getReporter().getId());
        dto.setReporterName(report.getReporter() == null ? "Ẩn danh" : report.getReporter().getName());
        dto.setReason(report.getReason());
        dto.setDetails(report.getDetails());
        dto.setStatus(report.getStatus().name());
        dto.setModerationAction(report.getModerationAction());
        dto.setModerationReason(report.getModerationReason());
        dto.setResolvedById(report.getResolvedBy() == null ? null : report.getResolvedBy().getId());
        dto.setResolvedByName(report.getResolvedBy() == null ? null : report.getResolvedBy().getName());
        dto.setCreatedAt(report.getCreatedAt());
        dto.setResolvedAt(report.getResolvedAt());
        return dto;
    }

    private ForumUserViolationDTO toViolationDto(ForumUserViolation violation) {
        ForumUserViolationDTO dto = new ForumUserViolationDTO();
        dto.setId(violation.getId());
        dto.setUserId(violation.getUser() == null ? null : violation.getUser().getId());
        dto.setUserName(violation.getUser() == null ? null : violation.getUser().getName());
        dto.setWarningCount(violation.getWarningCount());
        dto.setMutedUntil(violation.getMutedUntil());
        dto.setLastReason(violation.getLastReason());
        dto.setLastActionAt(violation.getLastActionAt());
        return dto;
    }

    private void requireNotMuted(User user) {
        if (user == null || authContextService.isAdmin(user)) {
            return;
        }
        forumUserViolationRepository.findByUserId(user.getId())
                .filter(violation -> violation.getMutedUntil() != null && violation.getMutedUntil().isAfter(LocalDateTime.now()))
                .ifPresent(violation -> {
                    throw new ResponseStatusException(HttpStatus.FORBIDDEN, "TÃ i khoáº£n Ä‘ang bá»‹ táº¡m khá»a khá»i diá»…n Ä‘Ã n");
                });
    }

    private boolean canEdit(User owner, User currentUser) {
        return currentUser != null
                && ((owner != null && owner.getId() != null && owner.getId().equals(currentUser.getId()))
                || authContextService.isAdmin(currentUser));
    }

    private List<String> parseHashtags(String raw) {
        if (raw == null || raw.isBlank()) {
            return List.of();
        }
        return Arrays.stream(raw.split("\\R"))
                .map(String::trim)
                .filter(value -> !value.isBlank())
                .toList();
    }

    private String toVietnameseRole(User user) {
        if (user == null || user.getRole() == null) {
            return "Thành viên TTJobSocial";
        }
        return switch (user.getRole()) {
            case ADMIN -> "Quản trị viên";
            case RECRUITER -> "Nhà tuyển dụng";
            case CANDIDATE -> "Ứng viên";
        };
    }

    private void notifyPostAuthor(ForumPost post, User actor, String title, String content) {
        if (post.getAuthor() == null || actor == null || post.getAuthor().getId().equals(actor.getId())) {
            return;
        }
        notificationService.createNotification(post.getAuthor(), title, content, "FORUM", "/community#post-" + post.getId());
    }

    private void publishForumEvent(String type, Long postId, Object payload) {
        realtimeEventPublisher.publish("/topic/forum/events", Map.of(
                "type", type,
                "postId", postId,
                "payload", payload
        ));
    }

    private int safePage(Integer page) {
        return page == null ? 0 : Math.max(page, 0);
    }

    private int safeSize(Integer size) {
        return size == null ? 30 : Math.min(Math.max(size, 1), 100);
    }
}

