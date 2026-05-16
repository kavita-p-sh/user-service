package com.ecommerce.user.mapper;


import com.ecommerce.user.dto.UserResponseDTO;
import com.ecommerce.user.entity.UserEntity;
import org.springframework.stereotype.Component;


/**
 * Mapper class for converting UserEntity to UserResponseDTO.
 */
@Component
public class UserMapper {

    public UserResponseDTO toDTO(UserEntity user) {
        if (user == null) {
            return null;
        }

        UserResponseDTO dto = new UserResponseDTO();
        dto.setUserId(user.getUserId());
        dto.setUsername(user.getUsername());
        dto.setEmail(user.getEmail());
        dto.setPhoneNumber(user.getPhoneNumber());

        if (user.getRole() != null) {
            dto.setRole(user.getRole().getRoleName());
        }

        dto.setCreatedBy(user.getCreatedBy());
        dto.setUpdatedBy(user.getUpdatedBy());
        dto.setCreatedTimestamp(user.getCreatedTimestamp());
        dto.setUpdatedTimestamp(user.getUpdatedTimestamp());

        return dto;
    }
}