package com.store.erp.Controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/predict")
public class PredictionController {

    private final RestTemplate restTemplate = new RestTemplate();

    @PostMapping
    public ResponseEntity<String> getPrediction(@RequestParam double x) {
        String pythonUrl = "http://localhost:5000/predict";

        Map<String, Object> request = new HashMap<>();
        request.put("x", x);

        ResponseEntity<Map> response = restTemplate.postForEntity(pythonUrl, request, Map.class);

        return ResponseEntity.ok("Predicción: " + response.getBody().get("prediction"));
    }
}
