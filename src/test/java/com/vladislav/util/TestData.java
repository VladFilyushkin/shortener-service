package com.vladislav.util;

import com.vladislav.dto.request.CreateLinkRequest;
import com.vladislav.dto.response.LinkInfoResponse;
import com.vladislav.dto.response.LinkStatsResponse;
import com.vladislav.entity.Link;
import org.testcontainers.shaded.com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.time.LocalDateTime;

public class TestData {

    public static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private static final String CREATE_LINK_RQ_BODY = "bodyRq/create-link.json";
    private static final String CREATE_INVALID_LINK_RQ_BODY = "bodyRq/create-link-invalid.json";
    private static final String CREATE_EXPIRED_LINK_RQ_BODY = "bodyRq/create-expired-link.json";

    public static <T> T deserialize(String path, Class<T> clazz) throws IOException {
        return OBJECT_MAPPER.readValue(TestData.class.getClassLoader().getResourceAsStream(path), clazz);
    }

    public static CreateLinkRequest getCreateLinkRqBody() throws IOException {
        return deserialize(CREATE_LINK_RQ_BODY, CreateLinkRequest.class);
    }

    public static CreateLinkRequest getInvalidLinkRqBody() throws IOException {
        return deserialize(CREATE_INVALID_LINK_RQ_BODY, CreateLinkRequest.class);
    }

    public static CreateLinkRequest getExpiredLinkRqBody() throws IOException {
        return deserialize(CREATE_EXPIRED_LINK_RQ_BODY, CreateLinkRequest.class);
    }

    public static CreateLinkRequest getCreateLinkRqForUnit() {
        return new CreateLinkRequest("https://example.com/page", null);
    }

    public static Link getLinkForUnit() {
        return Link.builder()
                .shortCode("abc12345")
                .originalUrl("https://example.com/page")
                .build();
    }

    public static Link getExpiredLinkForUnit() {
        return Link.builder()
                .shortCode("abc12345")
                .originalUrl("https://example.com/page")
                .expiresAt(LocalDateTime.now().minusDays(1))
                .build();
    }

    public static LinkInfoResponse getLinkInfoResponseForUnit() {
        return LinkInfoResponse.builder()
                .shortCode("abc12345")
                .originalUrl("https://example.com/page")
                .build();
    }

    public static LinkStatsResponse getLinkStatsResponseForUnit() {
        return LinkStatsResponse.builder()
                .clicks(5L)
                .build();
    }
}