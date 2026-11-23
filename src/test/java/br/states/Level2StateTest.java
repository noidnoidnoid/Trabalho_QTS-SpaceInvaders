package br.states;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.awt.Graphics;
import java.awt.Image;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import br.Game;
import br.characters.Alien;
import br.characters.Shot;
import br.input.KeyManager;

/**
 * Casos de teste unitarios da classe br.states.Level2State.
 *
 * Disciplina: Qualidade e Teste de Software
 * Responsavel: Breno Carvalho (123083015) - Testador da classe Level2State
 *
 * DECISAO DE TESTABILIDADE (registrar no Plano de Teste / AI-LOG):
 * Diferente de Ship.x, que ja era "protected" em Character e por isso o
 * ShipTest nao precisou tocar no SUT, aqui os campos ship/shots/aliens/
 * victory/gameOver eram "private" em Level2State e precisaram ser rebaixados
 * para acesso de pacote (sem modificador). Esta classe de teste fica de
 * proposito no pacote br.states, pelo mesmo motivo do ShipTest: acesso
 * direto ao estado interno sem getters novos na API publica e sem reflection.
 *
 * ARMADILHA DE ESTADO ESTATICO:
 * StateManager.states so e populado dentro do construtor de StateManager
 * (StateManager.java:20-27). Testes que levam victory/gameOver a true
 * acionam StateManager.setState(...) dentro de render(), e com o array
 * vazio isso lanca NullPointerException. A primeira versao resolvia com
 * "new StateManager()" antes de cada caso, mas o construtor cria um
 * MenuState que carrega a musica tema inteira na memoria (~51 MB, nunca
 * liberados). Em maquina com placa de som, as instancias se acumulam na
 * JVM do Surefire e a suite estourou o heap. Por isso as classes aninhadas
 * chamam usarEstadosFalsos(), que poe estados falsos no array, e
 * restaurarEstados() devolve o array original depois de cada caso.
 */
@DisplayName("Level2State - segunda fase do jogo")
class Level2StateTest {

    private Level2State state;

    @BeforeEach
    void prepararEstado() {
        // KeyManager.a/d/space sao static: zera antes de cada teste para a
        // suite nao depender da ordem de execucao (mesmo cuidado documentado
        // em ShipTest).
        KeyManager.a = false;
        KeyManager.d = false;
        KeyManager.space = false;

        state = new Level2State();
    }

    // Guardados por usarEstadosFalsos() e devolvidos por restaurarEstados().
    private State[] estadosOriginais;
    private int estadoAtualOriginal;

    // Ver nota "ARMADILHA DE ESTADO ESTATICO" no cabecalho da classe.
    void usarEstadosFalsos() {
        estadosOriginais = StateManager.states.clone();
        estadoAtualOriginal = StateManager.currentState;
        for (int i = 0; i < StateManager.NUMBERSTATES; i++) {
            StateManager.states[i] = mock(State.class);
        }
    }

    @AfterEach
    void restaurarEstados() {
        if (estadosOriginais != null) {
            System.arraycopy(estadosOriginais, 0, StateManager.states, 0, StateManager.NUMBERSTATES);
            StateManager.currentState = estadoAtualOriginal;
        }
    }

    @Nested
    @DisplayName("init()")
    class Inicializacao {

        @Test
        @DisplayName("cria exatamente 8 aliens")
        void criaOitoAliens() {
            state.init();

            assertEquals(8, state.aliens.size());
        }

        @Test
        @DisplayName("posiciona os aliens em X = indice*100 e Y = Game.HEIGHT/4")
        void posicionaAliensEmGrade() {
            state.init();

            for (int i = 0; i < 8; i++) {
                Alien alien = state.aliens.get(i);
                assertEquals(i * 100, alien.getX());
                assertEquals(Game.HEIGHT / 4, alien.getY());
            }
        }

        @Test
        @DisplayName("comeca sem vitoria nem derrota")
        void comecaSemVitoriaOuDerrota() {
            state.init();

            assertFalse(state.victory);
            assertFalse(state.gameOver);
        }

        @Test
        @DisplayName("lista de tiros do state e a mesma lista interna da nave")
        void listaDeTirosCompartilhadaComANave() {
            state.init();

            // Mesmo vazamento de encapsulamento ja documentado em ShipTest
            // (getShotsExpoeListaInterna): quem mexer em state.shots mexe
            // direto na lista de tiros da nave, e vice-versa.
            assertSame(state.ship.getShots(), state.shots);
        }
    }

    @Nested
    @DisplayName("update() - entrada do jogador")
    class Movimentacao {

        @BeforeEach
        void prepararMovimentacao() {
            state.init();
        }

        @Test
        @DisplayName("tecla D move a nave 6 pixels para a direita")
        void teclaDMoveNaveParaDireita() {
            int xInicial = state.ship.getX();
            KeyManager.d = true;

            state.update();

            assertEquals(xInicial + 6, state.ship.getX());
        }

        @Test
        @DisplayName("tecla A move a nave 6 pixels para a esquerda")
        void teclaAMoveNaveParaEsquerda() {
            // afasta da borda esquerda antes, senao limits() trava x em 0
            state.ship.moveShip(1);
            int xInicial = state.ship.getX();
            KeyManager.a = true;

            state.update();

            assertEquals(xInicial - 6, state.ship.getX());
        }

        @Test
        @DisplayName("tecla space dispara um tiro")
        void teclaSpaceDisparaTiro() {
            KeyManager.space = true;

            state.update();

            assertEquals(1, state.ship.getShots().size());
        }

        @Test
        @DisplayName("sem teclas pressionadas, a nave nao se move e nao atira")
        void semTeclasNaoAlteraEstado() {
            int xInicial = state.ship.getX();

            state.update();

            assertEquals(xInicial, state.ship.getX());
            assertTrue(state.ship.getShots().isEmpty());
        }
    }

    @Nested
    @DisplayName("Colisoes e fim de jogo (via render)")
    class Colisoes {

        private final Graphics g = mock(Graphics.class);

        @BeforeEach
        void prepararColisoes() {
            state.init();
            // ver nota "ARMADILHA DE ESTADO ESTATICO" no cabecalho da classe.
            usarEstadosFalsos();
        }

        @Test
        @DisplayName("nave colidindo com alien define gameOver")
        void colisaoNaveAlienDefineGameOver() {
            // A nave nao se move dentro de render(), entao seu retangulo fica
            // constante durante a chamada: capturar o valor uma unica vez e
            // suficiente aqui (diferente do caso do tiro, abaixo).
            Alien alienNaMesmaPosicaoDaNave = spy(state.aliens.get(0));
            doReturn(state.ship.getBounds()).when(alienNaMesmaPosicaoDaNave).getBounds();
            state.aliens.set(0, alienNaMesmaPosicaoDaNave);

            state.render(g);

            assertTrue(state.gameOver);
            assertFalse(state.victory);
        }

        @Test
        @DisplayName("tiro que atinge um alien torna os dois invisiveis")
        void tiroAtingeAlienTornaAmbosInvisiveis() {
            state.ship.simpleShot();
            Shot tiro = state.ship.getShots().get(0);

            // O proprio render() move o tiro (s.update()) antes de checar
            // colisao, entao o retangulo do tiro muda durante a chamada.
            // doAnswer faz o alien espiao sempre refletir a posicao ATUAL do
            // tiro, em vez de travar num valor capturado antes do movimento.
            Alien alvo = spy(state.aliens.get(0));
            doAnswer(invocation -> tiro.getBounds()).when(alvo).getBounds();
            state.aliens.set(0, alvo);

            state.render(g);

            assertFalse(alvo.isVisible());
            assertFalse(tiro.isVisible());
        }

        @Test
        @DisplayName("sem aliens restantes, define vitoria")
        void semAliensRestantesDefineVitoria() {
            state.aliens.clear();

            state.render(g);

            assertTrue(state.victory);
            assertFalse(state.gameOver);
        }
    }

    @Nested
    @DisplayName("Remocao de aliens e tiros invisiveis (issue #10)")
    class RemocaoDeInvisiveis {

        private final Graphics g = mock(Graphics.class);

        @BeforeEach
        void prepararRemocao() {
            state.init();
            // a vitoria chama StateManager.setState: ver "ARMADILHA DE ESTADO ESTATICO".
            usarEstadosFalsos();
        }

        @Test
        @DisplayName("dois aliens invisiveis seguidos saem da lista no mesmo frame")
        void removeAliensInvisiveisConsecutivos() {
            state.aliens.get(0).setVisible(false);
            state.aliens.get(1).setVisible(false);

            state.render(g);

            assertEquals(6, state.aliens.size());
            assertTrue(state.aliens.stream().allMatch(Alien::isVisible));
        }

        @Test
        @DisplayName("vitoria no mesmo frame em que todos os aliens ficam invisiveis")
        void vitoriaNoMesmoFrameQueOsAliensSomem() {
            state.aliens.forEach(alien -> alien.setVisible(false));

            state.render(g);

            assertTrue(state.aliens.isEmpty());
            assertTrue(state.victory);
        }

        @Test
        @DisplayName("tiro logo depois de um tiro removido continua subindo no mesmo frame")
        void tiroSeguinteAoRemovidoContinuaSubindo() {
            state.ship.simpleShot();
            state.ship.simpleShot();
            Shot removido = state.shots.get(0);
            Shot seguinte = state.shots.get(1);
            removido.setVisible(false);
            int yAntes = seguinte.getY();

            state.render(g);

            assertFalse(state.shots.contains(removido));
            assertEquals(yAntes - Shot.getVelocity(), seguinte.getY());
        }
    }

    @Nested
    @DisplayName("Desenho e troca de tela (via render)")
    class DesenhoETrocaDeTela {

        private final Graphics g = mock(Graphics.class);

        @BeforeEach
        void prepararDesenho() {
            state.init();
            usarEstadosFalsos();
            StateManager.currentState = StateManager.LEVEL2;
        }

        @Test
        @DisplayName("desenha o fundo, a nave, os 8 aliens e o tiro em cada frame")
        void desenhaTodosOsElementos() {
            state.ship.simpleShot();

            state.render(g);

            // 1 fundo + 1 nave + 8 aliens + 1 tiro: cada um e uma chamada a drawImage.
            verify(g, times(11)).drawImage(any(Image.class), anyInt(), anyInt(), anyInt(), anyInt(),
                    anyInt(), anyInt(), anyInt(), anyInt(), any());
        }

        @Test
        @DisplayName("os aliens se movem a cada frame")
        void aliensSeMovem() {
            int xAntes = state.aliens.get(0).getX();

            state.render(g);

            assertNotEquals(xAntes, state.aliens.get(0).getX());
        }

        @Test
        @DisplayName("sem colisao e com aliens restantes, a partida continua na fase 2")
        void semColisaoPartidaContinua() {
            state.render(g);

            assertFalse(state.gameOver);
            assertFalse(state.victory);
            assertEquals(StateManager.LEVEL2, StateManager.currentState);
        }

        @Test
        @DisplayName("vitoria muda para a tela You Win")
        void vitoriaMudaParaYouWin() {
            State telaYouWin = StateManager.states[StateManager.YOUWIN];
            state.aliens.clear();

            state.render(g);

            assertEquals(StateManager.YOUWIN, StateManager.currentState);
            verify(telaYouWin).init();
        }

        @Test
        @DisplayName("derrota muda para a tela Game Over")
        void derrotaMudaParaGameOver() {
            State telaGameOver = StateManager.states[StateManager.GAMEOVER];
            Alien alienNaNave = spy(state.aliens.get(0));
            doReturn(state.ship.getBounds()).when(alienNaNave).getBounds();
            state.aliens.set(0, alienNaNave);

            state.render(g);

            assertEquals(StateManager.GAMEOVER, StateManager.currentState);
            verify(telaGameOver).init();
        }
    }
}
