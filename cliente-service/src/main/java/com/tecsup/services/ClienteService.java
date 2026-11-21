package com.tecsup.services;

import com.tecsup.model.Cliente;
import com.tecsup.repository.ClienteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ClienteService {

    private final ClienteRepository repo;

    @Value("${mensaje.texto:Configuración local por defecto}")
    private String mensaje;

    public List<Cliente> listar() {
        return repo.findAll();
    }

    public Cliente obtener(Long id) {
        return repo.findById(id).orElse(null);
    }

    public Cliente guardar(Cliente cliente) {
        return repo.save(cliente);
    }

    public Cliente actualizar(Long id, Cliente cliente) {
        return repo.findById(id).map(c -> {
            c.setNombre(cliente.getNombre());
            c.setEmail(cliente.getEmail());
            c.setTelefono(cliente.getTelefono());
            return repo.save(c);
        }).orElse(null);
    }

    public void eliminar(Long id) {
        repo.deleteById(id);
    }

    public String mensajeConfig() {
        return mensaje;
    }
}
