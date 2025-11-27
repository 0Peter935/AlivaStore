package com.store.erp.Services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.store.erp.Models.ReporteIndicadoresCardsDTO;
import com.store.erp.Models.ReportePedidosDepartamentoDTO;
import com.store.erp.Models.ReportePedidosEstadoDTO;
import com.store.erp.Models.ReportePedidosFechaDTO;
import com.store.erp.Models.ReportePedidosVendedorDTO;
import com.store.erp.Models.ReporteProductosVendidosDTO;
import com.store.erp.Repo.ReporteriaRepo;

import java.sql.Date;
import java.util.List;

@Service
public class ReporteriaService {

    @Autowired
    private ReporteriaRepo reporteriaRepo;

    public ReporteIndicadoresCardsDTO ObtenerIndicadoresCards(Date fechaInicio, Date fechaFin, String lugar) {
        return reporteriaRepo.ObtenerIndicadoresCards(fechaInicio, fechaFin, lugar);
    }

    public List<ReportePedidosEstadoDTO> ObtenerReportePedidosEstado(Date fechaInicio, Date fechaFin, String lugar) {
        return reporteriaRepo.obtenerPedidosPorEstado(fechaInicio, fechaFin, lugar);
    }

    public List<ReportePedidosFechaDTO> ObtenerReportePedidosFecha(Date fechaInicio, Date fechaFin, String lugar) {
        return reporteriaRepo.obtenerPedidosPorFecha(fechaInicio, fechaFin, lugar);
    }

    public List<ReportePedidosVendedorDTO> ObtenerReportePedidosVendedor(Date fechaInicio, Date fechaFin, String lugar) {
        return reporteriaRepo.obtenerPedidosPorVendedor(fechaInicio, fechaFin, lugar);
    }

    public List<ReporteProductosVendidosDTO> ObtenerProductosVendidos(Date fechaInicio, Date fechaFin, String lugar) {
        return reporteriaRepo.obtenerProductosVendidosTOP10(fechaInicio, fechaFin, lugar);
    }

    public List<ReportePedidosDepartamentoDTO> ObtenerPedidosDepartamento(Date fechaInicio, Date fechaFin) {
        return reporteriaRepo.obtenerPedidosDepartamento(fechaInicio, fechaFin);
    }
}