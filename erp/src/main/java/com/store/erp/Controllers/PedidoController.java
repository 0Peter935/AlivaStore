package com.store.erp.Controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.store.erp.Models.PedidoDTO;
import com.store.erp.Models.PedidoLogDTO;
import com.store.erp.Services.PedidoService;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@RestController
@RequestMapping("/api/pedidos")
public class PedidoController {

    @Autowired
    private PedidoService pedidoService;

    @GetMapping
    public ResponseEntity<List<PedidoDTO>> listarPedidos() {
        try {
            return ResponseEntity.ok(pedidoService.listarPedidos());
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/vendedor/{idVendedor}")
    public ResponseEntity<List<PedidoDTO>> listarPedidosPorVendedor(@PathVariable("idVendedor") int idVendedor) {
        try {
            return ResponseEntity.ok(pedidoService.listarPedidosPorVendedor(idVendedor));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/vendedor/estado/{estado}")
    public ResponseEntity<?> listarPedidosPorEstadoVendedor(
            @PathVariable("estado") int estado,
            @RequestParam("idUsuario") Integer idUsuario
    ) {
        try {
            if (idUsuario == null) {
                return ResponseEntity.badRequest().body("El idUsuario es requerido");
            }

            return ResponseEntity.ok(
                    pedidoService.listarPedidosPorEstadoyUsuario(estado, idUsuario)
            );

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500)
                    .body("Error al obtener pedidos: " + e.getMessage());
        }
    }


    @GetMapping("/logistica/estado/{estado}")
    public ResponseEntity<?> listarPedidosPorEstado(@PathVariable("estado") int estado) {
        try {
            return ResponseEntity.ok(pedidoService.listarPedidosPorEstado(estado,null));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("Error al obtener pedidos: " + e.getMessage());
        }
    }



    @GetMapping("/{cod}")
    public ResponseEntity<?> obtenerPedido(@PathVariable("cod") String codPedido) {
        PedidoDTO pedido = pedidoService.obtenerPedidoPorCod(codPedido);
        if (pedido == null)
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("mensaje", "Pedido no encontrado"));
        return ResponseEntity.ok(pedido);
    }

    @GetMapping("/logs/{codPedido}")
    public ResponseEntity<?> obtenerLogsPedido(@PathVariable("codPedido") String codPedido) {
        try {
            List<PedidoLogDTO> logs = pedidoService.listarlogsPedido(codPedido);
            return ResponseEntity.ok(logs);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error al obtener logs del pedido: " + e.getMessage());
        }
    }


    @PostMapping("/subirEvidencia/{idPedido}")
    public ResponseEntity<?> subirEvidencia(
            @PathVariable int idPedido,
            @RequestParam("file") MultipartFile file
    ) {
        try {
            if (file.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("success", false, "message", "Archivo vacío"));
            }

            // Ruta base
            String basePath = "C:\\Users\\picon\\proyectos\\erp\\recursos\\img\\evidencia";

            // Crear carpeta si no existe
            File directorio = new File(basePath);
            if (!directorio.exists()) {
                directorio.mkdirs();
            }

            // Nombre del archivo = ID del pedido + extensión original
            String extension = Objects.requireNonNull(file.getOriginalFilename())
                                    .substring(file.getOriginalFilename().lastIndexOf("."));
            String nombreArchivo = idPedido + extension;
            Path destino = Paths.get(basePath, nombreArchivo);

            // Guardar archivo
            Files.copy(file.getInputStream(), destino, StandardCopyOption.REPLACE_EXISTING);

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Evidencia guardada correctamente",
                    "ruta", destino.toString()
            ));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body(Map.of(
                    "success", false,
                    "message", "Error al guardar la evidencia: " + e.getMessage()
            ));
        }
    }

    @PostMapping("/regresarPedido")
    public ResponseEntity<?> regresarPedido(@RequestBody PedidoDTO pedido) {
        try {
            pedidoService.regresarPedidoRevision(pedido);

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Pedido regresado a revisión correctamente"
            ));

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body(Map.of(
                    "success", false,
                    "message", "Error al regresar el pedido: " + e.getMessage()
            ));
        }
    }


    @PostMapping("/guardarPedidoCompleto")
    public ResponseEntity<?> guardarPedidoCompleto(
            @RequestPart("pedido") PedidoDTO pedido,
            @RequestPart(value = "evidencias", required = false) List<MultipartFile> evidencias
    ) throws JsonProcessingException {

        System.out.println("Pedido recibido: " + new ObjectMapper().writeValueAsString(pedido));
        System.out.println("Archivos recibidos: " + (evidencias != null ? evidencias.size() : 0));

        try {
            pedidoService.actualizarPedidoCompleto(pedido, evidencias);

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Pedido guardado correctamente"
            ));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body(Map.of(
                    "success", false,
                    "message", "Error al guardar el pedido: " + e.getMessage()
            ));
        }
    }

    @PostMapping("/completar")
    public ResponseEntity<?> completarPedido(
            @RequestPart("pedido") PedidoDTO pedido,
            @RequestPart(value = "evidencias", required = false) List<MultipartFile> evidencias
    ) {
        try {
            pedidoService.completarPedido(pedido, evidencias != null ? evidencias : List.of());
            return ResponseEntity.ok("Pedido completado");
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body(e.getMessage());
        }
    }

}
