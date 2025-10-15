package com.store.erp.Repo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import com.store.erp.Models.AlmacenStockDTO;
import com.store.erp.Models.ProductoDTO;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Repository
public class ProductoRepo extends Mappers {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    public List<ProductoDTO> listarProductos() {
        String sql = "EXEC SP_PRODUCTO_LISTAR";

        return jdbcTemplate.query(sql, rs -> {
            Map<Integer, ProductoDTO> map = new LinkedHashMap<>();

            while (rs.next()) {
                int idProducto = safeInt(rs, "ID_PRODUCTO");

                ProductoDTO producto = map.get(idProducto);
                if (producto == null) {
                    producto = mapProducto(rs);
                    map.put(idProducto, producto);
                }

                if (hasColumn(rs, "ID_ALMACEN") || hasColumn(rs, "ID_ALMACEN_STOCK")) {
                    AlmacenStockDTO stock = mapAlmacenStock(rs);
                    if (stock != null) {
                        if (producto.getAlmacenStock() == null)
                            producto.setAlmacenStock(new ArrayList<>());
                        producto.getAlmacenStock().add(stock);
                    }
                }
            }

            return new ArrayList<>(map.values());
        });
    }

    public void registrarProducto(ProductoDTO p) {
        jdbcTemplate.update(
            "EXEC SP_PRODUCTO_INSERTAR ?, ?, ?, ?, ?, ?, ?",
            p.getCodProducto(),
            p.getDescProducto(),
            p.getStock(),
            p.getPrecio(),
            p.getImagen(),
            p.getRegalo(),
            p.getEstado()
        );
    }

    public void actualizarRegalo(int idProducto, boolean regalo) {
        jdbcTemplate.update(
            "EXEC SP_PRODUCTO_ACTUALIZAR_REGALO ?, ?",
            idProducto, regalo
        );
    }


    public void cambiarEstado(int idProducto, boolean estado) {
        jdbcTemplate.update(
            "EXEC SP_PRODUCTO_CAMBIAR_ESTADO ?, ?",
            idProducto, estado
        );
    }

}
