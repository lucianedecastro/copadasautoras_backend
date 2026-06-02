package br.com.copadasautoras.storage;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CloudinaryStorageService {

    private final Cloudinary cloudinary;

    // =========================
    // UPLOAD DE ARQUIVO DA OBRA
    // =========================
    public String uploadObra(MultipartFile arquivo, Long autoraId) {
        try {
            String publicId = "obras/" + autoraId + "/" + UUID.randomUUID();

            Map<?, ?> resultado = cloudinary.uploader().upload(
                    arquivo.getBytes(),
                    ObjectUtils.asMap(
                            "public_id",      publicId,
                            "resource_type",  "raw",       // PDF, DOCX, etc.
                            "access_mode",    "authenticated", // privado por padrão
                            "use_filename",   false,
                            "overwrite",      false
                    )
            );

            return (String) resultado.get("secure_url");

        } catch (IOException e) {
            throw new RuntimeException("Erro ao fazer upload da obra: " + e.getMessage(), e);
        }
    }

    // =========================
    // UPLOAD DE ARQUIVO PÚBLICO (trecho)
    // =========================
    public String uploadObraPublica(MultipartFile arquivo, Long autoraId) {
        try {
            String publicId = "obras-publicas/" + autoraId + "/" + UUID.randomUUID();

            Map<?, ?> resultado = cloudinary.uploader().upload(
                    arquivo.getBytes(),
                    ObjectUtils.asMap(
                            "public_id",      publicId,
                            "resource_type",  "raw",
                            "access_mode",    "public",    // acessível publicamente
                            "use_filename",   false,
                            "overwrite",      false
                    )
            );

            return (String) resultado.get("secure_url");

        } catch (IOException e) {
            throw new RuntimeException("Erro ao fazer upload do arquivo público: " + e.getMessage(), e);
        }
    }

    // =========================
    // UPLOAD DO TERMO PDF
    // =========================
    public String uploadTermoPdf(byte[] pdfBytes, Long submissaoId) {
        try {
            String publicId = "termos/" + submissaoId + "/termo-aceite-" + UUID.randomUUID();

            Map<?, ?> resultado = cloudinary.uploader().upload(
                    pdfBytes,
                    ObjectUtils.asMap(
                            "public_id",      publicId,
                            "resource_type",  "raw",
                            "access_mode",    "authenticated",
                            "format",         "pdf",
                            "use_filename",   false,
                            "overwrite",      false
                    )
            );

            return (String) resultado.get("secure_url");

        } catch (IOException e) {
            throw new RuntimeException("Erro ao fazer upload do termo PDF: " + e.getMessage(), e);
        }
    }

    // =========================
    // DELETAR ARQUIVO
    // =========================
    public void deletar(String url) {
        try {
            // Extrai o public_id da URL do Cloudinary
            String publicId = extrairPublicId(url);
            cloudinary.uploader().destroy(publicId,
                    ObjectUtils.asMap("resource_type", "raw"));
        } catch (IOException e) {
            throw new RuntimeException("Erro ao deletar arquivo: " + e.getMessage(), e);
        }
    }

    private String extrairPublicId(String url) {
        // URL formato: https://res.cloudinary.com/{cloud}/raw/upload/v123/{public_id}
        int uploadIdx = url.indexOf("/upload/");
        if (uploadIdx == -1) {
            throw new RuntimeException("URL do Cloudinary inválida: " + url);
        }
        String semUpload = url.substring(uploadIdx + 8); // remove "/upload/"
        // Remove versão (v1234567/) se presente
        if (semUpload.startsWith("v") && semUpload.contains("/")) {
            semUpload = semUpload.substring(semUpload.indexOf("/") + 1);
        }
        return semUpload;
    }
}
