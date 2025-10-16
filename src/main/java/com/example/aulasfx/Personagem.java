package com.example.aulasfx;

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Personagem {

    private final ImageView imageView;
    private final Map<String, List<Image>> animacoes = new HashMap<>();

    private List<Image> framesAtuais;
    private String nomeAnimacaoAtual;
    private int currentFrameIndex = 0;
    private long lastFrameTime = 0;
    private final String imagePrefix;

    // Constantes de Animação
    private static final long FRAME_DURATION_DEFAULT = 100_000_000L; // 0.1s para golpes
    private static final long FRAME_DURATION_IDLE = 200_000_000L;    // 0.2s para Idle

    // --- Lógica de Vida ---
    private int vidaAtual;
    private final int vidaMaxima = 100;

    // CONSTRUTOR
    public Personagem(ImageView imageView, boolean isCPU) {
        this.imageView = imageView;
        this.vidaAtual = vidaMaxima;

        // Define o prefixo do caminho: /Imagens/Player1/ ou /Imagens/CPU/
        this.imagePrefix = isCPU ? "/Imagens/CPU/" : "/Imagens/Player1/";

        carregarAnimacoes();
        setAnimacao("Idle");
    }

    // *** MÉTODO DE INÍCIO DE ANIMAÇÃO CORRIGIDO ***
    // Recebe o tempo atual para inicializar corretamente a contagem.
    public void setAnimacao(String nomeAnimacao, long now) {
        if (animacoes.containsKey(nomeAnimacao)) {
            this.framesAtuais = animacoes.get(nomeAnimacao);
            this.nomeAnimacaoAtual = nomeAnimacao;
            this.currentFrameIndex = 0;

            // Define o tempo do último frame como o tempo atual (now).
            this.lastFrameTime = now;

            if (framesAtuais != null && !framesAtuais.isEmpty()) {
                this.imageView.setImage(framesAtuais.get(0));
            } else {
                System.err.println("Animação '" + nomeAnimacao + "' está vazia!");
            }
        } else {
            System.err.println("Animação '" + nomeAnimacao + "' não encontrada no mapa.");
        }
    }

    // Sobrecarga para chamadas que não precisam do tempo (como no initialize)
    public void setAnimacao(String nomeAnimacao) {
        setAnimacao(nomeAnimacao, 0);
    }
    // **********************************************


    private void carregarAnimacoes() {
        String path = imagePrefix;

        try {
            // --- 1. IDLE (Parado) ---
            List<Image> idleFrames = new ArrayList<>();
            // Certifique-se de que os nomes dos arquivos abaixo existem nas pastas Player1/ e CPU/
            idleFrames.add(new Image(getClass().getResourceAsStream(path + "idle_frame1.png")));
            idleFrames.add(new Image(getClass().getResourceAsStream(path + "idle_frame2.png")));
            animacoes.put("Idle", idleFrames);

            // --- 2. GOLPE 1 (Soco) ---
            List<Image> golpe1Frames = new ArrayList<>();
            golpe1Frames.add(new Image(getClass().getResourceAsStream(path + "soco_frame1.png")));
            golpe1Frames.add(new Image(getClass().getResourceAsStream(path + "soco_frame2.png")));
            animacoes.put("Soco", golpe1Frames);

            // --- 3. GOLPE 2 (Chute) ---
            List<Image> golpe2Frames = new ArrayList<>();
            golpe2Frames.add(new Image(getClass().getResourceAsStream(path + "chute_frame1.png")));
            golpe2Frames.add(new Image(getClass().getResourceAsStream(path + "chute_frame2.png")));
            animacoes.put("Chute", golpe2Frames);

            // --- 4. GOLPE 3 (Especial) ---
            List<Image> golpe3Frames = new ArrayList<>();
            golpe3Frames.add(new Image(getClass().getResourceAsStream(path + "especial_frame1.png")));
            golpe3Frames.add(new Image(getClass().getResourceAsStream(path + "especial_frame2.png")));
            animacoes.put("Especial", golpe3Frames);

        } catch (Exception e) {
            System.err.println("ERRO FATAL: Falha ao carregar frames para " + (imagePrefix.contains("CPU") ? "CPU" : "Player 1") + ". Verifique os caminhos: " + path);
            e.printStackTrace();
            throw new RuntimeException("Falha no carregamento de animações: " + e.getMessage());
        }
    }

    /**
     * Atualiza o frame. Congela no último frame do golpe.
     * @return true se o golpe atingiu o último frame e precisa de cooldown.
     */
    public boolean update(long now) {
        if (framesAtuais == null || framesAtuais.isEmpty()) return false;

        // Calcula a duração do frame
        long frameDuration = (nomeAnimacaoAtual.equals("Idle")) ? FRAME_DURATION_IDLE : FRAME_DURATION_DEFAULT;

        boolean golpeTerminado = false;


        // Tenta avançar o frame se o tempo decorrido for suficiente
        if (now - lastFrameTime >= frameDuration) {
            currentFrameIndex++;
            lastFrameTime = now; // Atualiza o tempo do último avanço

            if (currentFrameIndex >= framesAtuais.size()) {

                if (!nomeAnimacaoAtual.equals("Idle")) {
                    // Golpe chegou ao fim
                    currentFrameIndex = framesAtuais.size() - 1; // Trava no último frame
                    imageView.setImage(framesAtuais.get(currentFrameIndex));
                    golpeTerminado = true; // Sinaliza o término
                } else {
                    // Idle faz loop
                    currentFrameIndex = 0;
                }
            }

            // Renderiza o novo frame (somente se não tivermos terminado o golpe)
            if (!golpeTerminado && currentFrameIndex < framesAtuais.size()) {
                imageView.setImage(framesAtuais.get(currentFrameIndex));
            }
        }

        return golpeTerminado;
    }

    // --- Métodos de Vida e Ataque ---
    public int getVidaAtual() { return vidaAtual; }
    public int getVidaMaxima() { return vidaMaxima; }
    public void receberDano(int dano) { this.vidaAtual = Math.max(0, this.vidaAtual - dano); }
    public boolean estaVivo() { return vidaAtual > 0; }

    // Os métodos de ataque agora chamam setAnimacao(String) que passa 0.
    public Ataque darSoco() { setAnimacao("Soco"); return Ataque.SOCO; }
    public Ataque darChute() { setAnimacao("Chute"); return Ataque.CHUTE; }
    public Ataque usarEspecial() { setAnimacao("Especial"); return Ataque.ESPECIAL; }
}