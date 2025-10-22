package com.store.erp.Models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ClienteLogDTO {

  private int idClienteLog;
  private int idCliente;
  private String actividad;
  private String fechaActividad;

}
