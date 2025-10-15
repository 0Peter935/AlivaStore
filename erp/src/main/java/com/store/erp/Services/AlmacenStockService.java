package com.store.erp.Services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.store.erp.Models.AlmacenStockDTO;
import com.store.erp.Repo.AlmacenStockRepo;

import java.util.List;

@Service
public class AlmacenStockService {

    @Autowired
    private AlmacenStockRepo almacenStockRepo;

    public List<AlmacenStockDTO> listarPorProducto(int idProducto) {
        return almacenStockRepo.listarPorProducto(idProducto);
    }


    public void guardarStockProducto(int idProducto, List<AlmacenStockDTO> detalleStock) {
        almacenStockRepo.guardarStockProducto(idProducto, detalleStock);
    }
}
