package com.store.erp.Controllers;

import com.fasterxml.jackson.databind.JsonNode;
import com.store.erp.Repo.ProductoRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/webhooks")
public class ShopifyWebhookController {

    private final ProductoRepository productoRepository;

    public ShopifyWebhookController(ProductoRepository productoRepository) {
        this.productoRepository = productoRepository;
    }

    @PostMapping("/product")
    public ResponseEntity<String> productCreated(@RequestBody JsonNode payload) {
        try {
            // ID de Shopify
            String idShopify = payload.get("id").asText();

            // Título
            String titulo = payload.get("title").asText();

            // Precio (del primer variant)
            Double precio = 0.0;
            if (payload.has("variants") && payload.get("variants").isArray() && payload.get("variants").size() > 0) {
                JsonNode variant = payload.get("variants").get(0);
                precio = variant.get("price").asDouble(0.0);
            }

            // Guardar en SQL Server
            productoRepository.guardarProducto(idShopify, titulo, precio);

            return ResponseEntity.ok("✅ Producto guardado en SQL Server");
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body("❌ Error al procesar el webhook: " + e.getMessage());
        }
    }
}
