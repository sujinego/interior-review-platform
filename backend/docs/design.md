# 인테리어 비용공개 커뮤니티 - 백엔드 설계 문서

## 프로젝트 개요
- 목표: 4년차 백엔드 개발자 포트폴리오용 프로젝트
- 기존 Next.js + Supabase 프로젝트(schema.sql)를 **참고용**으로만 사용
- 코드는 전부 새로 작성 (마이그레이션 아님)

## 기술 스택
- Spring Boot 3.x + Java 17
- Spring Data JPA + QueryDSL (동적 검색)
- Spring Security + JWT
- Spring Batch (인기글 집계)
- Redis (캐싱)
- MySQL (RDS)
- JUnit5 + Mockito + Testcontainers (통합테스트)
- Swagger(OpenAPI)

## 배포 구조
```
EC2 1대
├── Nginx
│   ├── /     → React 정적파일
│   └── /api  → Spring Boot(8080) 프록시
├── Spring Boot (Docker)
├── MySQL (RDS 또는 컨테이너)
└── Redis (컨테이너)
```

---

## 1. MySQL ERD (1차 범위)

```
users
├── id              BIGINT PK AUTO_INCREMENT
├── email           VARCHAR(255) UNIQUE NOT NULL
├── password        VARCHAR(255) NOT NULL          ← BCrypt 암호화
├── nickname        VARCHAR(20) UNIQUE NOT NULL
├── avatar_url      VARCHAR(500)
├── role            VARCHAR(20) DEFAULT 'USER'      (USER, BUSINESS, ADMIN)
├── is_active       BOOLEAN DEFAULT TRUE
└── created_at      DATETIME

posts
├── id                  BIGINT PK AUTO_INCREMENT
├── user_id             BIGINT FK → users.id
├── category            VARCHAR(20)   (SELF, TURNKEY)
├── title               VARCHAR(200) NOT NULL
├── content             TEXT NOT NULL
├── region_city         VARCHAR(50) NOT NULL
├── region_district     VARCHAR(50)
├── area_sqm            DECIMAL(8,2) NOT NULL
├── area_pyeong         DECIMAL(8,2)
├── construction_days   INT
├── completion_year     SMALLINT
├── company_name        VARCHAR(100)
├── is_company_public   BOOLEAN DEFAULT TRUE
├── total_cost          BIGINT NOT NULL
├── cost_per_pyeong     BIGINT
├── is_ad               BOOLEAN DEFAULT FALSE
├── status              VARCHAR(20) DEFAULT 'ACTIVE' (ACTIVE, BLINDED, DELETED)
├── view_count          INT DEFAULT 0
├── like_count          INT DEFAULT 0
├── bookmark_count      INT DEFAULT 0
├── version             BIGINT DEFAULT 0   ← 낙관적 락 (좋아요/북마크 동시성)
├── created_at          DATETIME
└── updated_at          DATETIME

  인덱스: (status), (category), (region_city), (created_at DESC)

post_style_tags  ← PostgreSQL TEXT[] 정규화
├── id        BIGINT PK AUTO_INCREMENT
├── post_id   BIGINT FK → posts.id
└── tag       VARCHAR(30)

  인덱스: (post_id, tag)

cost_items
├── id        BIGINT PK AUTO_INCREMENT
├── post_id   BIGINT FK → posts.id
├── category  VARCHAR(30)   (demolition, floor, wallpaper, bathroom, kitchen, lighting, furniture, electrical, window, other)
├── label     VARCHAR(100)
├── amount    BIGINT NOT NULL
└── memo      VARCHAR(500)

post_images
├── id          BIGINT PK AUTO_INCREMENT
├── post_id     BIGINT FK → posts.id
├── url         VARCHAR(500) NOT NULL
├── is_cover    BOOLEAN DEFAULT FALSE
├── sort_order  SMALLINT DEFAULT 0
└── created_at  DATETIME

comments
├── id          BIGINT PK AUTO_INCREMENT
├── post_id     BIGINT FK → posts.id
├── user_id     BIGINT FK → users.id
├── parent_id   BIGINT FK → comments.id (NULL 가능, 대댓글)
├── content     VARCHAR(2000) NOT NULL
├── status      VARCHAR(20) DEFAULT 'ACTIVE' (ACTIVE, BLINDED, DELETED)
└── created_at  DATETIME

likes
├── user_id     BIGINT FK → users.id
├── post_id     BIGINT FK → posts.id
├── created_at  DATETIME
└── PK (user_id, post_id)

bookmarks  ← likes와 동일 구조
├── user_id, post_id, created_at
└── PK (user_id, post_id)

reports
├── id            BIGINT PK AUTO_INCREMENT
├── reporter_id   BIGINT FK → users.id
├── target_type   VARCHAR(20)  (POST, COMMENT)
├── target_id     BIGINT
├── reason        VARCHAR(30)  (FALSE_INFO, DEFAMATION, AD_NOT_DISCLOSED, PRIVACY, COPYRIGHT, SPAM, OTHER)
├── detail        VARCHAR(1000)
├── status        VARCHAR(20) DEFAULT 'PENDING' (PENDING, REVIEWED, DISMISSED, ACTIONED)
├── admin_note    VARCHAR(1000)
├── created_at    DATETIME
└── reviewed_at   DATETIME

  인덱스: (status), (target_type, target_id)
```

---

## 2. Batch용 테이블 (2차)

```
daily_popular_posts
├── id               BIGINT PK AUTO_INCREMENT
├── post_id          BIGINT FK → posts.id
├── rank_no          INT
├── like_count       INT
├── comment_count    INT
└── aggregated_date  DATE

batch_job_history
├── id               BIGINT PK AUTO_INCREMENT
├── job_name         VARCHAR(100)
├── status           VARCHAR(20)  (SUCCESS, FAILED, RUNNING)
├── processed_count  INT
├── started_at       DATETIME
├── finished_at      DATETIME
└── error_message    VARCHAR(1000)
```

---

## 3. JPA Entity 관계도

```
User (1) ─── (N) Post           : Post.user (ManyToOne)
User (1) ─── (N) Comment        : Comment.user (ManyToOne)

Post (1) ─── (N) CostItem        : CostItem.post (ManyToOne, cascade=ALL, orphanRemoval=true)
Post (1) ─── (N) PostImage       : PostImage.post (ManyToOne, cascade=ALL, orphanRemoval=true)
Post (1) ─── (N) PostStyleTag    : PostStyleTag.post (ManyToOne, cascade=ALL, orphanRemoval=true)
Post (1) ─── (N) Comment         : Comment.post (ManyToOne)

Comment (self-ref) : parentId(Long)만 컬럼으로 보유 → 양방향 매핑 없음
                     (대댓글 조회는 QueryDSL로 parentId 그룹핑)

User ─(N:M via Like)─ Post     → Like Entity (복합키: @EmbeddedId UserPostKey)
User ─(N:M via Bookmark)─ Post → Bookmark Entity (동일 패턴)

Report : target_type + target_id (polymorphic, FK 없음 — 코드에서 검증)
```

설계 포인트:
- `CostItem`, `PostImage`, `PostStyleTag`는 Post 생성/수정 시 `cascade=ALL, orphanRemoval=true`로 일괄 관리
- `Like`/`Bookmark`는 복합키 Entity로 N:M 관계 단순 매핑 (조회 시 `existsBy...`로 토글 여부 판단)
- `Post.version`은 `@Version`으로 좋아요/북마크 카운트 동시성 제어

---

## 4. 패키지 구조 (도메인형)

```
com.portfolio.interior
├── InteriorApplication.java
│
├── global
│   ├── config
│   │   ├── SecurityConfig.java
│   │   ├── QuerydslConfig.java
│   │   ├── RedisConfig.java
│   │   ├── SwaggerConfig.java
│   │   └── AsyncConfig.java
│   ├── security
│   │   ├── JwtTokenProvider.java
│   │   ├── JwtAuthenticationFilter.java
│   │   └── CustomUserDetails.java
│   ├── exception
│   │   ├── GlobalExceptionHandler.java
│   │   └── CustomException.java (+ ErrorCode enum)
│   └── common
│       ├── BaseTimeEntity.java   (createdAt/updatedAt @MappedSuperclass)
│       └── ApiResponse.java
│
├── domain
│   ├── user
│   │   ├── entity/User.java
│   │   ├── repository/UserRepository.java
│   │   ├── service/AuthService.java, UserService.java
│   │   ├── controller/AuthController.java
│   │   └── dto/SignupRequest.java, LoginRequest.java, TokenResponse.java
│   │
│   ├── post
│   │   ├── entity/Post.java, CostItem.java, PostImage.java, PostStyleTag.java
│   │   ├── repository/
│   │   │   ├── PostRepository.java          (JpaRepository)
│   │   │   ├── PostQueryRepository.java     (interface)
│   │   │   └── PostQueryRepositoryImpl.java (QueryDSL 구현)
│   │   ├── service/PostService.java
│   │   ├── controller/PostController.java
│   │   └── dto/
│   │       ├── PostSearchCondition.java  (동적검색 파라미터)
│   │       ├── PostCreateRequest.java
│   │       └── PostResponse.java, PostDetailResponse.java
│   │
│   ├── comment/  (entity, repository, service, controller, dto)
│   ├── like/     (entity, repository, service, controller)
│   ├── bookmark/ (entity, repository, service, controller)
│   ├── report/
│   │   ├── entity/Report.java
│   │   ├── event/ReportCreatedEvent.java
│   │   ├── listener/ReportEventListener.java  (@Async)
│   │   └── ...
│   │
│   └── batch
│       ├── entity/DailyPopularPost.java, BatchJobHistory.java
│       ├── config/PopularPostBatchConfig.java
│       └── controller/BatchAdminController.java
│
└── infra
    └── s3/S3Uploader.java
```

---

## 5. build.gradle 의존성

```gradle
dependencies {
    // Web / Validation
    implementation 'org.springframework.boot:spring-boot-starter-web'
    implementation 'org.springframework.boot:spring-boot-starter-validation'

    // JPA / DB
    implementation 'org.springframework.boot:spring-boot-starter-data-jpa'
    runtimeOnly 'com.mysql:mysql-connector-j'

    // QueryDSL
    implementation 'com.querydsl:querydsl-jpa:5.0.0:jakarta'
    annotationProcessor 'com.querydsl:querydsl-apt:5.0.0:jakarta'
    annotationProcessor 'jakarta.annotation:jakarta.annotation-api'
    annotationProcessor 'jakarta.persistence:jakarta.persistence-api'

    // Security / JWT
    implementation 'org.springframework.boot:spring-boot-starter-security'
    implementation 'io.jsonwebtoken:jjwt-api:0.11.5'
    runtimeOnly 'io.jsonwebtoken:jjwt-impl:0.11.5'
    runtimeOnly 'io.jsonwebtoken:jjwt-jackson:0.11.5'

    // Redis
    implementation 'org.springframework.boot:spring-boot-starter-data-redis'

    // Batch
    implementation 'org.springframework.boot:spring-boot-starter-batch'

    // S3
    implementation 'software.amazon.awssdk:s3:2.25.0'

    // Swagger
    implementation 'org.springdoc:springdoc-openapi-starter-webmvc-ui:2.5.0'

    // Lombok
    compileOnly 'org.projectlombok:lombok'
    annotationProcessor 'org.projectlombok:lombok'

    // Test
    testImplementation 'org.springframework.boot:spring-boot-starter-test'
    testImplementation 'org.springframework.security:spring-security-test'
    testImplementation 'org.springframework.batch:spring-batch-test'
    testImplementation 'org.testcontainers:junit-jupiter:1.19.7'
    testImplementation 'org.testcontainers:mysql:1.19.7'
    testImplementation 'com.h2database:h2'  // 빠른 단위테스트용 (선택)
}
```

---

## 6. 동적 검색 API 설계

### `GET /api/posts/search`

| 파라미터 | 타입 | 설명 |
|---|---|---|
| `category` | String (optional) | SELF / TURNKEY |
| `regionCity` | String (optional) | 서울, 경기 등 |
| `regionDistrict` | String (optional) | 마포구 등 |
| `areaPyeongMin` / `areaPyeongMax` | Double (optional) | 평수 범위 |
| `totalCostMin` / `totalCostMax` | Long (optional) | 비용 범위 |
| `styleTags` | List<String> (optional) | 모던, 북유럽 등 (OR 조건) |
| `keyword` | String (optional) | 제목+내용 LIKE 검색 |
| `sort` | Enum | LATEST(기본) / LIKES / COST_ASC / COST_DESC |
| `page`, `size` | Int | 페이징 |

QueryDSL 구현 포인트:
- `BooleanBuilder`로 각 조건을 null 체크 후 동적 결합
- `status = 'ACTIVE'`는 항상 고정 조건 (블라인드/삭제 게시글 제외)
- `styleTags` 검색은 `PostStyleTag`와 서브쿼리 또는 `exists` 활용
- `Sort`는 `OrderSpecifier`로 분기 처리
- `Page<PostResponse>` 반환 (count 쿼리 분리로 성능 고려)

---

## 진행 단계 (Claude Code 프롬프트 순서)

1. 설계 (완료, 이 문서)
2. Post 도메인 + QueryDSL 동적검색 (+ Testcontainers 테스트)
3. JWT 인증 (Spring Security)
4. 댓글 / 좋아요 / 북마크
5. 신고 → 자동 블라인드 (비동기 이벤트)
6. Redis 캐싱
7. Spring Batch (인기글 TOP10 집계)
8. 이미지 업로드 (S3)
9. Swagger API 문서
10. 배포 (Docker + Nginx + GitHub Actions)

각 단계는 "design.md 기준으로 N단계 진행해줘" 형태로 요청.
