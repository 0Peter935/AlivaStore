package com.store.erp.Services;

import com.store.erp.Models.ProductoDTO;
import com.store.erp.Repo.ProductoRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProductoService {

    @Autowired
    private ProductoRepo productoRepo;

    public List<ProductoDTO> listarProductos() {
        return productoRepo.listar();
    }

    public ProductoDTO buscarProductoPorId(Long id) {
        return productoRepo.buscarPorId(id);
    }

    public void guardarProducto(ProductoDTO producto) {
        productoRepo.guardar(producto);
    }

    public void actualizarProducto(ProductoDTO producto) {
        productoRepo.actualizar(producto);
    }
}
