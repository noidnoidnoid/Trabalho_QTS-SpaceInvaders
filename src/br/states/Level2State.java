package br.states;

import java.awt.Graphics;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.List;

import br.Game;
import br.characters.Alien;
import br.characters.Ship;
import br.characters.Shot;
import br.display.Background;
import br.input.KeyManager;

public class Level2State implements State {

	// Campos de acesso de pacote (sem "private") por decisao de testabilidade:
	// Level2StateTest.java fica de proposito no pacote br.states para ler e
	// manipular este estado direto nos testes, sem getters novos na API
	// publica e sem reflection. Mesma logica ja usada em Ship.x (protected em
	// Character), documentada no cabecalho de ShipTest.java.
	Ship ship;
	List<Shot> shots;
	List<Alien> aliens;
	private Background background;

	boolean victory;
	boolean gameOver;

	@Override
	public void init() {
		//INSTANCIANDO O SHIP
		ship = new Ship();
		
		//PEGANDO A LISTA DE TIROS
		shots = ship.getShots();
		
		//INSTANCIANDO A LISTA DE ALIENS
		int arrayDeAliens[] = new int[8]; //NUMERO DE ALIENS
		aliens = new ArrayList<Alien>();
		for (int a = 0; a < arrayDeAliens.length; a++) {
			aliens.add(new Alien((0 + (a * 100)), Game.HEIGHT * 1/4));
		}
		
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
		
			background = new Background();
			background.print(g);
			
			//DESENHANDO O SHIP
			ship.paint(g);

			//DESENHANDO OS ALIENS
			for (int b = 0; b < aliens.size(); b++) {
				Alien alien = aliens.get(b);
				alien.paint(g);
			}
			
			//MOVIMENTACAO DO ALIEN
			for (int c = 0; c < aliens.size(); c++) {
				Alien alien = aliens.get(c);
				if (alien.isVisible()) {
					alien.update();
				}
				else {
					aliens.remove(c);
				}
			}
			
			//DESENHANDO E MOVIMENTANDO OS TIROS
			for (int e = 0; e < shots.size(); e++) {
				Shot s = shots.get(e);
				if (s.isVisible()) {
					s.paint(g);
					s.update();
				}
				else {
					shots.remove(e);
				}
			}
			checarColisoes();
			
		if (victory == true) {
			//YouWinState CASO victory == true
			StateManager.setState(StateManager.YOUWIN);
		}
		if (gameOver == true) {
			//GameOverState CASO emJogo == false
			StateManager.setState(StateManager.GAMEOVER);
		}
	}
	
	private void checarColisoes() {
		Rectangle formaShip = ship.getBounds();
		Rectangle formaAlien;
		Rectangle formaShot;
		
		//COLISAO ENTRE O SHIP E O ALIEN
		for (int d = 0; d < aliens.size(); d++) {
			Alien alienAux = aliens.get(d);
			formaAlien = alienAux.getBounds();
			if (formaShip.intersects(formaAlien)) {
				gameOver = true;
			}
		}
		
		//COLISAO ENTRE O SHOT E O ALIEN
		List<Shot> shots = ship.getShots();
		for (int i = 0; i < shots.size(); i++) {
			Shot shotAux = shots.get(i);
			formaShot = shotAux.getBounds();
			for (int e = 0; e < aliens.size(); e++) {
				Alien alienAux = aliens.get(e);
				formaAlien = alienAux.getBounds();
				if (formaShot.intersects(formaAlien)) {
					alienAux.setVisible(false);
					shotAux.setVisible(false);
				}
			}
		}
		if (aliens.size() == 0) {
			victory = true;
		}
	}

	@Override
	public void KeyPressed(int cod) {}

	@Override
	public void KeyReleased(int cod) {}

}
