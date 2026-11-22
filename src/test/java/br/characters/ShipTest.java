package br.characters;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;

import java.awt.Graphics;
import java.awt.Rectangle;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.List;

import javax.imageio.ImageIO;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.MockedStatic;

import br.Game;

/**
 * Casos de teste unitarios da classe br.characters.Ship.
 *
 * Disciplina: Qualidade e Teste de Software
 * Responsavel: Luiz Eduardo (123083019) - Testador da classe Ship
 *
 * Os valores usados sao de fronteira (e nao valores "do meio") porque o criterio
 * cobrado e Todas-Arestas, e porque sao eles que matam os mutantes de troca de
 * operador relacional gerados pelo PIT.
 *
 * A classe de teste fica no pacote br.characters de proposito: os campos x e img
 * sao protected em br.characters.Character e ficam acessiveis dentro do mesmo
 * pacote, o que permite montar os cenarios de fronteira sem reflection e sem
 * alterar uma linha sequer do SUT.
 */
@DisplayName("Ship - nave do jogador")
class ShipTest {

    /** Ship() posiciona a nave em Game.WIDTH / 2. */
    private static final int X_INICIAL = Game.WIDTH / 2;        // 400

    /** Campo privado movex de Ship. */
    private static final int VELOCIDADE = 6;

    /** A nave nao se move em Y. */
    private static final int Y_FIXO = Game.HEIGHT - 150;        // 930

    /** Valor fixo devolvido por getShipWidth(), que NAO e a largura da imagem. */
    private static final int LARGURA_NAVE = 100;

    /** Maior x em que a nave ainda cabe inteira na tela. */
    private static final int X_MAXIMO = Game.WIDTH - LARGURA_NAVE;   // 700

    /** Dimensoes reais do arquivo src/space-ship.png. */
    private static final int LARGURA_IMG = 444;
    private static final int ALTURA_IMG = 512;

    private Ship ship;

    @BeforeEach
    void preparar() {
        // Shot.velocity e static no SUT. Qualquer teste que mude a velocidade
        // contamina os demais, entao ela e zerada antes de cada caso para que a
        // suite nao dependa da ordem de execucao. Shot.isVisible deixou de ser
        // static na correcao da issue #9, entao nao precisa mais ser zerado aqui.
        Shot.setVelocity(7);

        ship = new Ship();
    }

    @Nested
    @DisplayName("Construtor")
    class Construtor {

        @Test
        @DisplayName("posiciona a nave no centro horizontal da tela")
        void posicionaNoCentroHorizontal() {
            assertEquals(X_INICIAL, ship.getX());
        }

        @Test
        @DisplayName("comeca com a lista de tiros criada e vazia")
        void comecaSemTiros() {
            assertNotNull(ship.getShots(), "getShots() nao pode devolver null");
            assertTrue(ship.getShots().isEmpty());
        }

        @Test
        @DisplayName("carrega src/space-ship.png")
        void carregaImagemDaNave() {
            // load() engole a IOException e deixa img nulo. Se este teste falhar,
            // o diretorio de trabalho do surefire nao e a raiz do projeto, porque
            // Ship.load() usa new File("src/space-ship.png"), que e um caminho relativo.
            assertNotNull(ship.img, "imagem nao carregada: confira o workingDirectory do surefire");
            assertEquals(LARGURA_IMG, ship.img.getWidth());
            assertEquals(ALTURA_IMG, ship.img.getHeight());
        }

        @Test
        @DisplayName("quando o PNG nao pode ser lido, a nave e criada sem imagem")
        void tolerariaFalhaDeLeituraDaImagem() {
            // Caminho de excecao de load(). Mockito 5 usa o inline mock maker por
            // padrao, o que permite interceptar o metodo estatico ImageIO.read
            // sem tocar no SUT. Isola o teste do sistema de arquivos e cobre o
            // unico bloco de Ship que os demais casos nao alcancam.
            PrintStream saidaOriginal = System.out;
            PrintStream erroOriginal = System.err;
            ByteArrayOutputStream saidaCapturada = new ByteArrayOutputStream();
            // load() tambem faz e.printStackTrace(); a saida de erro e capturada em vez
            // de descartada, senao o mutante que remove essa chamada sobrevive.
            ByteArrayOutputStream erroCapturado = new ByteArrayOutputStream();

            Ship naveSemImagem;
            try {
                System.setOut(new PrintStream(saidaCapturada));
                System.setErr(new PrintStream(erroCapturado));

                try (MockedStatic<ImageIO> imageIO = mockStatic(ImageIO.class)) {
                    imageIO.when(() -> ImageIO.read(any(File.class)))
                           .thenThrow(new IOException("arquivo de imagem ausente"));

                    naveSemImagem = new Ship();
                }
            } finally {
                System.setOut(saidaOriginal);
                System.setErr(erroOriginal);
            }

            assertNull(naveSemImagem.img, "load() deve engolir a IOException e deixar img nulo");
            assertTrue(saidaCapturada.toString().contains("carregar a imagem da nave"),
                    "load() deve avisar na saida padrao");
            assertTrue(erroCapturado.toString().contains("arquivo de imagem ausente"),
                    "load() deve registrar a excecao na saida de erro");
            // O construtor termina mesmo sem a imagem: a nave continua utilizavel.
            assertEquals(X_INICIAL, naveSemImagem.getX());
            assertTrue(naveSemImagem.getShots().isEmpty());
        }
    }

    @Nested
    @DisplayName("moveShip(int)")
    class MoveShip {

        @Test
        @DisplayName("valor 1 desloca a nave 6 pixels para a direita")
        void moveParaDireita() {
            ship.moveShip(1);

            assertEquals(X_INICIAL + VELOCIDADE, ship.getX());
        }

        @Test
        @DisplayName("valor -1 desloca a nave 6 pixels para a esquerda")
        void moveParaEsquerda() {
            ship.moveShip(-1);

            assertEquals(X_INICIAL - VELOCIDADE, ship.getX());
        }

        @ParameterizedTest(name = "valor {0}")
        @ValueSource(ints = { 0, 2, -2, 7, -99 })
        @DisplayName("qualquer valor diferente de 1 e -1 nao move a nave")
        void valorNeutroNaoMove(int valor) {
            ship.moveShip(valor);

            assertEquals(X_INICIAL, ship.getX());
        }

        @Test
        @DisplayName("aplica os limites da tela depois de mover para a direita")
        void aplicaLimiteAoMoverParaDireita() {
            ship.x = X_MAXIMO - 1;   // 699: o passo de 6 estoura a borda direita

            ship.moveShip(1);

            assertEquals(X_MAXIMO, ship.getX());
        }

        @Test
        @DisplayName("aplica os limites da tela depois de mover para a esquerda")
        void aplicaLimiteAoMoverParaEsquerda() {
            ship.x = VELOCIDADE - 3;   // 3: o passo de 6 estoura a borda esquerda

            ship.moveShip(-1);

            assertEquals(0, ship.getX());
        }
    }

    @Nested
    @DisplayName("limits() - analise de valor limite")
    class Limites {

        @Test
        @DisplayName("x = 699 esta dentro da tela e nao e corrigido")
        void abaixoDoLimiteDireito() {
            ship.x = X_MAXIMO - 1;

            ship.limits();

            assertEquals(X_MAXIMO - 1, ship.getX());
        }

        @Test
        @DisplayName("x = 700 e a fronteira exata e nao e corrigido")
        void exatamenteNoLimiteDireito() {
            // 700 + 100 > 800 e falso: a nave encosta na borda mas ainda cabe.
            ship.x = X_MAXIMO;

            ship.limits();

            assertEquals(X_MAXIMO, ship.getX());
        }

        @Test
        @DisplayName("x = 701 ultrapassa a borda direita e volta para 700")
        void acimaDoLimiteDireito() {
            ship.x = X_MAXIMO + 1;

            ship.limits();

            assertEquals(X_MAXIMO, ship.getX());
        }

        @Test
        @DisplayName("x = 1 esta dentro da tela e nao e corrigido")
        void acimaDoLimiteEsquerdo() {
            ship.x = 1;

            ship.limits();

            assertEquals(1, ship.getX());
        }

        @Test
        @DisplayName("x = 0 e a fronteira exata e nao e corrigido")
        void exatamenteNoLimiteEsquerdo() {
            ship.x = 0;

            ship.limits();

            assertEquals(0, ship.getX());
        }

        @Test
        @DisplayName("x = -1 ultrapassa a borda esquerda e volta para 0")
        void abaixoDoLimiteEsquerdo() {
            ship.x = -1;

            ship.limits();

            assertEquals(0, ship.getX());
        }
    }

    @Nested
    @DisplayName("Posicao e dimensoes")
    class PosicaoEDimensoes {

        @Test
        @DisplayName("getY() devolve uma altura fixa de 930")
        void getYEhFixo() {
            assertEquals(Y_FIXO, ship.getY());
        }

        @Test
        @DisplayName("getY() nao muda depois de movimentos horizontais")
        void getYNaoMudaComMovimento() {
            ship.moveShip(1);
            ship.moveShip(1);
            ship.moveShip(-1);

            assertEquals(Y_FIXO, ship.getY());
        }

        @Test
        @DisplayName("getShipWidth() devolve 100, e nao a largura da imagem")
        void getShipWidthEhConstante() {
            assertEquals(LARGURA_NAVE, ship.getShipWidth());
            // A imagem tem 444px; o SUT usa a constante 100 e deixa
            // img.getWidth() comentado em Ship.java:72.
            assertEquals(LARGURA_IMG, ship.img.getWidth());
        }
    }

    @Nested
    @DisplayName("getBounds() - retangulo de colisao")
    class Bounds {

        @Test
        @DisplayName("acompanha a posicao horizontal da nave")
        void acompanhaPosicaoX() {
            ship.moveShip(1);

            Rectangle bounds = ship.getBounds();

            assertEquals(X_INICIAL + VELOCIDADE, bounds.x);
            assertEquals(Y_FIXO, bounds.y);
        }

        @Test
        @DisplayName("tem a largura da nave")
        void temLarguraDaNave() {
            assertEquals(LARGURA_NAVE, ship.getBounds().width);
        }

        @Test
        @DisplayName("BUG #3: a altura e 930 em vez de 100")
        void alturaIncorretaDocumentada() {
            // Ship.java:77 passa getY() como quarto argumento de Rectangle, que e
            // a ALTURA, e nao a coordenada inferior. O retangulo de colisao da nave
            // tem 100x930 e se estende muito abaixo da tela, o que faz a deteccao
            // de colisao de Level1State e Level2State disparar cedo demais.
            //
            // Este caso registra o comportamento ATUAL de proposito: o defeito esta
            // aberto nas Issues e o SUT nao foi corrigido nesta entrega. Quando a
            // correcao entrar, o valor esperado passa a ser LARGURA_NAVE (100).
            Rectangle bounds = ship.getBounds();

            assertEquals(Y_FIXO, bounds.height, "comportamento defeituoso conhecido - ver Issues do repositorio");
        }
    }

    @Nested
    @DisplayName("simpleShot() e getShots()")
    class Tiros {

        @Test
        @DisplayName("adiciona um tiro na lista")
        void adicionaUmTiro() {
            ship.simpleShot();

            assertEquals(1, ship.getShots().size());
        }

        @Test
        @DisplayName("cria o tiro na posicao atual da nave")
        void tiroNasceNaPosicaoDaNave() {
            ship.moveShip(1);

            ship.simpleShot();

            Shot tiro = ship.getShots().get(0);
            assertEquals(ship.getX(), tiro.getX());
            assertEquals(ship.getY(), tiro.getY());
        }

        @Test
        @DisplayName("chamadas sucessivas acumulam tiros")
        void acumulaTiros() {
            ship.simpleShot();
            ship.simpleShot();
            ship.simpleShot();

            assertEquals(3, ship.getShots().size());
        }

        @Test
        @DisplayName("tiros disparados de posicoes diferentes guardam x diferentes")
        void tirosGuardamPosicoesDistintas() {
            ship.simpleShot();
            ship.moveShip(1);
            ship.simpleShot();

            List<Shot> tiros = ship.getShots();
            assertEquals(X_INICIAL, tiros.get(0).getX());
            assertEquals(X_INICIAL + VELOCIDADE, tiros.get(1).getX());
        }

        @Test
        @DisplayName("getShots() expoe a lista interna, e nao uma copia")
        void getShotsExpoeListaInterna() {
            // Vazamento de encapsulamento: quem chama getShots() pode limpar a
            // lista de tiros da nave. E exatamente o que Level1State e Level2State
            // fazem ao remover tiros durante a renderizacao. Registrado como
            // achado de manutenibilidade para a inspecao do Sonar na Entrega 2.
            assertSame(ship.getShots(), ship.getShots());

            ship.simpleShot();
            ship.getShots().clear();

            assertTrue(ship.getShots().isEmpty());
        }
    }

    @Nested
    @DisplayName("paint(Graphics)")
    class Pintura {

        @Test
        @DisplayName("desenha a imagem no retangulo de destino correto")
        void desenhaNoDestinoCorreto() {
            Graphics g = mock(Graphics.class);

            ship.paint(g);

            // Ship.java:41 -> drawImage(img, x, 930, x + 100, 1030, 0, 0, 444, 512, null)
            verify(g).drawImage(
                    eq(ship.img),
                    eq(X_INICIAL), eq(Y_FIXO),
                    eq(X_INICIAL + LARGURA_NAVE), eq(Y_FIXO + LARGURA_NAVE),
                    eq(0), eq(0),
                    eq(LARGURA_IMG), eq(ALTURA_IMG),
                    isNull());
        }

        @Test
        @DisplayName("acompanha a nave depois de um movimento")
        void acompanhaMovimento() {
            Graphics g = mock(Graphics.class);
            ship.moveShip(-1);
            int x = X_INICIAL - VELOCIDADE;

            ship.paint(g);

            verify(g).drawImage(
                    eq(ship.img),
                    eq(x), eq(Y_FIXO),
                    eq(x + LARGURA_NAVE), eq(Y_FIXO + LARGURA_NAVE),
                    eq(0), eq(0),
                    eq(LARGURA_IMG), eq(ALTURA_IMG),
                    isNull());
        }
    }
}
