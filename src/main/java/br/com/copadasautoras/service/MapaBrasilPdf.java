package br.com.copadasautoras.service;

import br.com.copadasautoras.util.MapaBrasilVetor;
import br.com.copadasautoras.util.MapaBrasilVetor.Estado;

import com.itextpdf.io.font.constants.StandardFonts;
import com.itextpdf.kernel.colors.DeviceRgb;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.geom.Rectangle;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.canvas.PdfCanvas;
import com.itextpdf.kernel.pdf.xobject.PdfFormXObject;

import java.io.IOException;
import java.util.Map;

/**
 * Desenha o mapa coroplético do Brasil como vetor dentro do PDF.
 *
 * Só desenho: as regras de faixa e cor vêm de {@link MapaBrasilVetor}, as
 * mesmas do mapa da tela. Devolve um XObject (mapa sem legenda) que o
 * chamador encaixa no layout com Image.
 */
final class MapaBrasilPdf {

    private static final double ROTULO_TAMANHO = 13;
    private static final double HACHURA_PASSO = 5;

    private MapaBrasilPdf() {
    }

    static PdfFormXObject desenhar(
            PdfDocument pdf,
            Map<String, Long> autorasPorUf,
            long[] faixas
    ) throws IOException {

        double w = MapaBrasilVetor.LARGURA;
        double h = MapaBrasilVetor.ALTURA;

        PdfFormXObject xobj = new PdfFormXObject(
                new Rectangle(0, 0, (float) w, (float) h));

        PdfCanvas c = new PdfCanvas(xobj, pdf);

        PdfFont fonte = PdfFontFactory.createFont(StandardFonts.HELVETICA_BOLD);

        // 1) preenchimento (e hachura nos estados sem autoras)
        for (Estado e : MapaBrasilVetor.estados()) {

            long valor = autorasPorUf.getOrDefault(e.sigla(), 0L);
            int cor = MapaBrasilVetor.corDoEstado(valor, faixas);

            c.saveState();
            c.setFillColor(rgb(cor));
            tracar(c, e, h);
            c.fill();
            c.restoreState();

            if (valor <= 0) {
                hachurar(c, e, h);
            }
        }

        // 2) divisas em branco, por cima de tudo
        c.saveState();
        c.setStrokeColor(new DeviceRgb(255, 255, 255));
        c.setLineWidth(0.8f);
        for (Estado e : MapaBrasilVetor.estados()) {
            tracar(c, e, h);
            c.stroke();
        }
        c.restoreState();

        // 3) siglas, só onde cabem
        for (Estado e : MapaBrasilVetor.estados()) {

            if (!e.rotuloCabe()) {
                continue;
            }

            long valor = autorasPorUf.getOrDefault(e.sigla(), 0L);

            int corRotulo = valor <= 0
                    ? MapaBrasilVetor.COR_ROTULO_ZERO
                    : MapaBrasilVetor.corDoRotulo(
                            MapaBrasilVetor.corDoEstado(valor, faixas));

            float tw = fonte.getWidth(e.sigla(), (float) ROTULO_TAMANHO);
            double x = e.cx() - tw / 2.0;
            double y = (h - e.cy()) - ROTULO_TAMANHO * 0.35;

            c.beginText()
                    .setFontAndSize(fonte, (float) ROTULO_TAMANHO)
                    .setFillColor(rgb(corRotulo))
                    .moveText(x, y)
                    .showText(e.sigla())
                    .endText();
        }

        c.release();

        return xobj;
    }

    /** Monta o caminho do estado (SVG tem y para baixo; PDF, para cima). */
    private static void tracar(PdfCanvas c, Estado e, double h) {

        for (double[][] anel : e.aneis()) {

            for (int i = 0; i < anel.length; i++) {
                float x = (float) anel[i][0];
                float y = (float) (h - anel[i][1]);
                if (i == 0) {
                    c.moveTo(x, y);
                } else {
                    c.lineTo(x, y);
                }
            }

            c.closePath();
        }
    }

    /** Traços diagonais dentro do estado (recorte pelo contorno). */
    private static void hachurar(PdfCanvas c, Estado e, double h) {

        double x0 = e.cx() - e.largura() / 2.0;
        double y0 = e.cy() - e.altura() / 2.0;

        c.saveState();

        tracar(c, e, h);
        c.clip();
        c.endPath();

        c.setStrokeColor(rgb(MapaBrasilVetor.COR_HACHURA));
        c.setLineWidth(0.9f);

        for (double d = -e.altura(); d < e.largura(); d += HACHURA_PASSO) {
            c.moveTo((float) (x0 + d), (float) (h - y0 - e.altura()));
            c.lineTo((float) (x0 + d + e.altura()), (float) (h - y0));
            c.stroke();
        }

        c.restoreState();
    }

    private static DeviceRgb rgb(int rgb) {
        return new DeviceRgb(
                (rgb >> 16) & 0xFF, (rgb >> 8) & 0xFF, rgb & 0xFF);
    }
}
