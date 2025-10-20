package com.store.erp.Controllers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.store.erp.Models.ProductoDTO;
import com.store.erp.Services.ProductoService;

import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/shopify")
public class ShopifyController {

    @Value("${shopify.domain}")
    private String shopDomain;

    @Value("${shopify.token}")
    private String accessToken;

    @Autowired
    private ProductoService productoService;

    @PostMapping("/webhook")
    public ResponseEntity<String> recibirWebhook(
            @RequestBody String payload,
            @RequestHeader(value = "X-Shopify-Topic", required = false) String topic) {

        System.out.println("🛍️ [Shopify] Webhook recibido: " + topic);

        try {
            if (topic == null || topic.isEmpty()) {
                return ResponseEntity.badRequest().body("❌ Falta cabecera X-Shopify-Topic");
            }

            ObjectMapper mapper = new ObjectMapper();
            JsonNode json = mapper.readTree(payload);

            switch (topic) {
                case "products/create":
                    ProductoDTO productoNuevo = mapearProductoShopify(json);
                    productoService.registrarProducto(productoNuevo);
                    break;

                case "products/update" ,  "inventory_levels/update":
                    ProductoDTO productoActualizado = mapearProductoShopify(json);
                    productoService.actualizarProducto(productoActualizado);
                    break;

                case "orders/create":
                    System.out.println("🧾 Pedido Shopify recibido, aún no implementado.");
                    break;

                default:
                    System.out.println("⚠️ Evento no manejado: " + topic);
                    break;
            }

            return ResponseEntity.ok("✅ Webhook procesado correctamente");

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body("Error: " + e.getMessage());
        }
    }

    @PostMapping("/sync")
    public ResponseEntity<?> sincronizarProductosShopify() {
        int insertados = 0;
        int actualizados = 0;

        try {
            System.out.println("🔄 Iniciando sincronización manual con Shopify...");

            // Endpoint REST de Shopify
            String url = "https://" + shopDomain + "/admin/api/2024-10/products.json";

            HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("X-Shopify-Access-Token", accessToken);
            conn.setRequestProperty("Content-Type", "application/json");

            if (conn.getResponseCode() != 200) {
                return ResponseEntity.status(conn.getResponseCode()).body("Error al conectar con Shopify");
            }

            // Leer respuesta JSON
            String jsonResponse = new String(conn.getInputStream().readAllBytes());
            conn.disconnect();

            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(jsonResponse);
            JsonNode productos = root.get("products");

            for (JsonNode p : productos) {
                try {
                    ProductoDTO dto = new ProductoDTO();
                    dto.setCodProducto(p.get("handle").asText());
                    dto.setDescProducto(p.get("title").asText());
                    dto.setPrecio(p.get("variants").get(0).get("price").asDouble());
                    dto.setStock(p.get("variants").get(0).get("inventory_quantity").asInt());
                    dto.setImagen(
                        p.get("images").size() > 0
                            ? p.get("images").get(0).get("src").asText()
                            : "no-image.jpg"
                    );
                    dto.setRegalo(false);
                    dto.setEstado(!p.get("status").asText().equalsIgnoreCase("draft"));

                    // 🔹 Llamar al servicio para sincronizar producto
                    boolean insertado = productoService.sincronizarProductos(dto);

                    if (insertado) insertados++;
                    else actualizados++;

                } catch (Exception prodErr) {
                    System.err.println("⚠️ Error al procesar producto: " + prodErr.getMessage());
                }
            }

            System.out.println("✅ Sincronización completa. Insertados: " + insertados + " | Actualizados: " + actualizados);

            return ResponseEntity.ok(Map.of(
                "mensaje", "Sincronización exitosa",
                "insertados", insertados,
                "actualizados", actualizados
            ));

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body("Error al sincronizar productos: " + e.getMessage());
        }
    }

    // ==========================================================
    // Mapeo de PRODUCTO Shopify → ProductoDTO
    // ==========================================================
    private ProductoDTO mapearProductoShopify(JsonNode json) {

        double precio = 0.0;
        int stock = 0;
        if (json.has("variants") && json.get("variants").isArray() && json.get("variants").size() > 0) {
            JsonNode variant = json.get("variants").get(0);
            precio = variant.path("price").asDouble(0.0);
            stock = variant.path("inventory_quantity").asInt(0);
        }

        String estadoShopify = json.path("status").asText("active").toLowerCase();
        boolean estado = "active".equals(estadoShopify);

        String imagenUrl = null;
        if (json.has("image") && json.get("image").has("src")) {
            imagenUrl = json.get("image").get("src").asText();
        } else if (json.has("images") && json.get("images").isArray() && json.get("images").size() > 0) {
            imagenUrl = json.get("images").get(0).path("src").asText(null);
        }

        ProductoDTO producto = new ProductoDTO();

        producto.setCodProducto(json.path("id").asText());
        producto.setDescProducto(json.hasNonNull("title") ? json.get("title").asText() : "Sin título");

        producto.setPrecio(precio);
        producto.setStock(stock);

        producto.setEstado(!"archived".equalsIgnoreCase(json.path("status").asText("active")));
        producto.setRegalo(false);
        producto.setEstado(estado);
        producto.setImagen(imagenUrl != null ? imagenUrl : "sin-imagen.jpg");

        System.out.println("📦 ProductoDTO generado desde Shopify → " + producto.getDescProducto());
        return producto;
    }

}
