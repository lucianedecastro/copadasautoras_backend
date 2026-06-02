package br.com.copadasautoras.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "confronto")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Confronto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    private FaseCompeticao fase;

    @ManyToOne
    @JoinColumn(name = "submissao_casa_id")
    private Submissao casa;

    @ManyToOne
    @JoinColumn(name = "submissao_fora_id")
    private Submissao fora;

    @ManyToOne
    @JoinColumn(name = "submissao_vencedora_id")
    private Submissao vencedora;

    private Boolean resolvido;

    @PrePersist
    public void prePersist() {
        if (this.resolvido == null) {
            this.resolvido = false;
        }
    }
}