package com.store.erp.Controllers;

import com.store.erp.Models.ProductoRegaloDTO;
import com.store.erp.Services.ProductoRegaloService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/productos-regalo")
public class ProductoRegaloController {

    @Autowired
    private ProductoRegaloService productoRegaloService;

    @GetMapping("/")
    public List<ProductoRegaloDTO> listarProductosRegalo() {
        return productoRegaloService.listarProductosRegalo();
    }

    @GetMapping("/{id}")
    public ProductoRegaloDTO buscarProductoRegaloPorId(@PathVariable Integer id) {
        return productoRegaloService.buscarProductoRegaloPorId(id);
    }

    @PostMapping("/")
    public void guardarProductoRegalo(@RequestBody ProductoRegaloDTO productoRegalo) {
        productoRegaloService.guardarProductoRegalo(productoRegalo);
    }

    @PutMapping("/{id}")
    public void actualizarProductoRegalo(@PathVariable Integer id, @RequestBody ProductoRegaloDTO productoRegalo) {
        productoRegalo.setIdRegalo(id);
        productoRegaloService.actualizarProductoRegalo(productoRegalo);
    }
}
