package com.store.erp.Controllers;

import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.store.erp.Services.UtilsService;

@RestController
@RequestMapping("/api/utils")
public class UtilsController {

    @Autowired
    private UtilsService utilsService;

    // ============================
    // LISTAR CIUDADES
    // ============================
    @GetMapping("/ciudades")
    public ResponseEntity<?> listarCiudades() {
        try {
            return ResponseEntity.ok(utilsService.listarCiudades());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }
}
