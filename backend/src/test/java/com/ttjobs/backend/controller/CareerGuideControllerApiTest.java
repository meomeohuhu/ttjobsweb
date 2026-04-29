package com.ttjobs.backend.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class CareerGuideControllerApiTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void getPublishedArticles_shouldReturnSeededArticles() throws Exception {
        mockMvc.perform(get("/api/career-guides"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(greaterThanOrEqualTo(4)))
                .andExpect(jsonPath("$[0].slug").exists());
    }

    @Test
    void getArticleBySlug_shouldReturnDetail() throws Exception {
        mockMvc.perform(get("/api/career-guides/cv-thuyet-phuc-nha-tuyen-dung"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.slug").value("cv-thuyet-phuc-nha-tuyen-dung"))
                .andExpect(jsonPath("$.title").value("Cách viết CV thuyết phục nhà tuyển dụng"));
    }
}
