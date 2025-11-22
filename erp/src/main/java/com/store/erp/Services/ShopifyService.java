package com.store.erp.Services;

import com.store.erp.Models.ProductoDTO;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

@Service
public class ShopifyService {

    @Value("${shopify.domain}")
    private String shopDomain;

    @Value("${shopify.token}")
    private String accessToken;

    // 🔹 Método genérico para enviar peticiones a Shopify
    private boolean sendShopifyRequest(String endpoint, String method, String body) {
        try {
            String urlStr = "https://" + shopDomain + endpoint;
            System.out.println("🌐 [Shopify] URL: " + urlStr);

            URL url = new URL(urlStr);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod(method);
            conn.setRequestProperty("X-Shopify-Access-Token", accessToken);
            conn.setRequestProperty("Content-Type", "application/json");

            if (body != null && !body.isEmpty()) {
                conn.setDoOutput(true);
                try (OutputStream os = conn.getOutputStream()) {
                    os.write(body.getBytes(StandardCharsets.UTF_8));
                }
            }

            int code = conn.getResponseCode();
            if (code >= 200 && code < 300) {
                System.out.println("✅ [Shopify] " + method + " completado correctamente (" + code + ")");
                return true;
            } else {
                try (InputStream err = conn.getErrorStream()) {
                    if (err != null) {
                        String error = new String(err.readAllBytes());
                        System.err.println("❌ [Shopify] Error (" + code + "): " + error);
                    } else {
                        System.err.println("❌ [Shopify] Error (" + code + "): sin cuerpo de error");
                    }
                }
                return false;
            }

        } catch (Exception e) {
            System.err.println("💥 [Shopify] Error de conexión: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    // 🔹 Actualizar el estado del producto (activo / borrador)
    public boolean actualizarEstadoProducto(ProductoDTO producto, boolean activo) {
        try {
            String endpoint = "/admin/api/2025-10/products/" + producto.getCodProducto() + ".json";
            String estado = activo ? "active" : "draft";
            String body = """
                { "product": { "status": "%s" } }
            """.formatted(estado);

            System.out.println("🌀 [Shopify] Actualizando estado de producto " + producto.getCodProducto() + " → " + estado);
            return sendShopifyRequest(endpoint, "PUT", body);
            
        } catch (Exception e) {
            System.err.println("💥 [Shopify] Error al actualizar estado del producto: " + e.getMessage());
            return false;
        }
    }

    // 🔹 Actualizar el stock disponible
    public boolean actualizarStockProducto(String inventoryItemId, int stock) {
        try {
            String endpoint = "/admin/api/2025-10/inventory_levels/set.json";
            String body = """
                {
                  "location_id": 123456789,
                  "inventory_item_id": %s,
                  "available": %d
                }
            """.formatted(inventoryItemId, stock);

            System.out.println("📦 [Shopify] Actualizando stock → item " + inventoryItemId + ": " + stock);
            return sendShopifyRequest(endpoint, "POST", body);
        } catch (Exception e) {
            System.err.println("💥 [Shopify] Error al actualizar stock: " + e.getMessage());
            return false;
        }
    }

    // 🔹 Actualizar precio (puede servirte más adelante)
    public boolean actualizarPrecioProducto(ProductoDTO producto, double nuevoPrecio) {
        try {
            String endpoint = "/admin/api/2024-10/products/" + producto.getCodProducto() + ".json";
            String body = """
                {
                  "product": {
                    "variants": [
                      { "price": %.2f }
                    ]
                  }
                }
            """.formatted(nuevoPrecio);

            System.out.println("💰 [Shopify] Actualizando precio de " + producto.getCodProducto() + " → S/ " + nuevoPrecio);
            return sendShopifyRequest(endpoint, "PUT", body);
        } catch (Exception e) {
            System.err.println("💥 [Shopify] Error al actualizar precio: " + e.getMessage());
            return false;
        }
    }
}
