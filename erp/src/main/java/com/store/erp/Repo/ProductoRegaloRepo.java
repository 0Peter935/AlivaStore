package com.store.erp.Repo;

import com.store.erp.Models.ProductoRegaloDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

@Repository
public class ProductoRegaloRepo {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private ProductoRegaloDTO mapRowToProductoRegalo(ResultSet rs, int rowNum) throws SQLException {
        ProductoRegaloDTO productoRegalo = new ProductoRegaloDTO();
        productoRegalo.setIdRegalo(rs.getInt("id_regalo"));
        productoRegalo.setCodProducto(rs.getString("cod_producto"));
        productoRegalo.setDescProducto(rs.getString("desc_producto"));
        productoRegalo.setImagen(rs.getString("imagen"));
        productoRegalo.setFechaRegistro(rs.getTimestamp("fecha_registro").toLocalDateTime());
        productoRegalo.setEstadoRegalo(rs.getBoolean("estado_regalo"));

        return productoRegalo;
    }

    public List<ProductoRegaloDTO> listar() {
        return jdbcTemplate.query("EXEC ListarProductosRegalo", this::mapRowToProductoRegalo);
    }

    public ProductoRegaloDTO buscarPorId(Integer id) {
        return jdbcTemplate.queryForObject("EXEC BuscarProductoRegalo ?", this::mapRowToProductoRegalo, id);
    }

    public void guardar(ProductoRegaloDTO productoRegalo) {
        jdbcTemplate.update("EXEC GuardarProductoRegalo ?, ?, ?, ?, ?",
                productoRegalo.getIdRegalo(),
                productoRegalo.getCodProducto(),
                productoRegalo.getDescProducto(),
                productoRegalo.getImagen(),
                productoRegalo.getFechaRegistro());
    }

    public void actualizar(ProductoRegaloDTO productoRegalo) {
        jdbcTemplate.update("EXEC ActualizarProductoRegalo ?, ?, ?, ?",
                productoRegalo.getIdRegalo(),
                productoRegalo.getCodProducto(),
                productoRegalo.getDescProducto(),
                productoRegalo.getImagen());
    }
}
