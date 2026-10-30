package com.vladislav.controller;

import com.vladislav.dto.response.LinkInfoResponse;
import com.vladislav.dto.response.LinkResponse;
import com.vladislav.dto.response.LinkStatsResponse;
import com.vladislav.entity.Link;
import com.vladislav.mapper.LinkMapper;
import com.vladislav.repository.LinkRepository;
import io.restassured.http.ContentType;
import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static com.vladislav.util.TestData.getCreateLinkRqBody;
import static com.vladislav.util.TestData.getInvalidLinkRqBody;
import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class LinkControllerTest extends AbstractIntegrationControllerTest {

    private static final String SAVED_SHORT_CODE = "abc12345";
    private static final String UNKNOWN_SHORT_CODE = "zzzzzzzz";
    private static final String NOT_FOUND_MESSAGE = "Link with shortcode %s is not found";

    @Autowired
    private LinkMapper linkMapper;

    @Autowired
    private LinkRepository linkRepository;

    @BeforeEach
    void beforeEach() {
        linkRepository.deleteAll();
    }

    @SneakyThrows
    private Link saveLink() {
        var link = linkMapper.createEntity(getCreateLinkRqBody(), SAVED_SHORT_CODE);
        return linkRepository.save(link);
    }

    @Test
    @SneakyThrows
    void shouldCreateLinkSuccessfully() {
        var request = getCreateLinkRqBody();
        var response = given()
                .contentType(ContentType.JSON)
                .body(request)
                .when()
                .post("/api/links")
                .then()
                .assertThat()
                .statusCode(201)
                .extract()
                .as(LinkResponse.class);

        assertThat(response.getShortCode()).hasSize(8);
        assertThat(response.getShortUrl()).endsWith("/" + response.getShortCode());

        var saved = linkRepository.findByShortCode(response.getShortCode());
        assertThat(saved).isPresent();
        assertEquals(request.getOriginalUrl(), saved.get().getOriginalUrl());
        assertEquals(0L, saved.get().getClicks());
        assertThat(saved.get().getCreatedAt()).isNotNull();
    }

    @Test
    @SneakyThrows
    void shouldReturnBadRequestWhenUrlIsInvalid() {
        var errors = given()
                .contentType(ContentType.JSON)
                .body(getInvalidLinkRqBody())
                .when()
                .post("/api/links")
                .then()
                .assertThat()
                .statusCode(400)
                .extract()
                .jsonPath();

        assertThat(errors.getString("originalUrl")).isNotBlank();
        assertThat(linkRepository.count()).isZero();
    }

    @Test
    void shouldFindLinkInfoSuccessfully() {
        var saved = saveLink();

        var response = given()
                .when()
                .get("/api/links/{shortCode}", saved.getShortCode())
                .then()
                .assertThat()
                .statusCode(200)
                .extract()
                .as(LinkInfoResponse.class);

        assertEquals(saved.getShortCode(), response.getShortCode());
        assertEquals(saved.getOriginalUrl(), response.getOriginalUrl());
        assertThat(response.getCreatedAt()).isNotNull();
    }

    @Test
    void shouldReturnNotFoundWhenLinkInfoDoesNotExist() {
        var message = given()
                .when()
                .get("/api/links/{shortCode}", UNKNOWN_SHORT_CODE)
                .then()
                .assertThat()
                .statusCode(404)
                .extract()
                .asString();

        assertEquals(String.format(NOT_FOUND_MESSAGE, UNKNOWN_SHORT_CODE), message);
    }

    @Test
    void shouldReturnStatsSuccessfully() {
        var saved = saveLink();
        saved.setClicks(5L);
        linkRepository.save(saved);

        var response = given()
                .when()
                .get("/api/links/{shortCode}/stats", saved.getShortCode())
                .then()
                .assertThat()
                .statusCode(200)
                .extract()
                .as(LinkStatsResponse.class);

        assertEquals(5L, response.getClicks());
    }

    @Test
    void shouldReturnNotFoundWhenStatsLinkDoesNotExist() {
        var message = given()
                .when()
                .get("/api/links/{shortCode}/stats", UNKNOWN_SHORT_CODE)
                .then()
                .assertThat()
                .statusCode(404)
                .extract()
                .asString();

        assertEquals(String.format(NOT_FOUND_MESSAGE, UNKNOWN_SHORT_CODE), message);
    }

    @Test
    void shouldDeleteLinkSuccessfully() {
        var saved = saveLink();

        given()
                .when()
                .delete("/api/links/{shortCode}", saved.getShortCode())
                .then()
                .assertThat()
                .statusCode(204);

        assertThat(linkRepository.findByShortCode(saved.getShortCode())).isEmpty();
    }

    @Test
    void shouldReturnNotFoundWhenDeletingLinkDoesNotExist() {
        var message = given()
                .when()
                .delete("/api/links/{shortCode}", UNKNOWN_SHORT_CODE)
                .then()
                .assertThat()
                .statusCode(404)
                .extract()
                .asString();

        assertEquals(String.format(NOT_FOUND_MESSAGE, UNKNOWN_SHORT_CODE), message);
    }
}