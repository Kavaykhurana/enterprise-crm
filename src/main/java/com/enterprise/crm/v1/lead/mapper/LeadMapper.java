package com.enterprise.crm.v1.lead.mapper;

import com.enterprise.crm.v1.lead.dto.*;
import com.enterprise.crm.v1.lead.entity.Lead;
import com.enterprise.crm.v1.tag.entity.Tag;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
public interface LeadMapper {
    LeadMapper INSTANCE = Mappers.getMapper(LeadMapper.class);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    @Mapping(target = "version", ignore = true)
    @Mapping(target = "convertedCustomerId", ignore = true)
    @Mapping(target = "lastContactedAt", ignore = true)
    @Mapping(target = "tags", ignore = true)
    Lead createRequestToLead(CreateLeadRequest request);

    @Mapping(source = "tags", target = "tags")
    LeadResponse leadToResponse(Lead lead);

    default Set<String> mapTags(Set<Tag> tags) {
        if (tags == null) return Collections.emptySet();
        return tags.stream().map(Tag::getName).collect(Collectors.toSet());
    }
}
