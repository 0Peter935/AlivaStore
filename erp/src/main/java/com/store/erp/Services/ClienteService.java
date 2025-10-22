package com.store.erp.Services;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.store.erp.Models.ClienteDTO;
import com.store.erp.Models.ClienteLogDTO;
import com.store.erp.Repo.ClienteRepo;

@Service
public class ClienteService {

    @Autowired
    private ClienteRepo clienteRepo;

    public List<ClienteDTO> listarClientes() {
        return clienteRepo.listarClientes();
    }

    public List<ClienteLogDTO> listarLogsPorCliente(int idCliente) {
        return clienteRepo.listarLogsPorCliente(idCliente);
    }

    public boolean sincronizarCliente(ClienteDTO dto) {
        try {
            clienteRepo.sincronizarClientes(dto);
            return true;
        } catch (Exception e) {
            System.err.println("⚠️ Error en sincronizarCliente(): " + e.getMessage());
            return false;
        }
    }

}
