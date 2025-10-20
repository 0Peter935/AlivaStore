package com.store.erp.Controllers;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.store.erp.Models.EmpresaEntregaDTO;
import com.store.erp.Services.EmpresaEntregaService;

import java.util.List;

@RestController
@RequestMapping("/api/empresas-entrega")
public class EmpresaEntregaController {

    @Autowired
    private EmpresaEntregaService empresaEntregaService;

    @GetMapping
    public List<EmpresaEntregaDTO> listarEmpresas() {
        return empresaEntregaService.listarEmpresas();
    }

}

