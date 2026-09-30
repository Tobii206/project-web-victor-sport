package com.example.victorsport.Entity;

import jakarta.persistence.*;

@Entity
@Table(name = "chat_lieu")
public class ChatLieu {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "ma_chat_lieu", insertable = false, updatable = false)
    private String maChatLieu;

    @Column(name = "ten_chat_lieu", nullable = false)
    private String tenChatLieu;

    @Column(name = "trang_thai")
    private Boolean trangThai;

    @Column(name = "xoa_mem")
    private Boolean xoaMem;
}
