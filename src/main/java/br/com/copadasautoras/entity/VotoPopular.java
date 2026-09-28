package br.com.copadasautoras.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * Voto no "Escolha do Público". Um registro nasce
 * como pendente (confirmado = false) ao ser solicitado
 * e só passa a valer após confirmação por e-mail.
 *
 * A unicidade de voto por pessoa é garantida por índice
 * único parcial no banco (email, apenas onde confirmado
 * = true) — ver migration V16. Isso permite reenviar o
 * e-mail de confirmação sem violar constraint.
 */
@Entity
@Table(name = "voto_popular")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VotoPopular {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "submissao_id", nullable = false)
    private Submissao submissao;

    @Column(nullable = false)
    private String email;

    @Column(nullable = false, unique = true, length = 36)
    private String token;

    @Column(nullable = false)
    @Builder.Default
    private boolean confirmado = false;

    @CreationTimestamp
    @Column(
            name = "data_voto",
            nullable = false,
            updatable = false
    )
    private LocalDateTime dataVoto;

    @Column(name = "data_confirmacao")
    private LocalDateTime dataConfirmacao;
}
