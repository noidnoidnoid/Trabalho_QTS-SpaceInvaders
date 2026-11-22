package br.display;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.imageio.ImageIO;

/**
 * Reads each sprite file from disk once and shares the image afterwards.
 *
 * The game creates a new Background on every frame and a new Alien or Shot for
 * every alien and every shot, and each constructor used to read its PNG again.
 * Reading the background alone took ~15 ms of the 16.7 ms frame budget (issues
 * #11 and #12). The images are only drawn, never modified, so sharing them is safe.
 */
public final class Sprites {

	private static final Map<String, BufferedImage> CACHE = new ConcurrentHashMap<>();

	private Sprites() {}

	/**
	 * Same contract as ImageIO.read(new File(path)): throws IOException when the file
	 * cannot be read. Failures are not cached, so a later call tries again.
	 */
	public static BufferedImage read(String path) throws IOException {
		BufferedImage cached = CACHE.get(path);
		if (cached != null) {
			return cached;
		}
		BufferedImage image = ImageIO.read(new File(path));
		if (image == null) {
			throw new IOException("Not a readable image: " + path);
		}
		CACHE.putIfAbsent(path, image);
		return image;
	}
}
