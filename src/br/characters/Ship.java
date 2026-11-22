package br.characters;

import java.awt.Graphics;
import java.awt.Rectangle;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.imageio.ImageIO;

import br.Game;

public class Ship extends Character {

	private static final Logger LOGGER = Logger.getLogger(Ship.class.getName());

	private static final int MOVEX = 6; //VELOCIDADE DE MOVIMENTACAO DO OBJETO
	private final List<Shot> shots; //PARA ALOCAR UMA QUANTIDADE DESCONHECIDA DE TIROS
	
	//CONSTRUTOR
	public Ship() {
		this.load();
		
		this.x = Game.WIDTH/2;
		
		shots = new ArrayList<Shot>();
	}
	
	@Override
	public void load() {
		try {
			img = ImageIO.read(new File("src/space-ship.png"));
		} catch (IOException e) {
			LOGGER.log(Level.WARNING, "Não foi possível carregar a imagem da nave", e);
		}
	}
	
	@Override
	public void paint(Graphics g) {
		g.drawImage(img, x, Game.HEIGHT - 150, x + 100, Game.HEIGHT - 150 + 100, 0, 0, img.getWidth(), img.getHeight(), null);
	}
	
	
	public void moveShip(int valor) {
		if (valor == 1) {
			x += MOVEX; // >>>
		} else if (valor == -1) {
			x -= MOVEX; // <<<
		}
		
		limits();
	}
	
	public void limits() {
		//MARGIN RIGHT
		if (x + 100 > Game.WIDTH) {
			x = 700;
		}
		//MARGIN LEFT
		if (x < 0) {
			x = 0;
		}
	}
	
	@Override
	public int getY() {
		return Game.HEIGHT - 150; //NÃO SE MOVE EM Y
	}
	
	public int getShipWidth() {
		return 100;
	}
	
	@Override
	public Rectangle getBounds() {
		return new Rectangle(x, getY(), getShipWidth(), getY());
	}
	
	public void simpleShot() {
		this.shots.add(new Shot(this.getX(), this.getY()));
	}
	
	public List<Shot> getShots() {
		return shots;
	}
}