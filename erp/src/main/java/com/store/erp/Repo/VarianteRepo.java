package com.store.erp.Repo;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import com.store.erp.Models.VarianteProductoDTO;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class VarianteRepo {

    private final JdbcTemplate jdbcTemplate;

    public List<VarianteProductoDTO> listarVariantes() {
        return jdbcTemplate.query("EXEC SP_VARIANTE_LISTAR", (rs, _) -> Mappers.mapVariante(rs));
    }

    public List<VarianteProductoDTO> listarPorProducto(int idProducto) {
        return jdbcTemplate.query(
            "EXEC SP_VARIANTE_LISTAR_POR_PRODUCTO ?",
            (rs, _) -> Mappers.mapVariante(rs),
            idProducto
        );
    }

    public VarianteProductoDTO buscarPorCod(String codVariante) {
        return jdbcTemplate.queryForObject(
            "EXEC SP_VARIANTE_BUSCAR_COD ?",
            (rs, _) -> Mappers.mapVariante(rs),
            codVariante
        );
    }

    public int guardarVariante(VarianteProductoDTO dto) {
        return jdbcTemplate.update(
            "EXEC SP_VARIANTE_GUARDAR ?, ?, ?, ?, ?, ?, ?",
            dto.getCodProducto(),
            dto.getCodVariante(),
            dto.getTitulo(),
            dto.getPrecio(),
            dto.getImgVariante(),
            dto.getFechaReg(),
            dto.getFechaAct()
        );
    }


    // 🔹 Eliminar variante
    public int eliminarVariante(int idVariante) {
        return jdbcTemplate.update(
            "EXEC SP_VARIANTE_ELIMINAR ?",
            idVariante
        );
    }
}
