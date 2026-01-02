package com.traveler.common.dto.provider;

import com.traveler.common.entity.Transaction;
import com.traveler.common.entity.TransactionItem;
import lombok.Data;

/**
 * ALL RIGHT RESERVED By kavinda_d
 *
 * @AUTHOR : kavinda_d
 * @PROJECT : traveler backend
 */

@Data
public class SaveTrandDTO {
    Transaction transaction;
    TransactionItem item;
}
