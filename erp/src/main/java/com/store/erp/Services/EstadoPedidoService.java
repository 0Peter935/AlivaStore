package com.store.erp.Services;

import com.store.erp.Models.EstadoPedidoDTO;
import com.store.erp.Repo.EstadoPedidoRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class EstadoPedidoService {

    @Autowired
    private EstadoPedidoRepo estadoPedidoRepo;

    public List<EstadoPedidoDTO> listarEstadoPedido() {
        return estadoPedidoRepo.listar();
    }

    public EstadoPedidoDTO buscarEstadoPedidoPorId(Integer id) {
        return estadoPedidoRepo.buscarPorId(id);
    }

    public void guardarEstadoPedido(EstadoPedidoDTO estadoPedido) {
        estadoPedidoRepo.guardar(estadoPedido);
    }

    public void actualizarEstadoPedido(EstadoPedidoDTO estadoPedido) {
        estadoPedidoRepo.actualizar(estadoPedido);
    }
}
