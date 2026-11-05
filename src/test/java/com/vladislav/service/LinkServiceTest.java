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

import java.time.LocalDateTime;
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

    @Mock
    private LinkLookupService linkLookupService;

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
        var info = getLinkInfoResponseForUnit();

        when(linkLookupService.find(SHORT_CODE)).thenReturn(info);
        when(linkRepository.incrementClicks(SHORT_CODE)).thenReturn(1);

        var result = linkService.redirect(SHORT_CODE);

        assertEquals(info.getOriginalUrl(), result);
        verify(linkRepository).incrementClicks(SHORT_CODE);
    }

    @Test
    void shouldRedirectWhenExpirationIsInFuture() {
        var info = getLinkInfoResponseWithExpirationForUnit(LocalDateTime.now().plusDays(1));

        when(linkLookupService.find(SHORT_CODE)).thenReturn(info);
        when(linkRepository.incrementClicks(SHORT_CODE)).thenReturn(1);

        assertEquals(info.getOriginalUrl(), linkService.redirect(SHORT_CODE));
    }

    @Test
    void shouldThrowExceptionWhenRedirectLinkIsNotFound() {
        when(linkLookupService.find(SHORT_CODE)).thenThrow(new LinkNotFoundException("not found"));

        assertThrows(LinkNotFoundException.class, () -> linkService.redirect(SHORT_CODE));
        verify(linkRepository, never()).incrementClicks(anyString());
    }

    @Test
    void shouldThrowExceptionWhenLinkIsExpired() {
        var info = getLinkInfoResponseWithExpirationForUnit(LocalDateTime.now().minusDays(1));

        when(linkLookupService.find(SHORT_CODE)).thenReturn(info);

        assertThrows(LinkExpiredException.class, () -> linkService.redirect(SHORT_CODE));
        verify(linkRepository, never()).incrementClicks(anyString());
    }

    @Test
    void shouldThrowNotFoundWhenLinkIsCachedButAlreadyDeletedFromDb() {
        var info = getLinkInfoResponseForUnit();

        when(linkLookupService.find(SHORT_CODE)).thenReturn(info);
        when(linkRepository.incrementClicks(SHORT_CODE)).thenReturn(0);

        assertThrows(LinkNotFoundException.class, () -> linkService.redirect(SHORT_CODE));
    }

    @Test
    void shouldFindLinkInfo() {
        var info = getLinkInfoResponseForUnit();

        when(linkLookupService.find(SHORT_CODE)).thenReturn(info);

        var result = linkService.getLink(SHORT_CODE);

        assertEquals(info.getShortCode(), result.getShortCode());
        assertEquals(info.getOriginalUrl(), result.getOriginalUrl());
    }

    @Test
    void shouldThrowExceptionWhenLinkInfoIsNotFound() {
        when(linkLookupService.find(SHORT_CODE)).thenThrow(new LinkNotFoundException("not found"));

        assertThrows(LinkNotFoundException.class, () -> linkService.getLink(SHORT_CODE));
    }

    @Test
    void shouldReturnStats() {
        var link = getLinkForUnit();
        var statsResponse = getLinkStatsResponseForUnit();

        when(linkRepository.findByShortCode(SHORT_CODE)).thenReturn(Optional.of(link));
        when(linkMapper.fromEntityToStatsDto(link)).thenReturn(statsResponse);

        assertEquals(statsResponse.getClicks(), linkService.getStats(SHORT_CODE).getClicks());
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