package com.vladislav.mapper;

import com.vladislav.dto.request.CreateLinkRequest;
import com.vladislav.dto.response.LinkInfoResponse;
import com.vladislav.dto.response.LinkStatsResponse;
import com.vladislav.entity.Link;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface LinkMapper {

    @Mapping(target = "shortCode", source = "shortCode")
    @Mapping(target = "originalUrl", source = "request.originalUrl")
    @Mapping(target = "expiresAt", source = "request.expiresAt")
    Link createEntity(CreateLinkRequest request, String shortCode);

    @Mapping(target = "originalUrl", source = "originalUrl")
    @Mapping(target = "shortCode", source = "shortCode")
    @Mapping(target = "expiresAt", source = "expiresAt")
    @Mapping(target = "createdAt", source = "createdAt")
    LinkInfoResponse fromEntityToInfoDto(Link link);

    LinkStatsResponse fromEntityToStatsDto(Link link);
}
