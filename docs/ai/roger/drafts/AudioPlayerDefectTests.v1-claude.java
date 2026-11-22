package br.audio;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

import java.io.InputStream;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.LineUnavailableException;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

/**
 * Unit tests for br.audio.AudioPlayer.
 *
 * Owner: Roger Egito (220083118).
 */
@DisplayName("AudioPlayer - reprodutor de audio")
class AudioPlayerTest {

    private static final String EXISTING_RESOURCE = "/audio/explosion.mp3";

    /**
     * Stubs every AudioSystem call made by the constructor up to getClip(),
     * so each test only decides what getClip() does. No sound card is touched.
     */
    private static void stubDecodingPipeline(MockedStatic<AudioSystem> audioSystem) {
        AudioInputStream encoded = mock(AudioInputStream.class);
        when(encoded.getFormat()).thenReturn(new AudioFormat(44100f, 16, 2, true, false));
        audioSystem.when(() -> AudioSystem.getAudioInputStream(any(InputStream.class))).thenReturn(encoded);
        audioSystem.when(() -> AudioSystem.getAudioInputStream(any(AudioFormat.class), any(AudioInputStream.class)))
                .thenReturn(mock(AudioInputStream.class));
    }

    @Nested
    @DisplayName("defeitos encontrados na revisao")
    class Defects {

        @Test
        @DisplayName("D1: recurso inexistente nao derruba o construtor")
        void missingResourceDoesNotThrow() {
            assertDoesNotThrow(() -> new AudioPlayer("/audio/does-not-exist.mp3"));
        }

        @Test
        @DisplayName("D2: maquina sem dispositivo de audio nao derruba o construtor")
        void missingAudioDeviceDoesNotThrow() {
            try (MockedStatic<AudioSystem> audioSystem = mockStatic(AudioSystem.class)) {
                stubDecodingPipeline(audioSystem);
                // What AudioSystem.getClip() throws when no mixer supports a Clip.
                audioSystem.when(AudioSystem::getClip)
                        .thenThrow(new IllegalArgumentException("No line matching interface Clip is supported."));

                assertDoesNotThrow(() -> new AudioPlayer(EXISTING_RESOURCE));
            }
        }

        @Test
        @DisplayName("D3: stop() sem clip carregado nao lanca NullPointerException")
        void stopWithoutClipDoesNotThrow() {
            AudioPlayer player;
            try (MockedStatic<AudioSystem> audioSystem = mockStatic(AudioSystem.class)) {
                stubDecodingPipeline(audioSystem);
                // A failure the original catch block already handles, leaving clip == null.
                audioSystem.when(AudioSystem::getClip).thenThrow(new LineUnavailableException());
                player = new AudioPlayer(EXISTING_RESOURCE);
            }

            assertDoesNotThrow(player::stop);
        }
    }
}
