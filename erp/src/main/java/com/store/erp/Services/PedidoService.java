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

    public PedidoDTO obtenerPedidoPorCod(String codPedido) {
        return pedidoRepo.obtenerPedidoPorCod(codPedido);
    }

    public void registrarPedidoCompleto(PedidoDTO pedido) {
        pedidoRepo.actualizarPedidoCompleto(pedido);
    }

    public boolean registrarPedido(PedidoDTO dto) {
        try {
            pedidoRepo.guardarPedidoCompleto(dto);
            return true;

        } catch (Exception ex) {
            System.err.println("Error al registrar pedido en repo: " + ex.getMessage());
            if (ex.getCause() != null)
                System.err.println("Causa interna: " + ex.getCause().getMessage());
            ex.printStackTrace();
            throw ex;
        }
    }

}