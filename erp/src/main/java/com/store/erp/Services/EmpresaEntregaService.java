package com.store.erp.Services;

import com.store.erp.Models.EmpresaEntregaDTO;
import com.store.erp.Repo.EmpresaEntregaRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class EmpresaEntregaService {

    @Autowired
    private EmpresaEntregaRepo empresaEntregaRepo;

    public List<EmpresaEntregaDTO> listarEmpresaEntrega() {
        return empresaEntregaRepo.listar();
    }

    public EmpresaEntregaDTO buscarEmpresaEntregaPorId(Integer id) {
        return empresaEntregaRepo.buscarPorId(id);
    }

    public void guardarEmpresaEntrega(EmpresaEntregaDTO empresaEntrega) {
        empresaEntregaRepo.guardar(empresaEntrega);
    }

    public void actualizarEmpresaEntrega(EmpresaEntregaDTO empresaEntrega) {
        empresaEntregaRepo.actualizar(empresaEntrega);
    }
}
