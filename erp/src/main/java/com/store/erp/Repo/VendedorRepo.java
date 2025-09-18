package com.store.erp.Repo;

import com.store.erp.Models.VendedorDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

@Repository
public class VendedorRepo {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private VendedorDTO mapRowToVendedor(ResultSet rs, int rowNum) throws SQLException {
        VendedorDTO vendedor = new VendedorDTO();
        vendedor.setIdVendedor(rs.getInt("id_vendedor"));
        vendedor.setCodVendedor(rs.getString("cod_vendedor"));
        vendedor.setNombreVendedor(rs.getString("nombre_vendedor"));
        vendedor.setEstado(rs.getBoolean("estado"));

        return vendedor;
    }

    public List<VendedorDTO> listar() {
        return jdbcTemplate.query("EXEC ListarVendedores", this::mapRowToVendedor);
    }

    public VendedorDTO buscarPorId(Integer id) {
        return jdbcTemplate.queryForObject("EXEC BuscarVendedor ?", this::mapRowToVendedor, id);
    }

    public void guardar(VendedorDTO vendedor) {
        jdbcTemplate.update("EXEC GuardarVendedor ?, ?, ?, ?",
                vendedor.getIdVendedor(),
                vendedor.getCodVendedor(),
                vendedor.getNombreVendedor(),
                vendedor.getEstado());
    }

    public void actualizar(VendedorDTO vendedor) {
        jdbcTemplate.update("EXEC ActualizarVendedor ?, ?, ?, ?",
                vendedor.getIdVendedor(),
                vendedor.getCodVendedor(),
                vendedor.getNombreVendedor(),
                vendedor.getEstado());
    }
}
