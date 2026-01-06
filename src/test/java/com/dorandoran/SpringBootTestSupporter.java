package com.dorandoran;

import com.dorandoran.domain.member.repository.MemberRepository;
import com.dorandoran.domain.member.service.EmailService;
import com.dorandoran.domain.member.service.MemberService;
import com.dorandoran.factory.MemberFactory;
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

    /**
     * repository
     */
    @MockitoBean
    protected RedisRepository redisRepository;

    @Autowired
    protected MemberRepository memberRepository;

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