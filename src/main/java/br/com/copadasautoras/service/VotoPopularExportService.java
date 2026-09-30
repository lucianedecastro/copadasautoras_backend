package br.com.copadasautoras.service;

import br.com.copadasautoras.dto.VotoPopularResultadoDTO;

import org.springframework.stereotype.Service;

/**
 * Relatórios do placar do Escolha do Público (Excel e PDF) — mesma
 * arquitetura do AutoraExportService: fachada fina, builders próprios.
 */
@Service
public class VotoPopularExportService {

    public byte[] gerarExcel(VotoPopularResultadoDTO resultado) {
        return VotoPopularExcelBuilder.gerar(resultado);
    }

    public byte[] gerarPdf(VotoPopularResultadoDTO resultado) {
        return VotoPopularPdfBuilder.gerar(resultado);
    }
}
