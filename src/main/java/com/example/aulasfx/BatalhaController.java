package com.example.aulasfx;

import javafx.animation.AnimationTimer;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ProgressBar;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.event.ActionEvent;
import javafx.application.Platform;

import java.net.URL;
import java.util.ResourceBundle;
import java.util.List;
import java.util.Arrays;
import java.util.Random;

public class BatalhaController implements Initializable {

    // Background
    @FXML private ImageView backgroundImageView;

    // --- FXMLs do Player 1 (Controlado) ---
    @FXML private ImageView player1Img;
    @FXML private ProgressBar player1HealthBar;
    @FXML private Button btnSoco;
    @FXML private Button btnChute;
    @FXML private Button btnEspecial;

    // --- FXMLs do Player 2 (CPU) ---
    @FXML private ImageView player2Img;
    @FXML private ProgressBar player2HealthBar;

    // --- Estado Global de Turno e Animação ---
    private Personagem player1;
    private Personagem player2;
    private List<Button> botoesGolpe;
    private final Random random = new Random(); // Variável final para o Random

    // Variáveis de Controle de Estado
    private boolean aguardandoEscolha = true;
    private boolean golpeEmExecucao = false;
    private boolean emCooldown = false;
    private boolean danoAplicadoNesteTurno = false;
    private boolean jogoEncerrado = false;

    // Variáveis de Turno
    private Ataque ataqueP1Escolhido = null;
    private Ataque ataqueP2Escolhido = null;

    private boolean p2AnimacaoIniciada = false; // Flag de estado do P2

    // Cooldown Global (1 segundo)
    private static final long COOLDOWN_NANO = 95_000_000L;
    private long tempoInicioCongelamento = 0;

    private AnimationTimer gameLoop;

    // --- Métodos de Manipulação dos Botões (Player 1) ---

    @FXML
    protected void handleSoco(ActionEvent event) {
        iniciarEscolhaTurno(player1.darSoco());
    }

    @FXML
    protected void handleChute(ActionEvent event) {
        iniciarEscolhaTurno(player1.darChute());
    }

    @FXML
    protected void handleEspecial(ActionEvent event) {
        iniciarEscolhaTurno(player1.usarEspecial());
    }

    private void iniciarEscolhaTurno(Ataque ataqueP1) {
        if (!aguardandoEscolha || jogoEncerrado) return;

        aguardandoEscolha = false;
        setBotoesHabilitados(false);

        // 1. Armazena as escolhas
        this.ataqueP1Escolhido = ataqueP1;
        this.ataqueP2Escolhido = escolherGolpeCPU();

        // 2. Inicia a primeira animação (Player 1 ataca primeiro)
        // Aqui não precisa de 'now' porque o loop está parado, mas vamos usar para consistência
        player1.setAnimacao(ataqueP1.getNome(), System.nanoTime());
        golpeEmExecucao = true;
    }

    private Ataque escolherGolpeCPU() {
        Ataque[] ataques = Ataque.values();
        return ataques[random.nextInt(ataques.length)];
    }

    private void aplicarDanoEFinalizarTurno() {
        if (danoAplicadoNesteTurno || jogoEncerrado) return;

        danoAplicadoNesteTurno = true;

        // 1. Aplica o dano do P1 no P2
        player2.receberDano(ataqueP1Escolhido.getDanoBase());

        // 2. Aplica o dano do P2 no P1
        player1.receberDano(ataqueP2Escolhido.getDanoBase());

        Platform.runLater(() -> {
            atualizarBarrasDeVida();

            // <--- LINHAS DE ERRO REMOVIDAS AQUI --->
            // ataqueP2Escolhido.getDanoBase(), player1.getVidaAtual();))
            // ataqueP1Escolhido.getDanoBase(), player2.getVidaAtual();));

            if (!player1.estaVivo() || !player2.estaVivo()) {
                encerrarBatalha();
            } else {
                iniciarNovoTurno();
            }
        });
    }

    private void iniciarNovoTurno() {
        aguardandoEscolha = true;
        danoAplicadoNesteTurno = false;
        ataqueP1Escolhido = null;
        ataqueP2Escolhido = null;
        setBotoesHabilitados(true);
        // Usa o método padrão, pois o tempo de Idle não importa tanto no início
        player1.setAnimacao("Idle");
        player2.setAnimacao("Idle");
    }

    private void encerrarBatalha() {
        jogoEncerrado = true;
        if (gameLoop != null) {
            gameLoop.stop();
        }

        String resultado;
        if (player1.estaVivo()) {
            resultado = "FIM DA BATALHA! O Player 1 VENCEU!";
        } else if (player2.estaVivo()) {
            resultado = "FIM DA BATALHA! A CPU VENCEU!";
        } else {
            resultado = "FIM DA BATALHA! EMPATE!";
        }

        // Se você não tem Labels para exibir o resultado, o resultado só será logado no console
        // ou você pode usar um Alert (pop-up) se quiser.
        System.out.println(resultado);

        setBotoesHabilitados(false);
    }

    private void atualizarBarrasDeVida() {
        double p1Fracao = (double) player1.getVidaAtual() / player1.getVidaMaxima();
        double p2Fracao = (double) player2.getVidaAtual() / player2.getVidaMaxima();

        player1HealthBar.setProgress(p1Fracao);
        player2HealthBar.setProgress(p2Fracao);
    }

    private void setBotoesHabilitados(boolean habilitado) {
        if (botoesGolpe != null) {
            botoesGolpe.forEach(btn -> btn.setDisable(!habilitado));
        }
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

        // ----------------------------------------------------
        // --- NOVO CÓDIGO PARA CARREGAR A IMAGEM DE FUNDO ---
        // ----------------------------------------------------

        // Ajuste o nome do arquivo (ex: "fundo.png") conforme o seu caso real
        String caminhoImagem = "/Imagens/Dojô Tradicional.jpg";

        try {
            // Usa getClass().getResourceAsStream() para carregar o recurso
            // O caminho é relativo à raiz dos recursos (resources folder)
            Image backgroundImage = new Image(getClass().getResourceAsStream(caminhoImagem));
            backgroundImageView.setImage(backgroundImage);

        } catch (Exception e) {
            // Este catch irá te ajudar a ver se o arquivo não foi encontrado
            System.err.println("ERRO: Não foi possível carregar a imagem de fundo: " + caminhoImagem);
            System.err.println("Verifique se o caminho e o nome do arquivo estão corretos.");
            e.printStackTrace();
        }

        // Note: Assumindo que 'Personagem' e 'Ataque' são classes/enums existentes
        player1 = new Personagem(this.player1Img, false);
        player2 = new Personagem(this.player2Img, true);

        botoesGolpe = Arrays.asList(btnSoco, btnChute, btnEspecial);
        atualizarBarrasDeVida();

        // --- LOOP PRINCIPAL DE ANIMAÇÃO (AnimationTimer) ---
        gameLoop = new AnimationTimer(){

            @Override
            public void handle(long now) {

                if (jogoEncerrado) return;

                // 1. Atualiza as animações
                boolean p1GolpeTerminou = player1.update(now);
                boolean p2GolpeTerminou = player2.update(now);

                // --- FASE 1: Fim da Animação P1 -> INÍCIO DO COOLDOWN ---
                if (p1GolpeTerminou && !emCooldown && !p2AnimacaoIniciada) {
                    golpeEmExecucao = false;
                    emCooldown = true;
                    tempoInicioCongelamento = now;
                    player1.setAnimacao("Idle");
                }

                // --- FASE 2: Fim do Cooldown -> INÍCIO DA ANIMAÇÃO P2 (CPU) ---
                if (emCooldown) {
                    if (now - tempoInicioCongelamento >= COOLDOWN_NANO) {

                        emCooldown = false;

                        // *** CORREÇÃO: Chama setAnimacao com o tempo 'now' ***
                        player2.setAnimacao(ataqueP2Escolhido.getNome(), now);

                        p2AnimacaoIniciada = true;
                        golpeEmExecucao = true;
                    }
                }

                // --- FASE 3: Fim da Animação P2 -> APLICAR DANO E FINALIZAR TURNO ---
                if (p2GolpeTerminou && p2AnimacaoIniciada) {
                    golpeEmExecucao = false;
                    p2AnimacaoIniciada = false;

                    Platform.runLater(() -> aplicarDanoEFinalizarTurno());
                }
            }
        };

        gameLoop.start();
        iniciarNovoTurno();
    }
}