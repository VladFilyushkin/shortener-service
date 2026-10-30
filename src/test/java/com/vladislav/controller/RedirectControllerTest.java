package com.vladislav.controller;

import com.vladislav.entity.Link;
import com.vladislav.mapper.LinkMapper;
import com.vladislav.repository.LinkRepository;
import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static com.vladislav.util.TestData.getCreateLinkRqBody;
import static com.vladislav.util.TestData.getExpiredLinkRqBody;
import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class RedirectControllerTest extends AbstractIntegrationControllerTest {

    private static final String SAVED_SHORT_CODE = "abc12345";
    private static final String UNKNOWN_SHORT_CODE = "zzzzzzzz";

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

    @SneakyThrows
    private Link saveExpiredLink() {
        var link = linkMapper.createEntity(getExpiredLinkRqBody(), SAVED_SHORT_CODE);
        return linkRepository.save(link);
    }

    @Test
    void shouldRedirectToOriginalUrl() {
        var saved = saveLink();

        given()
                .redirects().follow(false)
                .when()
                .get("/{shortCode}", saved.getShortCode())
                .then()
                .assertThat()
                .statusCode(302)
                .header("Location", saved.getOriginalUrl());
    }

    @Test
    void shouldIncreaseClicksAfterRedirect() {
        var saved = saveLink();

        for (int i = 0; i < 3; i++) {
            given()
                    .redirects().follow(false)
                    .when()
                    .get("/{shortCode}", saved.getShortCode())
                    .then()
                    .statusCode(302);
        }

        var updated = linkRepository.findByShortCode(saved.getShortCode()).orElseThrow();
        assertEquals(3L, updated.getClicks());
    }

    @Test
    void shouldReturnNotFoundWhenShortCodeDoesNotExist() {
        given()
                .redirects().follow(false)
                .when()
                .get("/{shortCode}", UNKNOWN_SHORT_CODE)
                .then()
                .assertThat()
                .statusCode(404);
    }

    @Test
    void shouldReturnGoneWhenLinkIsExpired() {
        var saved = saveExpiredLink();

        given()
                .redirects().follow(false)
                .when()
                .get("/{shortCode}", saved.getShortCode())
                .then()
                .assertThat()
                .statusCode(410);

        var notChanged = linkRepository.findByShortCode(saved.getShortCode()).orElseThrow();
        assertThat(notChanged.getClicks()).isZero();
    }
}