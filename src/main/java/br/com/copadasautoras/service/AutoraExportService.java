package br.com.copadasautoras.service;

import br.com.copadasautoras.entity.Autora;
import br.com.copadasautoras.entity.StatusAutora;

import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Relatórios de autoras (Excel e PDF) — mesma arquitetura do
 * LanceExportService e do MetricasExportService.
 *
 * Fachada fina: o Excel (Apache POI) e o PDF (iText7, com o mapa do Brasil
 * em vetor) ficam em builders próprios.
 */
@Service
public class AutoraExportService {

    public byte[] gerarExcel(List<Autora> autoras, StatusAutora filtro) {
        return AutoraExcelBuilder.gerar(autoras, filtro);
    }

    public byte[] gerarPdf(
            List<Autora> autoras,
            StatusAutora filtro,
            boolean incluirLista
    ) {
        return AutoraPdfBuilder.gerar(autoras, filtro, incluirLista);
    }
}
