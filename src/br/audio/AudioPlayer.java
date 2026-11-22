package br.audio;

import java.io.IOException;
import java.io.InputStream;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;

public class AudioPlayer {

	// null when the sound could not be loaded. The game then runs silently.
	private final Clip clip;

	public AudioPlayer(String path) {
		this(loadClip(path));
	}

	// Package-private seam: tests pass a mocked Clip instead of opening a real sound device.
	AudioPlayer(Clip clip) {
		this.clip = clip;
	}

	static Clip loadClip(String path) {
		try (InputStream resource = AudioPlayer.class.getResourceAsStream(path)) {
			if (resource == null) {
				System.err.println("Audio resource not found: " + path);
				return null;
			}
			try (AudioInputStream ais = AudioSystem.getAudioInputStream(resource)) {
				AudioFormat baseFormat = ais.getFormat();
				// Decode MP3 into 16-bit signed little-endian PCM, which every Clip can play.
				AudioFormat decodeFormat = new AudioFormat(
						AudioFormat.Encoding.PCM_SIGNED, //encoding
						baseFormat.getSampleRate(), //sampleRate
						16, //sampleSizeInBits
						baseFormat.getChannels(), //channels
						baseFormat.getChannels() * 2, //frameSize: 2 bytes per sample per channel
						baseFormat.getSampleRate(), //frameRate
						false); //bigEndian
				try (AudioInputStream dais = AudioSystem.getAudioInputStream(decodeFormat, ais)) {
					Clip loaded = AudioSystem.getClip();
					// open() reads the whole stream into memory, so closing the streams afterwards is safe.
					loaded.open(dais);
					return loaded;
				}
			}
		} catch (UnsupportedAudioFileException | IOException | LineUnavailableException
				| IllegalArgumentException e) {
			// AudioSystem throws IllegalArgumentException when no installed mixer supports a Clip
			// (machine without a sound device) or when the decoding conversion is not supported.
			System.err.println("Audio unavailable for " + path + ", continuing without sound: " + e);
			return null;
		}
	}
	
	public void play() {
		if (clip == null) {
			return;
		}
		stop();
		clip.setFramePosition(0); //VOLTAR PARA O INICIO DA MUSICA
		clip.start();
	}

	public void stop() {
		if (clip == null) {
			return;
		}
		if (clip.isRunning()) {
			clip.stop();
		}
	}
}
