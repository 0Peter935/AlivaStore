package com.store.erp.Controllers;

import com.store.erp.Models.VendedorDTO;
import com.store.erp.Services.VendedorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/vendedores")
public class VendedorController {

    @Autowired
    private VendedorService vendedorService;

    @GetMapping("/")
    public List<VendedorDTO> listarVendedores() {
        return vendedorService.listarVendedores();
    }

    @GetMapping("/{id}")
    public VendedorDTO buscarVendedorPorId(@PathVariable Integer id) {
        return vendedorService.buscarVendedorPorId(id);
    }

    @PostMapping("/")
    public void guardarVendedor(@RequestBody VendedorDTO vendedor) {
        vendedorService.guardarVendedor(vendedor);
    }

    @PutMapping("/{id}")
    public void actualizarVendedor(@PathVariable Integer id, @RequestBody VendedorDTO vendedor) {
        vendedor.setIdVendedor(id);
        vendedorService.actualizarVendedor(vendedor);
    }
}
