package com.store.erp.Models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AlmacenStockDTO {

    private int idAlmacenStock;
    private int idProducto;
    private int inventario;
    private AlmacenDTO almacen;

}