package com.store.erp.Repo;

import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.stereotype.Repository;

import com.store.erp.Models.PedidoPredictivoDTO;
import com.store.erp.Models.ReporteIndicadoresCardsDTO;
import com.store.erp.Models.ReportePedidosDepartamentoDTO;
import com.store.erp.Models.ReportePedidosEstadoDTO;
import com.store.erp.Models.ReportePedidosFechaDTO;
import com.store.erp.Models.ReportePedidosVendedorDTO;
import com.store.erp.Models.ReporteProductosVendidosDTO;
import com.store.erp.Repo.Mappers;

import java.util.List;

@Repository
public class ReporteriaRepo {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    public ReporteIndicadoresCardsDTO ObtenerIndicadoresCards(Date fechaInicio, Date fechaFin, String lugar) {

        return jdbcTemplate.queryForObject(
                "EXEC SP_REPORTE_INDICADORES_CARDS ?, ?, ?",
                new Object[] { fechaInicio, fechaFin, lugar },
                (rs, rowNum) -> Mappers.mapReporteCards(rs));
    }

    public List<ReportePedidosEstadoDTO> obtenerPedidosPorEstado(Date fechaInicio, Date fechaFin, String lugar) {

        return jdbcTemplate.query(
                "EXEC SP_REPORTE_PEDIDOS_POR_ESTADO ?, ?, ?",
                new Object[] { fechaInicio, fechaFin, lugar },
                (rs, rowNum) -> Mappers.mapReportePedidosEstado(rs));
    }

    public List<ReportePedidosFechaDTO> obtenerPedidosPorFecha(Date fechaInicio, Date fechaFin, String lugar) {

        return jdbcTemplate.query(
                "EXEC SP_REPORTE_PEDIDOS_POR_FECHA ?, ?, ?",
                new Object[] { fechaInicio, fechaFin, lugar },
                (rs, rowNum) -> Mappers.mapReportePedidosFecha(rs));
    }

    public List<ReportePedidosVendedorDTO> obtenerPedidosPorVendedor(Date fechaInicio, Date fechaFin, String lugar) {

        return jdbcTemplate.query(
                "EXEC SP_REPORTE_PEDIDOS_POR_VENDEDOR ?, ?, ?",
                new Object[] { fechaInicio, fechaFin, lugar },
                (rs, rowNum) -> Mappers.mapReportePedidosVendedor(rs));
    }

    public List<ReporteProductosVendidosDTO> obtenerProductosVendidosTOP10(Date fechaInicio, Date fechaFin,
            String lugar) {

        return jdbcTemplate.query(
                "EXEC SP_REPORTE_PRODUCTOS_MAS_VENDIDOS ?, ?, ?",
                new Object[] { fechaInicio, fechaFin, lugar },
                (rs, rowNum) -> Mappers.mapReporteProductosVendidos(rs));
    }

    public List<ReportePedidosDepartamentoDTO> obtenerPedidosDepartamento(Date fechaInicio, Date fechaFin) {

        return jdbcTemplate.query(
                "EXEC SP_REPORTE_VENTAS_POR_DEPARTAMENTO ?, ?",
                new Object[] { fechaInicio, fechaFin },
                (rs, rowNum) -> Mappers.mapReportePedidosDepartamento(rs));
    }

    public List<PedidoPredictivoDTO> obtenerDatasetPredictivo() {

        String sql = """
                    SELECT FECHA, TOTAL_PEDIDOS, DIA_SEMANA, MES, DIA_ANIO
                    FROM VW_DATASET_PEDIDOS_DIARIOS
                    ORDER BY FECHA
                """;

        return jdbcTemplate.query(sql, (rs, rowNum) -> Mappers.mapAnalisisPredictivoPedidos(rs));
    }
}