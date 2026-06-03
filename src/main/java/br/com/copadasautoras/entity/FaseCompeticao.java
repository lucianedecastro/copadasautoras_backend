package br.com.copadasautoras.entity;

public enum FaseCompeticao {

    FASE_32,
    OITAVAS,
    QUARTAS,
    SEMIFINAL,
    FINAL,
    CAMPEA;

    public FaseCompeticao proxima() {

        return switch (this) {
            case FASE_32 -> OITAVAS;
            case OITAVAS -> QUARTAS;
            case QUARTAS -> SEMIFINAL;
            case SEMIFINAL -> FINAL;
            case FINAL -> CAMPEA;
            case CAMPEA -> CAMPEA;
        };
    }

    public boolean isUltima() {
        return this == CAMPEA;
    }
}

