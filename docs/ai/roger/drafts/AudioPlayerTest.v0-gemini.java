package br.audio;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.lang.reflect.Field;

import javax.sound.sampled.Clip;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/**
 * Casos de teste unitarios da classe br.audio.AudioPlayer.
 *
 * Disciplina: Qualidade e Teste de Software
 * Testador: Leon Stevans - Testador da classe AudioPlayer
 */
@DisplayName("AudioPlayer - reprodutor de midia")
class AudioPlayerTest {

    private AudioPlayer audioPlayer;
    private Clip clipMock;

    @BeforeEach
    void preparar() throws Exception {
        // Inicializa passando um caminho invalido para que o construtor capture a 
        // exception e deixe o clip interno como null inicialmente.
        audioPlayer = new AudioPlayer("/caminho_inexistente.mp3");
        
        // Mock da interface Clip do Java Sound API
        clipMock = mock(Clip.class);
    }

    private void injetarClipMock() throws Exception {
        Field clipField = AudioPlayer.class.getDeclaredField("clip");
        clipField.setAccessible(true);
        clipField.set(audioPlayer, clipMock);
    }

    @Nested
    @DisplayName("play()")
    class Play {

        @Test
        @DisplayName("ignora a reproducao se o clip for nulo (evita NullPointerException)")
        void ignoraPlayComClipNulo() {
            // Nao injetamos o mock aqui. O clip sera null.
            assertDoesNotThrow(() -> audioPlayer.play());
        }

        @Test
        @DisplayName("reinicia a musica e da play se o clip estiver configurado")
        void tocaMusicaDoinicio() throws Exception {
            injetarClipMock();
            when(clipMock.isRunning()).thenReturn(false);

            audioPlayer.play();

            // Verifica se a posicao foi zerada e se o clip foi iniciado
            verify(clipMock).setFramePosition(0);
            verify(clipMock).start();
        }
        
        @Test
        @DisplayName("se ja estiver tocando, para antes de reiniciar")
        void paraAntesDeReiniciarSeEstiverTocando() throws Exception {
            injetarClipMock();
            when(clipMock.isRunning()).thenReturn(true);

            audioPlayer.play();

            // O metodo stop() interno eh chamado, o que aciona clip.stop()
            verify(clipMock).stop();
            verify(clipMock).setFramePosition(0);
            verify(clipMock).start();
        }
    }

    @Nested
    @DisplayName("stop()")
    class Stop {

        @Test
        @DisplayName("para a musica apenas se ela estiver rodando (isRunning = true)")
        void paraMusicaSeEstiverRodando() throws Exception {
            injetarClipMock();
            when(clipMock.isRunning()).thenReturn(true);

            audioPlayer.stop();

            verify(clipMock).stop();
        }

        @Test
        @DisplayName("nao aciona stop no clip se ele nao estiver rodando")
        void naoParaSeNaoEstiverRodando() throws Exception {
            injetarClipMock();
            when(clipMock.isRunning()).thenReturn(false);

            audioPlayer.stop();

            // Verifica que o metodo stop() do clip NUNCA foi chamado
            verify(clipMock, never()).stop();
        }
    }
}