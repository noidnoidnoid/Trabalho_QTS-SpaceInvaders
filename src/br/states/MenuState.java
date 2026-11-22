package br.states;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.event.KeyEvent;

import br.Game;
import br.audio.AudioPlayer;
import br.characters.Alien;
import br.display.Background;

public class MenuState implements State {
	
	private String[] options = { "START", "HELP", "EXIT" };
	private Font font1;
	private Font font2;
	// Package-private (no "private") for testability, same decision documented in
	// Level2State: MenuStateTest lives in br.states and sets up boundary positions and
	// menu choices directly, without reflection and without new public getters.
	int choice = 0;
	//POSICAO INICIAL, X=0 E Y=0; ACRESCIDO DO MOVIMENTO UNIFORME MOVEX=3 E MOVEY=3
	int x = 0;
	int y = 0;
	int movex = 3;
	int movey = 3;
	
	private final AudioPlayer themeSound;
	private final Runnable exitAction;
	
	public MenuState() {
		this(new AudioPlayer("/audio/theblackframe.mp3"), () -> System.exit(0));
	}

	// Package-private seam: tests inject a fake theme player and a fake exit action,
	// so they neither play music nor kill the test JVM through System.exit.
	MenuState(AudioPlayer themeSound, Runnable exitAction) {
		this.themeSound = themeSound;
		this.exitAction = exitAction;
	}

	@Override
	public void init() {
		font1 = new Font("Dialog", Font.PLAIN, 48);
		font2 = new Font("Dialog", Font.PLAIN, 24);
		
		themeSound.play();
	}

	//ANIMAÇÃO DO MENU
	@Override
	public void update() {
		//A CADA TICK DA RENDERIZAÇAO DE TELA O OBJETO SE MOVE
		x += movex;
		y += movey;
		
		limits();
	}

	//ANIMAÇÃO DO MENU - LIMITANDO O OBJETO PARA NÃO SAIR DA TELA DE VISÃO
	private void limits() {
		//MARGIN RIGHT
		if (x + 50 > Game.WIDTH) {
			movex = -3;
		}
		//BOTTOM
		if (y + 80 > Game.HEIGHT) {
			movey = -3;
		}
		//MARGIN LEFT
		if (x < 0) {
			movex = 3;
		}
		//TOP
		if (y < 0) {
			movey = 3;
		}
	}

	@Override
	public void render(Graphics g) {
		Background background = new Background();
		background.print(g);
		
		g.setColor(Color.WHITE);
		g.setFont(font1);
		String text = "Space Invaders";
		g.drawString(text,
				Game.WIDTH/2 - g.getFontMetrics().stringWidth(text)/2,
				Game.HEIGHT * 1/4);
		
		g.setFont(font2);
		for (int i = 0; i < options.length; i++) {
			g.setColor(Color.WHITE);
			if (i == choice) {
				g.setColor(Color.YELLOW);
			}
			g.drawString(options[i],
					Game.WIDTH/2 - g.getFontMetrics().stringWidth(options[i])/2,
					 Game.HEIGHT * 3/4 + g.getFontMetrics(font2).getHeight() * i); //+ FONTMETRICS X A ALTURA PARA ESPAÇAR AS OPÇÕES
		}
		
		//ANIMAÇÃO DO MENU
		Alien alien = new Alien(x, y);
		alien.paint(g);
	}

	//METODOS ONDE ACONTECE A INTERACAO
	@Override
	public void KeyPressed(int cod) {
		// Intentionally empty: the menu acts when the key is released (KeyReleased),
		// so holding a key does not repeat the action.
	}

	@Override
	public void KeyReleased(int cod) {
		if (cod == KeyEvent.VK_W) {
			choice--;
			if (choice < 0) {
				choice = options.length - 1;
			}
		}
		if (cod == KeyEvent.VK_S) {
			choice++;
			if (choice > options.length - 1) {
				choice = 0;
			}
		}
		if (cod == KeyEvent.VK_ENTER) {
			select();
		}
	}

	private void select() {
		switch (choice) {
		case 0:
			StateManager.setState(StateManager.LEVEL1);
			break;
		case 1:
			StateManager.setState(StateManager.HELP);
			break;
		case 2:
			exitAction.run();
			break;
		default:
			break;
		}
	}

}
