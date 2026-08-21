package br.com.copadasautoras.service;

import br.com.copadasautoras.dto.*;
import br.com.copadasautoras.entity.CategoriaLance;
import br.com.copadasautoras.entity.Lance;
import br.com.copadasautoras.entity.LanceMidia;
import br.com.copadasautoras.entity.OrigemMidia;
import br.com.copadasautoras.entity.TipoMidia;
import br.com.copadasautoras.repository.LanceRepository;
import br.com.copadasautoras.storage.CloudinaryStorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.text.Normalizer;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class LanceService {

    private final LanceRepository lanceRepository;
    private final CloudinaryStorageService storageService;
    private final LanceExportService exportService;

    // =========================
    // PÚBLICO — TIMELINE
    // =========================

    @Transactional(readOnly = true)
    public List<LancePublicoDTO> listarPublico(
            CategoriaLance categoria,
            boolean apenasGolaco
    ) {

        return lanceRepository
                .buscarVisiveis(
                        LocalDateTime.now(),
                        categoria,
                        apenasGolaco
                )
                .stream()
                .map(LancePublicoDTO::fromEntity)
                .toList();
    }

    @Transactional(readOnly = true)
    public LancePublicoDTO buscarPublicoPorSlug(String slug) {

        Lance lance = lanceRepository
                .buscarVisivelPorSlug(
                        slug,
                        LocalDateTime.now()
                )
                .orElseThrow(() ->
                        new RuntimeException(
                                "Lance não encontrado."
                        )
                );

        return LancePublicoDTO.fromEntity(lance);
    }

    // =========================
    // ADMIN — LISTAGEM
    // =========================

    @Transactional(readOnly = true)
    public List<LanceAdminDTO> listarAdmin(
            CategoriaLance categoria,
            boolean apenasGolaco
    ) {

        return lanceRepository
                .buscarParaAdmin(categoria, apenasGolaco)
                .stream()
                .map(LanceAdminDTO::fromEntity)
                .toList();
    }

    @Transactional(readOnly = true)
    public LanceAdminDTO buscarAdminPorId(Long id) {
        return LanceAdminDTO.fromEntity(obterLance(id));
    }

    // =========================
    // ADMIN — CRIAR
    // =========================

    @Transactional
    public LanceAdminDTO criar(LanceRequestDTO dto) {

        Lance lance = Lance.builder()
                .titulo(dto.titulo())
                .resumo(dto.resumo())
                .categoria(dto.categoria())
                .golaco(dto.golaco())
                .veiculo(dto.veiculo())
                .linkExterno(dto.linkExterno())
                .status(dto.status())
                .dataAcontecimento(dto.dataAcontecimento())
                .publicarEm(dto.publicarEm())
                .slug(gerarSlugUnico(dto.titulo()))
                .build();

        // Mídias por embed informadas no cadastro.
        if (dto.midiasEmbed() != null) {

            int i = 0;

            for (LanceEmbedMidiaDTO m : dto.midiasEmbed()) {

                LanceMidia midia = LanceMidia.builder()
                        .tipo(m.tipo())
                        .origem(OrigemMidia.EMBED)
                        .url(m.url())
                        .legenda(m.legenda())
                        .ordem(m.ordem() != null ? m.ordem() : i)
                        .build();

                lance.adicionarMidia(midia);
                i++;
            }
        }

        lance = lanceRepository.save(lance);

        return LanceAdminDTO.fromEntity(lance);
    }

    // =========================
    // ADMIN — EDITAR
    // =========================

    @Transactional
    public LanceAdminDTO editar(Long id, LanceUpdateDTO dto) {

        Lance lance = obterLance(id);

        lance.setTitulo(dto.titulo());
        lance.setResumo(dto.resumo());
        lance.setCategoria(dto.categoria());
        lance.setGolaco(dto.golaco());
        lance.setVeiculo(dto.veiculo());
        lance.setLinkExterno(dto.linkExterno());
        lance.setStatus(dto.status());
        lance.setDataAcontecimento(dto.dataAcontecimento());
        lance.setPublicarEm(dto.publicarEm());

        // slug permanece estável de propósito — não quebra
        // links já divulgados.

        lanceRepository.save(lance);

        return LanceAdminDTO.fromEntity(lance);
    }

    // =========================
    // ADMIN — EXCLUIR
    // =========================

    @Transactional
    public void excluir(Long id) {

        Lance lance = obterLance(id);

        // Remove do Cloudinary apenas as mídias que são
        // nossas (UPLOAD). Embeds são do veículo — ficam.
        for (LanceMidia midia : lance.getMidias()) {
            apagarArquivoSeUpload(midia);
        }

        lanceRepository.delete(lance);
    }

    // =========================
    // ADMIN — MÍDIAS (UPLOAD)
    // =========================

    @Transactional
    public LanceAdminDTO adicionarMidiaUpload(
            Long lanceId,
            MultipartFile arquivo,
            TipoMidia tipo,
            String legenda,
            Integer ordem
    ) {

        Lance lance = obterLance(lanceId);

        String url = storageService.uploadMidiaLance(arquivo);

        LanceMidia midia = LanceMidia.builder()
                .tipo(tipo)
                .origem(OrigemMidia.UPLOAD)
                .url(url)
                .legenda(legenda)
                .ordem(ordem != null ? ordem : lance.getMidias().size())
                .build();

        lance.adicionarMidia(midia);

        lanceRepository.save(lance);

        return LanceAdminDTO.fromEntity(lance);
    }

    @Transactional
    public LanceAdminDTO removerMidia(Long lanceId, Long midiaId) {

        Lance lance = obterLance(lanceId);

        LanceMidia midia = lance.getMidias().stream()
                .filter(m -> m.getId().equals(midiaId))
                .findFirst()
                .orElseThrow(() ->
                        new RuntimeException(
                                "Mídia não encontrada neste lance."
                        )
                );

        apagarArquivoSeUpload(midia);

        lance.removerMidia(midia);

        lanceRepository.save(lance);

        return LanceAdminDTO.fromEntity(lance);
    }

    // =========================
    // ADMIN — EXPORTAÇÃO (RELATÓRIO)
    // =========================

    @Transactional(readOnly = true)
    public byte[] exportarExcel(
            CategoriaLance categoria,
            boolean apenasGolaco
    ) {

        List<Lance> lances =
                lanceRepository.buscarParaAdmin(categoria, apenasGolaco);

        return exportService.gerarExcel(lances);
    }

    @Transactional(readOnly = true)
    public byte[] exportarPdf(
            CategoriaLance categoria,
            boolean apenasGolaco
    ) {

        List<Lance> lances =
                lanceRepository.buscarParaAdmin(categoria, apenasGolaco);

        return exportService.gerarPdf(lances, categoria, apenasGolaco);
    }

    // =========================
    // HELPERS
    // =========================

    private Lance obterLance(Long id) {
        return lanceRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Lance não encontrado."
                        )
                );
    }

    private void apagarArquivoSeUpload(LanceMidia midia) {

        if (midia.getOrigem() != OrigemMidia.UPLOAD) {
            return;
        }

        // Falha ao apagar no Cloudinary não pode travar a
        // operação no banco — apenas registramos o aviso.
        try {
            storageService.deletarMidiaLance(midia.getUrl());
        } catch (Exception e) {
            System.err.println(
                    "Aviso: falha ao apagar mídia "
                            + midia.getId()
                            + " no Cloudinary: "
                            + e.getMessage()
            );
        }
    }

    /**
     * Gera um slug amigável a partir do título e garante
     * unicidade acrescentando um sufixo numérico quando
     * necessário (ex.: "gol-a-gol", "gol-a-gol-2").
     */
    private String gerarSlugUnico(String titulo) {

        String base = Normalizer
                .normalize(titulo, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")     // remove acentos
                .toLowerCase()
                .replaceAll("[^a-z0-9\\s-]", "")
                .trim()
                .replaceAll("\\s+", "-")
                .replaceAll("-+", "-");

        if (base.isBlank()) {
            base = "lance";
        }

        String slug = base;
        int sufixo = 2;

        while (lanceRepository.existsBySlug(slug)) {
            slug = base + "-" + sufixo++;
        }

        return slug;
    }
}
