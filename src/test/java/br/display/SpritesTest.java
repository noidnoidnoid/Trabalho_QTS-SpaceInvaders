package br.display;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mockStatic;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

/**
 * Unit tests for br.display.Sprites, the image cache added for issues #11 and #12.
 *
 * Owner: Roger Egito (220083118).
 */
@DisplayName("Sprites - cache de imagens")
class SpritesTest {

    @Test
    @DisplayName("le o arquivo uma vez e devolve a mesma imagem nas chamadas seguintes")
    void readsOnceAndReuses() throws IOException {
        BufferedImage first = Sprites.read("src/pinkShot.png");

        try (MockedStatic<ImageIO> imageIO = mockStatic(ImageIO.class)) {
            // If the second call touched the disk again, this mock would make it fail.
            imageIO.when(() -> ImageIO.read(any(File.class))).thenThrow(new IOException("disk read"));

            assertSame(first, Sprites.read("src/pinkShot.png"));
        }
    }

    @Test
    @DisplayName("imagem lida e a do arquivo (dimensoes reais do sprite do alien)")
    void returnsTheFileImage() throws IOException {
        BufferedImage alien = Sprites.read("src/space-green-alien.png");

        assertEquals(ImageIO.read(new File("src/space-green-alien.png")).getWidth(), alien.getWidth());
    }

    @Test
    @DisplayName("arquivo inexistente lanca IOException, como ImageIO.read")
    void missingFileThrows() {
        assertThrows(IOException.class, () -> Sprites.read("src/does-not-exist.png"));
    }

    @Test
    @DisplayName("arquivo que nao e imagem lanca IOException em vez de devolver null")
    void nonImageThrows() {
        assertThrows(IOException.class, () -> Sprites.read("pom.xml"));
    }

    @Test
    @DisplayName("falha nao fica no cache: a proxima chamada tenta ler de novo")
    void failureIsNotCached() throws IOException {
        try (MockedStatic<ImageIO> imageIO = mockStatic(ImageIO.class)) {
            imageIO.when(() -> ImageIO.read(any(File.class))).thenThrow(new IOException("temporary"));
            assertThrows(IOException.class, () -> Sprites.read("src/space-ship.png"));
        }

        assertEquals(ImageIO.read(new File("src/space-ship.png")).getHeight(), Sprites.read("src/space-ship.png").getHeight());
    }
}
