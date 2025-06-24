package com.evaruiz.healthcarer.model.DTO;

public record RegisterUserDTO(
    String name,
    String email,
    String password
) {
    public boolean validate() {
        return name != null && email != null && password != null &&
               !name.isBlank() && !email.isBlank() && !password.isBlank();
    }
}
