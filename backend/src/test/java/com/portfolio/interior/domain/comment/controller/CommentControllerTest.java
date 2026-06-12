package com.portfolio.interior.domain.comment.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.assertj.core.api.Assertions.assertThat;
import com.portfolio.interior.domain.comment.entity.Comment;
import com.portfolio.interior.domain.comment.entity.CommentStatus;
import com.portfolio.interior.domain.comment.repository.CommentRepository;
import com.portfolio.interior.domain.post.entity.Post;
import com.portfolio.interior.domain.post.entity.PostCategory;
import com.portfolio.interior.domain.post.repository.PostRepository;
import com.portfolio.interior.domain.user.entity.User;
import com.portfolio.interior.domain.user.repository.UserRepository;
import java.util.Map;

import com.portfolio.interior.support.IntegrationTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.junit.jupiter.Testcontainers;
import java.math.BigDecimal;

@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
@Transactional
class CommentControllerTest extends IntegrationTest {

    @Autowired private UserRepository userRepository;
    @Autowired private PostRepository postRepository;
    @Autowired private CommentRepository commentRepository;

    private Long testPostId;

    @BeforeEach
    void setUp() throws Exception {
        // 1) 게시글 작성자용 유저 회원가입 + 조회
        Long authorId = signup("comment-author@example.com", "commentauthor");
        User author = userRepository.findById(authorId).orElseThrow();

        // 2) 테스트용 게시글 생성
        Post post = Post.builder()
                .user(author)
                .category(PostCategory.SELF)
                .title("댓글 테스트용 게시글")
                .content("내용")
                .regionCity("서울")
                .areaSqm(new BigDecimal("66.0"))
                .totalCost(10000000L)
                .build();
        testPostId = postRepository.save(post).getId();
    }

    @Test
    @DisplayName("인증된 사용자가 댓글을 작성하면 201과 댓글 ID를 반환한다")
    void createComment_success() throws Exception {
        String token = signupAndLogin("commenter1@example.com", "commenter1");

        Map<String, Object> request = Map.of("content", "첫 댓글입니다");

        mockMvc.perform(post("/api/posts/" + testPostId + "/comments")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isNumber());
    }

    @Test
    @DisplayName("부모 댓글에 대댓글을 작성하면 GET 조회 시 children에 포함된다")
    void createReplyComment_appearsInChildren() throws Exception {
        String token = signupAndLogin("commenter1@example.com", "commenter2");

        Map<String, Object> parentRequest = Map.of("content", "부모댓글");

        MvcResult parentResult = mockMvc.perform(post("/api/posts/" + testPostId + "/comments")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(parentRequest)))
                .andExpect(status().isCreated())
                .andReturn();


        Long parentId = objectMapper.readTree(parentResult.getResponse().getContentAsString())
                .path("data").asLong();

        Map<String, Object> replyRequest = Map.of(
                "content", "대댓글입니다",
                "parentId", parentId
        );

        mockMvc.perform(post("/api/posts/" + testPostId + "/comments")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(replyRequest)))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/posts/" + testPostId + "/comments"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].id").value(parentId))
                .andExpect(jsonPath("$.data[0].children").isArray())
                .andExpect(jsonPath("$.data[0].children[0].content").value("대댓글입니다"));
    }
    @Test
    @DisplayName("토큰 없이 댓글 작성 시 401을 반환한다")
    void createComment_withoutToken_returns401() throws Exception {
        Map<String, Object> request = Map.of("content", "토큰 없는 댓글");

        mockMvc.perform(post("/api/posts/" + testPostId + "/comments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("A002"));
    }

    @Test
    @DisplayName("작성자가 아닌 사용자가 댓글을 수정하면 403을 반환한다")
    void updateComment_notOwner_returns403() throws Exception {
        // 1) 사용자 A로 댓글 작성
        String tokenA = signupAndLogin("commentowner@example.com", "commentowner");

        Map<String, Object> createRequest = Map.of("content", "원래 댓글");

        MvcResult createResult = mockMvc.perform(post("/api/posts/" + testPostId + "/comments")
                        .header("Authorization", "Bearer " + tokenA)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated())
                .andReturn();

        Long commentId = objectMapper.readTree(createResult.getResponse().getContentAsString())
                .path("data").asLong();

        // 2) 사용자 B로 수정 시도
        String tokenB = signupAndLogin("commentintruder@example.com", "commentintruder");

        Map<String, Object> updateRequest = Map.of("content", "수정 시도");

        mockMvc.perform(put("/api/comments/" + commentId)
                        .header("Authorization", "Bearer " + tokenB)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("A005"));
    }

    @Test
    @DisplayName("작성자 본인이 댓글을 삭제하면 200을 반환하고 status가 DELETED로 변경된다")
    void deleteComment_byOwner_success() throws Exception {
        // 1) 댓글 작성
        String token = signupAndLogin("commentdeleter@example.com", "commentdeleter");

        Map<String, Object> createRequest = Map.of("content", "삭제될 댓글");

        MvcResult createResult = mockMvc.perform(post("/api/posts/" + testPostId + "/comments")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated())
                .andReturn();

        Long commentId = objectMapper.readTree(createResult.getResponse().getContentAsString())
                .path("data").asLong();

        // 2) 본인이 삭제
        mockMvc.perform(delete("/api/comments/" + commentId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        // 3) DB에서 status가 DELETED로 변경되었는지 확인
        Comment deletedComment = commentRepository.findById(commentId).orElseThrow();
        assertThat(deletedComment.getStatus()).isEqualTo(CommentStatus.DELETED);
    }
}