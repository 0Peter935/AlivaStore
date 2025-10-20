package com.store.erp.Services;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.store.erp.Models.PedidoDTO;
import com.store.erp.Repo.PedidoRepo;

@Service
public class PedidoService {

    @Autowired
    private PedidoRepo pedidoRepo;

    public List<PedidoDTO> listarPedidos() {
        return pedidoRepo.listarPedidos();
    }

    public PedidoDTO obtenerPedidoPorId(int idPedido) {
        return pedidoRepo.obtenerPedidoPorId(idPedido);
    }

    public void registrarPedidoCompleto(PedidoDTO pedido) {
        pedidoRepo.actualizarPedidoCompleto(pedido);
    }
}
