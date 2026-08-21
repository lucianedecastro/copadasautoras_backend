package br.com.copadasautoras.entity;

public enum OrigemMidia {

    /**
     * Arquivo hospedado por nós no Cloudinary
     * (fotos e vídeos próprios da Copa).
     */
    UPLOAD,

    /**
     * URL externa / embed (rádio, TV, YouTube, Spotify).
     * O conteúdo é do veículo — nós apenas referenciamos.
     */
    EMBED
}
