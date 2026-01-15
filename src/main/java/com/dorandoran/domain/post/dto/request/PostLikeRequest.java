package com.dorandoran.domain.post.dto.request;

import com.dorandoran.domain.post.type.LikeType;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class PostLikeRequest {
    @NotNull
    private LikeType likeType;
}
