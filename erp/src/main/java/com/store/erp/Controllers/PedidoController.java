package com.store.erp.Controllers;

import com.store.erp.Models.DetallePedidoDTO;
import com.store.erp.Models.PedidoDTO;
import com.store.erp.Services.DetallePedidoService;
import com.store.erp.Services.PedidoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/pedidos")
public class PedidoController {

    @Autowired
    private PedidoService pedidoService;

    @Autowired
    private DetallePedidoService detallePedidoService;

    @GetMapping
    public List<PedidoDTO> listarPedidos() {
        return pedidoService.listarPedidos();
    }

    @GetMapping("/{id}")
    public PedidoDTO obtenerPedido(@PathVariable("id") Long id) {
        return pedidoService.buscarPedidoPorId(id);
    }

    @GetMapping("/{id}/detalle")
public List<DetallePedidoDTO> obtenerDetalle(@PathVariable("id") Long id) {
    return detallePedidoService.buscarDetallePedidoPorId(id);
}


}
