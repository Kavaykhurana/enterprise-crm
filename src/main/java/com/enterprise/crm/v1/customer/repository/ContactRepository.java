package com.enterprise.crm.v1.customer.repository;

import com.enterprise.crm.v1.customer.entity.Contact;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface ContactRepository extends JpaRepository<Contact, UUID> {
    Optional<Contact> findByCustomerIdAndEmail(UUID customerId, String email);
}
