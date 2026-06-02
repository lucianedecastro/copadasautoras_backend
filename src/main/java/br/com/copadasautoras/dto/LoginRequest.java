package br.com.copadasautoras.dto;

public record LoginRequest(
        String email,
        String password
) {}

