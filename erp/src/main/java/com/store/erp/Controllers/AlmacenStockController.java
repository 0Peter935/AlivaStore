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

    @GetMapping("/producto/{codVariante}")
        public ResponseEntity<?> listarStockPorProducto(@PathVariable("codVariante") String codVariante) {
            List<AlmacenStockDTO> lista = almacenStockService.listarPorVariante(codVariante);
            return ResponseEntity.ok(lista);
        }

    @PutMapping("/producto/{idProducto}")
    public ResponseEntity<?> actualizarStockProducto(
            @PathVariable("idProducto") String codVariante,
            @RequestBody List<AlmacenStockDTO> detalle) {

        try {
            almacenStockService.guardarStockProducto(codVariante, detalle);
            return ResponseEntity.ok("Stock actualizado correctamente");
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError()
                    .body("Error al actualizar stock: " + e.getMessage());
        }
    }
}
