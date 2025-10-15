package com.store.erp.Models;

import java.time.LocalDate;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductoDTO {

    private int idProducto;
    private String codProducto;
    private String descProducto;
    private int stock;
    private Double precio;
    private String imagen;
    private Boolean regalo;
    private Boolean estado;
    private LocalDate fechaRegistro;

    private List<AlmacenStockDTO> almacenStock;

}
