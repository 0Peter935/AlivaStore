package com.store.erp.Services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.store.erp.Models.TipoAlmacenDTO;
import com.store.erp.Repo.TipoAlmacenRepo;

import java.util.List;

@Service
public class TipoAlmacenService {

    @Autowired
    private TipoAlmacenRepo tipoRepo;

    public List<TipoAlmacenDTO> listarTipos() {
        return tipoRepo.listarTipos();
    }

    public TipoAlmacenDTO buscarPorId(int id) {
        return tipoRepo.buscarPorId(id);
    }
}
