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

	boolean victory;
	boolean gameOver;

	private static final int NUMERO_DE_ALIENS = 8;

	@Override
	public void init() {
		//INSTANCIANDO O SHIP
		ship = new Ship();
		
		//PEGANDO A LISTA DE TIROS
		shots = ship.getShots();
		
		//INSTANCIANDO A LISTA DE ALIENS
		aliens = new ArrayList<Alien>();
		for (int a = 0; a < NUMERO_DE_ALIENS; a++) {
			aliens.add(new Alien(a * 100, Game.HEIGHT / 4));
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
		
			Background background = new Background();
			background.print(g);
			
			//DESENHANDO O SHIP
			ship.paint(g);

			//DESENHANDO OS ALIENS
			for (Alien alien : aliens) {
				alien.paint(g);
			}
			
			//MOVIMENTACAO DO ALIEN
			// Remove os invisiveis antes de percorrer a lista: remover dentro
			// do laco com indice pulava o elemento seguinte (issue #10).
			aliens.removeIf(alien -> !alien.isVisible());
			for (Alien alien : aliens) {
				alien.update();
			}
			
			//DESENHANDO E MOVIMENTANDO OS TIROS
			shots.removeIf(s -> !s.isVisible());
			for (Shot s : shots) {
				s.paint(g);
				s.update();
			}
			checarColisoes();
			
		if (victory) {
			//YouWinState CASO victory
			StateManager.setState(StateManager.YOUWIN);
		}
		if (gameOver) {
			//GameOverState CASO gameOver
			StateManager.setState(StateManager.GAMEOVER);
		}
	}
	
	private void checarColisoes() {
		Rectangle formaShip = ship.getBounds();
		Rectangle formaAlien;
		Rectangle formaShot;
		
		//COLISAO ENTRE O SHIP E O ALIEN
		for (Alien alienAux : aliens) {
			formaAlien = alienAux.getBounds();
			if (formaShip.intersects(formaAlien)) {
				gameOver = true;
			}
		}
		
		//COLISAO ENTRE O SHOT E O ALIEN
		for (Shot shotAux : shots) {
			formaShot = shotAux.getBounds();
			for (Alien alienAux : aliens) {
				formaAlien = alienAux.getBounds();
				if (formaShot.intersects(formaAlien)) {
					alienAux.setVisible(false);
					shotAux.setVisible(false);
				}
			}
		}
		if (aliens.isEmpty()) {
			victory = true;
		}
	}

	@Override
	public void KeyPressed(int cod) {
		// Vazio de proposito: a fase 2 le o teclado pelo KeyManager em update().
	}

	@Override
	public void KeyReleased(int cod) {
		// Vazio de proposito: a fase 2 le o teclado pelo KeyManager em update().
	}

}
