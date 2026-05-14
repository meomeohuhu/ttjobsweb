package com.ttjobs.backend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ttjobs.backend.dto.forum.ForumCommentDTO;
import com.ttjobs.backend.dto.forum.ForumPostDTO;
import com.ttjobs.backend.repository.UserRepository;
import com.ttjobs.backend.service.ForumService;
import com.ttjobs.backend.service.JwtService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ForumController.class)
@AutoConfigureMockMvc(addFilters = false)
class ForumControllerApiTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ForumService forumService;

    @MockBean
    private JwtService jwtService;

    @MockBean
    private UserRepository userRepository;

    @Test
    void getPosts_shouldReturnForumFeed() throws Exception {
        ForumPostDTO post = new ForumPostDTO();
        post.setId(10L);
        post.setAuthor("Mai Anh");
        post.setRole("Ứng viên");
        post.setTitle("Hỏi về CV");
        post.setTag("CV");
        post.setLikes(3);
        post.setCommentCount(1);

        when(forumService.getPosts("CV", 0, 20)).thenReturn(List.of(post));

        mockMvc.perform(get("/api/forum/posts")
                        .param("tag", "CV")
                        .param("page", "0")
                        .param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(10))
                .andExpect(jsonPath("$[0].author").value("Mai Anh"))
                .andExpect(jsonPath("$[0].tag").value("CV"))
                .andExpect(jsonPath("$[0].likes").value(3));
    }

    @Test
    void createComment_shouldReturnCreatedComment() throws Exception {
        ForumCommentDTO comment = new ForumCommentDTO();
        comment.setId(7L);
        comment.setPostId(10L);
        comment.setAuthor("Quang Huy");
        comment.setBody("Nên thêm link demo.");

        when(forumService.createComment(eq(10L), any())).thenReturn(comment);

        mockMvc.perform(post("/api/forum/posts/10/comments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("body", "Nên thêm link demo."))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(7))
                .andExpect(jsonPath("$.postId").value(10))
                .andExpect(jsonPath("$.body").value("Nên thêm link demo."));
    }

    @Test
    void updatePost_shouldReturnUpdatedPost() throws Exception {
        ForumPostDTO post = new ForumPostDTO();
        post.setId(10L);
        post.setTitle("Tiêu đề mới");
        post.setTag("CV");

        when(forumService.updatePost(eq(10L), any())).thenReturn(post);

        mockMvc.perform(put("/api/forum/posts/10")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "title", "Tiêu đề mới",
                                "body", "Nội dung mới",
                                "tag", "CV"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(10))
                .andExpect(jsonPath("$.title").value("Tiêu đề mới"));
    }

    @Test
    void deletePost_shouldCallService() throws Exception {
        mockMvc.perform(delete("/api/forum/posts/10"))
                .andExpect(status().isOk());

        verify(forumService).deleteOwnPost(10L);
    }
}

