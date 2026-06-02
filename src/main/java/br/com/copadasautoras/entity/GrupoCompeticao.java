package br.com.copadasautoras.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "grupo_competicao")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GrupoCompeticao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private NomeGrupo nomeGrupo;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private FaseCompeticao fase;

    /**
     * Jurada responsável pelo grupo.
     * Deve possuir role BANCA.
     */
    @ManyToOne
    @JoinColumn(name = "banca_id")
    private Usuario banca;
}

