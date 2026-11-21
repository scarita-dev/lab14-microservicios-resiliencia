package com.tecsup.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PedidoDTO {
    private Long id;
    private Long clienteId;
    private Long productoId;
    private Integer cantidad;
    private BigDecimal total;
}
