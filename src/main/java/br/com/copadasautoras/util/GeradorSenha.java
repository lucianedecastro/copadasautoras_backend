package br.com.copadasautoras.util;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class GeradorSenha {

    public static void main(String[] args) {

        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

        String senhaPura = "Lu719698022425@#$";

        String senhaCriptografada = encoder.encode(senhaPura);

        System.out.println("Senha original: " + senhaPura);
        System.out.println("Senha criptografada (BCrypt): " + senhaCriptografada);
    }
}