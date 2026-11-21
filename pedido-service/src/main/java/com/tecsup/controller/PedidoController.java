package com.tecsup.controller;

import com.tecsup.model.Pedido;
import com.tecsup.services.PedidoService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/pedidos")
@RequiredArgsConstructor
public class PedidoController {

    private final PedidoService service;

    @GetMapping
    public List<Pedido> listar() {
        return service.listar();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Pedido> obtener(@PathVariable Long id) {
        Pedido pedido = service.obtener(id);
        return pedido == null ? ResponseEntity.notFound().build() : ResponseEntity.ok(pedido);
    }

    @GetMapping("/retry")
    public List<Pedido> listarConClienteLento() {
        return service.listarConClienteLento();
    }

    @GetMapping("/bulkhead")
    public List<Pedido> listarConProductoLento() {
        return service.listarConProductoLento();
    }

    @PostMapping
    public Pedido guardar(@RequestBody Pedido pedido) {
        return service.guardar(pedido);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        service.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
