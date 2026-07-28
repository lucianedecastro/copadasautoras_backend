package br.com.copadasautoras.service;

public record SubmissaoRegistradaEvent(
        String destinatario,
        String nomeAutora,
        byte[] termoPdf
) {
}