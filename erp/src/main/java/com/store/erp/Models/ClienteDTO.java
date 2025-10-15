package com.store.erp.Models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ClienteDTO {

    private int idCliente;
    private String nombres;
    private String apellidos;
    private String documento;
    private String correo;
    private String telefono;

}
