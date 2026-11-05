package com.vladislav.controller;

import com.vladislav.service.RateLimitService;
import io.restassured.RestAssured;
import io.zonky.test.db.AutoConfigureEmbeddedDatabase;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureEmbeddedDatabase(provider = AutoConfigureEmbeddedDatabase.DatabaseProvider.EMBEDDED,
        refresh = AutoConfigureEmbeddedDatabase.RefreshMode.BEFORE_CLASS,
        type = AutoConfigureEmbeddedDatabase.DatabaseType.POSTGRES)
@ActiveProfiles("test")
public class AbstractIntegrationControllerTest {

    @LocalServerPort
    private int port;

    @BeforeEach
    public void setup() {
        RestAssured.port = port;
    }

    @MockitoBean
    protected RateLimitService rateLimitService;
}
