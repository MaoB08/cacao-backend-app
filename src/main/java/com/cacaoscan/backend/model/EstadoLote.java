package com.cacaoscan.backend.model;

/**
 * Estados posibles de un lote agrícola.
 * ACTIVO: Lote en producción.
 * INACTIVO: Lote fuera de producción temporal.
 * EN_RENOVACION: Lote en proceso de replantación.
 * ABANDONADO: Lote sin mantenimiento.
 */
public enum EstadoLote {
    ACTIVO,
    INACTIVO,
    EN_RENOVACION,
    ABANDONADO
}
