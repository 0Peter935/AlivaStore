package com.store.erp.Controllers;

import com.store.erp.Services.PedidoPredictivoService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.Map;
import java.util.HashMap;
import java.util.List;

@RestController
@RequestMapping("/api/predictivo/pedidos")
public class PedidoPredictivoController {

    private final PedidoPredictivoService service;

    public PedidoPredictivoController(PedidoPredictivoService service) {
        this.service = service;
    }

    // 1) Entrenar el modelo manualmente
    @GetMapping("/entrenar")
    public String entrenar() {
        service.entrenarModelo();
        return "✅ Modelo entrenado correctamente";
    }

    // 2) Predecir pedidos para un día específico
    @GetMapping("/dia")
    public Integer predecirDia(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha) {
        return service.predecirParaFecha(fecha);
    }

    // 3) Ver predicciones del próximo mes completo
    @GetMapping("/proximo-mes")
    public Map<LocalDate, Integer> predecirProximoMes() {
        return service.predecirProximoMes();
    }

    @GetMapping("/proximo-mes/json")
    public List<Map<String, Object>> proximoMesJson() {
        Map<LocalDate, Integer> datos = service.predecirProximoMes();

        return datos.entrySet().stream()
                .map(e -> {
                    Map<String, Object> m = new HashMap<>();
                    m.put("fecha", e.getKey().toString());
                    m.put("valor", e.getValue());
                    return m;
                })
                .toList();
    }
}