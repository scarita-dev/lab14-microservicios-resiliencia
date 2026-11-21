package com.tecsup.controller;

import com.tecsup.model.Producto;
import com.tecsup.services.ProductoService;
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

@RestController
@RequestMapping("/api/productos")
@RequiredArgsConstructor
public class ProductoController {

    private final ProductoService service;

    @GetMapping
    public List<Producto> listar() {
        return service.listar();
    }

    @GetMapping("/bulkhead")
    public List<Producto> listarConCategoriaLenta() {
        return service.listarConCategoriaLenta();
    }

    @GetMapping("/bulkhead/{id}")
    public ResponseEntity<Producto> obtenerLento(@PathVariable Long id) throws InterruptedException {
        Thread.sleep(10000);
        Producto producto = service.obtener(id);
        return producto == null ? ResponseEntity.notFound().build() : ResponseEntity.ok(producto);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Producto> obtener(@PathVariable Long id) {
        Producto producto = service.obtener(id);
        return producto == null ? ResponseEntity.notFound().build() : ResponseEntity.ok(producto);
    }

    @PostMapping
    public Producto guardar(@RequestBody Producto producto) {
        return service.guardar(producto);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Producto> actualizar(@PathVariable Long id, @RequestBody Producto producto) {
        Producto actualizado = service.actualizar(id, producto);
        return actualizado == null ? ResponseEntity.notFound().build() : ResponseEntity.ok(actualizado);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        service.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
