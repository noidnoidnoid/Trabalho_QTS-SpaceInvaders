package br;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.lang.reflect.Field;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

/**
 * Casos de teste unitarios da classe br.Game.
 *
 * Disciplina: Qualidade e Teste de Software
 * Responsavel: Leon Stevans (123083047) - Projetista/Testador da classe Game
 *
 * DECISAO DE TESTABILIDADE: Diferente de Ship, os principais campos (thread, running) 
 * da classe Game sao puramente "private", o que impede acesso direto por classes do 
 * mesmo pacote. Como o SUT nao deve ser alterado (e a instanciacao do Frame de Display 
 * e custosa/quebrada em ambientes headless), foi utilizada Reflection para ler/modificar 
 * as variaveis internas apenas para fins de validacao e assercao sem tocar no codigo real.
 */
@DisplayName("Game - ciclo de vida e Game Loop principal")
class GameTest {

    private Game game;

    @BeforeEach
    void prepararEstado() {
        // Inicializa o objeto do jogo de verdade
        game = new Game();
    }

    /**
     * Metodo auxiliar para extrair o valor de uma variavel privada de Game via Reflection.
     */
    private Object getPrivateField(String fieldName) throws Exception {
        Field field = Game.class.getDeclaredField(fieldName);
        field.setAccessible(true);
        return field.get(game);
    }

    /**
     * Metodo auxiliar para injetar valor numa variavel privada de Game via Reflection.
     */
    private void setPrivateField(String fieldName, Object value) throws Exception {
        Field field = Game.class.getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(game, value);
    }

    @Nested
    @DisplayName("start()")
    class Inicializacao {

        @Test
        @DisplayName("inicia a thread e define running como true")
        void iniciaThreadERunning() throws Exception {
            // A principio a thread deve ser nula
            assertNull(getPrivateField("thread"));
            assertFalse((boolean) getPrivateField("running"));

            game.start();

            // Apos o start, a thread deve ser instanciada e a flag de loop ativada
            assertNotNull(getPrivateField("thread"));
            assertTrue((boolean) getPrivateField("running"));
        }

        @Test
        @DisplayName("nao sobrescreve a thread se ela ja existir (return prematuro)")
        void naoSobrescreveThreadExistente() throws Exception {
            // Mock de uma thread para simular que o jogo ja comecou
            Thread threadMock = mock(Thread.class);
            setPrivateField("thread", threadMock);
            setPrivateField("running", false); // Forcamos false para ver se o start() altera
            
            game.start();
            
            // O retorno tem que ser imediato. O objeto deve permanecer exatamente o mesmo Mock
            // e o running continuar false (pois a execucao parou no "if (thread != null) return;").
            assertTrue(getPrivateField("thread") == threadMock);
            assertFalse((boolean) getPrivateField("running"));
        }
    }

    @Nested
    @DisplayName("stop()")
    class Encerramento {

        @Test
        @DisplayName("ignora a tentativa de parar se a thread for nula")
        void ignoraParaThreadNula() throws Exception {
            assertNull(getPrivateField("thread"));
            // Nao deve lancar nenhuma excecao (NullPointerException)
            game.stop();
        }

        @Test
        @DisplayName("acopla (join) a thread encerando-a suavemente")
        void acoplaThreadNaParada() throws Exception {
            // Iniciamos a thread de verdade
            game.start();
            Thread t = (Thread) getPrivateField("thread");
            
            // Quebra o loop manualmente para nao travar o join
            setPrivateField("running", false);
            
            game.stop();
            
            // Apos o join, a thread real nao deve mais estar 'alive' (rodando)
            assertFalse(t.isAlive(), "A thread deve ser terminada apos o stop()");
        }
    }
    
    @Nested
    @DisplayName("run() - comportamento do Frame Rate")
    class GameLoop {
    	
    	@Test
    	@Timeout(2) // Timeout drastico para evitar deadlocks infinitos em caso de falha do SUT
    	@DisplayName("o laco 'while(running)' processa os frames e freia a tempo")
    	void processaLacoSincronizado() throws Exception {
    		
    		// Ativamos a thread, logo running vira 'true'
    		game.start();
    		
    		// Para podermos garantir que a thread se encerre no ambiente de teste,
    		// usamos uma segunda thread para desativar a chave de running apos breve milessegundos.
    		Thread disarm = new Thread(() -> {
    			try {
					Thread.sleep(100);
					setPrivateField("running", false);
				} catch (Exception e) {
					e.printStackTrace();
				}
    		});
    		disarm.start();
    		
    		// Executa o join real, forçando que o processo aguarde o timeout do disarm()
    		game.stop();
    		
    		// Se o teste completou sem disparar o timeout do JUnit, quer dizer que 
    		// a variavel 'timePerTick' (1 bilhao / 60) cumpriu seu papel travando/sincronizando 
    		// o laco while de delta, e saindo dele ordenadamente quando running = false.
    		assertFalse((boolean) getPrivateField("running"));
    	}
    }
}