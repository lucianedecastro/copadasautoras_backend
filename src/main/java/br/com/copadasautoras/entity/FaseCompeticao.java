package br.com.copadasautoras.entity;

public enum FaseCompeticao {

    FASE_32,
    OITAVAS,
    QUARTAS,
    SEMIFINAL,
    FINAL;

    public FaseCompeticao proxima() {

        return switch (this) {
            case FASE_32 -> OITAVAS;
            case OITAVAS -> QUARTAS;
            case QUARTAS -> SEMIFINAL;
            case SEMIFINAL -> FINAL;
            case FINAL -> FINAL;
        };
    }

    public boolean isUltima() {
        return this == FINAL;
    }
}

