package com.store.erp.Services;

import com.store.erp.Models.ClienteLogDTO;
import com.store.erp.Repo.ClienteLogRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ClienteLogService {

    @Autowired
    private ClienteLogRepo clienteLogRepo;

    public List<ClienteLogDTO> listarClientesLog() {
        return clienteLogRepo.listar();
    }

    public ClienteLogDTO buscarClienteLogPorId(Long id) {
        return clienteLogRepo.buscarPorId(id);
    }

    public void guardarClienteLog(ClienteLogDTO clienteLog) {
        clienteLogRepo.guardar(clienteLog);
    }

    public void actualizarClienteLog(ClienteLogDTO clienteLog) {
        clienteLogRepo.actualizar(clienteLog);
    }
}
