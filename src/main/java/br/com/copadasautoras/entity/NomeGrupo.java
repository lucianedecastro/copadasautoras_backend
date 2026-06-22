package br.com.copadasautoras.entity;

public enum NomeGrupo {

    // Grupos da FASE_32 (8 grupos de 4 obras cada)
    A,
    B,
    C,
    D,
    E,
    F,
    G,
    H,

    // Confrontos 1x1 a partir das OITAVAS (sorteados pelo
    // ConfrontoService/SubmissaoService.gerarConfrontos) — nomenclatura
    // separada de A–H pra não se confundir com os grupos da FASE_32.
    CONFRONTO_1,
    CONFRONTO_2,
    CONFRONTO_3,
    CONFRONTO_4,
    CONFRONTO_5,
    CONFRONTO_6,
    CONFRONTO_7,
    CONFRONTO_8
}