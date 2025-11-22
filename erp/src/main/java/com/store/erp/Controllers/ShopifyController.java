package com.store.erp.Controllers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.store.erp.Models.ClienteDTO;
import com.store.erp.Models.EmpresaEntregaDTO;
import com.store.erp.Models.EstadoPedidoDTO;
import com.store.erp.Models.PedidoDTO;
import com.store.erp.Models.PedidoDetalleDTO;
import com.store.erp.Models.PedidoNotaDTO;
import com.store.erp.Models.ProductoDTO;
import com.store.erp.Models.UsuarioDTO;
import com.store.erp.Models.VarianteProductoDTO;
import com.store.erp.Services.ClienteService;
import com.store.erp.Services.PedidoService;
import com.store.erp.Services.ProductoService;

import java.net.HttpURLConnection;
import java.net.URL;
import java.time.OffsetDateTime;
import java.util.ArrayList;
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
            String nextUrl = "https://" + shopDomain + "/admin/api/2024-10/products.json?limit=50";
            ObjectMapper mapper = new ObjectMapper();

            int totalInsertados = 0;
            int totalActualizados = 0;

            while (nextUrl != null) {

                System.out.println("🌐 Request → " + nextUrl);

                HttpURLConnection conn = (HttpURLConnection) new URL(nextUrl).openConnection();
                conn.setRequestMethod("GET");
                conn.setRequestProperty("X-Shopify-Access-Token", accessToken);
                conn.setRequestProperty("Content-Type", "application/json");

                int responseCode = conn.getResponseCode();
                System.out.println("📡 Código respuesta: " + responseCode);

                if (responseCode != 200) {
                    return ResponseEntity.status(responseCode)
                            .body(Map.of("error", "Error al obtener productos de Shopify"));
                }

                String jsonResponse = new String(conn.getInputStream().readAllBytes());
                JsonNode root = mapper.readTree(jsonResponse);
                JsonNode productos = root.get("products");

                if (productos != null && productos.isArray()) {
                    for (JsonNode p : productos) {
                        ProductoDTO dto = mapearProductoShopify(p);
                        boolean actualizado = productoService.registrarProducto(dto);

                        if (actualizado) totalActualizados++;
                        else totalInsertados++;
                    }
                }

                // ========== PAGINACIÓN: LECTURA DEL HEADER ==========
                String linkHeader = conn.getHeaderField("Link");
                nextUrl = extraerSiguientePagina(linkHeader);

                // Debug
                System.out.println("➡️ Next URL: " + nextUrl);
            }

            return ResponseEntity.ok(Map.of(
                    "mensaje", "Sincronización exitosa",
                    "insertados", totalInsertados,
                    "actualizados", totalActualizados
            ));

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/sync-clientes")
    public ResponseEntity<?> sincronizarClientesShopify() {
        System.out.println("🟢 [INICIO] Sincronización manual de clientes con Shopify iniciada...");

        int insertados = 0;
        int actualizados = 0;

        try {
            ObjectMapper mapper = new ObjectMapper();

            String nextUrl = "https://" + shopDomain + "/admin/api/2025-10/customers.json?limit=50";

            while (nextUrl != null) {
                System.out.println("🌐 Request → " + nextUrl);

                HttpURLConnection conn = (HttpURLConnection) new URL(nextUrl).openConnection();
                conn.setRequestMethod("GET");
                conn.setRequestProperty("X-Shopify-Access-Token", accessToken);
                conn.setRequestProperty("Content-Type", "application/json");

                int responseCode = conn.getResponseCode();
                System.out.println("📡 Código respuesta Shopify: " + responseCode);

                if (responseCode != 200) {
                    return ResponseEntity.status(responseCode)
                            .body(Map.of("error", "Error al conectar con Shopify (" + responseCode + ")"));
                }

                String jsonResponse = new String(conn.getInputStream().readAllBytes());
                JsonNode root = mapper.readTree(jsonResponse);
                JsonNode clientes = root.get("customers");

                if (clientes != null && clientes.isArray()) {
                    System.out.println("📊 Procesando " + clientes.size() + " clientes...");

                    for (JsonNode p : clientes) {
                        try {
                            String idCliente = p.path("id").asText();
                            System.out.println("\n👤 Procesando cliente ID: " + idCliente);

                            // === Mapeo DTO ===
                            ClienteDTO clienteDTO = mapearClienteShopify(p);
                            boolean estado = clienteService.sincronizarCliente(clienteDTO);

                            if (estado) {
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
                }

                String linkHeader = conn.getHeaderField("Link");
                nextUrl = extraerSiguientePagina(linkHeader);
                System.out.println("➡️ Next page → " + nextUrl);
            }

            return ResponseEntity.ok(Map.of(
                    "mensaje", "Sincronización completa",
                    "insertados", insertados,
                    "actualizados", actualizados
            ));

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/sync-pedidos")
    public ResponseEntity<?> sincronizarPedidosShopify() {
        System.out.println("🟢 Iniciando sincronización de pedidos Shopify...");

        int insertados = 0;

        try {
            String nextUrl = "https://" + shopDomain + "/admin/api/2025-10/orders.json?limit=50";

            while (nextUrl != null) {
                System.out.println("🌐 Solicitando: " + nextUrl);

                HttpURLConnection conn = (HttpURLConnection) new URL(nextUrl).openConnection();
                conn.setRequestMethod("GET");
                conn.setRequestProperty("X-Shopify-Access-Token", accessToken);
                conn.setRequestProperty("Content-Type", "application/json");

                int responseCode = conn.getResponseCode();
                System.out.println("📡 Código Shopify: " + responseCode);

                if (responseCode != 200) {
                    return ResponseEntity.status(responseCode)
                            .body(Map.of("error", "Error al conectar con Shopify (" + responseCode + ")"));
                }

                String jsonResponse = new String(conn.getInputStream().readAllBytes());
                ObjectMapper mapper = new ObjectMapper();
                JsonNode root = mapper.readTree(jsonResponse);

                JsonNode pedidos = root.get("orders");
                if (pedidos == null || !pedidos.isArray()) {
                    return ResponseEntity.badRequest().body(Map.of("error", "Respuesta inválida de Shopify"));
                }

                System.out.println("📦 Procesando " + pedidos.size() + " pedidos...");

                for (JsonNode p : pedidos) {
                    try {
                        PedidoDTO pedidoDTO = mapearPedidoShopify(p);
                        boolean ok = pedidoService.registrarPedido(pedidoDTO);

                        if (ok) insertados++;

                    } catch (Exception exPedido) {
                        System.out.println("💥 Error procesando pedido: " + exPedido.getMessage());
                    }
                }

                String linkHeader = conn.getHeaderField("Link");
                nextUrl = extraerSiguientePagina(linkHeader);

                if (nextUrl != null && !nextUrl.startsWith("http")) {
                    nextUrl = "https://" + shopDomain + nextUrl;
                }
            }

            System.out.println("🏁 Sincronización completada. Insertados: " + insertados);

            return ResponseEntity.ok(Map.of(
                    "mensaje", "Sincronización exitosa",
                    "insertados", insertados
            ));

        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Error al sincronizar pedidos Shopify", e);
        }
    }

    private String extraerSiguientePagina(String linkHeader) {
        if (linkHeader == null || !linkHeader.contains("rel=\"next\"")) {
            return null;
        }

        String[] partes = linkHeader.split(",");
        for (String parte : partes) {
            if (parte.contains("rel=\"next\"")) {
                int start = parte.indexOf("<") + 1;
                int end = parte.indexOf(">");
                return parte.substring(start, end);
            }
        }

        return null;
    }

    // ==========================================================
    // Mapeo de PRODUCTO Shopify → ProductoDTO
    // ==========================================================
    private ProductoDTO mapearProductoShopify(JsonNode json) {

        String codProducto = json.path("id").asText();
        String descripcion = json.path("title").asText("Sin título");
        String estadoShopify = json.path("status").asText("active").toLowerCase();
        OffsetDateTime fechaReg = OffsetDateTime.parse(json.path("created_at").asText());
        OffsetDateTime fechaAct = OffsetDateTime.parse(json.path("updated_at").asText());
        boolean estado = "active".equals(estadoShopify);
        String imagenUrl = null;

        if (json.has("image") && json.get("image").has("src")) {
            imagenUrl = json.get("image").get("src").asText();
        } else if (json.has("images") && json.get("images").isArray() && json.get("images").size() > 0) {
            imagenUrl = json.get("images").get(0).path("src").asText(null);
        }

        ProductoDTO producto = new ProductoDTO();
        producto.setCodProducto(codProducto);
        producto.setDescProducto(descripcion);
        producto.setImg(imagenUrl != null ? imagenUrl : "sin-imagen.jpg");
        producto.setRegalo(false);
        producto.setEstado(estado);
        producto.setFechaReg(fechaReg);
        producto.setFechaAct(fechaAct);

        // ======= VARIANTES =======
        List<VarianteProductoDTO> variantes = new ArrayList<>();

        if (json.has("variants") && json.get("variants").isArray()) {
            for (JsonNode variantNode : json.get("variants")) {

                VarianteProductoDTO variante = new VarianteProductoDTO();

                // ID interno de la variante en Shopify
                String codVariante = variantNode.path("id").asText();
                String tituloVariante = variantNode.path("title").asText("Sin título");
                double precio = variantNode.path("price").asDouble(0.0);
                OffsetDateTime fechaRegVariante = OffsetDateTime.parse(json.path("created_at").asText());
                OffsetDateTime fechaActVariante = OffsetDateTime.parse(json.path("updated_at").asText());

                // Imagen asociada (si tiene)
                String imgVariante = null;
                if (variantNode.has("image_id") && json.has("images")) {
                    long imageId = variantNode.path("image_id").asLong(0);
                    for (JsonNode imgNode : json.get("images")) {
                        if (imgNode.path("id").asLong(0) == imageId) {
                            imgVariante = imgNode.path("src").asText(null);
                            break;
                        }
                    }
                }

                variante.setCodProducto(codProducto);
                variante.setCodVariante(codVariante);
                variante.setTitulo(tituloVariante);
                variante.setPrecio(precio);
                variante.setFechaReg(fechaRegVariante);
                variante.setFechaAct(fechaActVariante);
                variante.setImgVariante(imgVariante != null ? imgVariante : imagenUrl);
                variante.setAlmacenStock(new ArrayList<>());

                variantes.add(variante);
            }
        }

        producto.setVariante(variantes);

        System.out.println("📦 ProductoDTO generado desde Shopify → " + producto.getDescProducto() +
                        " | Variantes: " + variantes.size());

        return producto;
    }

    // ==========================================================
    // Mapeo de CLIENTE Shopify → ClienteDTO
    // ==========================================================
    private ClienteDTO mapearClienteShopify(JsonNode json) {

        ClienteDTO cliente = new ClienteDTO();

        String codCliente = json.path("id").asText();

        String firstName = json.path("first_name").asText("").trim();
        String lastNameRaw = json.path("last_name").asText("").trim();

        boolean esDni = lastNameRaw.matches("\\d{8}");
        String dni = esDni ? lastNameRaw : "--------";

        String nombreCompleto = esDni
                ? firstName
                : (firstName + " " + lastNameRaw).trim();

        OffsetDateTime fechaReg = OffsetDateTime.parse(json.path("created_at").asText());
        OffsetDateTime fechaAct = OffsetDateTime.parse(json.path("updated_at").asText());

        int canOrdenes = json.path("orders_count").asInt(0);
        String direccion = json.path("default_address").path("address1").asText("");
        String ciudad = json.path("default_address").path("city").asText("");
        String provincia = json.path("default_address").path("province").asText("");
        String pais = json.path("default_address").path("country").asText("");

        String correo = "";
        if (json.hasNonNull("email")) {
            correo = json.get("email").asText("");
        } else if (json.has("emails") && json.get("emails").isArray() && json.get("emails").size() > 0) {
            correo = json.get("emails").get(0).path("email").asText("");
        }

        String telefono = json.path("phone").asText("");
        if (telefono.isBlank() && json.has("addresses") && json.get("addresses").isArray() && json.get("addresses").size() > 0) {
            telefono = json.get("addresses").get(0).path("phone").asText("");
        }

        cliente.setCodigoCliente(codCliente);
        cliente.setNombres(nombreCompleto);
        cliente.setCorreo(correo);
        cliente.setTelefono(telefono);
        cliente.setDni(dni);
        cliente.setCanOrdenes(canOrdenes);
        cliente.setDireccion(direccion);
        cliente.setCiudad(ciudad);
        cliente.setProvincia(provincia);
        cliente.setPais(pais);
        cliente.setFechaReg(fechaReg);
        cliente.setFechaAct(fechaAct);

        System.out.printf("👤 ClienteDTO generado → %s | DNI: %s | 📧 %s | 📱 %s%n",
        cliente.getNombres(), cliente.getDni(), cliente.getCorreo(), cliente.getTelefono());

        return cliente;
    }

    // ==========================================================
    // Mapeo de PEDIDO Shopify → PedidoDTO
    // ==========================================================
    private PedidoDTO mapearPedidoShopify(JsonNode json) {

        PedidoDTO pedido = new PedidoDTO();

        pedido.setCodPedido(json.path("id").asText(""));
        pedido.setDocumento(json.path("name").asText(""));
        pedido.setSubtotal(json.path("subtotal_price").asDouble(0.0));
        pedido.setIgv(json.path("total_tax").asDouble(0.0));
        pedido.setMontoTotal(json.path("total_price").asDouble(0.0));
        pedido.setCiudad(json.path("shipping_address").path("province").asText("Lima"));
        pedido.setObservacion("Pedido importado desde Shopify");
        pedido.setFechaReg(OffsetDateTime.parse(json.path("created_at").asText()));

        // Tipo de pago
        if (json.has("payment_gateway_names") && json.get("payment_gateway_names").isArray())
            pedido.setTipoPago(json.get("payment_gateway_names").get(0).asText("DESCONOCIDO"));
        else
            pedido.setTipoPago("");

        String tipoComprobante = "";
        JsonNode notes = json.path("note_attributes");

        if (notes.isArray()) {
            for (JsonNode note : notes) {
                String name = note.path("name").asText("");
                if (name.equalsIgnoreCase("Elige tu Comprobante")) {
                    tipoComprobante = note.path("value").asText("");
                    break;
                }
            }
        }

        pedido.setTipoComprobante(tipoComprobante);

        // Cliente
        if (json.hasNonNull("customer")) {
            JsonNode c = json.get("customer");

            ClienteDTO cli = new ClienteDTO();
            cli.setCodigoCliente(c.path("id").asText(""));

            pedido.setCliente(cli);
        }

        // Empresa de entrega (por default)
        EmpresaEntregaDTO empresa = new EmpresaEntregaDTO();
        empresa.setIdEmpresaEntrega(1);
        pedido.setEmpresaEntrega(empresa);

        // Usuario (por default)
        UsuarioDTO usuario = new UsuarioDTO();
        usuario.setIdUsuario(1);
        pedido.setUsuario(usuario);

        // Estado inicial (completado o como lo manejes)
        EstadoPedidoDTO estado = new EstadoPedidoDTO();
        String estatus = json.path("financial_status").asText("").toLowerCase();

        estado.setIdEstadoPedido(
            switch (estatus) {
                case "authorized" -> 1;
                case "pending" -> 2;
                case "paid" -> 3;
                case "partially_paid" -> 4;
                case "refunded" -> 5;
                case "voided" -> 6;
                case "partially_refunded" -> 8;
                case "any" -> 9;
                case "unpaid" -> 10;
                default -> 2;
            }
        );

        pedido.setEstadoPedido(estado);

        // ==========================================================
        // Detalles del pedido (line_items)
        // ==========================================================
        List<PedidoDetalleDTO> detalles = new ArrayList<>();

        if (json.has("line_items") && json.get("line_items").isArray()) {
            for (JsonNode item : json.get("line_items")) {

                PedidoDetalleDTO det = new PedidoDetalleDTO();

                det.setCodProducto(item.path("product_id").asText(""));
                det.setCodVariante(item.path("variant_id").asText(""));

                String nombreProducto = item.path("title").asText("");
                String nombreVariante = item.path("variant_title").asText("");
                det.setNombreProducto((nombreProducto + " - " + nombreVariante).trim());

                det.setCantidad(item.path("quantity").asInt(1));

                double precio = item.path("price").asDouble(0.0);
                det.setPrecioUnitario(precio);
                det.setPrecioTotal(precio * det.getCantidad());

                detalles.add(det);
            }
        }

        pedido.setDetalles(detalles);

        // ==========================================================
        // Notas (note_attributes)
        // ==========================================================
        List<PedidoNotaDTO> notas = new ArrayList<>();

        if (json.has("note_attributes") && json.get("note_attributes").isArray()) {
            for (JsonNode notaJson : json.get("note_attributes")) {

                PedidoNotaDTO nota = new PedidoNotaDTO();

                nota.setCodPedido(pedido.getCodPedido());
                nota.setTitulo(notaJson.path("name").asText(""));
                nota.setDescripcion(notaJson.path("value").asText(""));

                notas.add(nota);
            }
        }

        pedido.setNotas(notas);

        return pedido;
    }

}
