package com.vladislav.service.impl;

import com.vladislav.dto.response.LinkInfoResponse;
import com.vladislav.exception.LinkNotFoundException;
import com.vladislav.mapper.LinkMapper;
import com.vladislav.repository.LinkRepository;
import com.vladislav.service.LinkLookupService;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class LinkLookupServiceImpl implements LinkLookupService {

    private final LinkRepository linkRepository;
    private final LinkMapper linkMapper;

    @Override
    @Cacheable(cacheNames = "link", key = "#shortCode")
    public LinkInfoResponse find(String shortCode) {
        return linkMapper.fromEntityToInfoDto(linkRepository.findByShortCode(shortCode)
                .orElseThrow(() ->
                        new LinkNotFoundException("Link with shortcode " + shortCode + " is not found")));
    }
}
