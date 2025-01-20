package com.example.bookstore.repository;

import com.example.bookstore.entity.Address;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AddressRepository extends JpaRepository<Address,Long>
{
    List<Address> findByUserId(Long userId);

    Optional<Address> findByAddressIdAndUserId(Long addressId, Long userId);
}
