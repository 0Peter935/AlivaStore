package com.store.erp.Services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.store.erp.Models.ZonaEmpresaEntregaDTO;
import com.store.erp.Repo.ZonaEmpresaEntregaRepo;

import java.util.List;

@Service
public class ZonaEmpresaEntregaService {

    @Autowired
    private ZonaEmpresaEntregaRepo zonaRepo;

    public List<ZonaEmpresaEntregaDTO> listarZonas() {
        return zonaRepo.listarZonas();
    }

    public ZonaEmpresaEntregaDTO buscarPorId(int id) {
        return zonaRepo.buscarPorId(id);
    }
}
