package com.store.erp.Services;

import com.store.erp.Models.VendedorDTO;
import com.store.erp.Repo.VendedorRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class VendedorService {

    @Autowired
    private VendedorRepo vendedorRepo;

    public List<VendedorDTO> listarVendedores() {
        return vendedorRepo.listar();
    }

    public VendedorDTO buscarVendedorPorId(Integer id) {
        return vendedorRepo.buscarPorId(id);
    }

    public void guardarVendedor(VendedorDTO vendedor) {
        vendedorRepo.guardar(vendedor);
    }

    public void actualizarVendedor(VendedorDTO vendedor) {
        vendedorRepo.actualizar(vendedor);
    }
}
