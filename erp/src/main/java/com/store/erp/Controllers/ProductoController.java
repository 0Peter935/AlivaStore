package com.store.erp.Controllers;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.store.erp.Models.ProductoDTO;
import com.store.erp.Services.ProductoService;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/productos")
public class ProductoController {

    @Autowired
    private ProductoService productoService;

    @GetMapping
    public List<ProductoDTO> listarProductos() {
        return productoService.listarProductos();
    }

    @GetMapping("/{idProducto}/buscar")
    public ProductoDTO obtenerProductoPorId(@PathVariable int idProducto) {
        return productoService
                .listarProductos()
                .stream()
                .filter(p -> p.getIdProducto() == idProducto)
                .findFirst()
                .orElse(null);
    }

    @PostMapping("/nuevo")
    public ResponseEntity<String> registrarProducto(@RequestBody ProductoDTO producto) {
        productoService.registrarProducto(producto);
        return ResponseEntity.ok("Producto registrado correctamente");
    }

    @PutMapping("/{id}/regalo")
    public ResponseEntity<?> actualizarRegalo(@PathVariable("id") int idProducto, @RequestBody Map<String, Object> body) {
        try {
            boolean regalo = Boolean.parseBoolean(body.get("regalo").toString());
            productoService.actualizarRegalo(idProducto, regalo);
            return ResponseEntity.ok(Map.of("mensaje", "Regalo actualizado correctamente"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Error al actualizar regalo", "details", e.getMessage()));
        }
        
    }

    @PutMapping("/{id}/estado")
    public ResponseEntity<?> cambiarEstado(@PathVariable("id") int idProducto, @RequestBody Map<String, Object> body) {
        try {
            boolean estado = Boolean.parseBoolean(body.get("estado").toString());
            productoService.cambiarEstado(idProducto, estado);
            return ResponseEntity.ok(Map.of("message", "Estado actualizado correctamente"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Error al actualizar estado", "details", e.getMessage()));
        }
    }
}

