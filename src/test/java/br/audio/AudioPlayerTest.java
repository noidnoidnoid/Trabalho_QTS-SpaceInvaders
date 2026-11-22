package br.audio;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Stream;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InOrder;
import org.mockito.MockedStatic;

/**
 * Unit tests for br.audio.AudioPlayer.
 *
 * Owner: Roger Egito (220083118).
 *
 * Isolation strategy:
 * - play()/stop() receive a mocked Clip through the package-private constructor,
 *   so no sound device is needed and no reflection on private field names is used.
 * - loadClip() talks to the static javax.sound.sampled.AudioSystem API. Mockito's
 *   mockStatic replaces it inside each test, which lets us force every failure the
 *   real API can produce (no device, unsupported file, I/O error) on any machine.
 * - The only real AudioSystem calls are the ones that do not need hardware:
 *   a missing resource and a resource that is not audio.
 */
@DisplayName("AudioPlayer - reprodutor de audio")
class AudioPlayerTest {

    private static final String EXISTING_RESOURCE = "/audio/explosion.mp3";

    /** Mocks for the decoding pipeline, recreated for each test that needs them. */
    private static class Pipeline {
        final AudioInputStream encoded = mock(AudioInputStream.class);
        final AudioInputStream decoded = mock(AudioInputStream.class);
        final Clip clip = mock(Clip.class);
        final AtomicReference<AudioFormat> requestedFormat = new AtomicReference<>();

        Pipeline(MockedStatic<AudioSystem> audioSystem, AudioFormat baseFormat) {
            when(encoded.getFormat()).thenReturn(baseFormat);
            audioSystem.when(() -> AudioSystem.getAudioInputStream(any(InputStream.class))).thenReturn(encoded);
            audioSystem.when(() -> AudioSystem.getAudioInputStream(any(AudioFormat.class), any(AudioInputStream.class)))
                    .thenAnswer(call -> {
                        requestedFormat.set(call.getArgument(0));
                        return decoded;
                    });
            audioSystem.when(AudioSystem::getClip).thenReturn(clip);
        }
    }

    /**
     * Captures System.err so tests can check the diagnostic the player prints when it
     * falls back to silence. Without that message the player would fail silently.
     */
    private final ByteArrayOutputStream errOutput = new ByteArrayOutputStream();
    private PrintStream originalErr;

    @BeforeEach
    void captureStdErr() {
        originalErr = System.err;
        System.setErr(new PrintStream(errOutput, true));
    }

    @AfterEach
    void restoreStdErr() {
        System.setErr(originalErr);
    }

    private void assertWarnedAbout(String path) {
        assertTrue(errOutput.toString().contains(path),
                "expected a diagnostic on System.err mentioning " + path + ", got: " + errOutput);
    }

    private void assertNoWarning() {
        assertEquals("", errOutput.toString());
    }

    private static AudioFormat stereoMp3LikeFormat() {
        return new AudioFormat(44100f, 16, 2, true, false);
    }

    @Nested
    @DisplayName("defeitos encontrados na revisao (falhavam no codigo original)")
    class Defects {

        @Test
        @DisplayName("D1 (issue #2): recurso inexistente nao derruba o construtor")
        void missingResourceDoesNotThrow() {
            assertDoesNotThrow(() -> new AudioPlayer("/audio/does-not-exist.mp3"));
        }

        @Test
        @DisplayName("D2 (issue #3): maquina sem dispositivo de audio nao derruba o construtor")
        void missingAudioDeviceDoesNotThrow() {
            try (MockedStatic<AudioSystem> audioSystem = mockStatic(AudioSystem.class)) {
                new Pipeline(audioSystem, stereoMp3LikeFormat());
                // What AudioSystem.getClip() throws when no mixer supports a Clip.
                audioSystem.when(AudioSystem::getClip)
                        .thenThrow(new IllegalArgumentException("No line matching interface Clip is supported."));

                assertDoesNotThrow(() -> new AudioPlayer(EXISTING_RESOURCE));
            }
        }

        @Test
        @DisplayName("D3 (issue #4): stop() sem clip carregado nao lanca NullPointerException")
        void stopWithoutClipDoesNotThrow() {
            AudioPlayer player;
            try (MockedStatic<AudioSystem> audioSystem = mockStatic(AudioSystem.class)) {
                new Pipeline(audioSystem, stereoMp3LikeFormat());
                // A failure the original catch block already handled, leaving clip == null.
                audioSystem.when(AudioSystem::getClip).thenThrow(new LineUnavailableException());
                player = new AudioPlayer(EXISTING_RESOURCE);
            }

            assertDoesNotThrow(player::stop);
        }
    }

    @Nested
    @DisplayName("play() e stop() com Clip isolado")
    class PlayAndStop {

        private Clip clip;
        private AudioPlayer player;

        @BeforeEach
        void setUp() {
            clip = mock(Clip.class);
            player = new AudioPlayer(clip);
        }

        @Test
        @DisplayName("play() sem clip nao faz nada")
        void playWithoutClipIsNoOp() {
            assertDoesNotThrow(() -> new AudioPlayer((Clip) null).play());
        }

        @Test
        @DisplayName("play() parado: volta ao inicio e toca, sem chamar stop no clip")
        void playFromStoppedRewindsAndStarts() {
            when(clip.isRunning()).thenReturn(false);

            player.play();

            InOrder order = inOrder(clip);
            order.verify(clip).setFramePosition(0);
            order.verify(clip).start();
            verify(clip, never()).stop();
        }

        @Test
        @DisplayName("play() tocando: para, volta ao inicio e toca, nessa ordem")
        void playWhileRunningStopsRewindsAndStarts() {
            when(clip.isRunning()).thenReturn(true);

            player.play();

            InOrder order = inOrder(clip);
            order.verify(clip).stop();
            order.verify(clip).setFramePosition(0);
            order.verify(clip).start();
        }

        @Test
        @DisplayName("stop() tocando: para o clip")
        void stopWhileRunningStopsClip() {
            when(clip.isRunning()).thenReturn(true);

            player.stop();

            verify(clip).stop();
        }

        @Test
        @DisplayName("stop() parado: nao chama stop no clip")
        void stopWhileStoppedDoesNothing() {
            when(clip.isRunning()).thenReturn(false);

            player.stop();

            verify(clip, never()).stop();
        }

        @Test
        @DisplayName("stop() nunca reposiciona nem inicia o clip")
        void stopNeverRewindsOrStarts() {
            when(clip.isRunning()).thenReturn(true);

            player.stop();

            verify(clip, never()).setFramePosition(any(Integer.class));
            verify(clip, never()).start();
        }
    }

    @Nested
    @DisplayName("loadClip() com AudioSystem isolado")
    class LoadClip {

        private MockedStatic<AudioSystem> audioSystem;

        @BeforeEach
        void setUp() {
            audioSystem = mockStatic(AudioSystem.class);
        }

        @AfterEach
        void tearDown() {
            audioSystem.close();
        }

        @Test
        @DisplayName("sucesso: devolve o clip de AudioSystem aberto com o audio decodificado")
        void successOpensDecodedStream() throws Exception {
            Pipeline pipeline = new Pipeline(audioSystem, stereoMp3LikeFormat());

            Clip loaded = AudioPlayer.loadClip(EXISTING_RESOURCE);

            assertSame(pipeline.clip, loaded);
            verify(pipeline.clip).open(pipeline.decoded);
            assertNoWarning();
        }

        @ParameterizedTest(name = "{0} canal(is), {1} Hz -> frameSize {2}")
        @CsvSource({ "1, 22050, 2", "2, 44100, 4" })
        @DisplayName("sucesso: pede PCM 16 bits little-endian mantendo taxa e canais do original")
        void successRequestsSigned16BitPcm(int channels, float sampleRate, int expectedFrameSize) {
            Pipeline pipeline = new Pipeline(audioSystem, new AudioFormat(sampleRate, 8, channels, false, true));

            AudioPlayer.loadClip(EXISTING_RESOURCE);

            AudioFormat format = pipeline.requestedFormat.get();
            assertAll(
                    () -> assertEquals(AudioFormat.Encoding.PCM_SIGNED, format.getEncoding()),
                    () -> assertEquals(sampleRate, format.getSampleRate()),
                    () -> assertEquals(16, format.getSampleSizeInBits()),
                    () -> assertEquals(channels, format.getChannels()),
                    () -> assertEquals(expectedFrameSize, format.getFrameSize()),
                    () -> assertEquals(sampleRate, format.getFrameRate()),
                    () -> assertFalse(format.isBigEndian()));
        }

        @Test
        @DisplayName("sucesso: fecha os streams depois de abrir o clip")
        void successClosesStreams() throws Exception {
            Pipeline pipeline = new Pipeline(audioSystem, stereoMp3LikeFormat());

            AudioPlayer.loadClip(EXISTING_RESOURCE);

            InOrder order = inOrder(pipeline.clip, pipeline.decoded, pipeline.encoded);
            order.verify(pipeline.clip).open(pipeline.decoded);
            order.verify(pipeline.decoded).close();
            order.verify(pipeline.encoded).close();
        }

        static Stream<Arguments> loadingFailures() {
            return Stream.of(
                    Arguments.of("arquivo em formato nao suportado", (FailureSetup) (system, p) ->
                            system.when(() -> AudioSystem.getAudioInputStream(any(InputStream.class)))
                                    .thenThrow(new UnsupportedAudioFileException())),
                    Arguments.of("conversao para PCM nao suportada", (FailureSetup) (system, p) ->
                            system.when(() -> AudioSystem.getAudioInputStream(any(AudioFormat.class), any(AudioInputStream.class)))
                                    .thenThrow(new IllegalArgumentException("Unsupported conversion"))),
                    Arguments.of("sem dispositivo de audio", (FailureSetup) (system, p) ->
                            system.when(AudioSystem::getClip)
                                    .thenThrow(new IllegalArgumentException("No line matching interface Clip"))),
                    Arguments.of("linha de audio ocupada", (FailureSetup) (system, p) ->
                            system.when(AudioSystem::getClip).thenThrow(new LineUnavailableException())),
                    Arguments.of("linha recusa abrir", (FailureSetup) (system, p) ->
                            doThrow(new LineUnavailableException()).when(p.clip).open(any(AudioInputStream.class))),
                    Arguments.of("erro de leitura ao abrir", (FailureSetup) (system, p) ->
                            doThrow(new IOException()).when(p.clip).open(any(AudioInputStream.class))));
        }

        @FunctionalInterface
        interface FailureSetup {
            void apply(MockedStatic<AudioSystem> audioSystem, Pipeline pipeline) throws Exception;
        }

        @ParameterizedTest(name = "{0}")
        @MethodSource("loadingFailures")
        @DisplayName("falha no carregamento devolve null sem lancar excecao")
        void loadingFailureReturnsNull(String scenario, FailureSetup failure) throws Exception {
            Pipeline pipeline = new Pipeline(audioSystem, stereoMp3LikeFormat());
            failure.apply(audioSystem, pipeline);

            assertNull(AudioPlayer.loadClip(EXISTING_RESOURCE));
            assertWarnedAbout(EXISTING_RESOURCE);
        }
    }

    @Nested
    @DisplayName("loadClip() com AudioSystem real (sem hardware)")
    class LoadClipWithRealAudioSystem {

        @Test
        @DisplayName("recurso inexistente devolve null")
        void missingResourceReturnsNull() {
            assertNull(AudioPlayer.loadClip("/audio/does-not-exist.mp3"));
            assertWarnedAbout("/audio/does-not-exist.mp3");
        }

        @Test
        @DisplayName("recurso que nao e audio devolve null")
        void nonAudioResourceReturnsNull() {
            assertNull(AudioPlayer.loadClip("/audio/not-audio.txt"));
            assertWarnedAbout("/audio/not-audio.txt");
        }
    }
}
