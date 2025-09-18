package com.store.erp.Controllers;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import com.fasterxml.jackson.databind.JsonNode;
import com.store.erp.Repo.ProductoRepository;
import com.store.erp.Services.ShopifyService;

@RestController
public class ShopifyController {

    private final ShopifyService shopifyService;
    private final ProductoRepository productoRepository;

    public ShopifyController(ShopifyService shopifyService, ProductoRepository productoRepository) {
        this.shopifyService = shopifyService;
        this.productoRepository = productoRepository;
    }

    @GetMapping("/sync/products")
    public String syncProducts() {
        JsonNode response = shopifyService.getProducts();

        response.get("data").get("products").get("edges").forEach(edge -> {
            JsonNode node = edge.get("node");
            String id = node.get("id").asText();
            String title = node.get("title").asText();
            Double price = node.get("variants").get("edges").get(0).get("node").get("price").asDouble();

            productoRepository.guardarProducto(id, title, price);
        });

        return "Productos sincronizados con éxito 🚀";
    }
}
