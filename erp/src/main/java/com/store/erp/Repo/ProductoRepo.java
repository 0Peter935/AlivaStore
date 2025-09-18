package com.store.erp.Repo;

import com.store.erp.Models.ProductoDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

@Repository
public class ProductoRepo {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private ProductoDTO mapRowToProducto(ResultSet rs, int rowNum) throws SQLException {
        ProductoDTO producto = new ProductoDTO();
        producto.setIdProducto(rs.getLong("id_producto"));
        producto.setCodProducto(rs.getString("cod_producto"));
        producto.setDescProducto(rs.getString("desc_producto"));
        producto.setImagen(rs.getString("imagen"));
        producto.setFechaRegistro(rs.getTimestamp("fecha_registro").toLocalDateTime());
        producto.setEstado(rs.getBoolean("estado"));

        return producto;
    }

    public List<ProductoDTO> listar() {
        return jdbcTemplate.query("EXEC ListarProductos", this::mapRowToProducto);
    }

    public ProductoDTO buscarPorId(Long id) {
        return jdbcTemplate.queryForObject("EXEC BuscarProducto ?", this::mapRowToProducto, id);
    }

    public void guardar(ProductoDTO producto) {
        jdbcTemplate.update("EXEC GuardarProducto ?, ?, ?, ?, ?",
                producto.getIdProducto(),
                producto.getCodProducto(),
                producto.getDescProducto(),
                producto.getImagen(),
                producto.getFechaRegistro());
    }

    public void actualizar(ProductoDTO producto) {
        jdbcTemplate.update("EXEC ActualizarProducto ?, ?, ?, ?",
                producto.getIdProducto(),
                producto.getCodProducto(),
                producto.getDescProducto(),
                producto.getImagen());
    }
}
