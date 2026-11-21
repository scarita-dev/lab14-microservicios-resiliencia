package com.tecsup.config;

import com.tecsup.model.Pedido;
import com.tecsup.repository.PedidoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final PedidoRepository repo;

    @Override
    public void run(String... args) {
        if (repo.count() == 0) {
            repo.save(new Pedido(null, 1L, 1L, 1, new BigDecimal("2500"), LocalDateTime.now(), null, null));
            repo.save(new Pedido(null, 2L, 2L, 2, new BigDecimal("160"), LocalDateTime.now(), null, null));
        }
    }
}
