package com.store.erp.Services;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.store.erp.Models.AlmacenDTO;
import com.store.erp.Repo.AlmacenRepo;

import java.util.List;

@Service
public class AlmacenService {

    @Autowired
    private AlmacenRepo almacenRepo;

    public List<AlmacenDTO> listarAlmacenes() {
        return almacenRepo.listarAlmacenes();
    }
}
