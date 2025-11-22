package com.store.erp.Repo;

import com.microsoft.sqlserver.jdbc.SQLServerDataTable;
import com.store.erp.Models.AlmacenStockDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.util.List;

@Repository
public class AlmacenStockRepo extends Mappers {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    public List<AlmacenStockDTO> listarPorProducto(String codVariante) {
        return jdbcTemplate.query(
            "EXEC SP_ALMACEN_STOCK_LISTAR_POR_PRODUCTO ?",
            new Object[]{codVariante},
            (rs, _) -> Mappers.mapAlmacenStock(rs)
        );
    }

    public void guardarStockProducto(String codVariante, List<AlmacenStockDTO> detalleStock) {
        jdbcTemplate.execute((Connection con) -> {
            try (CallableStatement cs = con.prepareCall("{call SP_ALMACEN_STOCK_GUARDAR_PRODUCTO(?, ?)}")) {

                cs.setString(1, codVariante);

                SQLServerDataTable tvp = new SQLServerDataTable();
                tvp.addColumnMetadata("ID_ALMACEN", java.sql.Types.INTEGER);
                tvp.addColumnMetadata("INVENTARIO", java.sql.Types.INTEGER);

                for (AlmacenStockDTO stock : detalleStock) {
                    tvp.addRow(stock.getAlmacen().getIdAlmacen(), stock.getInventario());
                }

                cs.setObject(2, tvp);

                cs.execute();
                return null;
            }
        });
    }

}
