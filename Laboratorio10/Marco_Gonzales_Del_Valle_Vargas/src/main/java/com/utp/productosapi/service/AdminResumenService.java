package com.utp.productosapi.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.utp.productosapi.dto.ResumenBackendResponse;
import com.utp.productosapi.repository.MovimientoStockRepository;
import com.utp.productosapi.repository.ProductoRepository;

/**
 * Semana 10 (guia, seccion 8.2): resumen para administracion. Obliga a
 * recorrer todas las capas (Security -> Controller -> Service -> Repository
 * -> MySQL) sin introducir un tema nuevo.
 */
@Service
public class AdminResumenService {

    /** Por debajo de este stock un producto se considera "bajo". */
    static final int UMBRAL_STOCK_BAJO = 5;

    private final ProductoRepository productoRepository;
    private final MovimientoStockRepository movimientoRepository;

    public AdminResumenService(ProductoRepository productoRepository,
            MovimientoStockRepository movimientoRepository) {
        this.productoRepository = productoRepository;
        this.movimientoRepository = movimientoRepository;
    }

    /** Solo lectura: tres consultas que comparten una transaccion readOnly. */
    @Transactional(readOnly = true)
    public ResumenBackendResponse obtenerResumen() {
        long total = productoRepository.count();
        long bajo = productoRepository.countByStockLessThan(UMBRAL_STOCK_BAJO);
        long movimientos = movimientoRepository.count();

        return new ResumenBackendResponse(total, bajo, movimientos, "BACKEND_OPERATIVO");
    }
}
