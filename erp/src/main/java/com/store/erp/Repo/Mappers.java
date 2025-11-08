package com.store.erp.Repo;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.store.erp.Models.*;

public class Mappers extends RepoUtils {

    public static EmpresaEntregaDTO mapEmpresaEntrega(ResultSet rs) throws SQLException {
        if (!hasColumn(rs, "ID_EMPRESA_ENTREGA")) return null;
        EmpresaEntregaDTO e = new EmpresaEntregaDTO();
        e.setIdEmpresaEntrega(safeInt(rs, "ID_EMPRESA_ENTREGA"));
        e.setRazonSocial(safeString(rs, "RAZON_SOCIAL"));
        e.setRuc(safeString(rs, "RUC"));
        e.setDireccionFiscal(safeString(rs, "DIRECCION_FISCAL"));

        // Zona (si existe)
        ZonaEmpresaEntregaDTO z = new ZonaEmpresaEntregaDTO();
        z.setId(safeInt(rs, "ID_ZONA"));
        z.setDescripcion(safeString(rs, "DESC_ZONA"));
        e.setZona(z);

        return e;
    }

    public static ZonaEmpresaEntregaDTO mapZona(ResultSet rs) throws SQLException {
        if (!hasColumn(rs, "ID_ZONA") && !hasColumn(rs, "ID")) return null;

        return new ZonaEmpresaEntregaDTO(
            hasColumn(rs, "ID_ZONA") ? safeInt(rs, "ID_ZONA") : safeInt(rs, "ID"),
            hasColumn(rs, "DESC_ZONA") ? safeString(rs, "DESC_ZONA") : safeString(rs, "DESCRIPCION")
        );
    }

    public static TipoAlmacenDTO mapTipoAlmacen(ResultSet rs) throws SQLException {
        if (!hasColumn(rs, "ID_TIPO_ALMACEN")) return null;
        return new TipoAlmacenDTO(
            safeInt(rs, "ID_TIPO_ALMACEN"),
            safeString(rs, "DESC_TIPO_ALMACEN")
        );
    }

    public static AlmacenDTO mapAlmacen(ResultSet rs) throws SQLException {
        if (!hasColumn(rs, "ID_ALMACEN")) return null;
        AlmacenDTO a = new AlmacenDTO();
        a.setIdAlmacen(safeInt(rs, "ID_ALMACEN"));
        a.setDescripcion(safeString(rs, "DESC_ALMACEN") != null ? safeString(rs, "DESC_ALMACEN") : safeString(rs, "NOMBRE_ALMACEN"));
        a.setEmpresaEntrega(mapEmpresaEntrega(rs));
        a.setTipoAlmacen(mapTipoAlmacen(rs));
        return a;
    }

    public static ProductoDTO mapProducto(ResultSet rs) throws SQLException {
        ProductoDTO p = new ProductoDTO();
        p.setIdProducto(safeInt(rs, "ID_PRODUCTO"));
        p.setCodProducto(safeString(rs, "COD_PRODUCTO"));
        p.setDescProducto(safeString(rs, "DESC_PRODUCTO"));
        p.setStock(safeInt(rs, "STOCK"));
        p.setPrecio(safeDouble(rs, "PRECIO"));
        p.setImagen(safeString(rs, "IMAGEN"));
        p.setRegalo(safeBool(rs, "REGALO"));
        p.setEstado(safeBool(rs, "ESTADO"));
        p.setFechaRegistro(safeDate(rs, "FECHA_REGISTRO"));
        return p;
    }

    public static UsuarioDTO mapUsuario(ResultSet rs) throws SQLException {
        UsuarioDTO u = new UsuarioDTO();
        u.setIdUsuario(safeInt(rs, "ID_USUARIO"));
        u.setUsuario(safeString(rs, "USUARIO"));
        u.setCorreo(safeString(rs, "CORREO"));
        u.setClave(safeString(rs, "CLAVE"));
        u.setTelefono(safeString(rs, "TELEFONO"));
        u.setEstado(safeBool(rs, "ESTADO"));
        u.setNombre(safeString(rs, "NOMBRES"));
        u.setApPaterno(safeString(rs, "APELLIDO_PATERNO"));
        u.setApMaterno(safeString(rs, "APELLIDO_MATERNO"));
        u.setFechaRegistro(safeDate(rs, "FECHA_REGISTRO"));

        RolDTO rol = new RolDTO(
            safeInt(rs, "ID_ROL"),
            safeString(rs, "ROL_DESC")
        );

        u.setRol(rol);
        return u;
    }

    public static RolDTO mapRol(ResultSet rs) throws SQLException {
        if (!hasColumn(rs, "ID_ROL")) return null;

        RolDTO rol = new RolDTO(
                safeInt(rs, "ID_ROL"),
                safeString(rs, "DESCRIPCION")
            );
        return rol;
    }


    public static ClienteDTO mapCliente(ResultSet rs) throws SQLException {
        ClienteDTO c = new ClienteDTO();
        c.setIdCliente(safeInt(rs, "ID_CLIENTE"));
        c.setCodigoCliente(safeString(rs, "COD_CLIENTE"));
        c.setNombres(safeString(rs, "NOMBRE_COMPLETO"));
        c.setTelefono(safeString(rs, "TELEFONO"));
        c.setCorreo(safeString(rs, "CORREO"));
        
        return c;
    }

    public static ClienteLogDTO mapClienteLog(ResultSet rs) throws SQLException {
        ClienteLogDTO cl = new ClienteLogDTO();
        cl.setIdClienteLog(safeInt(rs, "ID_CLIENTE_LOG"));
        cl.setIdCliente(safeInt(rs, "ID_CLIENTE"));
        cl.setActividad(safeString(rs, "ACTIVIDAD"));
        cl.setFechaActividad(safeString(rs, "FECHA_ACTIVIDAD"));
        return cl;
    }

    

    public static EstadoPedidoDTO mapEstadoPedido(ResultSet rs) throws SQLException {
        EstadoPedidoDTO e = new EstadoPedidoDTO();
        e.setIdEstadoPedido(safeInt(rs, "ID_ESTADO_PEDIDO"));
        e.setDescripcion(safeString(rs, "DESC_ESTADO"));
        return e;
    }

    // ==========================================================
    // COMBINADOS (RELACIONES)
    // ==========================================================

    public static AlmacenStockDTO mapAlmacenStock(ResultSet rs) throws SQLException {
        if (!hasColumn(rs, "ID_ALMACEN_STOCK")) return null;
        return new AlmacenStockDTO(
            safeInt(rs, "ID_ALMACEN_STOCK"),
            safeInt(rs, "ID_PRODUCTO"),
            safeInt(rs, "INVENTARIO"),
            mapAlmacen(rs)
        );
    }

    public static PedidoDetalleDTO mapDetallePedido(ResultSet rs) throws SQLException {
        PedidoDetalleDTO d = new PedidoDetalleDTO();
        d.setIdDetallePedido(safeInt(rs, "ID_DETALLE_P"));
        d.setCantidad(safeInt(rs, "CANTIDAD"));
        d.setPrecioUnitario(safeDouble(rs, "PRECIO_UNITARIO"));
        d.setPrecioTotal(safeDouble(rs, "PRECIO_TOTAL"));
        d.setProducto(mapProducto(rs));
        return d;
    }

    public static PedidoDTO mapPedido(ResultSet rs) throws SQLException {
        PedidoDTO p = new PedidoDTO();
        p.setIdPedido(safeInt(rs, "ID_PEDIDO"));
        p.setCodigoPedido(safeString(rs, "CODIGO_PEDIDO"));
        p.setDocumento(safeString(rs, "DOCUMENTO"));
        p.setSubtotal(safeDouble(rs, "SUBTOTAL"));
        p.setIgv(safeDouble(rs, "IGV"));
        p.setAdelanto(safeDouble(rs, "ADELANTO"));
        p.setMontoTotal(safeDouble(rs, "MONTO_TOTAL"));
        p.setCiudad(safeString(rs, "CIUDAD"));
        p.setTipoPago(safeString(rs, "TIPO_PAGO"));
        p.setTipoComprobante(safeString(rs, "TIPO_COMPROBANTE"));
        p.setMontoCobrado(safeDouble(rs, "MONTO_COBRADO"));
        p.setObservacion(safeString(rs, "OBSERVACION"));
        p.setEvidencia(safeString(rs, "EVIDENCIA"));
        p.setFechaRegistro(safeDateTime(rs, "FECHA_REGISTRO"));

        p.setUsuario(mapUsuario(rs));
        p.setCliente(mapCliente(rs));
        p.setEmpresaEntrega(mapEmpresaEntrega(rs));
        p.setEstadoPedido(mapEstadoPedido(rs));

        p.setDetalles(new ArrayList<>());
        return p;
    }

    public static List<PedidoDTO> mapPedidosConDetalles(ResultSet rs) throws SQLException {
        Map<Integer, PedidoDTO> pedidosMap = new LinkedHashMap<>();

        while (rs.next()) {
            int idPedido = safeInt(rs, "ID_PEDIDO");

            PedidoDTO pedido = pedidosMap.computeIfAbsent(idPedido, _ -> {
                try {
                    return mapPedido(rs);
                } catch (SQLException e) {
                    e.printStackTrace();
                    return null;
                }
            });

            int idDetalle = safeInt(rs, "ID_DETALLE_P");
            if (idDetalle > 0) {
                pedido.getDetalles().add(mapDetallePedido(rs));
            }
        }

        return new ArrayList<>(pedidosMap.values());
    }

    public static PedidoDTO mapPedidoConDetalle(ResultSet rs) throws SQLException {
        Map<Integer, PedidoDTO> pedidosMap = new LinkedHashMap<>();
        int idPedido = safeInt(rs, "ID_PEDIDO");

        PedidoDTO pedido = pedidosMap.computeIfAbsent(idPedido, _ -> {
            try {
                return mapPedido(rs);
            } catch (SQLException e) {
                e.printStackTrace();
                return null;
            }
        });

        int idDetalle = safeInt(rs, "ID_DETALLE_P");
        if (idDetalle > 0) {
            pedido.getDetalles().add(mapDetallePedido(rs));
        }

        return pedido;
    }


}
