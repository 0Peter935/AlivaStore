package com.store.erp.Controllers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.store.erp.Models.ClienteDTO;
import com.store.erp.Models.ProductoDTO;
import com.store.erp.Services.ClienteService;
import com.store.erp.Services.ProductoService;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
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

    @Autowired
    private ClienteService clienteService;

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
        System.out.println("🟢 [INICIO] Sincronización manual con Shopify iniciada...");

        try {
            System.out.println("🔗 [1] Preparando conexión HTTP...");

            String url = "https://" + shopDomain + "/admin/api/2024-10/products.json";
            System.out.println("🌐 URL Shopify: " + url);

            HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("X-Shopify-Access-Token", accessToken);
            conn.setRequestProperty("Content-Type", "application/json");

            int responseCode = conn.getResponseCode();
            System.out.println("📡 [2] Código de respuesta Shopify: " + responseCode);

            if (responseCode != 200) {
                System.out.println("❌ Error HTTP " + responseCode + ": No se pudo conectar con Shopify.");
                return ResponseEntity
                        .status(responseCode)
                        .body(Map.of("error", "Error al conectar con Shopify (" + responseCode + ")"));
            }

            // ===================== LECTURA JSON =====================
            System.out.println("📦 [3] Leyendo respuesta JSON...");
            String jsonResponse = new String(conn.getInputStream().readAllBytes());
            System.out.println("📄 [3.1] Longitud de respuesta JSON: " + jsonResponse.length());

            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(jsonResponse);

            JsonNode productos = root.get("products");
            if (productos == null || !productos.isArray()) {
                System.out.println("⚠️ [3.2] No se encontraron productos en la respuesta.");
                return ResponseEntity.badRequest().body(Map.of("error", "Respuesta JSON inválida: no hay 'products'"));
            }

            int insertados = 0;
            int actualizados = 0;
            System.out.println("📊 [4] Procesando " + productos.size() + " productos...");

            for (JsonNode p : productos) {
                try {
                    String nombre = p.get("title").asText();
                    System.out.println("🧩 [4.1] Procesando producto: " + nombre);

                    ProductoDTO dto = mapearProductoShopify(p);

                    System.out.println("📥 [4.2] Insertando/actualizando producto en BD...");
                    boolean actualizado = productoService.sincronizarProductos(dto);

                    if (actualizado) {
                        actualizados++;
                        System.out.println("✅ Producto actualizado: " + nombre);
                    } else {
                        insertados++;
                        System.out.println("🆕 Producto insertado: " + nombre);
                    }

                } catch (Exception exProducto) {
                    System.out.println("💥 [ERROR PRODUCTO] " + exProducto.getMessage());
                    exProducto.printStackTrace();
                }
            }

            System.out.println("🏁 [FIN] Sincronización completada. Insertados: " + insertados + " | Actualizados: " + actualizados);

            return ResponseEntity.ok(Map.of(
                    "mensaje", "Sincronización exitosa",
                    "insertados", insertados,
                    "actualizados", actualizados
            ));

        } catch (IOException e) {
            System.out.println("🚫 [ERROR HTTP/IO] " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.internalServerError().body(Map.of("error", "Error de conexión con Shopify: " + e.getMessage()));

        } catch (Exception e) {
            System.out.println("🔥 [ERROR GENERAL] " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.internalServerError().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/sync-clientes")
    public ResponseEntity<?> sincronizarClientesShopify() {
        System.out.println("🟢 [INICIO] Sincronización manual de clientes con Shopify iniciada...");

        int insertados = 0;
        int actualizados = 0;

        try {
            String url = "https://" + shopDomain + "/admin/api/2025-10/customers.json";
            System.out.println("🌐 URL Shopify: " + url);

            HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("X-Shopify-Access-Token", accessToken);
            conn.setRequestProperty("Content-Type", "application/json");

            int responseCode = conn.getResponseCode();
            System.out.println("📡 [2] Código de respuesta Shopify: " + responseCode);

            if (responseCode != 200) {
                return ResponseEntity.status(responseCode)
                        .body(Map.of("error", "Error al conectar con Shopify (" + responseCode + ")"));
            }

            // Leer la respuesta
            String jsonResponse = new String(conn.getInputStream().readAllBytes());
            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(jsonResponse);

            JsonNode clientes = root.get("customers");
            if (clientes == null || !clientes.isArray()) {
                return ResponseEntity.badRequest().body(Map.of("error", "Respuesta JSON inválida: no hay 'customers'"));
            }

            System.out.println("📊 [4] Procesando " + clientes.size() + " clientes...");

            for (JsonNode p : clientes) {
                try {

                    System.out.println("🧠 JSON Cliente: " + p.toPrettyString());
                    
                    String idCliente = p.path("id").asText();
                    System.out.println("\n👤 [4.1] Procesando cliente ID: " + idCliente);

                    // Si el cliente viene sin correo, hacemos una segunda consulta detallada
                    if (!p.hasNonNull("email") || p.path("email").asText("").isBlank()) {
                        String detalleUrl = "https://" + shopDomain + "/admin/api/2025-10/customers/" + idCliente + ".json";
                        System.out.println("📡 Solicitando detalle: " + detalleUrl);

                        HttpURLConnection connDet = (HttpURLConnection) new URL(detalleUrl).openConnection();
                        connDet.setRequestMethod("GET");
                        connDet.setRequestProperty("X-Shopify-Access-Token", accessToken);
                        connDet.setRequestProperty("Content-Type", "application/json");

                        if (connDet.getResponseCode() == 200) {
                            String detalleResponse = new String(connDet.getInputStream().readAllBytes());
                            JsonNode detalleRoot = mapper.readTree(detalleResponse);
                            JsonNode clienteDetallado = detalleRoot.get("customer");
                            if (clienteDetallado != null) {
                                p = clienteDetallado; // reemplaza el nodo base por el completo
                            }
                        }
                    }

                    ClienteDTO clienteDTO = mapearClienteShopify(p);
                    boolean actualizadoCliente = clienteService.sincronizarCliente(clienteDTO);

                    if (actualizadoCliente) {
                        actualizados++;
                        System.out.println("✅ Cliente actualizado: " + clienteDTO.getNombres());
                    } else {
                        insertados++;
                        System.out.println("🆕 Cliente insertado: " + clienteDTO.getNombres());
                    }

                } catch (Exception exCliente) {
                    System.out.println("💥 [ERROR CLIENTE] " + exCliente.getMessage());
                    exCliente.printStackTrace();
                }
            }

            System.out.println("🏁 [FIN] Sincronización completada. Insertados: " + insertados + " | Actualizados: " + actualizados);

            return ResponseEntity.ok(Map.of(
                    "mensaje", "Sincronización exitosa",
                    "insertados", insertados,
                    "actualizados", actualizados
            ));

        } catch (Exception e) {
            System.out.println("🔥 [ERROR GENERAL] " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", e.getMessage()));
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

    // ==========================================================
    // Mapeo de CLIENTE Shopify → ClienteDTO
    // ==========================================================
    private ClienteDTO mapearClienteShopify(JsonNode json) {

        ClienteDTO cliente = new ClienteDTO();

        cliente.setCodigoCliente(json.path("id").asText());
        cliente.setNombres(
                (json.path("first_name").asText("") + " " + json.path("last_name").asText("")).trim()
        );

        // 📧 Manejo robusto de correo electrónico
        String correo = "";
        if (json.hasNonNull("email")) {
            correo = json.get("email").asText("");
        } else if (json.has("emails") && json.get("emails").isArray() && json.get("emails").size() > 0) {
            correo = json.get("emails").get(0).path("email").asText("");
        }
        cliente.setCorreo(correo);

        // 📱 Teléfono seguro
        String telefono = json.path("phone").asText("");
        if (telefono.isBlank() && json.has("addresses") && json.get("addresses").isArray() && json.get("addresses").size() > 0) {
            telefono = json.get("addresses").get(0).path("phone").asText("");
        }
        cliente.setTelefono(telefono);

        System.out.printf("👤 ClienteDTO generado desde Shopify → %s | 📧 %s | 📱 %s%n",
                cliente.getNombres(), cliente.getCorreo(), cliente.getTelefono());

        return cliente;
    }


}
