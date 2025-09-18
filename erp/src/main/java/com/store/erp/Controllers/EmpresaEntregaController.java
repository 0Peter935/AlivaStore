package com.store.erp.Controllers;

import com.store.erp.Models.EmpresaEntregaDTO;
import com.store.erp.Services.EmpresaEntregaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/empresa-entrega")
public class EmpresaEntregaController {

    @Autowired
    private EmpresaEntregaService empresaEntregaService;

    @GetMapping("/")
    public List<EmpresaEntregaDTO> listarEmpresaEntrega() {
        return empresaEntregaService.listarEmpresaEntrega();
    }

    @GetMapping("/{id}")
    public EmpresaEntregaDTO buscarEmpresaEntregaPorId(@PathVariable Integer id) {
        return empresaEntregaService.buscarEmpresaEntregaPorId(id);
    }

    @PostMapping("/")
    public void guardarEmpresaEntrega(@RequestBody EmpresaEntregaDTO empresaEntrega) {
        empresaEntregaService.guardarEmpresaEntrega(empresaEntrega);
    }

    @PutMapping("/{id}")
    public void actualizarEmpresaEntrega(@PathVariable Integer id, @RequestBody EmpresaEntregaDTO empresaEntrega) {
        empresaEntrega.setIdEmpresaEntrega(id);
        empresaEntregaService.actualizarEmpresaEntrega(empresaEntrega);
    }
}
