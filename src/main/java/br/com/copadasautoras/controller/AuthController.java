package br.com.copadasautoras.controller;

import br.com.copadasautoras.dto.AutoraCreateRequestDTO;
import br.com.copadasautoras.dto.LoginRequest;
import br.com.copadasautoras.entity.Autora;
import br.com.copadasautoras.entity.Role;
import br.com.copadasautoras.entity.StatusAutora;
import br.com.copadasautoras.entity.Usuario;
import br.com.copadasautoras.repository.AutoraRepository;
import br.com.copadasautoras.repository.UsuarioRepository;
import br.com.copadasautoras.security.JwtService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final UsuarioRepository usuarioRepository;
    private final AutoraRepository autoraRepository;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;

    @Operation(
            summary = "Cadastro público de autora",
            description = """
                    Realiza o cadastro de uma autora.
                    O perfil nasce como PENDENTE até aprovação admin.
                    """
    )
    @Transactional
    @PostMapping("/register")
    public ResponseEntity<String> register(
            @Valid @RequestBody AutoraCreateRequestDTO request
    ) {

        boolean usuarioExiste = usuarioRepository
                .findByEmail(request.email())
                .isPresent();

        if (usuarioExiste) {
            return ResponseEntity.badRequest()
                    .body("Já existe um usuário com este email.");
        }

        Usuario usuario = Usuario.builder()
                .nome(request.nome())
                .email(request.email())
                .senha(passwordEncoder.encode(request.senha()))
                .role(Role.AUTORA)
                .build();

        usuarioRepository.save(usuario);

        Autora autora = Autora.builder()
                .nome(request.nome())
                .nomeExibicao(request.nomeExibicao())
                .statusAutora(StatusAutora.PENDENTE)
                .usuario(usuario)
                .build();

        autoraRepository.save(autora);

        return ResponseEntity.ok(
                "Cadastro realizado com sucesso. Seu perfil está aguardando aprovação."
        );
    }

    @Operation(
            summary = "Login do usuário",
            description = "Autentica usuário por email e retorna token JWT."
    )
    @PostMapping("/login")
    public ResponseEntity<String> login(
            @RequestBody LoginRequest request
    ) {

        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.email(),
                        request.password()
                )
        );

        Usuario usuario = usuarioRepository.findByEmail(request.email())
                .orElseThrow(() ->
                        new RuntimeException("Usuário não encontrado")
                );

        String token = jwtService.gerarToken(usuario);

        return ResponseEntity.ok(token);
    }
}

