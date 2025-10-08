package com.store.erp.Services;

import com.store.erp.Models.DetallePedidoDTO;
import com.store.erp.Repo.DetallePedidoRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DetallePedidoService {

    @Autowired
    private DetallePedidoRepo detallePedidoRepo;

    public List<DetallePedidoDTO> listarDetallePedidos() {
        return detallePedidoRepo.listar();
    }

    public List<DetallePedidoDTO> buscarDetallePedidoPorId(Long id) {
        return detallePedidoRepo.buscarPorId(id);
    }

    public void guardarDetallePedido(DetallePedidoDTO detallePedido) {
        detallePedidoRepo.guardar(detallePedido);
    }

    public void actualizarDetallePedido(DetallePedidoDTO detallePedido) {
        detallePedidoRepo.actualizar(detallePedido);
    }
}
