package com.traveler.common.dto;

import com.traveler.common.entity.Backpack;
import com.traveler.common.entity.Order;
import lombok.Data;

import java.util.List;

/**
 * ALL RIGHT RESERVED By kavinda_d
 *
 * @AUTHOR : kavinda_d
 * @PROJECT : traveler backend
 */

@Data
public class SaveOrderDTO {
    private Order order;
    private List<Backpack> backpacks;
}
