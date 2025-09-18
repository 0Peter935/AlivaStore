package com.store.erp.Repo;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class ProductoRepository {

    private final JdbcTemplate jdbcTemplate;

    public ProductoRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void guardarProducto(String idShopify, String titulo, Double precio) {
        String sql = "INSERT INTO Productos (IdShopify, Titulo, Precio) VALUES (?, ?, ?)";
        jdbcTemplate.update(sql, idShopify, titulo, precio);
    }

    public void guardar2Producto(String nombre, String img, Double precio, int stock, int inventario,
            String descripcion, boolean estado) {
        String sql = "INSERT INTO productos (nombre, img, precio, stock, inventario, descripcion, estado) VALUES (?, ?, ?, ?, ?, ?, ?)";
        jdbcTemplate.update(sql, nombre, img, precio, stock, inventario, descripcion, estado);
    }

}
