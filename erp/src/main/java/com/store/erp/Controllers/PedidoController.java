package com.store.erp.Controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.store.erp.Models.PedidoDTO;
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
            List<PedidoDTO> pedidos = pedidoService.listarPedidos();
            return ResponseEntity.ok(pedidos);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> obtenerPedidoPorId(@PathVariable("id") int idPedido) {
        PedidoDTO pedido = pedidoService.obtenerPedidoPorId(idPedido);
        if (pedido == null)
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("mensaje", "Pedido no encontrado"));
        return ResponseEntity.ok(pedido);
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

    @PostMapping("/guardarPedidoCompleto")
    public ResponseEntity<?> guardarPedidoCompleto(
            @RequestPart("pedido") PedidoDTO pedido,
            @RequestPart(value = "file", required = false) MultipartFile file
    ) throws JsonProcessingException {

        System.out.println("📦 Pedido recibido: " + new ObjectMapper().writeValueAsString(pedido));
        System.out.println("🖼️ Archivo: " + (file != null ? file.getOriginalFilename() : "sin archivo"));

        try {
            String evidenciaPath = null;

            // 1️⃣ Guardar evidencia si se envía
            if (file != null && !file.isEmpty()) {
                String basePath = "C:\\Users\\picon\\proyectos\\erp\\recursos\\img\\evidencia";
                File dir = new File(basePath);
                if (!dir.exists()) dir.mkdirs();

                String extension = Objects.requireNonNull(file.getOriginalFilename())
                        .substring(file.getOriginalFilename().lastIndexOf("."));
                String nombreArchivo = pedido.getIdPedido() + extension;

                Path destino = Paths.get(basePath, nombreArchivo);
                Files.copy(file.getInputStream(), destino, StandardCopyOption.REPLACE_EXISTING);

                // ✅ Guardar solo el nombre del archivo en la BD
                evidenciaPath = nombreArchivo;
            }

            // 2️⃣ Setear ruta en DTO
            pedido.setEvidencia(evidenciaPath);

            // 3️⃣ Actualizar pedido completo (cabecera + detalles)
            pedidoService.registrarPedidoCompleto(pedido);

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Pedido guardado correctamente",
                    "rutaEvidencia", evidenciaPath
            ));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body(Map.of(
                    "success", false,
                    "message", "Error al guardar el pedido: " + e.getMessage()
            ));
        }
    }

}
