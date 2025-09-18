package com.store.erp.Controllers;

import com.store.erp.Models.ClienteDTO;
import com.store.erp.Services.ClienteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/clientes")
public class ClienteController {

    @Autowired
    private ClienteService clienteService;

    @GetMapping("/")
    public List<ClienteDTO> listarClientes() {
        return clienteService.listarClientes();
    }

    @GetMapping("/{id}")
    public ClienteDTO buscarClientePorId(@PathVariable Integer id) {
        return clienteService.buscarClientePorId(id);
    }

    @PostMapping("/")
    public void guardarCliente(@RequestBody ClienteDTO cliente) {
        clienteService.guardarCliente(cliente);
    }

    @PutMapping("/{id}")
    public void actualizarCliente(@PathVariable Integer id, @RequestBody ClienteDTO cliente) {
        cliente.setIdCliente(id);
        clienteService.actualizarCliente(cliente);
    }
}
