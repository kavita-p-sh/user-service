package com.ecommerce.user.repository;

import com.ecommerce.common.enums.RoleName;
import com.ecommerce.user.entity.RoleEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RoleRepository extends JpaRepository<RoleEntity, Long> {

    Optional<RoleEntity> findByRoleName(RoleName roleName);
}