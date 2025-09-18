package com.store.erp.Controllers;

import com.store.erp.Models.ClienteLogDTO;
import com.store.erp.Services.ClienteLogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/clientes-log")
public class ClienteLogController {

    @Autowired
    private ClienteLogService clienteLogService;

    @GetMapping("/")
    public List<ClienteLogDTO> listarClientesLog() {
        return clienteLogService.listarClientesLog();
    }

    @GetMapping("/{id}")
    public ClienteLogDTO buscarClienteLogPorId(@PathVariable Long id) {
        return clienteLogService.buscarClienteLogPorId(id);
    }

    @PostMapping("/")
    public void guardarClienteLog(@RequestBody ClienteLogDTO clienteLog) {
        clienteLogService.guardarClienteLog(clienteLog);
    }

    @PutMapping("/{id}")
    public void actualizarClienteLog(@PathVariable Long id, @RequestBody ClienteLogDTO clienteLog) {
        clienteLog.setIdClienteLog(id);
        clienteLogService.actualizarClienteLog(clienteLog);
    }
}
