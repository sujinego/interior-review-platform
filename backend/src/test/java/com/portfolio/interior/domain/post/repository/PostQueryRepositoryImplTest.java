package com.portfolio.interior.domain.post.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.portfolio.interior.domain.post.dto.PostSearchCondition;
import com.portfolio.interior.domain.post.dto.PostSortType;
import com.portfolio.interior.domain.post.entity.Post;
import com.portfolio.interior.domain.post.entity.PostCategory;
import com.portfolio.interior.domain.post.entity.PostStyleTag;
import com.portfolio.interior.domain.user.entity.User;
import com.portfolio.interior.domain.user.entity.UserRole;
import com.portfolio.interior.domain.user.repository.UserRepository;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@SpringBootTest
@Testcontainers
@Transactional
class PostQueryRepositoryImplTest {

    @Container
    static final MySQLContainer<?> MYSQL = new MySQLContainer<>("mysql:8.0.36")
            .withDatabaseName("interior_test")
            .withUsername("test")
            .withPassword("test");

    @DynamicPropertySource
    static void overrideProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", MYSQL::getJdbcUrl);
        registry.add("spring.datasource.username", MYSQL::getUsername);
        registry.add("spring.datasource.password", MYSQL::getPassword);
        registry.add("spring.datasource.driver-class-name", MYSQL::getDriverClassName);
    }

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private UserRepository userRepository;

    private Post mapoModern;        // SELF, 서울/마포구, 20평, 3000만원, [모던, 미니멀]
    private Post gangnamTurnkey;    // TURNKEY, 서울/강남구, 25평, 5000만원, [북유럽]
    private Post seongnamModern;    // SELF, 경기/성남시, 30평, 4000만원, [모던, 빈티지]
    private Post mapoTurnkeySmall;  // TURNKEY, 서울/마포구, 15평, 2000만원, [미니멀]
    private Post haeundaeNordic;    // SELF, 부산/해운대구, 35평, 6000만원, [북유럽, 빈티지]

    @BeforeEach
    void setUp() {
        User user = userRepository.save(User.builder()
                .email("tester@example.com")
                .password("encoded-password")
                .nickname("tester")
                .role(UserRole.USER)
                .active(true)
                .build());

        mapoModern = createPost(user, PostCategory.SELF, "서울", "마포구", 20.0, 30_000_000L,
                "마포구 모던 셀프 인테리어", "합리적인 비용으로 진행한 셀프 인테리어 후기입니다.", List.of("모던", "미니멀"));
        increaseLikeCount(mapoModern, 5);

        gangnamTurnkey = createPost(user, PostCategory.TURNKEY, "서울", "강남구", 25.0, 50_000_000L,
                "강남 턴키 북유럽 스타일", "턴키로 진행한 북유럽 감성 인테리어입니다.", List.of("북유럽"));
        increaseLikeCount(gangnamTurnkey, 10);

        seongnamModern = createPost(user, PostCategory.SELF, "경기", "성남시", 30.0, 40_000_000L,
                "성남 모던 빈티지 셀프 인테리어", "빈티지 가구로 꾸민 모던 인테리어입니다.", List.of("모던", "빈티지"));
        increaseLikeCount(seongnamModern, 3);

        mapoTurnkeySmall = createPost(user, PostCategory.TURNKEY, "서울", "마포구", 15.0, 20_000_000L,
                "마포구 소형 평수 턴키 인테리어", "작은 평수도 알차게 채운 턴키 인테리어입니다.", List.of("미니멀"));
        increaseLikeCount(mapoTurnkeySmall, 8);

        haeundaeNordic = createPost(user, PostCategory.SELF, "부산", "해운대구", 35.0, 60_000_000L,
                "해운대 북유럽 빈티지 인테리어 후기", "바다뷰가 보이는 북유럽풍 인테리어입니다.", List.of("북유럽", "빈티지"));
        increaseLikeCount(haeundaeNordic, 1);

        // 블라인드 처리된 게시글 - 모든 검색 결과에서 제외되어야 한다
        Post blinded = createPost(user, PostCategory.SELF, "서울", "마포구", 20.0, 25_000_000L,
                "블라인드 처리된 마포구 모던 게시글", "신고로 블라인드 처리된 게시글입니다.", List.of("모던"));
        blinded.blind();
        postRepository.save(blinded);
    }

    private Post createPost(User user, PostCategory category, String regionCity, String regionDistrict,
                             double areaPyeong, long totalCost, String title, String content, List<String> styleTags) {
        Post post = Post.builder()
                .user(user)
                .category(category)
                .title(title)
                .content(content)
                .regionCity(regionCity)
                .regionDistrict(regionDistrict)
                .areaSqm(BigDecimal.valueOf(areaPyeong * 3.3).setScale(2, java.math.RoundingMode.HALF_UP))
                .areaPyeong(BigDecimal.valueOf(areaPyeong).setScale(2, java.math.RoundingMode.HALF_UP))
                .totalCost(totalCost)
                .build();
        styleTags.forEach(tag -> post.addStyleTag(PostStyleTag.builder().tag(tag).build()));
        return postRepository.save(post);
    }

    private void increaseLikeCount(Post post, int count) {
        for (int i = 0; i < count; i++) {
            post.increaseLikeCount();
        }
        postRepository.save(post);
    }

    @Test
    @DisplayName("조건이 없으면 ACTIVE 상태인 게시글만 최신순으로 조회된다")
    void searchWithoutConditionReturnsOnlyActivePosts() {
        // when
        Page<Post> result = postRepository.search(PostSearchCondition.builder().build(), PageRequest.of(0, 10));

        // then
        assertThat(result.getTotalElements()).isEqualTo(5);
        assertThat(result.getContent())
                .extracting(Post::getTitle)
                .doesNotContain("블라인드 처리된 마포구 모던 게시글");
        // 최신순(LATEST) 기본 정렬 -> 가장 마지막에 저장한 게시글이 먼저 나온다
        assertThat(result.getContent().get(0).getId()).isEqualTo(haeundaeNordic.getId());
    }

    @Test
    @DisplayName("카테고리로 검색하면 해당 카테고리의 게시글만 반환된다")
    void searchByCategory() {
        PostSearchCondition condition = PostSearchCondition.builder()
                .category(PostCategory.SELF)
                .build();

        Page<Post> result = postRepository.search(condition, PageRequest.of(0, 10));

        assertThat(result.getTotalElements()).isEqualTo(3);
        assertThat(result.getContent())
                .extracting(Post::getId)
                .containsExactlyInAnyOrder(mapoModern.getId(), seongnamModern.getId(), haeundaeNordic.getId());
    }

    @Test
    @DisplayName("시/구 단위 지역으로 검색할 수 있다")
    void searchByRegion() {
        PostSearchCondition condition = PostSearchCondition.builder()
                .regionCity("서울")
                .regionDistrict("마포구")
                .build();

        Page<Post> result = postRepository.search(condition, PageRequest.of(0, 10));

        assertThat(result.getTotalElements()).isEqualTo(2);
        assertThat(result.getContent())
                .extracting(Post::getId)
                .containsExactlyInAnyOrder(mapoModern.getId(), mapoTurnkeySmall.getId());
    }

    @Test
    @DisplayName("평수 범위로 검색할 수 있다")
    void searchByAreaPyeongRange() {
        PostSearchCondition condition = PostSearchCondition.builder()
                .areaPyeongMin(20.0)
                .areaPyeongMax(30.0)
                .build();

        Page<Post> result = postRepository.search(condition, PageRequest.of(0, 10));

        assertThat(result.getTotalElements()).isEqualTo(3);
        assertThat(result.getContent())
                .extracting(Post::getId)
                .containsExactlyInAnyOrder(mapoModern.getId(), gangnamTurnkey.getId(), seongnamModern.getId());
    }

    @Test
    @DisplayName("총 비용 범위로 검색할 수 있다")
    void searchByTotalCostRange() {
        PostSearchCondition condition = PostSearchCondition.builder()
                .totalCostMin(30_000_000L)
                .totalCostMax(50_000_000L)
                .build();

        Page<Post> result = postRepository.search(condition, PageRequest.of(0, 10));

        assertThat(result.getTotalElements()).isEqualTo(3);
        assertThat(result.getContent())
                .extracting(Post::getId)
                .containsExactlyInAnyOrder(mapoModern.getId(), gangnamTurnkey.getId(), seongnamModern.getId());
    }

    @Test
    @DisplayName("스타일 태그는 하나라도 일치하면 포함되는 OR 조건으로 검색된다")
    void searchByStyleTagsIsOrCondition() {
        PostSearchCondition condition = PostSearchCondition.builder()
                .styleTags(List.of("모던", "북유럽"))
                .build();

        Page<Post> result = postRepository.search(condition, PageRequest.of(0, 10));

        assertThat(result.getTotalElements()).isEqualTo(4);
        assertThat(result.getContent())
                .extracting(Post::getId)
                .containsExactlyInAnyOrder(
                        mapoModern.getId(), gangnamTurnkey.getId(), seongnamModern.getId(), haeundaeNordic.getId());
    }

    @Test
    @DisplayName("키워드는 제목과 본문을 모두 검색한다")
    void searchByKeywordMatchesTitleAndContent() {
        // 제목에 포함된 키워드
        Page<Post> titleHit = postRepository.search(
                PostSearchCondition.builder().keyword("북유럽").build(), PageRequest.of(0, 10));
        assertThat(titleHit.getTotalElements()).isEqualTo(2);
        assertThat(titleHit.getContent())
                .extracting(Post::getId)
                .containsExactlyInAnyOrder(gangnamTurnkey.getId(), haeundaeNordic.getId());

        // 본문에만 포함된 키워드
        Page<Post> contentHit = postRepository.search(
                PostSearchCondition.builder().keyword("가구").build(), PageRequest.of(0, 10));
        assertThat(contentHit.getTotalElements()).isEqualTo(1);
        assertThat(contentHit.getContent().get(0).getId()).isEqualTo(seongnamModern.getId());
    }

    @Test
    @DisplayName("LIKES 정렬은 좋아요 수가 많은 순서로 정렬된다")
    void sortByLikes() {
        PostSearchCondition condition = PostSearchCondition.builder()
                .sort(PostSortType.LIKES)
                .build();

        Page<Post> result = postRepository.search(condition, PageRequest.of(0, 10));

        assertThat(result.getContent())
                .extracting(Post::getId)
                .containsExactly(
                        gangnamTurnkey.getId(),   // like 10
                        mapoTurnkeySmall.getId(), // like 8
                        mapoModern.getId(),       // like 5
                        seongnamModern.getId(),   // like 3
                        haeundaeNordic.getId());  // like 1
    }

    @Test
    @DisplayName("COST_ASC / COST_DESC 정렬은 총 비용 기준으로 정렬된다")
    void sortByCost() {
        Page<Post> ascResult = postRepository.search(
                PostSearchCondition.builder().sort(PostSortType.COST_ASC).build(), PageRequest.of(0, 10));
        assertThat(ascResult.getContent())
                .extracting(Post::getId)
                .containsExactly(
                        mapoTurnkeySmall.getId(), // 2000만
                        mapoModern.getId(),       // 3000만
                        seongnamModern.getId(),   // 4000만
                        gangnamTurnkey.getId(),   // 5000만
                        haeundaeNordic.getId());  // 6000만

        Page<Post> descResult = postRepository.search(
                PostSearchCondition.builder().sort(PostSortType.COST_DESC).build(), PageRequest.of(0, 10));
        assertThat(descResult.getContent())
                .extracting(Post::getId)
                .containsExactly(
                        haeundaeNordic.getId(),
                        gangnamTurnkey.getId(),
                        seongnamModern.getId(),
                        mapoModern.getId(),
                        mapoTurnkeySmall.getId());
    }

    @Test
    @DisplayName("카테고리, 평수, 스타일 태그 조건을 함께 적용하면 모두 만족하는 게시글만 조회된다")
    void searchWithMultipleConditions() {
        PostSearchCondition condition = PostSearchCondition.builder()
                .category(PostCategory.SELF)
                .areaPyeongMin(25.0)
                .styleTags(List.of("빈티지"))
                .build();

        Page<Post> result = postRepository.search(condition, PageRequest.of(0, 10));

        assertThat(result.getTotalElements()).isEqualTo(2);
        assertThat(result.getContent())
                .extracting(Post::getId)
                .containsExactlyInAnyOrder(seongnamModern.getId(), haeundaeNordic.getId());
    }

    @Test
    @DisplayName("페이징 정보가 올바르게 반영된다")
    void searchWithPaging() {
        PostSearchCondition condition = PostSearchCondition.builder().build();

        Page<Post> firstPage = postRepository.search(condition, PageRequest.of(0, 2));
        assertThat(firstPage.getContent()).hasSize(2);
        assertThat(firstPage.getTotalElements()).isEqualTo(5);
        assertThat(firstPage.getTotalPages()).isEqualTo(3);
        assertThat(firstPage.isFirst()).isTrue();
        assertThat(firstPage.isLast()).isFalse();

        Page<Post> lastPage = postRepository.search(condition, PageRequest.of(2, 2));
        assertThat(lastPage.getContent()).hasSize(1);
        assertThat(lastPage.isLast()).isTrue();
    }
}
