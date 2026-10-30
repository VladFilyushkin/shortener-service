package com.vladislav.controller.impl;

import com.vladislav.controller.LinkController;
import com.vladislav.dto.request.CreateLinkRequest;
import com.vladislav.dto.response.LinkInfoResponse;
import com.vladislav.dto.response.LinkResponse;
import com.vladislav.dto.response.LinkStatsResponse;
import com.vladislav.service.LinkService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class LinkControllerImpl implements LinkController {

    private final LinkService linkService;

    @Override
    public ResponseEntity<LinkResponse> create(CreateLinkRequest linkRequest) {
        return ResponseEntity.status(HttpStatus.CREATED).body(linkService.createLink(linkRequest));
    }

    @Override
    public ResponseEntity<LinkInfoResponse> getLink(String shortCode) {
        return ResponseEntity.status(HttpStatus.OK).body(linkService.getLink(shortCode));
    }

    @Override
    public ResponseEntity<LinkStatsResponse> getStats(String shortCode) {
        return ResponseEntity.ok().body(linkService.getStats(shortCode));
    }

    @Override
    public ResponseEntity<Void> delete(String shortCode) {
        linkService.deleteLink(shortCode);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}
