package com.store.erp.Controllers;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.store.erp.Models.AlmacenDTO;
import com.store.erp.Services.AlmacenService;
import java.util.List;

@RestController
@RequestMapping("/api/almacenes")
public class AlmacenController {

    @Autowired
    private AlmacenService almacenService;

    @GetMapping
    public List<AlmacenDTO> listarAlmacenes() {
        return almacenService.listarAlmacenes();
    }

}

