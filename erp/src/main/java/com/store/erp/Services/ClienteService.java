package com.store.erp.Services;

import com.store.erp.Models.ClienteDTO;
import com.store.erp.Repo.ClienteRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ClienteService {

    @Autowired
    private ClienteRepo clienteRepo;

    public List<ClienteDTO> listarClientes() {
        return clienteRepo.listar();
    }

    public ClienteDTO buscarClientePorId(Integer id) {
        return clienteRepo.buscarPorId(id);
    }

    public void guardarCliente(ClienteDTO cliente) {
        clienteRepo.guardar(cliente);
    }

    public void actualizarCliente(ClienteDTO cliente) {
        clienteRepo.actualizar(cliente);
    }
}
