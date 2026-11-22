package br.characters;

import java.awt.Graphics;
import java.awt.Rectangle;
import java.io.IOException;


import br.display.Sprites;

public class Shot extends Character {

	private static int velocity = 7;
	private int width = 6, height = 20; //LARGURA E ALTURA DO TIRO
	// Per shot, not static: a static flag made one hit hide every shot on screen (issue #9).
	private boolean isVisible; //TIRO DEIXARÁ DE SER VISIVEL QUANDO COLIDIR COM OUTRO OBJETO
	
	//CONSTRUTOR
	public Shot(int inicioX, int inicioY) {
		this.load();
		
		//TIRO VAI SAIR DA POSIÇÃO DO PLAYER
		this.x = inicioX;
		this.y = inicioY;
		
		isVisible = true;
	}
	
	@Override
	public void load() {
		try {
			// Read once and shared: holding space creates one shot per frame (issue #12).
			img = Sprites.read("src/pinkShot.png");
		} catch (IOException e) {
			System.out.println("Não foi possível carregar a imagem da nave");
			e.printStackTrace();
		}
	}
	
	@Override
	public void paint(Graphics g) {
		g.drawImage(img, x, y, x + width, y + 20, 0, 0, img.getWidth(), img.getHeight(), null);
	}
	
	@Override
	public void update() {
		y -= velocity;
		// The shot moves up, so it leaves the screen when its bottom edge passes y = 0.
		// The old check (y > Game.HEIGHT) was never true and shots were never removed (issue #8).
		if (y + height < 0) {
			isVisible = false;
		}
	}

	public static int getVelocity() {
		return velocity;
	}

	public static void setVelocity(int velocity) {
		Shot.velocity = velocity;
	}

	public boolean isVisible() {
		return isVisible;
	}

	public void setVisible(boolean isVisible) {
		this.isVisible = isVisible;
	}

	@Override
	public Rectangle getBounds() {
		return new Rectangle(x, y, width, height);
	}
	
	
}
