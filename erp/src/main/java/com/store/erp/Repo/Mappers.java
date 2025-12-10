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
        if (!hasColumn(rs, "ID_EMPRESA_ENTREGA"))
            return null;
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
        if (!hasColumn(rs, "ID_ZONA") && !hasColumn(rs, "ID"))
            return null;

        return new ZonaEmpresaEntregaDTO(
                hasColumn(rs, "ID_ZONA") ? safeInt(rs, "ID_ZONA") : safeInt(rs, "ID"),
                hasColumn(rs, "DESC_ZONA") ? safeString(rs, "DESC_ZONA") : safeString(rs, "DESCRIPCION"));
    }

    // =====================================================
    // Mapeo de TIPO DE ALMACEN
    // =====================================================
    public static TipoAlmacenDTO mapTipoAlmacen(ResultSet rs) throws SQLException {
        if (!hasColumn(rs, "ID_TIPO_ALMACEN"))
            return null;
        return new TipoAlmacenDTO(
                safeInt(rs, "ID_TIPO_ALMACEN"),
                safeString(rs, "DESC_TIPO_ALMACEN"));
    }

    // =====================================================
    // Mapeo de ALMACEN
    // =====================================================
    public static AlmacenDTO mapAlmacen(ResultSet rs) throws SQLException {
        if (!hasColumn(rs, "ID_ALMACEN"))
            return null;
        AlmacenDTO a = new AlmacenDTO();
        a.setIdAlmacen(safeInt(rs, "ID_ALMACEN"));
        a.setDescripcion(safeString(rs, "DESC_ALMACEN") != null ? safeString(rs, "DESC_ALMACEN")
                : safeString(rs, "NOMBRE_ALMACEN"));
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
                safeString(rs, "ROL_DESC"));

        u.setRol(rol);
        return u;
    }

    // =====================================================
    // Mapeo de ROL
    // =====================================================
    public static RolDTO mapRol(ResultSet rs) throws SQLException {
        if (!hasColumn(rs, "ID_ROL"))
            return null;

        RolDTO rol = new RolDTO(
                safeInt(rs, "ID_ROL"),
                safeString(rs, "DESCRIPCION"));
        return rol;
    }

    // =====================================================
    // Mapeo de CLIENTE
    // =====================================================
    public static ClienteDTO mapCliente(ResultSet rs) throws SQLException {
        ClienteDTO c = new ClienteDTO();
        c.setIdCliente(safeLong(rs, "ID_CLIENTE"));
        c.setCodCliente(safeString(rs, "COD_CLIENTE"));
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
        c.setFechaAct(safeLocalDateTime(rs, "FECHA_ACTUALIZACION"));

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
        if (!hasColumn(rs, "ID_ALMACEN_STOCK"))
            return null;
        return new AlmacenStockDTO(
                safeInt(rs, "ID_ALMACEN_STOCK"),
                safeString(rs, "COD_VARIANTE"),
                safeInt(rs, "INVENTARIO"),
                mapAlmacen(rs));
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
        p.setFechaReg(safeOffsetDateTime(rs, "FECHA_REGISTRO"));
        p.setFechaAprobado(safeLocalDateTime(rs, "FECHA_APROBADO"));

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

        VarianteProductoDTO v = new VarianteProductoDTO();
        v.setIdVariante(safeInt(rs, "ID_VARIANTE"));
        v.setCodVariante(safeString(rs, "COD_VARIANTE"));
        v.setImgVariante(safeString(rs, "IMG_VARIANTE"));
        d.setVariante(v);

        d.setNombreProducto(safeString(rs, "NOMBRE_PRODUCTO"));
        d.setCantidad(safeInt(rs, "CANTIDAD"));
        d.setPrecioUnitario(safeDouble(rs, "PRECIO_UNITARIO"));
        d.setPrecioTotal(safeDouble(rs, "PRECIO_TOTAL"));
        d.setEsRegalo(safeBool(rs, "DP_REGALO"));

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

    /// ====================================================
    /// Mapeo de EVIDENCIA DE PEDIDO
    /// ====================================================
    public static PedidoEvidenciaDTO mapEvidenciaPedido(ResultSet rs) throws SQLException {
        PedidoEvidenciaDTO e = new PedidoEvidenciaDTO();

        e.setIdEvidenciaPedido(safeLong(rs, "ID_EVIDENCIA"));
        e.setCodPedido(safeString(rs, "COD_PEDIDO"));
        e.setMotivo(safeString(rs, "MOTIVO"));
        e.setUrl(safeString(rs, "URL"));

        return e;
    }

    // =====================================================
    // Mapeo de LOG DE PEDIDO
    // =====================================================
    public static PedidoLogDTO mapPedidoLog(ResultSet rs) throws SQLException {
        PedidoLogDTO l = new PedidoLogDTO();

        l.setIdLog(safeLong(rs, "ID_LOG"));
        l.setIdUsuario(safeLong(rs, "ID_USUARIO_LOG"));
        l.setCodPedido(safeString(rs, "COD_PEDIDO"));
        l.setIdEstadoP(safeInt(rs, "ID_ESTADO_P_L"));
        l.setMotivoLog(safeString(rs, "MOTIVO_LOG"));
        l.setFechaLog(safeLocalDateTime(rs, "FECHA_LOG"));

        return l;
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

    // =====================================================
    // Mapeo de PEDIDO REPORTERIA . INDICADORES_DASHBOARD
    // =====================================================
    public static ReporteIndicadoresCardsDTO mapReporteCards(ResultSet rs) throws SQLException {
        ReporteIndicadoresCardsDTO r = new ReporteIndicadoresCardsDTO();

        r.setCantidadClientes(safeInt(rs, "CANTIDAD_CLIENTES"));
        r.setNumeroPedidos(safeInt(rs, "NUMERO_PEDIDOS"));
        r.setPromedioPedidosPorDia(safeDouble(rs, "PROMEDIO_PEDIDOS_POR_DIA"));
        r.setVentasTotales(safeDouble(rs, "VENTAS_TOTALES"));
        return r;
    }

    public static ReportePedidosEstadoDTO mapReportePedidosEstado(ResultSet rs) throws SQLException {
        ReportePedidosEstadoDTO r = new ReportePedidosEstadoDTO();

        r.setDescripcion(safeString(rs, "DESCRIPCION"));
        r.setTotalPedidos(safeInt(rs, "TOTAL_PEDIDOS"));
        return r;
    }

    public static ReportePedidosFechaDTO mapReportePedidosFecha(ResultSet rs) throws SQLException {
        ReportePedidosFechaDTO r = new ReportePedidosFechaDTO();

        r.setFecha(safeString(rs, "FECHA"));
        r.setTotalPedidos(safeInt(rs, "TOTAL_PEDIDOS"));
        return r;
    }

    public static ReportePedidosVendedorDTO mapReportePedidosVendedor(ResultSet rs) throws SQLException {
        ReportePedidosVendedorDTO r = new ReportePedidosVendedorDTO();

        r.setUsuario(safeString(rs, "USUARIO"));
        r.setTotalPedidos(safeInt(rs, "TOTAL_PEDIDOS"));
        return r;
    }

    public static ReporteProductosVendidosDTO mapReporteProductosVendidos(ResultSet rs) throws SQLException {
        ReporteProductosVendidosDTO r = new ReporteProductosVendidosDTO();

        r.setNombreProductoCorto(safeString(rs, "NOMBRE_PRODUCTO_CORTO"));
        r.setNombreProducto(safeString(rs, "NOMBRE_PRODUCTO"));
        r.setTotalVendido(safeInt(rs, "TOTAL_VENDIDO"));
        return r;
    }

    public static ReportePedidosDepartamentoDTO mapReportePedidosDepartamento(ResultSet rs) throws SQLException {
        ReportePedidosDepartamentoDTO r = new ReportePedidosDepartamentoDTO();

        r.setCiudad(safeString(rs, "CIUDAD"));
        r.setTotalPedidos(safeInt(rs, "TOTAL_PEDIDOS"));
        return r;
    }

    public static ReportePedidoTiempoPromedioDTO mapReportePedidoPromedio(ResultSet rs) throws SQLException {
        ReportePedidoTiempoPromedioDTO dto = new ReportePedidoTiempoPromedioDTO();

        dto.setPromedio_P_A(rs.getDouble("Promedio_P_A")); 
        dto.setPromedio_P_E(rs.getDouble("Promedio_P_E"));
        dto.setPromedio_Pedido(rs.getDouble("Promedio_Total_Suma")); 

        return dto;
    }

    public static ReporteErrorDespachoDTO mapReporteErrorDespacho(ResultSet rs) throws SQLException {
    ReporteErrorDespachoDTO dto = new ReporteErrorDespachoDTO();

    dto.setCantidad_Pedidos_Estado(rs.getInt("Cantidad_Pedidos_Error")); 
    dto.setPorcentaje_Error_Despacho(rs.getDouble("Porcentaje_Error_Despacho"));

    return dto;
}

    public static PedidoPredictivoDTO mapAnalisisPredictivoPedidos(ResultSet rs) throws SQLException {
        PedidoPredictivoDTO dto = new PedidoPredictivoDTO();

        dto.setFecha(safeDate(rs, "FECHA"));
        dto.setTotalPedidos(safeInt(rs, "TOTAL_PEDIDOS"));
        dto.setDiaSemana(rs.getInt("DIA_SEMANA"));
        dto.setMes(rs.getInt("MES"));
        dto.setDiaAnio(rs.getInt("DIA_ANIO"));

        return dto;
    }

    // UTILS

    public static UtilsDTO.ListarCiudad mapListarCiudad(ResultSet rs) throws SQLException {
        UtilsDTO.ListarCiudad lc = new UtilsDTO.ListarCiudad();

        lc.setCiudad(safeString(rs, "CIUDAD"));

        return lc;
    }

}
