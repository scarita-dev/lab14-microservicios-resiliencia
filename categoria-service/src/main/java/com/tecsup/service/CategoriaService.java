package com.tecsup.service;

import com.tecsup.dto.CategoriaDTO;
import com.tecsup.model.Categoria;
import com.tecsup.repository.CategoriaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CategoriaService {

    private final CategoriaRepository repo;

    @Value("${mensaje.texto:Configuración local por defecto}")
    private String mensaje;

    public List<CategoriaDTO> listar() {
        return repo.findAll().stream().map(this::mapear).collect(Collectors.toList());
    }

    public CategoriaDTO obtener(Long id) {
        return repo.findById(id).map(this::mapear).orElse(null);
    }

    public CategoriaDTO guardar(CategoriaDTO dto) {
        return mapear(repo.save(new Categoria(dto.getId(), dto.getNombre(), dto.getEstado())));
    }

    public CategoriaDTO actualizar(Long id, CategoriaDTO dto) {
        return repo.findById(id).map(c -> {
            c.setNombre(dto.getNombre());
            c.setEstado(dto.getEstado());
            return mapear(repo.save(c));
        }).orElse(null);
    }

    public void eliminar(Long id) {
        repo.deleteById(id);
    }

    public String mensajeConfig() {
        return mensaje;
    }

    private CategoriaDTO mapear(Categoria c) {
        return new CategoriaDTO(c.getId(), c.getNombre(), c.getEstado());
    }
}
