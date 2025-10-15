package com.store.erp.Services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.store.erp.Models.EmpresaEntregaDTO;
import com.store.erp.Repo.EmpresaEntregaRepo;

import java.util.List;

@Service
public class EmpresaEntregaService {

    @Autowired
    private EmpresaEntregaRepo empresaRepo;

    public List<EmpresaEntregaDTO> listarEmpresas() {
        return empresaRepo.listarEmpresas();
    }

    public EmpresaEntregaDTO buscarPorId(int idEmpresaEntrega) {
        return empresaRepo.buscarPorId(idEmpresaEntrega);
    }
}
