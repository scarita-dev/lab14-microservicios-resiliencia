package com.tecsup.services;

import com.tecsup.client.PedidoClient;
import com.tecsup.dto.PedidoDTO;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PedidoCircuitService {

    private final PedidoClient pedidoClient;

    @CircuitBreaker(name = "pedidoService", fallbackMethod = "pedidoFallback")
    public PedidoDTO obtenerPedido(Long id) {
        System.out.println("Llamando a pedido-service...");
        return pedidoClient.obtenerPedido(id);
    }

    public PedidoDTO pedidoFallback(Long id, Throwable ex) {
        System.out.println("Circuit Breaker activado (cliente -> pedido): " + ex.getMessage());
        PedidoDTO dto = new PedidoDTO();
        dto.setId(id);
        dto.setTotal(java.math.BigDecimal.ZERO);
        return dto;
    }
}
