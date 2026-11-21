package com.tecsup.services;

import com.tecsup.client.ProductoClient;
import com.tecsup.dto.ProductoDTO;
import io.github.resilience4j.bulkhead.annotation.Bulkhead;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ProductoResilienceService {

    private final ProductoClient productoClient;

    @Bulkhead(name = "productoBulkhead", fallbackMethod = "productoFallback")
    @CircuitBreaker(name = "pedidoProductoService", fallbackMethod = "productoFallback")
    public ProductoDTO obtenerProducto(Long id) {
        System.out.println("Llamando a producto-service...");
        return productoClient.obtenerProducto(id);
    }

    @Bulkhead(name = "productoBulkhead", fallbackMethod = "productoFallback")
    @CircuitBreaker(name = "pedidoProductoService", fallbackMethod = "productoFallback")
    public ProductoDTO obtenerProductoLento(Long id) {
        System.out.println("Llamando a producto-service (lento)...");
        return productoClient.obtenerProductoLento(id);
    }

    public ProductoDTO productoFallback(Long id, Throwable ex) {
        System.out.println("Fallback ejecutado (Bulkhead producto): " + ex.getMessage());
        ProductoDTO dto = new ProductoDTO();
        dto.setId(id);
        dto.setNombre("Producto temporal no disponible");
        return dto;
    }
}
