package com.store.erp.Repo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import com.store.erp.Models.ProductoDTO;

import java.sql.ResultSet;
import java.util.List;

@Repository
public class ProductoRepo extends Mappers {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    public List<ProductoDTO> listarProductos() {
        return jdbcTemplate.query("EXEC SP_PRODUCTO_LISTAR", (ResultSet rs) -> mapProductosConVariantesYStock(rs));
    }

    public ProductoDTO obtenerPorId(int idProducto) {
        return jdbcTemplate.queryForObject(
            "EXEC SP_PRODUCTO_BUSCAR_ID ?",
            (rs, _) -> Mappers.mapProducto(rs),
            idProducto
        );
    }

    public ProductoDTO obtenerPorCod(String codProducto) {
        return jdbcTemplate.queryForObject(
            "EXEC SP_PRODUCTO_BUSCAR_COD ?",
            (rs, _) -> Mappers.mapProducto(rs),
            codProducto
        );
    }

    public int guardarProducto(ProductoDTO dto) {
        return jdbcTemplate.update(
            "EXEC SP_PRODUCTO_GUARDAR ?, ?, ?, ?, ?, ?, ?",
            dto.getCodProducto(),
            dto.getDescProducto(),
            dto.getImg(),
            dto.getRegalo(),
            dto.getEstado(),
            dto.getFechaReg(),
            dto.getFechaAct()
        );
    }

    public void actualizarProducto(ProductoDTO p) {
        jdbcTemplate.update(
            "EXEC SP_PRODUCTO_ACTUALIZAR ?, ?, ?",
            p.getCodProducto(),
            p.getDescProducto(),
            p.getImg()
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
