package com.store.erp.Services;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.store.erp.Models.ClienteDTO;
import com.store.erp.Models.PedidoDTO;
import com.store.erp.Repo.ClienteRepo;
import com.store.erp.Repo.PedidoRepo;
import com.store.erp.Repo.ProductoRepo;

@Service
public class PedidoService {

    @Autowired
    private PedidoRepo pedidoRepo;

    @Autowired
    private ClienteRepo clienteRepo;

    @Autowired
    private ProductoRepo productoRepo;

    public List<PedidoDTO> listarPedidos() {
        return pedidoRepo.listarPedidos();
    }

    public PedidoDTO obtenerPedidoPorId(int idPedido) {
        return pedidoRepo.obtenerPedidoPorId(idPedido);
    }

    public void registrarPedidoCompleto(PedidoDTO pedido) {
        pedidoRepo.actualizarPedidoCompleto(pedido);
    }

    public boolean sincronizarPedido(PedidoDTO dto) {
        try {
            dto.getDetalles().forEach(det -> {
                det.setProducto(productoRepo.obtenerPorCod(det.getProducto().getCodProducto()));
            });
            dto.setCliente(clienteRepo.ObtenerPorCodigo(dto.getCliente().getCodigoCliente()));
            pedidoRepo.sincronizarPedidos(dto);
            return true;
        } catch (Exception ex) {
            System.err.println("💥 [SERVICE ERROR] Error al sincronizar pedido en repo: " + ex.getMessage());
            if (ex.getCause() != null)
                System.err.println("📄 Causa interna: " + ex.getCause().getMessage());
            ex.printStackTrace();
            throw ex;
        }
    }
}
