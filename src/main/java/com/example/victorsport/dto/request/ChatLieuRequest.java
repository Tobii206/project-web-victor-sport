package com.example.victorsport.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ChatLieuRequest {
    @NotBlank(message = "Tên chất liệu không được để trống")
    private String tenChatLieu;
    private Boolean trangThai = true;
}
