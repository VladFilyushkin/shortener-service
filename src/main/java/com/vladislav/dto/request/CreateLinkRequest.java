package com.vladislav.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.URL;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CreateLinkRequest {

    @NotBlank(message = "Original url can't be empty")
    @URL
    private String originalUrl;

    private LocalDateTime expiresAt;
}
