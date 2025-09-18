package com.store.erp.Services;

import com.store.erp.Models.PedidoDTO;
import com.store.erp.Repo.PedidoRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PedidoService {

    @Autowired
    private PedidoRepo pedidoRepo;

    public List<PedidoDTO> listarPedidos() {
        return pedidoRepo.listar();
    }

    public PedidoDTO buscarPedidoPorId(Long id) {
        return pedidoRepo.buscarPorId(id);
    }

    public void guardarPedido(PedidoDTO pedido) {
        pedidoRepo.guardar(pedido);
    }

    public void actualizarPedido(PedidoDTO pedido) {
        pedidoRepo.actualizar(pedido);
    }
}
