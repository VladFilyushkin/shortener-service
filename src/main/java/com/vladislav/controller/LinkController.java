package com.vladislav.controller;

import com.vladislav.dto.request.CreateLinkRequest;
import com.vladislav.dto.response.LinkInfoResponse;
import com.vladislav.dto.response.LinkResponse;
import com.vladislav.dto.response.LinkStatsResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RequestMapping("/api/links")
@Tag(name = "Links", description = "Create, inspect and delete short links")
public interface LinkController {

    @Operation(summary = "Create a short link")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Short link created"),
            @ApiResponse(responseCode = "400", description = "Invalid or blank URL")
    })
    @PostMapping()
    ResponseEntity<LinkResponse> create(@RequestBody @Valid CreateLinkRequest linkRequest);

    @Operation(summary = "Get link information")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Link found"),
            @ApiResponse(responseCode = "404", description = "Link not found")
    })
    @GetMapping("/{shortCode}")
    ResponseEntity<LinkInfoResponse> getLink(@PathVariable String shortCode);

    @Operation(summary = "Get click statistics")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Statistics returned"),
            @ApiResponse(responseCode = "404", description = "Link not found")
    })
    @GetMapping("/{shortCode}/stats")
    ResponseEntity<LinkStatsResponse> getStats(@PathVariable String shortCode);

    @Operation(summary = "Delete a link")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Link deleted"),
            @ApiResponse(responseCode = "404", description = "Link not found")
    })
    @DeleteMapping("/{shortCode}")
    ResponseEntity<Void> delete(@PathVariable String shortCode);

}
