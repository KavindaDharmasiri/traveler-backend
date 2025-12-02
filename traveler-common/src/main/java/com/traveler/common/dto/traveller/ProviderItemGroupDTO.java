package com.traveler.common.dto.traveller;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * ALL RIGHT RESERVED By kavinda_d
 *
 * @AUTHOR : kavinda_d
 * @PROJECT : traveler backend
 */

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProviderItemGroupDTO {
    private String tenant;
    private String providerName;
    private List<ItemDetailsDTO> items;
}
