package com.store.erp.Controllers;

import com.store.erp.Services.MergeExcelService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/excel")
public class MergeExcelController {

    @Autowired
    private MergeExcelService mergeExcelService;

    @PostMapping("/merge")
public ResponseEntity<byte[]> mergeExcels(
        @RequestParam("excel1") MultipartFile excel1,
        @RequestParam("excel2") MultipartFile excel2,
        @RequestParam("keyBase") String keyBase,
        @RequestParam("keyUpdate") String keyUpdate,
        @RequestParam("estado") String estadoColumnName
) throws Exception {

    byte[] file = mergeExcelService.mergeExcels(excel1, excel2, keyBase, keyUpdate, estadoColumnName);

    return ResponseEntity.ok()
            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=resultado.xlsx")
            .contentType(MediaType.APPLICATION_OCTET_STREAM)
            .body(file);
}

}
