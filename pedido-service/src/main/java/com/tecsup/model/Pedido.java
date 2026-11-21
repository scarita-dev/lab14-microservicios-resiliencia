package com.tecsup.model;

import com.tecsup.dto.ClienteDTO;
import com.tecsup.dto.ProductoDTO;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "pedido")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Pedido {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Long clienteId;
    private Long productoId;
    private Integer cantidad;
    private BigDecimal total;
    private LocalDateTime fecha;
    @Transient
    private ClienteDTO cliente;
    @Transient
    private ProductoDTO producto;
}
