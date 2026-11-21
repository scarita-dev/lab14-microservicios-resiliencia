package com.tecsup.controller;

import com.tecsup.dto.CategoriaDTO;
import com.tecsup.service.CategoriaService;
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
@RequestMapping("/api/categorias")
@RequiredArgsConstructor
public class CategoriaController {

    private final CategoriaService service;

    @GetMapping
    public List<CategoriaDTO> listar() {
        return service.listar();
    }

    @GetMapping("/{id}")
    public ResponseEntity<CategoriaDTO> obtener(@PathVariable Long id) {
        CategoriaDTO dto = service.obtener(id);
        return dto == null ? ResponseEntity.notFound().build() : ResponseEntity.ok(dto);
    }

    @GetMapping("/slow/{id}")
    public ResponseEntity<CategoriaDTO> obtenerLento(@PathVariable Long id) throws InterruptedException {
        Thread.sleep(10000);
        CategoriaDTO dto = service.obtener(id);
        return dto == null ? ResponseEntity.notFound().build() : ResponseEntity.ok(dto);
    }

    @GetMapping("/config")
    public Map<String, String> config() {
        return Map.of("mensaje", service.mensajeConfig());
    }

    @PostMapping
    public CategoriaDTO guardar(@RequestBody CategoriaDTO dto) {
        return service.guardar(dto);
    }

    @PutMapping("/{id}")
    public ResponseEntity<CategoriaDTO> actualizar(@PathVariable Long id, @RequestBody CategoriaDTO dto) {
        CategoriaDTO actualizado = service.actualizar(id, dto);
        return actualizado == null ? ResponseEntity.notFound().build() : ResponseEntity.ok(actualizado);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        service.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
