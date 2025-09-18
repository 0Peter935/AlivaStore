package com.store.erp.Repo;

import com.store.erp.Models.AlmacenDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

@Repository
public class AlmacenRepo {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private AlmacenDTO mapRowToAlmacen(ResultSet rs, int rowNum) throws SQLException {
        AlmacenDTO almacen = new AlmacenDTO();
        almacen.setIdAlmacen(rs.getInt("id_almacen"));
        almacen.setIdProducto(rs.getLong("id_producto"));
        almacen.setStockReal(rs.getInt("stock_real"));
        almacen.setStockSeparado(rs.getInt("stock_separado"));

        return almacen;
    }

    public List<AlmacenDTO> listar() {
        return jdbcTemplate.query("EXEC ListarAlmacen", this::mapRowToAlmacen);
    }

    public AlmacenDTO buscarPorId(Integer id) {
        return jdbcTemplate.queryForObject("EXEC BuscarAlmacen ?", this::mapRowToAlmacen, id);
    }

    public void guardar(AlmacenDTO almacen) {
        jdbcTemplate.update("EXEC GuardarAlmacen ?, ?, ?, ?",
                almacen.getIdAlmacen(),
                almacen.getIdProducto(),
                almacen.getStockReal(),
                almacen.getStockSeparado());
    }

    public void actualizar(AlmacenDTO almacen) {
        jdbcTemplate.update("EXEC ActualizarAlmacen ?, ?, ?, ?",
                almacen.getIdAlmacen(),
                almacen.getIdProducto(),
                almacen.getStockReal(),
                almacen.getStockSeparado());
    }
}
