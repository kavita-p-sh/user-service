package com.ecommerce.user.dto;
import lombok.Data;

import java.math.BigDecimal;

/**
 * Response DTO for order item details.
 */

@Data
public class OrderItemResponseDTO {

    private Long orderItemId;
    private Long productId;
    private String productName;
    private BigDecimal price;
    private Integer quantity;
}