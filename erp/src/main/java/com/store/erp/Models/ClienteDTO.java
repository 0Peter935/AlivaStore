package com.store.erp.Models;

import java.time.OffsetDateTime;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ClienteDTO {

    private int idCliente;
    private String codigoCliente;
    private String nombres;
    private String dni;
    private String correo;
    private String telefono;
    private int canOrdenes;
    private String direccion;
    private String ciudad;
    private String provincia;
    private String pais;
    private OffsetDateTime fechaReg;
    private OffsetDateTime fechaAct;

}
