package com.vladislav.service;

import com.vladislav.exception.LinkExpiredException;
import com.vladislav.exception.LinkNotFoundException;
import com.vladislav.mapper.LinkMapper;
import com.vladislav.repository.LinkRepository;
import com.vladislav.service.impl.LinkServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static com.vladislav.util.TestData.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LinkServiceTest {

    private static final String BASE_URL = "http://localhost:8080";
    private static final String SHORT_CODE = "abc12345";

    @Mock
    private LinkRepository linkRepository;

    @Mock
    private LinkMapper linkMapper;

    @InjectMocks
    private LinkServiceImpl linkService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(linkService, "baseUrl", BASE_URL);
    }

    @Test
    void shouldCreateLinkAndBuildShortUrl() {
        var request = getCreateLinkRqForUnit();
        var link = getLinkForUnit();

        when(linkMapper.createEntity(eq(request), anyString())).thenReturn(link);
        when(linkRepository.save(link)).thenReturn(link);

        var result = linkService.createLink(request);

        assertEquals(SHORT_CODE, result.getShortCode());
        assertEquals(BASE_URL + "/" + SHORT_CODE, result.getShortUrl());
        verify(linkRepository).save(link);
    }

    @Test
    void shouldGenerateNewShortCodeWhenPreviousOneIsTaken() {
        var request = getCreateLinkRqForUnit();
        var link = getLinkForUnit();

        when(linkRepository.existsByShortCode(anyString())).thenReturn(true, false);
        when(linkMapper.createEntity(eq(request), anyString())).thenReturn(link);
        when(linkRepository.save(link)).thenReturn(link);

        linkService.createLink(request);

        verify(linkRepository, times(2)).existsByShortCode(anyString());
        verify(linkRepository).save(link);
    }

    @Test
    void shouldRedirectAndIncreaseClicks() {
        var link = getLinkForUnit();

        when(linkRepository.findByShortCode(SHORT_CODE)).thenReturn(Optional.of(link));
        when(linkRepository.save(link)).thenReturn(link);

        var result = linkService.redirect(SHORT_CODE);

        assertEquals(link.getOriginalUrl(), result);
        assertEquals(1L, link.getClicks());
        verify(linkRepository).save(link);
    }

    @Test
    void shouldThrowExceptionWhenRedirectLinkIsNotFound() {
        when(linkRepository.findByShortCode(SHORT_CODE)).thenReturn(Optional.empty());

        assertThrows(LinkNotFoundException.class, () -> linkService.redirect(SHORT_CODE));
        verify(linkRepository, never()).save(any());
    }

    @Test
    void shouldThrowExceptionWhenLinkIsExpired() {
        var link = getExpiredLinkForUnit();

        when(linkRepository.findByShortCode(SHORT_CODE)).thenReturn(Optional.of(link));

        assertThrows(LinkExpiredException.class, () -> linkService.redirect(SHORT_CODE));
        assertEquals(0L, link.getClicks());
        verify(linkRepository, never()).save(any());
    }

    @Test
    void shouldFindLinkInfo() {
        var link = getLinkForUnit();
        var infoResponse = getLinkInfoResponseForUnit();

        when(linkRepository.findByShortCode(SHORT_CODE)).thenReturn(Optional.of(link));
        when(linkMapper.fromEntityToInfoDto(link)).thenReturn(infoResponse);

        var result = linkService.getLink(SHORT_CODE);

        assertEquals(infoResponse.getShortCode(), result.getShortCode());
        assertEquals(infoResponse.getOriginalUrl(), result.getOriginalUrl());
    }

    @Test
    void shouldThrowExceptionWhenLinkInfoIsNotFound() {
        when(linkRepository.findByShortCode(SHORT_CODE)).thenReturn(Optional.empty());

        assertThrows(LinkNotFoundException.class, () -> linkService.getLink(SHORT_CODE));
    }

    @Test
    void shouldReturnStats() {
        var link = getLinkForUnit();
        var statsResponse = getLinkStatsResponseForUnit();

        when(linkRepository.findByShortCode(SHORT_CODE)).thenReturn(Optional.of(link));
        when(linkMapper.fromEntityToStatsDto(link)).thenReturn(statsResponse);

        var result = linkService.getStats(SHORT_CODE);

        assertEquals(statsResponse.getClicks(), result.getClicks());
    }

    @Test
    void shouldDeleteLink() {
        var link = getLinkForUnit();

        when(linkRepository.findByShortCode(SHORT_CODE)).thenReturn(Optional.of(link));

        linkService.deleteLink(SHORT_CODE);

        verify(linkRepository).delete(link);
    }

    @Test
    void shouldThrowExceptionWhenDeletingLinkIsNotFound() {
        when(linkRepository.findByShortCode(SHORT_CODE)).thenReturn(Optional.empty());

        assertThrows(LinkNotFoundException.class, () -> linkService.deleteLink(SHORT_CODE));
        verify(linkRepository, never()).delete(any());
    }
}