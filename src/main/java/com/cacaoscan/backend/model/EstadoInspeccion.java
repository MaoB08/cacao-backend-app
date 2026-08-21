package com.cacaoscan.backend.model;

/**
 * Estados posibles de una inspección de campo.
 * EN_PROGRESO: Inspección en curso.
 * COMPLETADA: Inspección finalizada con éxito.
 * CANCELADA: Inspección abortada por el usuario.
 */
public enum EstadoInspeccion {
    EN_PROGRESO,
    COMPLETADA,
    CANCELADA
}
