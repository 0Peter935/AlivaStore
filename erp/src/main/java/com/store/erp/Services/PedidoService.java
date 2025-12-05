package com.store.erp.Services;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.store.erp.Configuration.EcommerceProperties;
import com.store.erp.Models.PedidoDTO;
import com.store.erp.Models.PedidoEvidenciaDTO;
import com.store.erp.Models.PedidoLogDTO;
import com.store.erp.Models.UsuarioDTO;
import com.store.erp.Repo.ClienteRepo;
import com.store.erp.Repo.PedidoRepo;
import com.store.erp.Repo.UsuarioRepo;

@Service
public class PedidoService {

    @Autowired
    private PedidoRepo pedidoRepo;

    @Autowired
    private ClienteRepo clienteRepo;

    @Autowired
    private UsuarioRepo usuarioRepo;

    @Autowired
    private EcommerceProperties properties;

    public List<PedidoDTO> listarPedidos() {
        return pedidoRepo.listarPedidos();
    }
    
    public List<PedidoDTO> listarPedidosPorVendedor(int idVendedor) {
        return pedidoRepo.listarPedidosPorVendedor(idVendedor);
    }

    public List<PedidoDTO> listarPedidosPorEstado(int idEstado, Integer idUsuario) {
        return pedidoRepo.listarPedidosPorEstado(idEstado, idUsuario);
    }

    public List<PedidoDTO> listarPedidosPorEstadoyUsuario(int idEstado, Integer idUsuario) {
        return pedidoRepo.listarPedidosPorEstadoyUsuario(idEstado, idUsuario);
    }

    public PedidoDTO obtenerPedidoPorCod(String codPedido) {
        PedidoDTO p = pedidoRepo.obtenerPedidoPorCod(codPedido);
        p.setEvidencias(pedidoRepo.listarEvidenciasPedido(p.getCodPedido()));
        p.setLogs(pedidoRepo.listarlogsPedido(codPedido));
        return p;
    }

    public List<PedidoLogDTO> listarlogsPedido(String codPedido){
        return pedidoRepo.listarlogsPedido(codPedido);
    }

    public void actualizarPedidoCompleto(PedidoDTO pedido, List<MultipartFile> archivosEvidencia) {

        // 1. Actualizar pedido y cliente
        pedidoRepo.actualizarPedidoConDetalle(pedido);
        clienteRepo.actualizarDatos(pedido.getCliente());

        // 2. Guardar evidencias nuevas
        if (pedido.getEvidencias() != null && archivosEvidencia != null) {
            for (int i = 0; i < pedido.getEvidencias().size(); i++) {

                PedidoEvidenciaDTO ev = pedido.getEvidencias().get(i);

                // Solo evidencias nuevas
                if (ev.getIdEvidenciaPedido() == null || ev.getIdEvidenciaPedido() == 0L) {
                    MultipartFile file = archivosEvidencia.get(i);
                    String nombre = guardarArchivoEvidencia(pedido.getCodPedido(), file);
                    ev.setUrl(nombre);
                    pedidoRepo.agregarEvidenciaPedido(ev);
                }
            }
        }

        // 3. Eliminar evidencias
        if (pedido.getEvidenciasEliminar() != null) {
            for (Integer id : pedido.getEvidenciasEliminar()) {
                pedidoRepo.eliminarEvidenciaPedidoPorId(id);
            }
        }

        // 4. Guardar el log (SOLO EL NUEVO)
        if (pedido.getLogNuevo() != null) {
            pedidoRepo.insertarLog(pedido.getLogNuevo());
        }
    }

    public boolean registrarPedido(PedidoDTO dto) {
        try {
            pedidoRepo.guardarPedidoCompleto(dto);
            return true;

        } catch (Exception ex) {
            System.err.println("Error al registrar pedido en repo: " + ex.getMessage());
            if (ex.getCause() != null)
                System.err.println("Causa interna: " + ex.getCause().getMessage());
            ex.printStackTrace();
            throw ex;
        }
    }

    public String guardarArchivoEvidencia(String codPedido, MultipartFile file) {
        String basePath = properties.getEvidenciaPath();

        File dir = new File(basePath);
        if (!dir.exists()) dir.mkdirs();

        try {
            if (file.isEmpty()) return null;

            String original = Objects.requireNonNull(file.getOriginalFilename());
            String extension = original.substring(original.lastIndexOf('.'));

            String nombreArchivo = codPedido + "_" + System.nanoTime() + extension;

            Path destino = Paths.get(basePath, nombreArchivo);
            Files.copy(file.getInputStream(), destino, StandardCopyOption.REPLACE_EXISTING);

            return "/recursos/img/evidencia/" + nombreArchivo;

        } catch (Exception e) {
            throw new RuntimeException("Error guardando evidencia", e);
        }
    }

    public void regresarPedidoRevision(PedidoDTO pedido) {

        if (pedido.getCodPedido() == null)
            throw new IllegalArgumentException("codPedido es requerido");

        if (pedido.getLogNuevo() == null)
            throw new IllegalArgumentException("logNuevo es requerido");

        pedidoRepo.actualizarEstado(pedido);
        pedidoRepo.insertarLog(pedido.getLogNuevo());
    }

    public void completarPedido(PedidoDTO pedido, List<MultipartFile> archivosEvidencia) throws Exception {

        pedidoRepo.actualizarEstado(pedido);

        if (pedido.getEvidencias() != null && archivosEvidencia != null) {

            for (int i = 0; i < pedido.getEvidencias().size(); i++) {

                PedidoEvidenciaDTO ev = pedido.getEvidencias().get(i);

                if (ev.getIdEvidenciaPedido() == null || ev.getIdEvidenciaPedido() == 0L) {

                    MultipartFile file = archivosEvidencia.get(i);
                    String url = guardarArchivoEvidencia(pedido.getCodPedido(), file);

                    ev.setUrl(url);
                    pedidoRepo.agregarEvidenciaPedido(ev);
                }
            }
        }

        pedidoRepo.insertarLog(pedido.getLogNuevo());
    }

    public Integer obtenerVendedorDisponible() {
        List<UsuarioDTO> vendedores = usuarioRepo.listarVendedoresActivos();
        if (vendedores.isEmpty()) return null;

        Map<Integer, Integer> cargas = pedidoRepo.obtenerCargaPedidosPorVendedor();

        // Asignar carga 0 a quienes no tengan pedidos
        for (UsuarioDTO v : vendedores) {
            cargas.putIfAbsent(v.getIdUsuario(), 0);
        }

        // Elegir el vendedor con menos pedidos
        return cargas.entrySet()
            .stream()
            .min(Comparator.comparingInt(Map.Entry::getValue))
            .map(Map.Entry::getKey)
            .orElse(null);
    }

}