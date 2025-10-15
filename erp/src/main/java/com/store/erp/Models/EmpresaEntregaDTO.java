package com.store.erp.Models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EmpresaEntregaDTO {

    private int idEmpresaEntrega;
    private String razonSocial;
    private String ruc;
    private String direccionFiscal;
    private ZonaEmpresaEntregaDTO zona;

}
