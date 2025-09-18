package com.store.erp.Services;

import com.store.erp.Models.ProductoRegaloDTO;
import com.store.erp.Repo.ProductoRegaloRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProductoRegaloService {

    @Autowired
    private ProductoRegaloRepo productoRegaloRepo;

    public List<ProductoRegaloDTO> listarProductosRegalo() {
        return productoRegaloRepo.listar();
    }

    public ProductoRegaloDTO buscarProductoRegaloPorId(Integer id) {
        return productoRegaloRepo.buscarPorId(id);
    }

    public void guardarProductoRegalo(ProductoRegaloDTO productoRegalo) {
        productoRegaloRepo.guardar(productoRegalo);
    }

    public void actualizarProductoRegalo(ProductoRegaloDTO productoRegalo) {
        productoRegaloRepo.actualizar(productoRegalo);
    }
}
