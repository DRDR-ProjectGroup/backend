CREATE TABLE category_group (
    id BIGINT NOT NULL AUTO_INCREMENT,
    created_at DATETIME(6) NULL,
    modified_at DATETIME(6) NULL,
    name VARCHAR(255) NOT NULL,
    deleted_at DATETIME(6) NULL,
    PRIMARY KEY (id),
    CONSTRAINT uk_category_group_name UNIQUE (name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE member (
    id BIGINT NOT NULL AUTO_INCREMENT,
    created_at DATETIME(6) NULL,
    modified_at DATETIME(6) NULL,
    username VARCHAR(255) NOT NULL,
    password VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL,
    nickname VARCHAR(255) NOT NULL,
    role VARCHAR(255) NOT NULL,
    status VARCHAR(255) NOT NULL,
    deleted_at DATETIME(6) NULL,
    PRIMARY KEY (id),
    CONSTRAINT uk_member_username UNIQUE (username),
    CONSTRAINT uk_member_email UNIQUE (email),
    CONSTRAINT uk_member_nickname UNIQUE (nickname)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE category (
    id BIGINT NOT NULL AUTO_INCREMENT,
    created_at DATETIME(6) NULL,
    modified_at DATETIME(6) NULL,
    group_id BIGINT NOT NULL,
    name VARCHAR(255) NOT NULL,
    address VARCHAR(255) NOT NULL,
    deleted_at DATETIME(6) NULL,
    PRIMARY KEY (id),
    CONSTRAINT uk_category_name UNIQUE (name),
    CONSTRAINT fk_category_group FOREIGN KEY (group_id) REFERENCES category_group (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE post (
    id BIGINT NOT NULL AUTO_INCREMENT,
    created_at DATETIME(6) NULL,
    modified_at DATETIME(6) NULL,
    version BIGINT NULL,
    member_id BIGINT NULL,
    category_id BIGINT NULL,
    title VARCHAR(255) NOT NULL,
    content TEXT NOT NULL,
    view_count INT NOT NULL,
    like_count INT NOT NULL,
    is_notice BIT NOT NULL,
    deleted_at DATETIME(6) NULL,
    popular_at DATETIME(6) NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_post_member FOREIGN KEY (member_id) REFERENCES member (id),
    CONSTRAINT fk_post_category FOREIGN KEY (category_id) REFERENCES category (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE comment (
    id BIGINT NOT NULL AUTO_INCREMENT,
    created_at DATETIME(6) NULL,
    modified_at DATETIME(6) NULL,
    post_id BIGINT NOT NULL,
    member_id BIGINT NOT NULL,
    parent_comment_id BIGINT NULL,
    depth INT NOT NULL,
    content VARCHAR(255) NOT NULL,
    deleted_at DATETIME(6) NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_comment_post FOREIGN KEY (post_id) REFERENCES post (id),
    CONSTRAINT fk_comment_member FOREIGN KEY (member_id) REFERENCES member (id),
    CONSTRAINT fk_comment_parent FOREIGN KEY (parent_comment_id) REFERENCES comment (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE message (
    id BIGINT NOT NULL AUTO_INCREMENT,
    created_at DATETIME(6) NULL,
    modified_at DATETIME(6) NULL,
    sender_id BIGINT NOT NULL,
    receiver_id BIGINT NOT NULL,
    content VARCHAR(255) NOT NULL,
    sender_deleted_at DATETIME(6) NULL,
    receiver_deleted_at DATETIME(6) NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_message_sender FOREIGN KEY (sender_id) REFERENCES member (id),
    CONSTRAINT fk_message_receiver FOREIGN KEY (receiver_id) REFERENCES member (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE post_like (
    id BIGINT NOT NULL AUTO_INCREMENT,
    created_at DATETIME(6) NULL,
    modified_at DATETIME(6) NULL,
    member_id BIGINT NULL,
    post_id BIGINT NULL,
    like_type VARCHAR(255) NULL,
    PRIMARY KEY (id),
    CONSTRAINT uk_member_post_like UNIQUE (member_id, post_id),
    CONSTRAINT fk_post_like_member FOREIGN KEY (member_id) REFERENCES member (id),
    CONSTRAINT fk_post_like_post FOREIGN KEY (post_id) REFERENCES post (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE post_media (
    id BIGINT NOT NULL AUTO_INCREMENT,
    post_id BIGINT NOT NULL,
    media_type VARCHAR(255) NOT NULL,
    original_name VARCHAR(255) NOT NULL,
    stored_name VARCHAR(255) NOT NULL,
    object_key VARCHAR(255) NOT NULL,
    size BIGINT NOT NULL,
    sort_order INT NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_post_media_post FOREIGN KEY (post_id) REFERENCES post (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE post_revision (
    id BIGINT NOT NULL AUTO_INCREMENT,
    created_at DATETIME(6) NULL,
    modified_at DATETIME(6) NULL,
    version BIGINT NULL,
    original_post_id BIGINT NOT NULL,
    member_id BIGINT NOT NULL,
    category_id BIGINT NOT NULL,
    title VARCHAR(255) NOT NULL,
    content TEXT NOT NULL,
    view_count INT NOT NULL,
    like_count INT NOT NULL,
    is_notice BIT NOT NULL,
    deleted_at DATETIME(6) NULL,
    popular_at DATETIME(6) NULL,
    post_created_at DATETIME(6) NULL,
    post_modified_at DATETIME(6) NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_post_revision_member FOREIGN KEY (member_id) REFERENCES member (id),
    CONSTRAINT fk_post_revision_category FOREIGN KEY (category_id) REFERENCES category (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_post_revision_original_post_id ON post_revision (original_post_id);

CREATE TABLE post_revision_media (
    id BIGINT NOT NULL AUTO_INCREMENT,
    post_revision_id BIGINT NOT NULL,
    media_type VARCHAR(255) NOT NULL,
    original_name VARCHAR(255) NOT NULL,
    stored_name VARCHAR(255) NOT NULL,
    object_key VARCHAR(255) NOT NULL,
    size BIGINT NOT NULL,
    sort_order INT NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_post_revision_media_post_revision FOREIGN KEY (post_revision_id) REFERENCES post_revision (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;