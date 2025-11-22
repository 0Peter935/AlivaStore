package com.store.erp.Services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.store.erp.Models.ProductoDTO;
import com.store.erp.Models.VarianteProductoDTO;
import com.store.erp.Repo.ProductoRepo;
import com.store.erp.Repo.VarianteRepo;

import java.util.List;

@Service
public class ProductoService {

    @Autowired
    private ProductoRepo productoRepo;

    @Autowired
    private VarianteRepo varianteRepo;

    public List<ProductoDTO> listarProductos() {
        return productoRepo.listarProductos();
    }

    public ProductoDTO obtenerPorId(int idProducto) {
        return productoRepo.obtenerPorId(idProducto);
    }

    public void actualizarProducto(ProductoDTO producto) {
        productoRepo.actualizarProducto(producto);
    }

    public void actualizarRegalo(int idProducto, boolean regalo) {
        productoRepo.actualizarRegalo(idProducto, regalo);
    }

    public void cambiarEstado(int idProducto, boolean estado) {
        productoRepo.cambiarEstado(idProducto, estado);
    }

    public boolean registrarProducto(ProductoDTO dto) {
        try {
            productoRepo.guardarProducto(dto);

            for (VarianteProductoDTO var : dto.getVariante()) {
                try {
                    var.setCodProducto(dto.getCodProducto());
                    varianteRepo.guardarVariante(var);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

}
