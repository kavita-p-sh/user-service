package com.ecommerce.user.entity;

import com.ecommerce.common.enums.RoleName;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;
import lombok.ToString;

import java.util.List;

/**
 * Entity representing User roles details.
 */
@Data
@Entity
@Table(name = "tb_roles")
public class RoleEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long roleId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, unique = true, length = 50)
    private RoleName roleName;

    @OneToMany(mappedBy="role")
    @JsonIgnore
    @ToString.Exclude
    private List<UserEntity> users;




}