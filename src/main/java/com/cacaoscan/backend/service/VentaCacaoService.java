package com.cacaoscan.backend.service;

import com.cacaoscan.backend.dto.VentaCacaoRequest;
import com.cacaoscan.backend.dto.VentaCacaoResponse;
import com.cacaoscan.backend.exception.ResourceNotFoundException;
import com.cacaoscan.backend.model.Usuario;
import com.cacaoscan.backend.model.VentaCacao;
import com.cacaoscan.backend.repository.UsuarioRepository;
import com.cacaoscan.backend.repository.VentaCacaoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class VentaCacaoService {

    private final VentaCacaoRepository ventaCacaoRepository;
    private final UsuarioRepository usuarioRepository;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    public VentaCacaoService(VentaCacaoRepository ventaCacaoRepository, UsuarioRepository usuarioRepository) {
        this.ventaCacaoRepository = ventaCacaoRepository;
        this.usuarioRepository = usuarioRepository;
    }

    @Transactional
    public VentaCacaoResponse registrarVenta(String email, VentaCacaoRequest request) {
        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));

        VentaCacao venta = new VentaCacao();
        venta.setUsuario(usuario);
        venta.setCantidad(request.getCantidad());
        venta.setUnidad(request.getUnidad());
        venta.setTipoGrano(request.getTipoGrano());
        venta.setHumedad(request.getHumedad());
        venta.setTotalEstimado(request.getTotalEstimado());

        VentaCacao guardada = ventaCacaoRepository.save(venta);
        return mapToResponse(guardada);
    }

    @Transactional(readOnly = true)
    public List<VentaCacaoResponse> obtenerVentasUsuario(String email) {
        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));

        return ventaCacaoRepository.findByUsuarioIdOrderByFechaDesc(usuario.getId())
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public void eliminarVenta(String email, UUID ventaId) {
        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));

        VentaCacao venta = ventaCacaoRepository.findByIdAndUsuarioId(ventaId, usuario.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Registro de venta no encontrado"));

        ventaCacaoRepository.delete(venta);
    }

    @Transactional
    public void eliminarTodasVentas(String email) {
        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));

        ventaCacaoRepository.deleteByUsuarioId(usuario.getId());
    }

    private VentaCacaoResponse mapToResponse(VentaCacao venta) {
        String fechaFormateada = venta.getFecha() != null
                ? venta.getFecha().format(DATE_FORMATTER)
                : "";

        return new VentaCacaoResponse(
                venta.getId(),
                venta.getCantidad(),
                venta.getUnidad(),
                venta.getTipoGrano(),
                venta.getHumedad(),
                venta.getTotalEstimado(),
                fechaFormateada
        );
    }
}
