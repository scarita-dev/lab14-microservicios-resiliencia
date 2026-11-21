package com.tecsup.config;

import com.tecsup.model.Cliente;
import com.tecsup.repository.ClienteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final ClienteRepository repo;

    @Override
    public void run(String... args) {
        if (repo.count() == 0) {
            repo.save(new Cliente(null, "Juan Pérez", "juan.perez@example.com", "987654321"));
            repo.save(new Cliente(null, "María Gómez", "maria.gomez@example.com", "912345678"));
        }
    }
}
