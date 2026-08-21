package br.com.copadasautoras.storage;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
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
                            "public_id",     publicId,
                            "resource_type", "raw",
                            "access_mode",   "authenticated",
                            "use_filename",  false,
                            "overwrite",     false
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
                            "public_id",     publicId,
                            "resource_type", "raw",
                            "access_mode",   "public",
                            "use_filename",  false,
                            "overwrite",     false
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
                            "public_id",     publicId,
                            "resource_type", "raw",
                            "access_mode",   "authenticated",
                            "format",        "pdf",
                            "use_filename",  false,
                            "overwrite",     false
                    )
            );
            return (String) resultado.get("secure_url");
        } catch (IOException e) {
            throw new RuntimeException("Erro ao fazer upload do termo PDF: " + e.getMessage(), e);
        }
    }

    // =========================
    // UPLOAD DE MÍDIA DO LANCE (newsroom)
    // =========================
    /**
     * Foto/vídeo próprio da Copa, exibido publicamente na timeline.
     *
     * Diferente das obras (raw + authenticated), a mídia do newsroom é
     * pública e pode ser imagem ou vídeo — por isso resource_type "auto"
     * (o Cloudinary detecta) e access_mode "public".
     */
    public String uploadMidiaLance(MultipartFile arquivo) {
        try {
            String publicId = "lances/" + UUID.randomUUID();
            Map<?, ?> resultado = cloudinary.uploader().upload(
                    arquivo.getBytes(),
                    ObjectUtils.asMap(
                            "public_id",     publicId,
                            "resource_type", "auto",
                            "access_mode",   "public",
                            "use_filename",  false,
                            "overwrite",     false
                    )
            );
            return (String) resultado.get("secure_url");
        } catch (IOException e) {
            throw new RuntimeException("Erro ao fazer upload da mídia do lance: " + e.getMessage(), e);
        }
    }

    // =========================
    // DELETAR MÍDIA DO LANCE
    // =========================
    /**
     * Apaga uma mídia do newsroom no Cloudinary.
     *
     * O resource_type é inferido pela URL (image/video/raw), porque a mídia
     * do lance pode ser foto ou vídeo — ao contrário das obras, sempre "raw".
     * Para imagem/vídeo, o public_id vai sem extensão (o destroy exige assim).
     */
    public void deletarMidiaLance(String url) {
        try {
            String resourceType = inferirResourceType(url);
            String publicId = extrairPublicId(url);

            if (!"raw".equals(resourceType)) {
                publicId = publicId.replaceAll("\\.[^./]+$", "");
            }

            cloudinary.uploader().destroy(publicId,
                    ObjectUtils.asMap("resource_type", resourceType));
        } catch (IOException e) {
            throw new RuntimeException("Erro ao deletar mídia do lance: " + e.getMessage(), e);
        }
    }

    private String inferirResourceType(String url) {
        if (url.contains("/image/")) return "image";
        if (url.contains("/video/")) return "video";
        return "raw";
    }

    // =========================
    // DOWNLOAD — stream pelo backend
    // =========================
    /**
     * Baixa o conteúdo do arquivo do Cloudinary e retorna os bytes.
     *
     * Para arquivos públicos  (/upload/)         → acessa a URL diretamente.
     * Para arquivos privados  (/authenticated/)  → gera uma signed URL temporária
     *                                               antes de baixar.
     *
     * Isso resolve o problema do frontend: o 302 redirect para URL autenticada
     * do Cloudinary é bloqueado pelo browser (CORS + auth). Streamar pelo backend
     * elimina esse problema — o frontend só faz fetch no nosso domínio.
     */
    public byte[] baixarArquivo(String cloudinaryUrl) {
        try {
            String urlParaDownload;

            if (cloudinaryUrl.contains("/authenticated/")) {
                // Arquivo privado — precisa de signed URL.
                String publicId = extrairPublicId(cloudinaryUrl);

                // Strip da extensão se presente (ex: "termo.pdf" → "termo").
                // Quando o publicId tem extensão, o SDK ignora .type("authenticated")
                // e gera URL com tipo "upload" — causando 401 no Cloudinary.
                // Sem extensão, o comportamento é idêntico ao arquivo-completo (que funciona).
                // O Cloudinary encontra o recurso pelo publicId limpo; o formato pdf
                // está registrado internamente no upload original.
                String publicIdBase = publicId.replaceAll("\\.[^./]+$", "");

                urlParaDownload = cloudinary.url()
                        .resourceType("raw")
                        .type("authenticated")
                        .signed(true)
                        .generate(publicIdBase);
            } else {
                // Arquivo público — URL direta funciona
                urlParaDownload = cloudinaryUrl;
            }

            HttpURLConnection conn = (HttpURLConnection) new URL(urlParaDownload).openConnection();
            conn.setConnectTimeout(15_000);
            conn.setReadTimeout(60_000);
            conn.setRequestProperty("User-Agent", "CopaLiteratura-Backend/1.0");

            int status = conn.getResponseCode();
            if (status != 200) {
                throw new RuntimeException(
                        "Cloudinary retornou HTTP " + status + " para: " + urlParaDownload);
            }

            try (InputStream is = conn.getInputStream()) {
                return is.readAllBytes();
            } finally {
                conn.disconnect();
            }

        } catch (IOException e) {
            throw new RuntimeException("Erro ao baixar arquivo do Cloudinary: " + e.getMessage(), e);
        }
    }

    // =========================
    // DELETAR ARQUIVO
    // =========================
    public void deletar(String url) {
        try {
            String publicId = extrairPublicId(url);
            cloudinary.uploader().destroy(publicId,
                    ObjectUtils.asMap("resource_type", "raw"));
        } catch (IOException e) {
            throw new RuntimeException("Erro ao deletar arquivo: " + e.getMessage(), e);
        }
    }

    private String extrairPublicId(String url) {
        // URL formato: https://res.cloudinary.com/{cloud}/raw/{type}/v123/{public_id}
        // type pode ser "upload" ou "authenticated"
        String marker = url.contains("/authenticated/") ? "/authenticated/" : "/upload/";
        int idx = url.indexOf(marker);
        if (idx == -1) throw new RuntimeException("URL do Cloudinary inválida: " + url);
        String semPrefixo = url.substring(idx + marker.length());
        // Remove versão (v1234567/) se presente
        if (semPrefixo.matches("v\\d+/.*")) {
            semPrefixo = semPrefixo.substring(semPrefixo.indexOf('/') + 1);
        }
        return semPrefixo;
    }
}
