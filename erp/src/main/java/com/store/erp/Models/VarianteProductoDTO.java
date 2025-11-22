package com.store.erp.Models;

import java.time.OffsetDateTime;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class VarianteProductoDTO {

    private int idVariante;
    private String codProducto;
    private String codVariante;
    private String titulo;
    private Double precio;
    private String imgVariante;
    private OffsetDateTime fechaReg;
    private OffsetDateTime fechaAct;

    private List<AlmacenStockDTO> almacenStock;

}
