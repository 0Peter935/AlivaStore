package com.store.erp.Models;

import java.time.LocalDate;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UsuarioDTO {

    private int idUsuario;
    private String usuario;
    private String correo;
    private String clave;
    private String telefono;
    private RolDTO rol;
    private boolean estado;
    private String nombre;
    private String apPaterno;
    private String apMaterno;
    private LocalDate fechaRegistro;
    
}
