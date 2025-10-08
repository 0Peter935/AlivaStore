package com.store.erp.Repo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import com.store.erp.Models.ClienteDTO;
import com.store.erp.Models.EmpresaEntregaDTO;
import com.store.erp.Models.EstadoPedidoDTO;
import com.store.erp.Models.PedidoDTO;
import com.store.erp.Models.ProductoRegaloDTO;
import com.store.erp.Models.VendedorDTO;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

@Repository
public class PedidoRepo {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private PedidoDTO mapRowToPedido(ResultSet rs, int rowNum) throws SQLException {
        PedidoDTO pedido = new PedidoDTO();

        // Campos del Pedido
        pedido.setIdPedido(rs.getLong("ID_PEDIDO"));
        pedido.setDocumento(rs.getString("DOCUMENTO"));
        pedido.setSubtotal(rs.getBigDecimal("SUBTOTAL"));
        pedido.setIgv(rs.getBigDecimal("IGV"));
        pedido.setAdelanto(rs.getInt("ADELANTO"));
        pedido.setMontoTotal(rs.getBigDecimal("MONTO_TOTAL"));
        pedido.setCiudad(rs.getString("CIUDAD"));
        pedido.setTipoPago(rs.getString("TIPO_PAGO"));
        pedido.setTipoComprobante(rs.getString("TIPO_COMPROBANTE"));
        pedido.setMontoCobrado(rs.getBigDecimal("MONTO_COBRADO"));
        pedido.setObservacion(rs.getString("OBSERVACION"));

        // Cliente
        ClienteDTO cliente = new ClienteDTO();
        cliente.setIdCliente(rs.getInt("ID_CLIENTE"));
        cliente.setNombreCompleto(rs.getString("ClienteNombre"));
        cliente.setTelefono(rs.getString("ClienteTelefono"));
        if (rs.getTimestamp("ClienteFechaRegistro") != null) {
            cliente.setFechaRegistro(rs.getTimestamp("ClienteFechaRegistro").toLocalDateTime());
        }
        if (rs.getTimestamp("ClienteFechaActualizacion") != null) {
            cliente.setFechaActualizacion(rs.getTimestamp("ClienteFechaActualizacion").toLocalDateTime());
        }
        pedido.setCliente(cliente);

        // Vendedor
        VendedorDTO vendedor = new VendedorDTO();
        vendedor.setIdVendedor(rs.getInt("ID_VENDEDOR"));
        vendedor.setCodVendedor(rs.getString("COD_VENDEDOR"));
        vendedor.setNombreVendedor(rs.getString("NOMBRE_VENDEDOR"));
        vendedor.setEstado(rs.getBoolean("VendedorEstado"));
        pedido.setVendedor(vendedor);

        // Estado Pedido
        EstadoPedidoDTO estado = new EstadoPedidoDTO();
        estado.setIdEstado(rs.getInt("EstadoId"));
        estado.setDescripcion(rs.getString("EstadoDescripcion"));
        pedido.setEstadoPedido(estado);

        // Empresa de Entrega
        EmpresaEntregaDTO empresa = new EmpresaEntregaDTO();
        empresa.setIdEmpresaEntrega(rs.getInt("ID_EMPRESA_ENTREGA"));
        empresa.setRazonSocial(rs.getString("RAZON_SOCIAL"));
        empresa.setRuc(rs.getString("RUC"));
        empresa.setDireccionFiscal(rs.getString("DIRECCION_FISCAL"));
        pedido.setEmpresaEntrega(empresa);

        // Producto Regalo
        ProductoRegaloDTO regalo = new ProductoRegaloDTO();
        regalo.setIdRegalo(rs.getInt("ID_REGALO"));
        regalo.setCodProducto(rs.getString("RegaloCodigo"));
        regalo.setDescProducto(rs.getString("RegaloDescripcion"));
        regalo.setImagen(rs.getString("RegaloImagen"));
        if (rs.getTimestamp("RegaloFechaRegistro") != null) {
            regalo.setFechaRegistro(rs.getTimestamp("RegaloFechaRegistro").toLocalDateTime());
        }
        regalo.setEstadoRegalo(rs.getBoolean("ESTADO_REGALO"));
        pedido.setRegalo(regalo);

        return pedido;
    }

    // Listar todos los pedidos
    public List<PedidoDTO> listar() {
        return jdbcTemplate.query("EXEC ListarPedidos", this::mapRowToPedido);
    }
    
    public PedidoDTO buscarPorId(Long id) {
        return jdbcTemplate.queryForObject("EXEC BuscarPedidoPorId ?", this::mapRowToPedido, id);
    }
}
