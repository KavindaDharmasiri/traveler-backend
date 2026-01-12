package com.traveler.auth.traveler.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "user_documents")
@Data
public class UserDocument {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    
    @Column(name = "doc_name", nullable = false)
    private String docName;
    
    @Column(name = "doc_uuid", nullable = false)
    private String docUuid;
}