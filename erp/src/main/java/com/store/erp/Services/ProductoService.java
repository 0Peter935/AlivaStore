package com.store.erp.Services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.store.erp.Models.ProductoDTO;
import com.store.erp.Repo.ProductoRepo;

import java.util.List;

@Service
public class ProductoService {

    @Autowired
    private ProductoRepo productoRepo;

    public List<ProductoDTO> listarProductos() {
        return productoRepo.listarProductos();
    }

    public void registrarProducto(ProductoDTO producto) {
        productoRepo.registrarProducto(producto);
    }

    public void actualizarRegalo(int idProducto, boolean regalo) {
        productoRepo.actualizarRegalo(idProducto, regalo);
    }

    public void cambiarEstado(int idProducto, boolean estado) {
        productoRepo.cambiarEstado(idProducto, estado);
    }
}
