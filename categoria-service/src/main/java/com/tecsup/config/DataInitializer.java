package com.tecsup.config;

import com.tecsup.model.Categoria;
import com.tecsup.repository.CategoriaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final CategoriaRepository repo;

    @Override
    public void run(String... args) {
        if (repo.count() == 0) {
            repo.save(new Categoria(null, "Tecnología", true));
            repo.save(new Categoria(null, "Hogar", true));
            repo.save(new Categoria(null, "Deportes", true));
        }
    }
}
