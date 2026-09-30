package br.com.copadasautoras.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "competicao")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Competicao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    private FaseCompeticao faseAtual;

    @Enumerated(EnumType.STRING)
    private StatusFase statusFase;

    /**
     * Controla se a página pública de chaveamento/finalistas está
     * visível. Ativado manualmente pelo admin (botão "Publicar
     * Chaveamento"), independente da fase técnica da competição.
     */
    @Column(name = "chaveamento_publicado", nullable = false)
    private Boolean chaveamentoPublicado;

    /**
     * Votação popular ("Escolha do Público") encerrada pelo admin.
     * Enquanto false, a votação aceita votos (desde que haja obras
     * elegíveis). Ao encerrar, não entram votos novos e os pendentes
     * deixam de poder ser confirmados. O admin pode reabrir.
     */
    @Column(name = "votacao_popular_encerrada", nullable = false)
    @Builder.Default
    private boolean votacaoPopularEncerrada = false;

    /**
     * Quando a votação popular foi encerrada (nulo se aberta).
     */
    @Column(name = "votacao_popular_encerrada_em")
    private LocalDateTime votacaoPopularEncerradaEm;

    @PrePersist
    public void prePersist() {
        if (faseAtual == null) {
            faseAtual = FaseCompeticao.FASE_32;
        }
        if (statusFase == null) {
            statusFase = StatusFase.NAO_INICIADA;
        }
        if (chaveamentoPublicado == null) {
            chaveamentoPublicado = false;
        }
    }
}