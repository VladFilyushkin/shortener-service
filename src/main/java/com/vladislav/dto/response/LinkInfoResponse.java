package com.vladislav.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class LinkInfoResponse implements Serializable {

    private String originalUrl;

    private String shortCode;

    private LocalDateTime expiresAt;

    private LocalDateTime createdAt;

}
