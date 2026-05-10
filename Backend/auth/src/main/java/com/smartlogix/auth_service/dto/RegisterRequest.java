package com.smartlogix.auth_service.dto;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;
@Data
public class RegisterRequest {
    @NotBlank(message = "Ingresa tu correo electronico")
    @Email(message = "Ingresa un correo electronico valido")
    private String email;

    @NotBlank(message = "Ingresa tu contrasena")
    @Size(min = 8, message = "La contrasena debe tener al menos 8 caracteres")
    private String password;

    @NotBlank(message = "Ingresa el nombre de la empresa")
    private String companyName;

    @NotBlank(message = "Ingresa el RUT de la empresa")
    @Pattern(regexp = "^[0-9.\\-]{7,12}[0-9Kk]$", message = "El RUT ingresado no es valido")
    private String taxId;

    @NotBlank(message = "Ingresa el nombre del usuario")
    private String firstName;

    @NotBlank(message = "Ingresa el apellido del usuario")
    private String lastName;

    @Email(message = "Ingresa un correo de contacto valido")
    private String contactEmail;
    private String phone;
}
