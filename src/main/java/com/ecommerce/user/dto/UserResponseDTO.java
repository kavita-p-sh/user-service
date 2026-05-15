package com.ecommerce.user.dto;
import com.ecommerce.common.enums.RoleName;
import lombok.*;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserResponseDTO implements Serializable {

   private UUID userId;
    private String username;
    private String email;
    private RoleName role;
    private String phoneNumber;
    private String createdBy;
    private String updatedBy;
    private LocalDateTime createdTimestamp;
    private LocalDateTime updatedTimestamp;

}
