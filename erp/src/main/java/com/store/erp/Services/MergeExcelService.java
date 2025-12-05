package com.store.erp.Services;

import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.HashMap;

@Slf4j
@Service
public class MergeExcelService {

    public byte[] mergeExcels(
            MultipartFile excelBase,
            MultipartFile excelUpdate,
            String keyBase,
            String keyUpdate,
            String estadoColumn
    ) throws Exception {

        log.info("========= INICIO DE FUSIÓN =========");

        // 🔹 Leer Excel Actualizado (donde están los estados nuevos)
        Workbook wbUpdate;
        try (InputStream inUpd = excelUpdate.getInputStream()) {
            wbUpdate = WorkbookFactory.create(inUpd);
        }
        Sheet sheetUpdate = wbUpdate.getSheetAt(0);

        Row headerUpd = sheetUpdate.getRow(0);
        int idxKeyUpd = findColumn(headerUpd, keyUpdate);
        int idxEstadoUpd = findColumn(headerUpd, estadoColumn);

        HashMap<String, String> mapEstados = new HashMap<>();

        for (int i = 1; i <= sheetUpdate.getLastRowNum(); i++) {
            Row r = sheetUpdate.getRow(i);
            if (r == null) continue;

            String key = getCellString(r.getCell(idxKeyUpd));
            String estado = getCellString(r.getCell(idxEstadoUpd));

            if (key != null) {
                mapEstados.put(key.trim(), estado);
            }
        }
        wbUpdate.close();

        log.info("Estados cargados desde Excel actualizado: {}", mapEstados.size());


        // 🔹 Leer Excel Base
        Workbook wbBase;
        try (InputStream inBase = excelBase.getInputStream()) {
            wbBase = WorkbookFactory.create(inBase);
        }
        Sheet sheetBase = wbBase.getSheetAt(0);

        Row headerBase = sheetBase.getRow(0);
        int idxKeyBase = findColumn(headerBase, keyBase);
        int idxEstadoBase = findColumn(headerBase, estadoColumn);


        // 🔹 Crear NUEVO EXCEL XLSX limpio
        Workbook wbNuevo = new XSSFWorkbook();
        Sheet nuevaHoja = wbNuevo.createSheet("RESULTADO");

        // Copiar encabezados
        Row headerNew = nuevaHoja.createRow(0);
        for (int c = 0; c < headerBase.getLastCellNum(); c++) {
            Cell orig = headerBase.getCell(c);
            Cell copy = headerNew.createCell(c);
            if (orig != null) copy.setCellValue(orig.toString());
        }

        // 🔹 Copiar filas y sobrescribir estado
        int rowNum = 1;
        for (int i = 1; i <= sheetBase.getLastRowNum(); i++) {

            Row rowBase = sheetBase.getRow(i);
            if (rowBase == null) continue;

            Row rowNew = nuevaHoja.createRow(rowNum++);

            String key = getCellString(rowBase.getCell(idxKeyBase));
            String estadoNuevo = (key != null) ? mapEstados.get(key.trim()) : null;

            for (int c = 0; c < headerBase.getLastCellNum(); c++) {

                Cell orig = rowBase.getCell(c);
                Cell copy = rowNew.createCell(c);

                if (c == idxEstadoBase && estadoNuevo != null) {
                    copy.setCellValue(estadoNuevo);
                } else {
                    if (orig != null) copy.setCellValue(orig.toString());
                }
            }
        }

        wbBase.close();


        // 🔹 Exportar nuevo archivo sin corrupción
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        wbNuevo.write(out);
        wbNuevo.close();

        log.info("========= FIN DE FUSIÓN (Excel generado OK) =========");

        return out.toByteArray();
    }

    // =============================================================
    // =============== MÉTODOS AUXILIARES ===========================
    // =============================================================

    private int findColumn(Row header, String colName) {
        for (int i = 0; i < header.getLastCellNum(); i++) {
            Cell c = header.getCell(i);
            if (c != null && c.getCellType() == CellType.STRING) {
                if (c.getStringCellValue().equalsIgnoreCase(colName)){
                    return i;
                }
            }
        }
        throw new RuntimeException("Columna no encontrada: " + colName);
    }

    private String getCellString(Cell cell) {
        if (cell == null) return null;
        return switch (cell.getCellType()) {
            case STRING -> cell.getStringCellValue();
            case NUMERIC -> String.valueOf((long) cell.getNumericCellValue());
            default -> cell.toString();
        };
    }
}
