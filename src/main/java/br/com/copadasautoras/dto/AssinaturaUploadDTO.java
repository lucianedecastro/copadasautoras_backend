package br.com.copadasautoras.dto;

/**
 * Dados que o navegador usa para enviar um arquivo DIRETO ao Cloudinary.
 *
 * cloudName e apiKey são públicos; a assinatura autoriza este upload
 * específico (amarrado ao timestamp e ao publicId). O api_secret não
 * está aqui — ele nunca sai do backend.
 */
public record AssinaturaUploadDTO(

        String cloudName,
        String apiKey,
        long timestamp,
        String publicId,
        String signature

) {}
