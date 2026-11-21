package com.tecsup.config;

import com.tecsup.model.Producto;
import com.tecsup.repository.ProductoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final ProductoRepository repo;

    @Override
    public void run(String... args) {
        if (repo.count() == 0) {
            repo.save(new Producto(null, "Laptop", new BigDecimal("2500"), 10, 1L, null));
            repo.save(new Producto(null, "Mouse", new BigDecimal("80"), 50, 1L, null));
            repo.save(new Producto(null, "Silla", new BigDecimal("450"), 15, 2L, null));
        }
    }
}
