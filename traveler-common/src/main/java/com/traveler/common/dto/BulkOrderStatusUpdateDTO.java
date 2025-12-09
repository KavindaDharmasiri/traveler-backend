package com.traveler.common.dto;

import com.traveler.common.utils.STATUS;
import java.util.List;

public class BulkOrderStatusUpdateDTO {
    private List<String> orderCodes;
    private STATUS status;

    public List<String> getOrderCodes() {
        return orderCodes;
    }

    public void setOrderCodes(List<String> orderCodes) {
        this.orderCodes = orderCodes;
    }

    public STATUS getStatus() {
        return status;
    }

    public void setStatus(STATUS status) {
        this.status = status;
    }
}
