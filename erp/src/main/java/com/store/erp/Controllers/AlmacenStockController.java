package com.store.erp.Controllers;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.store.erp.Models.AlmacenStockDTO;
import com.store.erp.Services.AlmacenStockService;

import java.util.List;

@RestController
@RequestMapping("/api/almacen-stock")
public class AlmacenStockController {

    @Autowired
    private AlmacenStockService almacenStockService;

    @GetMapping("/producto/{idProducto}")
        public ResponseEntity<?> listarStockPorProducto(@PathVariable("idProducto") int idProducto) {
            List<AlmacenStockDTO> lista = almacenStockService.listarPorProducto(idProducto);
            return ResponseEntity.ok(lista);
        }

    @PutMapping("/producto/{idProducto}")
    public ResponseEntity<?> actualizarStockProducto(
            @PathVariable("idProducto") int idProducto,
            @RequestBody List<AlmacenStockDTO> detalle) {

        try {
            almacenStockService.guardarStockProducto(idProducto, detalle);
            return ResponseEntity.ok("Stock actualizado correctamente");
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError()
                    .body("Error al actualizar stock: " + e.getMessage());
        }
    }
}
