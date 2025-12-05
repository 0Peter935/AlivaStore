package com.store.erp.Configuration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import lombok.Data;

@Data
@Component
public class EcommerceProperties {

  // Shopify API credenciales
  @Value("${shopify.domain}")
  private String shopDomain;

  @Value("${shopify.token}")
  private String accessToken;

  // File paths
  @Value("${file.evidencia.path}")
  private String evidenciaPath;

}
