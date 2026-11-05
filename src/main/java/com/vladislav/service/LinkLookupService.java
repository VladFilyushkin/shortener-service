package com.vladislav.service;

import com.vladislav.dto.response.LinkInfoResponse;

public interface LinkLookupService {

    LinkInfoResponse find(String shortCode);
}
