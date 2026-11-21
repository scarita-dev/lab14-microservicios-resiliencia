package com.tecsup.services;

import com.tecsup.client.ClienteClient;
import com.tecsup.dto.ClienteDTO;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ClienteResilienceService {

    private final ClienteClient clienteClient;

    @Retry(name = "clienteRetry", fallbackMethod = "clienteFallback")
    public ClienteDTO obtenerCliente(Long id) {
        System.out.println("Llamando a cliente-service...");
        return clienteClient.obtenerCliente(id);
    }

    @Retry(name = "clienteRetry", fallbackMethod = "clienteFallback")
    public ClienteDTO obtenerClienteLento(Long id) {
        System.out.println("Llamando a cliente-service (lento)...");
        return clienteClient.obtenerClienteLento(id);
    }

    public ClienteDTO clienteFallback(Long id, Throwable ex) {
        System.out.println("Fallback ejecutado (Retry cliente): " + ex.getMessage());
        ClienteDTO dto = new ClienteDTO();
        dto.setId(id);
        dto.setNombre("Cliente temporal no disponible");
        return dto;
    }
}
