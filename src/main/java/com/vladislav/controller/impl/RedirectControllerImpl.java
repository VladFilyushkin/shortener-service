package com.vladislav.controller.impl;

import com.vladislav.controller.RedirectController;
import com.vladislav.service.LinkService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;

@RestController
@RequiredArgsConstructor
public class RedirectControllerImpl implements RedirectController {

    private final LinkService linkService;

    @Override
    public ResponseEntity<Void> redirect(String shortCode) {
        String path = linkService.redirect(shortCode);
        return ResponseEntity
                .status(HttpStatus.FOUND)
                .location(URI.create(path))
                .build();
    }
}
