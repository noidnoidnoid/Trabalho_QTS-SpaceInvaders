package br.audio;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeFalse;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.Line;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Integration: AudioPlayer + real MP3 files on the classpath + the mp3spi decoder
 * (runtime dependency in pom.xml) + the machine's real Java Sound mixer.
 *
 * Owner: Roger Egito (220083118).
 *
 * Nothing is mocked here. Which case runs depends on the machine: with a sound
 * device the theme must load and play, and without one the player must fall back
 * to silence instead of crashing the game (defect D2).
 */
@DisplayName("Integracao: AudioPlayer com MP3 real e mixer real")
class ThemeAudioIT {

    private static final String THEME = "/audio/theblackframe.mp3";
    private static final String EXPLOSION = "/audio/explosion.mp3";

    private static boolean hasSoundDevice() {
        return AudioSystem.isLineSupported(new Line.Info(Clip.class));
    }

    @Test
    @DisplayName("CTI-20: com placa de som, o MP3 do tema e decodificado e carregado inteiro no Clip")
    void themeLoadsWithSoundDevice() {
        assumeTrue(hasSoundDevice(), "no sound device on this machine");

        Clip clip = AudioPlayer.loadClip(THEME);

        assertNotNull(clip, "mp3spi must be on the runtime classpath and the resource must be packaged");
        try {
            assertTrue(clip.getFrameLength() > 0);
        } finally {
            clip.close();
        }
    }

    @Test
    @DisplayName("CTI-21: com placa de som, play() toca e stop() para o audio de verdade")
    void playAndStopWithSoundDevice() throws InterruptedException {
        assumeTrue(hasSoundDevice(), "no sound device on this machine");
        Clip clip = AudioPlayer.loadClip(EXPLOSION);
        assertNotNull(clip);
        AudioPlayer player = new AudioPlayer(clip);
        try {
            player.play();
            long deadline = System.currentTimeMillis() + 2000;
            while (!clip.isRunning() && System.currentTimeMillis() < deadline) {
                Thread.sleep(10);
            }
            assertTrue(clip.isRunning(), "clip did not start within 2 s");

            player.stop();

            assertFalse(clip.isRunning());
        } finally {
            clip.close();
        }
    }

    @Test
    @DisplayName("CTI-22: sem placa de som, o jogo segue sem audio em vez de travar")
    void fallsBackToSilenceWithoutSoundDevice() {
        assumeFalse(hasSoundDevice(), "this machine has a sound device");

        assertNull(AudioPlayer.loadClip(THEME));
        assertDoesNotThrow(() -> new AudioPlayer(THEME).play());
    }
}
