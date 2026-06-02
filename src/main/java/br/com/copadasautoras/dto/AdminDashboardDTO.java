package br.com.copadasautoras.dto;

import br.com.copadasautoras.entity.FaseCompeticao;
import br.com.copadasautoras.entity.StatusFase;

public record AdminDashboardDTO(

        FaseCompeticao faseAtual,
        StatusFase statusFase,

        int totalConfrontos,
        int confrontosResolvidos,
        int confrontosPendentes

) {}
