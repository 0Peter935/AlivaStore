package com.store.erp.Controllers;

import com.store.erp.Models.ProductoDTO;
import com.store.erp.Services.ProductoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/productos")
public class ProductoController {

    @Autowired
    private ProductoService productoService;

    @GetMapping("/")
    public List<ProductoDTO> listarProductos() {
        return productoService.listarProductos();
    }

    @GetMapping("/{id}")
    public ProductoDTO buscarProductoPorId(@PathVariable Long id) {
        return productoService.buscarProductoPorId(id);
    }

    @PostMapping("/")
    public void guardarProducto(@RequestBody ProductoDTO producto) {
        productoService.guardarProducto(producto);
    }

    @PutMapping("/{id}")
    public void actualizarProducto(@PathVariable Long id, @RequestBody ProductoDTO producto) {
        producto.setIdProducto(id);
        productoService.actualizarProducto(producto);
    }
}
