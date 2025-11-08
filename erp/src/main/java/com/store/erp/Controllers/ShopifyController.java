package com.store.erp.Controllers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.store.erp.Models.ClienteDTO;
import com.store.erp.Models.EmpresaEntregaDTO;
import com.store.erp.Models.EstadoPedidoDTO;
import com.store.erp.Models.PedidoDTO;
import com.store.erp.Models.PedidoDetalleDTO;
import com.store.erp.Models.ProductoDTO;
import com.store.erp.Models.UsuarioDTO;
import com.store.erp.Services.ClienteService;
import com.store.erp.Services.PedidoService;
import com.store.erp.Services.ProductoService;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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

    @Autowired
    private PedidoService pedidoService;

    @PostMapping("/orders/create")
    public ResponseEntity<?> orderCreated(@RequestBody String payload) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode json = mapper.readTree(payload);

            PedidoDTO pedido = mapearPedidoShopify(json);
            pedidoService.registrarPedidoCompleto(pedido);

            return ResponseEntity.ok().build();
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Error al procesar el JSON del pedido de Shopify");
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error interno al registrar el pedido");
        }
    }

    @PostMapping("/orders/updated")
    public ResponseEntity<?> orderUpdated(@RequestBody Map<String, Object> body) {
        //pedidoService.actualizarPedido(body);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/products/create")
    public ResponseEntity<?> productCreated(@RequestBody String payload) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode json = mapper.readTree(payload);

            ProductoDTO productoNuevo = mapearProductoShopify(json);
            productoService.registrarProducto(productoNuevo);

            return ResponseEntity.ok().build();
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Error al procesar el JSON del producto de Shopify");
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error interno al registrar el producto");
        }
    }

    @PostMapping("/products/update")
    public ResponseEntity<?> productUpdated(@RequestBody String payload) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode json = mapper.readTree(payload);

            ProductoDTO productoActualizado = mapearProductoShopify(json);
            productoService.actualizarProducto(productoActualizado);

            return ResponseEntity.ok().build();
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Error al procesar el JSON del producto de Shopify");
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error interno al actualizar el producto");
        }
    }

    @PostMapping("/sync-productos")
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

    @PostMapping("/sync-pedidos")
    public ResponseEntity<?> sincronizarPedidosShopify() {
        System.out.println("🟢 [INICIO] Sincronización manual de pedidos con Shopify iniciada...");

        int insertados = 0;
        int actualizados = 0;

        try {
            String url = "https://" + shopDomain + "/admin/api/2025-10/orders.json";
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

            // Leer la respuesta JSON
            String jsonResponse = new String(conn.getInputStream().readAllBytes());
            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(jsonResponse);

            JsonNode pedidos = root.get("orders");
            if (pedidos == null || !pedidos.isArray()) {
                return ResponseEntity.badRequest().body(Map.of("error", "Respuesta JSON inválida: no hay 'orders'"));
            }

            System.out.println("📦 [4] Procesando " + pedidos.size() + " pedidos...");

            for (JsonNode p : pedidos) {
                try {
                    System.out.println("🧠 JSON Pedido: " + p.path("id").asText());

                    PedidoDTO pedidoDTO = mapearPedidoShopify(p);

                    boolean actualizadoPedido = pedidoService.sincronizarPedido(pedidoDTO);

                    if (actualizadoPedido) {
                        actualizados++;
                        System.out.println("✅ Pedido actualizado: " + pedidoDTO.getDocumento());
                    } else {
                        insertados++;
                        System.out.println("🆕 Pedido insertado: " + pedidoDTO.getDocumento());
                    }

                } catch (Exception exPedido) {
                    System.out.println("💥 [ERROR PEDIDO] " + exPedido.getMessage());
                    exPedido.printStackTrace();
                }
            }

            System.out.println("🏁 [FIN] Sincronización completada. Insertados: " + insertados + " | Actualizados: " + actualizados);

            return ResponseEntity.ok(Map.of(
                    "mensaje", "Sincronización exitosa",
                    "insertados", insertados,
                    "actualizados", actualizados
            ));

        } catch (Exception e) {
            System.err.println("💥 Error en sincronizarPedidos(): " + e.getMessage());
            if (e.getCause() != null) {
                System.err.println("📄 Causa interna: " + e.getCause().getMessage());
            }
            e.printStackTrace();
            throw new RuntimeException("Error al guardar pedido completo", e);
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

    // ==========================================================
    // Mapeo de PEDIDO Shopify → PedidoDTO
    // ==========================================================
private PedidoDTO mapearPedidoShopify(JsonNode json) {
    System.out.println("🔍 Iniciando mapeo de pedido Shopify ID: " + json.path("id").asText());
    PedidoDTO pedido = new PedidoDTO();

    try {
        // 🧾 Datos básicos
        pedido.setCodigoPedido(json.path("id").asText("")); // ID de Shopify
        pedido.setDocumento(json.path("name").asText(""));  // Ej: "#1003"
        pedido.setSubtotal(json.path("subtotal_price").asDouble(0.0));
        pedido.setIgv(json.path("total_tax").asDouble(0.0));
        pedido.setMontoTotal(json.path("total_price").asDouble(0.0));
        pedido.setCiudad(json.path("shipping_address").path("province").asText("Lima"));
        pedido.setObservacion("Pedido importado desde Shopify");
        System.out.println("✅ Paso 1: Datos básicos mapeados");

        // 💳 Tipo de pago
        if (json.has("payment_gateway_names") && json.get("payment_gateway_names").isArray()) {
            pedido.setTipoPago(json.get("payment_gateway_names").get(0).asText("DESCONOCIDO"));
        } else {
            pedido.setTipoPago("");
        }
        System.out.println("✅ Paso 2: Tipo de pago asignado: " + pedido.getTipoPago());

        // 🧾 Tipo comprobante
        pedido.setTipoComprobante("BOLETA");

        // 👤 Cliente
        if (json.hasNonNull("customer")) {
            JsonNode cliente = json.get("customer");
            ClienteDTO clienteDTO = new ClienteDTO();
            clienteDTO.setCodigoCliente(cliente.path("id").asText(""));
            clienteDTO.setNombres(
                    (cliente.path("first_name").asText("") + " " + cliente.path("last_name").asText("")).trim());
            pedido.setCliente(clienteDTO);
            System.out.println("✅ Paso 3: Cliente mapeado -> " + clienteDTO.getNombres());
        } else {
            System.out.println("⚠️ Pedido sin cliente (Shopify ID: " + json.path("id").asText() + ")");
        }

        // 🚚 Empresa de entrega
        EmpresaEntregaDTO empresa = new EmpresaEntregaDTO();
        empresa.setIdEmpresaEntrega(1);
        pedido.setEmpresaEntrega(empresa);

        // 👤 Usuario
        UsuarioDTO usuario = new UsuarioDTO();
        usuario.setIdUsuario(1);
        pedido.setUsuario(usuario);

        // 📦 Estado del pedido
        EstadoPedidoDTO estado = new EstadoPedidoDTO();
        estado.setIdEstadoPedido(1);
        pedido.setEstadoPedido(estado);
        System.out.println("✅ Paso 4: Usuario, empresa y estado asignados");

        // 💰 Detalles
        if (json.has("line_items") && json.get("line_items").isArray()) {
            List<PedidoDetalleDTO> detalles = new ArrayList<>();

            for (JsonNode item : json.get("line_items")) {
                PedidoDetalleDTO detalle = new PedidoDetalleDTO();
                ProductoDTO producto = new ProductoDTO();

                producto.setIdProducto(0); // aún no mapeas ID interno
                producto.setCodProducto(item.path("product_id").asText(""));
                detalle.setProducto(producto);

                detalle.setCantidad(item.path("quantity").asInt(1));
                detalle.setPrecioUnitario(item.path("price").asDouble(0.0));
                detalle.setPrecioTotal(
                        item.path("price").asDouble(0.0) * item.path("quantity").asInt(1));

                detalles.add(detalle);
            }

            pedido.setDetalles(detalles);
            System.out.println("✅ Paso 5: Detalles procesados (" + detalles.size() + " ítems)");
        } else {
            System.out.println("⚠️ Pedido sin detalles de producto (Shopify ID: " + json.path("id").asText() + ")");
        }

        System.out.println("✅ Mapeo completado correctamente para pedido: " + pedido.getDocumento());
        return pedido;

    } catch (Exception e) {
        System.err.println("💥 [MAPEO ERROR] Pedido Shopify ID: " + json.path("id").asText());
        e.printStackTrace();
        throw e; // vuelve a lanzar para que el controlador lo capture
    }
}


}
