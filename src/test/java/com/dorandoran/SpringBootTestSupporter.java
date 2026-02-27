package com.dorandoran;

import com.dorandoran.domain.comment.repository.CommentRepository;
import com.dorandoran.domain.comment.service.CommentService;
import com.dorandoran.domain.member.repository.MemberRepository;
import com.dorandoran.domain.member.service.EmailService;
import com.dorandoran.domain.member.service.MemberService;
import com.dorandoran.domain.message.repository.MessageRepository;
import com.dorandoran.domain.message.service.MessageService;
import com.dorandoran.domain.post.repository.PostLikeRepository;
import com.dorandoran.domain.post.repository.PostMediaRepository;
import com.dorandoran.domain.post.repository.PostRepository;
import com.dorandoran.domain.post.service.PostService;
import com.dorandoran.factory.*;
import com.dorandoran.global.jwt.JWTUtil;
import com.dorandoran.global.jwt.JwtProperties;
import com.dorandoran.global.redis.RedisRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
public abstract class SpringBootTestSupporter {

    /**
     * Test data factory
     */
    @Autowired
    public MemberFactory memberFactory;

    @Autowired
    public CategoryFactory categoryFactory;

    @Autowired
    public CategoryGroupFactory categoryGroupFactory;

    @Autowired
    public PostFactory postFactory;

    @Autowired
    public CommentFactory commentFactory;

    @Autowired
    public MessageFactory messageFactory;

    /**
     * mock Mvc
     */
    @Autowired
    public MockMvc mockMvc;

    /**
     * service
     */
    @MockitoBean
    protected EmailService emailService;

    @Autowired
    protected MemberService memberService;

    @Autowired
    protected PostService postService;

    @Autowired
    protected CommentService commentService;

    @Autowired
    protected MessageService messageService;

    /**
     * repository
     */
    @MockitoBean
    protected RedisRepository redisRepository;

    @Autowired
    protected MemberRepository memberRepository;

    @Autowired
    protected PostRepository postRepository;

    @Autowired
    protected PostMediaRepository postMediaRepository;

    @Autowired
    protected PostLikeRepository postLikeRepository;

    @Autowired
    protected CommentRepository commentRepository;

    @Autowired
    protected MessageRepository messageRepository;

    /**
     * Common
     */
    @Autowired
    protected EntityManager em;

    @Autowired
    protected PasswordEncoder passwordEncoder;

    @Autowired
    protected ObjectMapper objectMapper;

    @Autowired
    protected JWTUtil jwtUtil;

    @Autowired
    protected JwtProperties jwtProperties;
}