package com.store.erp.Repo;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.store.erp.Models.*;

public class Mappers extends RepoUtils {

    // =====================================================
    // Mapeo de EMPRESA DE ENTREGA
    // =====================================================
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

    // =====================================================
    // Mapeo de ZONA DE EMPRESA DE ENTREGA
    // =====================================================
    public static ZonaEmpresaEntregaDTO mapZona(ResultSet rs) throws SQLException {
        if (!hasColumn(rs, "ID_ZONA") && !hasColumn(rs, "ID")) return null;

        return new ZonaEmpresaEntregaDTO(
            hasColumn(rs, "ID_ZONA") ? safeInt(rs, "ID_ZONA") : safeInt(rs, "ID"),
            hasColumn(rs, "DESC_ZONA") ? safeString(rs, "DESC_ZONA") : safeString(rs, "DESCRIPCION")
        );
    }

    // =====================================================
    // Mapeo de TIPO DE ALMACEN
    // =====================================================
    public static TipoAlmacenDTO mapTipoAlmacen(ResultSet rs) throws SQLException {
        if (!hasColumn(rs, "ID_TIPO_ALMACEN")) return null;
        return new TipoAlmacenDTO(
            safeInt(rs, "ID_TIPO_ALMACEN"),
            safeString(rs, "DESC_TIPO_ALMACEN")
        );
    }

    // =====================================================
    // Mapeo de ALMACEN
    // =====================================================
    public static AlmacenDTO mapAlmacen(ResultSet rs) throws SQLException {
        if (!hasColumn(rs, "ID_ALMACEN")) return null;
        AlmacenDTO a = new AlmacenDTO();
        a.setIdAlmacen(safeInt(rs, "ID_ALMACEN"));
        a.setDescripcion(safeString(rs, "DESC_ALMACEN") != null ? safeString(rs, "DESC_ALMACEN") : safeString(rs, "NOMBRE_ALMACEN"));
        a.setEmpresaEntrega(mapEmpresaEntrega(rs));
        a.setTipoAlmacen(mapTipoAlmacen(rs));
        return a;
    }

    // =====================================================
    // Mapeo de PRODUCTO
    // =====================================================
    public static ProductoDTO mapProducto(ResultSet rs) throws SQLException {
        ProductoDTO p = new ProductoDTO();

        p.setIdProducto(safeInt(rs, "ID_PRODUCTO"));
        p.setCodProducto(safeString(rs, "COD_PRODUCTO"));
        p.setDescProducto(safeString(rs, "DESC_PRODUCTO"));
        p.setImg(safeString(rs, "IMAGEN"));
        p.setRegalo(safeBool(rs, "REGALO"));
        p.setEstado(safeBool(rs, "ESTADO"));
        p.setFechaReg(safeOffsetDateTime(rs, "FECHA_REGISTRO"));
        p.setFechaAct(safeOffsetDateTime(rs, "FECHA_ACTUALIZACION"));
        p.setVariante(new ArrayList<>());

        return p;
    }

    // =====================================================
    // Mapeo de VARIANTE PRODUCTO
    // =====================================================
    public static VarianteProductoDTO mapVariante(ResultSet rs) throws SQLException {
        VarianteProductoDTO v = new VarianteProductoDTO();

        v.setIdVariante(safeInt(rs, "ID_VARIANTE"));
        v.setCodProducto(safeString(rs, "COD_PRODUCTO"));
        v.setCodVariante(safeString(rs, "COD_VARIANTE"));
        v.setTitulo(safeString(rs, "TITULO"));
        v.setPrecio(safeDouble(rs, "PRECIO"));
        v.setImgVariante(safeString(rs, "IMG_VARIANTE"));
        v.setFechaReg(safeOffsetDateTime(rs, "FECHA_REGISTRO"));
        v.setFechaAct(safeOffsetDateTime(rs, "FECHA_ACTUALIZACION"));

        // No cargamos almacenes aquí — se pueden asociar por otro método
        v.setAlmacenStock(new ArrayList<>());

        return v;
    }

    // =====================================================
    // Mapeo de USUARIO
    // =====================================================
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

    // =====================================================
    // Mapeo de ROL
    // =====================================================
    public static RolDTO mapRol(ResultSet rs) throws SQLException {
        if (!hasColumn(rs, "ID_ROL")) return null;

        RolDTO rol = new RolDTO(
                safeInt(rs, "ID_ROL"),
                safeString(rs, "DESCRIPCION")
            );
        return rol;
    }

    // =====================================================
    // Mapeo de CLIENTE
    // =====================================================
    public static ClienteDTO mapCliente(ResultSet rs) throws SQLException {
        ClienteDTO c = new ClienteDTO();
        c.setIdCliente(safeInt(rs, "ID_CLIENTE"));
        c.setCodigoCliente(safeString(rs, "COD_CLIENTE"));
        c.setNombres(safeString(rs, "NOMBRE_COMPLETO"));
        c.setDni(safeString(rs, "DNI"));
        c.setCorreo(safeString(rs, "CORREO"));
        c.setTelefono(safeString(rs, "TELEFONO"));
        c.setCanOrdenes(safeInt(rs, "CANTIDAD_ORDENES"));
        c.setDireccion(safeString(rs, "DIRECCION"));
        c.setCiudad(safeString(rs, "CIUDAD_CLIENTE"));
        c.setProvincia(safeString(rs, "PROVINCIA"));
        c.setPais(safeString(rs, "PAIS"));
        c.setFechaReg(safeOffsetDateTime(rs, "FECHA_REGISTRO"));
        c.setFechaAct(safeOffsetDateTime(rs, "FECHA_ACTUALIZACION"));
        
        return c;
    }

    // =====================================================
    // Mapeo de CLIENTE LOG
    // =====================================================
    public static ClienteLogDTO mapClienteLog(ResultSet rs) throws SQLException {
        ClienteLogDTO cl = new ClienteLogDTO();
        cl.setIdClienteLog(safeInt(rs, "ID_CLIENTE_LOG"));
        cl.setIdCliente(safeInt(rs, "ID_CLIENTE"));
        cl.setActividad(safeString(rs, "ACTIVIDAD"));
        cl.setFechaActividad(safeString(rs, "FECHA_ACTIVIDAD"));
        return cl;
    }

    // =====================================================
    // Mapeo de ESTADO DEL PEDIDO
    // =====================================================
    public static EstadoPedidoDTO mapEstadoPedido(ResultSet rs) throws SQLException {
        EstadoPedidoDTO e = new EstadoPedidoDTO();
        e.setIdEstadoPedido(safeInt(rs, "ID_ESTADO_PEDIDO"));
        e.setDescripcion(safeString(rs, "DESC_ESTADO"));
        return e;
    }

    // =====================================================
    // Mapeo de STOCK EN ALMACEN
    // =====================================================
    public static AlmacenStockDTO mapAlmacenStock(ResultSet rs) throws SQLException {
        if (!hasColumn(rs, "ID_ALMACEN_STOCK")) return null;
        return new AlmacenStockDTO(
            safeInt(rs, "ID_ALMACEN_STOCK"),
            safeString(rs, "COD_VARIANTE"),
            safeInt(rs, "INVENTARIO"),
            mapAlmacen(rs)
        );
    }

    // =====================================================
    // Mapeo de PEDIDO
    // =====================================================
    public static PedidoDTO mapPedido(ResultSet rs) throws SQLException {
        PedidoDTO p = new PedidoDTO();

        // Campos propios del pedido
        p.setIdPedido(safeInt(rs, "ID_PEDIDO"));
        p.setCodPedido(safeString(rs, "COD_PEDIDO"));
        p.setDocumento(safeString(rs, "DOCUMENTO"));
        p.setSubtotal(safeDouble(rs, "SUBTOTAL"));
        p.setIgv(safeDouble(rs, "IGV"));
        p.setMontoTotal(safeDouble(rs, "MONTO_TOTAL"));
        p.setCiudad(safeString(rs, "CIUDAD"));
        p.setTipoPago(safeString(rs, "TIPO_PAGO"));
        p.setTipoComprobante(safeString(rs, "TIPO_COMPROBANTE"));
        p.setAdelanto(safeBool(rs, "ADELANTO"));
        p.setMontoAdelanto(safeDouble(rs, "MONTO_ADELANTO"));
        p.setObservacion(safeString(rs, "OBSERVACION"));
        p.setEvidencia(safeString(rs, "EVIDENCIA"));
        p.setFechaReg(safeOffsetDateTime(rs, "FECHA_REGISTRO"));

        p.setUsuario(mapUsuario(rs));
        p.setCliente(mapCliente(rs));
        p.setEmpresaEntrega(mapEmpresaEntrega(rs));
        p.setEstadoPedido(mapEstadoPedido(rs));

        p.setNotas(new ArrayList<>());
        p.setDetalles(new ArrayList<>());

        return p;
    }

    // =====================================================
    // Mapeo de DETALLE DE PEDIDO
    // =====================================================
    public static PedidoDetalleDTO mapDetallePedido(ResultSet rs) throws SQLException {
        PedidoDetalleDTO d = new PedidoDetalleDTO();

        d.setIdDetallePedido(safeInt(rs, "ID_DETALLE_P"));
        d.setCodPedido(safeString(rs, "COD_PEDIDO"));
        d.setCodProducto(safeString(rs, "COD_PRODUCTO"));
        d.setCodVariante(safeString(rs, "COD_VARIANTE"));
        d.setNombreProducto(safeString(rs, "NOMBRE_PRODUCTO"));
        d.setCantidad(safeInt(rs, "CANTIDAD"));
        d.setPrecioUnitario(safeDouble(rs, "PRECIO_UNITARIO"));
        d.setPrecioTotal(safeDouble(rs, "PRECIO_TOTAL"));

        return d;
    }

    // =====================================================
    // Mapeo de NOTAS DE PEDIDO
    // =====================================================
    public static PedidoNotaDTO mapNotaPedido(ResultSet rs) throws SQLException {
        PedidoNotaDTO n = new PedidoNotaDTO();

        n.setIdNotaPedido(safeInt(rs, "ID_NOTA_PEDIDO"));
        n.setCodPedido(safeString(rs, "COD_PEDIDO"));
        n.setTitulo(safeString(rs, "TITULO"));
        n.setDescripcion(safeString(rs, "DESCRIPCION"));

        return n;
    }

    // =====================================================
    // Mapeo de PEDIDOS CON DETALLES + NOTAS
    // =====================================================
    public static List<PedidoDTO> mapPedidosConDetalles(ResultSet rs) throws SQLException {
        Map<Integer, PedidoDTO> pedidosMap = new LinkedHashMap<>();

        while (rs.next()) {

            int idPedido = safeInt(rs, "ID_PEDIDO");

            PedidoDTO pedido = pedidosMap.get(idPedido);
            if (pedido == null) {
                pedido = mapPedido(rs);
                pedidosMap.put(idPedido, pedido);
            }

            // --- DETALLES ---
            int idDetalle = safeInt(rs, "ID_DETALLE_P");
            if (idDetalle > 0) {
                pedido.getDetalles().add(mapDetallePedido(rs));
            }

            // --- NOTAS ---
            int idNota = safeInt(rs, "ID_NOTA_PEDIDO");
            if (idNota > 0) {
                pedido.getNotas().add(mapNotaPedido(rs));
            }
        }

        return new ArrayList<>(pedidosMap.values());
    }

    // =====================================================
    // Mapeo de PEDIDO CON DETALLES + NOTAS (unitario)
    // =====================================================
    public static PedidoDTO mapPedidoConDetalle(ResultSet rs) throws SQLException {
        PedidoDTO pedido = null;

        while (rs.next()) {

            if (pedido == null) {
                pedido = mapPedido(rs);
            }

            // --- DETALLES ---
            int idDetalle = safeInt(rs, "ID_DETALLE_P");
            if (idDetalle > 0) {
                pedido.getDetalles().add(mapDetallePedido(rs));
            }

            // --- NOTAS ---
            int idNota = safeInt(rs, "ID_NOTA_PEDIDO");
            if (idNota > 0) {
                pedido.getNotas().add(mapNotaPedido(rs));
            }
        }
        
        return pedido;
    }

    // =====================================================
    // Mapeo de PRODUCTO CON VARIANTES Y INVENTARIO
    // =====================================================
    public static List<ProductoDTO> mapProductosConVariantesYStock(ResultSet rs) throws SQLException {
        Map<String, ProductoDTO> productosMap = new LinkedHashMap<>();

        while (rs.next()) {
            String codProducto = safeString(rs, "COD_PRODUCTO");

            // 🧱 Crear o reutilizar producto
            ProductoDTO producto = productosMap.computeIfAbsent(codProducto, _ -> {
                try {
                    return mapProducto(rs);
                } catch (SQLException e) {
                    e.printStackTrace();
                    return null;
                }
            });

            // 🧩 Procesar variante (usando el mapVariante)
            int idVariante = safeInt(rs, "ID_VARIANTE");
            if (idVariante > 0) {
                VarianteProductoDTO variante = producto.getVariante()
                        .stream()
                        .filter(v -> v.getIdVariante() == idVariante)
                        .findFirst()
                        .orElseGet(() -> {
                            try {
                                VarianteProductoDTO nueva = mapVariante(rs);
                                producto.getVariante().add(nueva);
                                return nueva;
                            } catch (SQLException e) {
                                e.printStackTrace();
                                return null;
                            }
                        });

                // 🏪 Agregar stock por almacén (usando mapAlmacenStock)
                AlmacenStockDTO stock = mapAlmacenStock(rs);
                if (stock != null) {
                    variante.getAlmacenStock().add(stock);
                }
            }
        }

        return new ArrayList<>(productosMap.values());
    }
    
}
