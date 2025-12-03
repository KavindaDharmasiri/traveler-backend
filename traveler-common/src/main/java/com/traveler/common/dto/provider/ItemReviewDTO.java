package com.traveler.common.dto.provider;

import com.traveler.common.entity.provider.Item;
import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.Date;

@Data
public class ItemReviewDTO {
    private String travelerTenant;
    private int rating;
    private String reviewText;
    private Date createdAt;
}
