package com.tecsup.controller;

import com.tecsup.dto.PedidoDTO;
import com.tecsup.model.Cliente;
import com.tecsup.services.ClienteService;
import com.tecsup.services.PedidoCircuitService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/clientes")
@RequiredArgsConstructor
public class ClienteController {

    private final ClienteService service;
    private final PedidoCircuitService pedidoCircuitService;

    @GetMapping
    public List<Cliente> listar() {
        return service.listar();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Cliente> obtener(@PathVariable Long id) {
        Cliente cliente = service.obtener(id);
        return cliente == null ? ResponseEntity.notFound().build() : ResponseEntity.ok(cliente);
    }

    @GetMapping("/slow/{id}")
    public ResponseEntity<Cliente> obtenerLento(@PathVariable Long id) throws InterruptedException {
        Thread.sleep(5000);
        Cliente cliente = service.obtener(id);
        return cliente == null ? ResponseEntity.notFound().build() : ResponseEntity.ok(cliente);
    }

    @GetMapping("/{id}/pedidos")
    public PedidoDTO pedidos(@PathVariable Long id) {
        return pedidoCircuitService.obtenerPedido(id);
    }

    @GetMapping("/config")
    public Map<String, String> config() {
        return Map.of("mensaje", service.mensajeConfig());
    }

    @PostMapping
    public Cliente guardar(@RequestBody Cliente cliente) {
        return service.guardar(cliente);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Cliente> actualizar(@PathVariable Long id, @RequestBody Cliente cliente) {
        Cliente actualizado = service.actualizar(id, cliente);
        return actualizado == null ? ResponseEntity.notFound().build() : ResponseEntity.ok(actualizado);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        service.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
