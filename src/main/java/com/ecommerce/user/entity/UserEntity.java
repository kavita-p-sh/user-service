package com.ecommerce.user.entity;


import jakarta.persistence.*;
import lombok.Data;
import lombok.ToString;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.UUID;


/**
 * Entity representing user table in database.
 */

@Data
@Entity
@Table(name = "tb_users")

public class UserEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(name = "user_id", length = 36)
    private UUID userId;

    @Column(unique = true , nullable = false)
    private String username;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false, unique = true)
    private String phoneNumber;

    @Column(nullable = false, unique = true, length = 100)
    private String email;

    @ManyToOne
    @ToString.Exclude
    @JoinColumn(nullable = false, name="role_id")
    private RoleEntity role;




}
