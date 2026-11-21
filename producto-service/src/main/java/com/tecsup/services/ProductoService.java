package com.tecsup.services;

import com.tecsup.dto.CategoriaDTO;
import com.tecsup.model.Producto;
import com.tecsup.repository.ProductoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductoService {

    private final ProductoRepository repo;
    private final CategoriaCircuitService categoriaCircuitService;

    public List<Producto> listar() {
        List<Producto> productos = repo.findAll();
        productos.forEach(p -> p.setCategoria(categoriaCircuitService.obtenerCategoria(p.getCategoriaId())));
        return productos;
    }

    public List<Producto> listarConCategoriaLenta() {
        List<Producto> productos = repo.findAll();
        productos.forEach(p -> p.setCategoria(categoriaCircuitService.obtenerCategoriaLenta(p.getCategoriaId())));
        return productos;
    }

    public Producto guardar(Producto p) {
        Producto producto = repo.save(p);
        producto.setCategoria(categoriaCircuitService.obtenerCategoria(producto.getCategoriaId()));
        return producto;
    }

    public Producto obtener(Long id) {
        Producto producto = repo.findById(id).orElse(null);
        if (producto != null) {
            producto.setCategoria(categoriaCircuitService.obtenerCategoria(producto.getCategoriaId()));
        }
        return producto;
    }

    public Producto actualizar(Long id, Producto p) {
        return repo.findById(id).map(producto -> {
            producto.setNombre(p.getNombre());
            producto.setPrecio(p.getPrecio());
            producto.setStock(p.getStock());
            producto.setCategoriaId(p.getCategoriaId());
            Producto guardado = repo.save(producto);
            guardado.setCategoria(categoriaCircuitService.obtenerCategoria(guardado.getCategoriaId()));
            return guardado;
        }).orElse(null);
    }

    public void eliminar(Long id) {
        repo.deleteById(id);
    }
}
