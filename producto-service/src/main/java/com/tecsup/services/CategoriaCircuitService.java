package com.tecsup.services;

import com.tecsup.client.CategoriaClient;
import com.tecsup.dto.CategoriaDTO;
import io.github.resilience4j.bulkhead.annotation.Bulkhead;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CategoriaCircuitService {

    private final CategoriaClient categoriaClient;

    @Bulkhead(name = "categoriaBulkhead", fallbackMethod = "categoriaFallback")
    @Retry(name = "categoriaRetry", fallbackMethod = "categoriaFallback")
    @CircuitBreaker(name = "categoriaService", fallbackMethod = "categoriaFallback")
    public CategoriaDTO obtenerCategoria(Long id) {
        System.out.println("Llamando a categoria-service...");
        return categoriaClient.obtenerCategoria(id);
    }

    @Bulkhead(name = "categoriaBulkheadLenta", fallbackMethod = "categoriaFallback")
    @Retry(name = "categoriaRetryLenta", fallbackMethod = "categoriaFallback")
    @CircuitBreaker(name = "categoriaService", fallbackMethod = "categoriaFallback")
    public CategoriaDTO obtenerCategoriaLenta(Long id) {
        System.out.println("Llamando a categoria-service (lento)...");
        return categoriaClient.obtenerCategoriaLenta(id);
    }

    public CategoriaDTO categoriaFallback(Long id, Throwable ex) {
        System.out.println("Fallback ejecutado: " + ex.getMessage());
        CategoriaDTO dto = new CategoriaDTO();
        dto.setId(id);
        dto.setNombre("Categoría temporal no disponible");
        return dto;
    }
}
