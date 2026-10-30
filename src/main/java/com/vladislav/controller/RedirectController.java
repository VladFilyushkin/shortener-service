package com.vladislav.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

@RequestMapping("/{shortCode}")
public interface RedirectController {

    @Operation(summary = "Follow a short link")
    @ApiResponses({
            @ApiResponse(responseCode = "302", description = "Redirect to the original URL"),
            @ApiResponse(responseCode = "404", description = "Link not found"),
            @ApiResponse(responseCode = "410", description = "Link has expired")
    })
    @GetMapping
    ResponseEntity<Void> redirect(@PathVariable String shortCode);
}
