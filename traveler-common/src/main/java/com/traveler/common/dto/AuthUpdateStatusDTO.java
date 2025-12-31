package com.traveler.common.dto;

import lombok.Data;

/**
 * ALL RIGHT RESERVED By kavinda_d
 *
 * @AUTHOR : kavinda_d
 * @PROJECT : traveler backend
 */

@Data
public class AuthUpdateStatusDTO {
    private String orderId;
    private String itemId;
    private String status;

}
