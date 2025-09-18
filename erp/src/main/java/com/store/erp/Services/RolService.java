package com.store.erp.Services;

import com.store.erp.Models.RolDTO;
import com.store.erp.Repo.RolRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RolService {

    @Autowired
    private RolRepo rolRepo;

    public List<RolDTO> listarRoles() {
        return rolRepo.listar();
    }

    public RolDTO buscarRolPorId(Short id) {
        return rolRepo.buscarPorId(id);
    }
}
