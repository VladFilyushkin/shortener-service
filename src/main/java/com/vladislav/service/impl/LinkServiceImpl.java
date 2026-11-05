package com.vladislav.service.impl;

import com.vladislav.dto.request.CreateLinkRequest;
import com.vladislav.dto.response.LinkInfoResponse;
import com.vladislav.dto.response.LinkResponse;
import com.vladislav.dto.response.LinkStatsResponse;
import com.vladislav.entity.Link;
import com.vladislav.exception.LinkExpiredException;
import com.vladislav.exception.LinkNotFoundException;
import com.vladislav.mapper.LinkMapper;
import com.vladislav.repository.LinkRepository;
import com.vladislav.service.LinkLookupService;
import com.vladislav.service.LinkService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class LinkServiceImpl implements LinkService {

    private final LinkRepository linkRepository;
    private final LinkMapper linkMapper;
    private final LinkLookupService linkLookupService;
    @Value("${app.url}")
    private String baseUrl;

    @Override
    public LinkResponse createLink(CreateLinkRequest request) {
        String shortCode;
        do {
            shortCode = generateShortCode();
        } while (linkRepository.existsByShortCode(shortCode));

        Link link = linkMapper.createEntity(request, shortCode);

        Link savedLink = linkRepository.save(link);
        return LinkResponse.builder()
                .shortCode(savedLink.getShortCode())
                .shortUrl(baseUrl + "/" + savedLink.getShortCode())
                .build();
    }

    @Override
    @Transactional
    public String redirect(String shortCode) {
        LinkInfoResponse link = linkLookupService.find(shortCode);
        if (link.getExpiresAt() != null && link.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new LinkExpiredException("Link is already expired");
        }
        if (linkRepository.incrementClicks(shortCode) == 0) {
            throw new LinkNotFoundException("Link with shortcode: " + shortCode + " is not found");
        }
        return link.getOriginalUrl();
    }

    @Override
    public LinkInfoResponse getLink(String shortCode) {
        return linkLookupService.find(shortCode);
    }

    @Override
    public LinkStatsResponse getStats(String shortCode) {
        return linkMapper.fromEntityToStatsDto(findOrThrow(shortCode));
    }

    @Override
    @CacheEvict(cacheNames = "link", key = "#shortCode")
    public void deleteLink(String shortCode) {
        linkRepository.delete(findOrThrow(shortCode));
    }

    private String generateShortCode() {
        String uuid = UUID.randomUUID().toString();
        return uuid.replace("-", "").substring(0, 8);
    }

    private Link findOrThrow(String shortCode) {
        return linkRepository.findByShortCode(shortCode)
                .orElseThrow(() ->
                        new LinkNotFoundException("Link with shortcode " + shortCode + " is not found"));
    }
}
