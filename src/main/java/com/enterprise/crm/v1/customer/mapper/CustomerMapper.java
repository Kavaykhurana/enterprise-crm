package com.enterprise.crm.v1.customer.mapper;

import com.enterprise.crm.v1.customer.dto.*;
import com.enterprise.crm.v1.customer.entity.Address;
import com.enterprise.crm.v1.customer.entity.Contact;
import com.enterprise.crm.v1.customer.entity.Customer;
import com.enterprise.crm.v1.tag.entity.Tag;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
public interface CustomerMapper {
    CustomerMapper INSTANCE = Mappers.getMapper(CustomerMapper.class);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    @Mapping(target = "version", ignore = true)
    @Mapping(target = "contacts", ignore = true)
    @Mapping(target = "addresses", ignore = true)
    @Mapping(target = "tags", ignore = true)
    Customer createRequestToCustomer(CreateCustomerRequest request);

    @Mapping(source = "tags", target = "tags")
    CustomerResponse customerToResponse(Customer customer);

    ContactResponse contactToResponse(Contact contact);

    AddressRequest addressToRequest(Address address);

    default Set<String> mapTags(Set<Tag> tags) {
        if (tags == null) return Collections.emptySet();
        return tags.stream().map(Tag::getName).collect(Collectors.toSet());
    }
}
