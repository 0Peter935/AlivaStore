package com.store.erp.Controllers;

import com.store.erp.Models.RolDTO;
import com.store.erp.Services.RolService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/roles")
public class RolController {

    @Autowired
    private RolService rolService;

    @GetMapping("/")
    public List<RolDTO> listarRoles() {
        return rolService.listarRoles();
    }

    @GetMapping("/{id}")
    public RolDTO buscarRolPorId(@PathVariable Short id) {
        return rolService.buscarRolPorId(id);
    }
}
