package br.com.copadasautoras.entity;

import jakarta.persistence.*;
import lombok.*;

/**
 * Mídia vinculada a um lance.
 *
 * A origem determina o tratamento:
 *  - UPLOAD → arquivo nosso, hospedado no Cloudinary;
 *  - EMBED  → URL do veículo (rádio, TV, YouTube), que
 *             apenas referenciamos, sem rehospedar.
 */
@Entity
@Table(name = "lance_midia")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LanceMidia {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lance_id")
    private Lance lance;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TipoMidia tipo;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private OrigemMidia origem;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String url;

    @Column(length = 500)
    private String legenda;

    private Integer ordem;
}
