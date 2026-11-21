package com.tecsup.services;

import com.tecsup.model.Pedido;
import com.tecsup.repository.PedidoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PedidoService {

    private final PedidoRepository repo;
    private final ClienteResilienceService clienteResilienceService;
    private final ProductoResilienceService productoResilienceService;

    public List<Pedido> listar() {
        List<Pedido> pedidos = repo.findAll();
        pedidos.forEach(p -> {
            p.setCliente(clienteResilienceService.obtenerCliente(p.getClienteId()));
            p.setProducto(productoResilienceService.obtenerProducto(p.getProductoId()));
        });
        return pedidos;
    }

    public List<Pedido> listarConClienteLento() {
        List<Pedido> pedidos = repo.findAll();
        pedidos.forEach(p -> {
            p.setCliente(clienteResilienceService.obtenerClienteLento(p.getClienteId()));
            p.setProducto(productoResilienceService.obtenerProducto(p.getProductoId()));
        });
        return pedidos;
    }

    public List<Pedido> listarConProductoLento() {
        List<Pedido> pedidos = repo.findAll();
        pedidos.forEach(p -> {
            p.setCliente(clienteResilienceService.obtenerCliente(p.getClienteId()));
            p.setProducto(productoResilienceService.obtenerProductoLento(p.getProductoId()));
        });
        return pedidos;
    }

    public Pedido guardar(Pedido pedido) {
        Pedido guardado = repo.save(pedido);
        guardado.setCliente(clienteResilienceService.obtenerCliente(guardado.getClienteId()));
        guardado.setProducto(productoResilienceService.obtenerProducto(guardado.getProductoId()));
        return guardado;
    }

    public Pedido obtener(Long id) {
        Pedido pedido = repo.findById(id).orElse(null);
        if (pedido != null) {
            pedido.setCliente(clienteResilienceService.obtenerCliente(pedido.getClienteId()));
            pedido.setProducto(productoResilienceService.obtenerProducto(pedido.getProductoId()));
        }
        return pedido;
    }

    public void eliminar(Long id) {
        repo.deleteById(id);
    }
}
