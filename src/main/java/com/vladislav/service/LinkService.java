package com.vladislav.service;

import com.vladislav.dto.request.CreateLinkRequest;
import com.vladislav.dto.response.LinkInfoResponse;
import com.vladislav.dto.response.LinkResponse;
import com.vladislav.dto.response.LinkStatsResponse;

public interface LinkService {

    LinkResponse createLink(CreateLinkRequest request);

    String redirect(String shortCode);

    LinkInfoResponse getLink(String shortCode);

    LinkStatsResponse getStats(String shortCode);

    void deleteLink(String shortCode);

}