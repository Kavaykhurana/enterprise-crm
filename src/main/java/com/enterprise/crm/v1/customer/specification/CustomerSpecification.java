package com.enterprise.crm.v1.customer.specification;

import com.enterprise.crm.v1.customer.entity.Customer;
import com.enterprise.crm.v1.tag.entity.Tag;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class CustomerSpecification {

    public static Specification<Customer> filter(
            String companyName,
            String customerStatus,
            UUID assignedSalesRepId,
            String tag,
            LocalDateTime startCreated,
            LocalDateTime endCreated,
            LocalDateTime startUpdated,
            LocalDateTime endUpdated,
            String companySize,
            String taxIdentifier) {

        return (root, query, builder) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (companyName != null && !companyName.trim().isEmpty()) {
                predicates.add(builder.like(builder.lower(root.get("companyName")), "%" + companyName.toLowerCase() + "%"));
            }

            if (customerStatus != null && !customerStatus.trim().isEmpty()) {
                predicates.add(builder.equal(root.get("customerStatus"), customerStatus));
            }

            if (assignedSalesRepId != null) {
                predicates.add(builder.equal(root.get("assignedSalesRepId"), assignedSalesRepId));
            }

            if (tag != null && !tag.trim().isEmpty()) {
                Join<Customer, Tag> tagJoin = root.join("tags");
                predicates.add(builder.equal(builder.lower(tagJoin.get("name")), tag.toLowerCase()));
            }

            if (startCreated != null) {
                predicates.add(builder.greaterThanOrEqualTo(root.get("createdAt"), startCreated));
            }

            if (endCreated != null) {
                predicates.add(builder.lessThanOrEqualTo(root.get("createdAt"), endCreated));
            }

            if (startUpdated != null) {
                predicates.add(builder.greaterThanOrEqualTo(root.get("updatedAt"), startUpdated));
            }

            if (endUpdated != null) {
                predicates.add(builder.lessThanOrEqualTo(root.get("updatedAt"), endUpdated));
            }

            if (companySize != null && !companySize.trim().isEmpty()) {
                predicates.add(builder.equal(root.get("companySize"), companySize));
            }

            if (taxIdentifier != null && !taxIdentifier.trim().isEmpty()) {
                predicates.add(builder.equal(root.get("taxIdentifier"), taxIdentifier));
            }

            return builder.and(predicates.toArray(new Predicate[0]));
        };
    }
}
