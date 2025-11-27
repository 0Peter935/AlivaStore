package com.store.erp.Services;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.store.erp.Models.UtilsDTO;
import com.store.erp.Repo.UtilsRepo;


@Service
public class UtilsService {

    @Autowired
    private UtilsRepo utilsRepo;

    public List<UtilsDTO.ListarCiudad> listarCiudades() {
        return utilsRepo.listarCiudad();
    }
}