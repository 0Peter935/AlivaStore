package com.store.erp.Controllers;

import java.sql.Date;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.store.erp.Services.ReporteriaService;

@RestController
@RequestMapping("/api/reporte")
public class ReporteriaController {

    @Autowired
    private ReporteriaService reporteriaService;

    @GetMapping("/cards")
    public ResponseEntity<?> obtenerCardsIndicadores(
        @RequestParam("inicio") Date fechaInicio,
        @RequestParam("fin") Date fechaFin,
        @RequestParam(value = "lugar", required = false) String lugar
    ) {
        try {

            if (lugar == null || lugar.trim().isEmpty()) {
                lugar = null;
            }

            return ResponseEntity.ok(
                reporteriaService.ObtenerIndicadoresCards(fechaInicio, fechaFin, lugar)
            );

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/pedidos-estado")
    public ResponseEntity<?> obtenerPedidosPorEstado(
            @RequestParam("inicio") Date inicio,
            @RequestParam("fin") Date fin,
            @RequestParam(value = "lugar", required = false) String lugar
    ) {
        try {
            if (lugar == null || lugar.trim().isEmpty()) {
                lugar = null;
            }

            return ResponseEntity.ok(
                reporteriaService.ObtenerReportePedidosEstado(inicio, fin, lugar)
            );
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/pedidos-fecha")
    public ResponseEntity<?> obtenerPedidosPorFecha(
            @RequestParam("inicio") Date inicio,
            @RequestParam("fin") Date fin,
            @RequestParam(value = "lugar", required = false) String lugar
    ) {
        try {
            if (lugar == null || lugar.trim().isEmpty()) {
                lugar = null;
            }
            
            return ResponseEntity.ok(
                reporteriaService.ObtenerReportePedidosFecha(inicio, fin, lugar)
            );
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/pedidos-vendedor")
    public ResponseEntity<?> obtenerPedidosVendedor(
            @RequestParam("inicio") Date inicio,
            @RequestParam("fin") Date fin,
            @RequestParam(value = "lugar", required = false) String lugar
    ) {
        try {
            if (lugar == null || lugar.trim().isEmpty()) {
                lugar = null;
            }
            
            return ResponseEntity.ok(
                reporteriaService.ObtenerReportePedidosVendedor(inicio, fin, lugar)
            );
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/productos-vendidos")
    public ResponseEntity<?> obtenerProductosVendidos(
            @RequestParam("inicio") Date inicio,
            @RequestParam("fin") Date fin,
            @RequestParam(value = "lugar", required = false) String lugar
    ) {
        try {
            if (lugar == null || lugar.trim().isEmpty()) {
                lugar = null;
            }
            
            return ResponseEntity.ok(
                reporteriaService.ObtenerProductosVendidos(inicio, fin, lugar)
            );
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/pedidos-departamento")
    public ResponseEntity<?> obtenerPedidosDepartamento(
            @RequestParam("inicio") Date inicio,
            @RequestParam("fin") Date fin
    ) {
        try {
            return ResponseEntity.ok(
                reporteriaService.ObtenerPedidosDepartamento(inicio, fin)
            );
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", e.getMessage()));
        }
    }
}
