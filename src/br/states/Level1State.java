package br.states;

import java.awt.Graphics;
import java.awt.Rectangle;
import java.util.List;

import br.Game;
import br.characters.Alien;
import br.characters.Ship;
import br.characters.Shot;
import br.display.Background;
import br.input.KeyManager;

public class Level1State implements State {
	
	private Ship ship;
	private List<Shot> shots;
	private Alien alien;
	
	private boolean victory;
	private boolean gameOver;

	@Override
	public void init() {
		//INSTANCIANDO O SHIP
		ship = new Ship();
		
		//PEGANDO A LISTA DE TIROS
		shots = ship.getShots();
		
		//INSTANCIANDO O ALIEN
		alien = new Alien(Game.WIDTH/2, Game.HEIGHT * 1/4);
		
		victory = false;
		gameOver = false;
	}

	@Override
	public void update() {
		//INTERACAO DO USUARIO
		if (KeyManager.a) {
			ship.moveShip(-1); //-1 PARA <<<
		}
		if (KeyManager.d) {
			ship.moveShip(1); //1 PARA >>>
		}
		if (KeyManager.space) {
			ship.simpleShot();
		}
	}

	@Override
	public void render(Graphics g) {
		Background background = new Background();
		background.print(g);
		
		//DESENHANDO O SHIP
		ship.paint(g);

		//DESENHANDO O ALIEN
		alien.paint(g);
		
		//MOVIMENTACAO DO ALIEN
		alien.update();
		
		//DESENHANDO E MOVIMENTANDO OS TIROS
		// Remove the invisible shots before going through the list: removing inside
		// an index loop skipped the next element (issue #10).
		shots.removeIf(s -> !s.isVisible());
		for (Shot s : shots) {
			s.paint(g);
			s.update();
		}
		checarColisoes();

		if (victory) {
			//YouWinState CASO victory
			StateManager.setState(StateManager.LEVEL2);
		}
		if (gameOver) {
			//GameOverState CASO gameOver
			StateManager.setState(StateManager.GAMEOVER);
		}
	}
	
	private void checarColisoes() {
		Rectangle formaShip = ship.getBounds();
		Rectangle formaAlien = alien.getBounds();
		
		//COLISAO ENTRE O SHIP E O ALIEN
		if (formaShip.intersects(formaAlien)) {
			gameOver = true;
		}
		
		//COLISAO ENTRE O SHOT E O ALIEN
		for (Shot tempShot : shots) {
			if (tempShot.getBounds().intersects(formaAlien)) {
				victory = true;
			}
		}
	}

	@Override
	public void KeyPressed(int cod) {
		// Intentionally empty: level 1 reads the keyboard through KeyManager in update().
	}

	@Override
	public void KeyReleased(int cod) {
		// Intentionally empty: level 1 reads the keyboard through KeyManager in update().
	}

}
