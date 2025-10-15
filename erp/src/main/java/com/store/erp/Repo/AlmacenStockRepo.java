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

    public List<AlmacenStockDTO> listarPorProducto(int idProducto) {
        return jdbcTemplate.query(
            "EXEC SP_ALMACEN_STOCK_LISTAR_POR_PRODUCTO ?",
            new Object[]{idProducto},
            (rs, _) -> Mappers.mapAlmacenStock(rs)
        );
    }

    public void guardarStockProducto(int idProducto, List<AlmacenStockDTO> detalleStock) {
        jdbcTemplate.execute((Connection con) -> {
            try (CallableStatement cs = con.prepareCall("{call SP_ALMACEN_STOCK_GUARDAR_PRODUCTO(?, ?)}")) {

                // 1️⃣ Parámetro: ID_PRODUCTO
                cs.setInt(1, idProducto);

                // 2️⃣ Creamos el Table-Valued Parameter
                SQLServerDataTable tvp = new SQLServerDataTable();
                tvp.addColumnMetadata("ID_ALMACEN", java.sql.Types.INTEGER);
                tvp.addColumnMetadata("INVENTARIO", java.sql.Types.INTEGER);

                for (AlmacenStockDTO stock : detalleStock) {
                    tvp.addRow(stock.getAlmacen().getIdAlmacen(), stock.getInventario());
                }

                // 3️⃣ Pasamos el TVP
                cs.setObject(2, tvp);

                // 4️⃣ Ejecutamos el SP
                cs.execute();
                return null;
            }
        });
    }

}
