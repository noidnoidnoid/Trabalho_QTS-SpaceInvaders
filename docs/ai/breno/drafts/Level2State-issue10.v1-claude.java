// Rascunho gerado pelo Claude em 22/11/2026 para a issue #10 (ver AI-LOG, interação #018).
// Revisado pelo Breno antes de ser aplicado. Antes da aplicação foram decididos:
// correção só no Level2State, remoção com removeIf e prova antes/depois.
// A correção do Level2State foi aplicada igual a este rascunho. Nos testes, a versão final
// troca o "new StateManager()" do @BeforeEach por usarEstadosFalsos() (estados falsos,
// restaurados no @AfterEach): na máquina do Breno, com placa de som, cada StateManager
// carregava a música tema (~51 MB) e a suíte completa estourou o heap. Ver AI-LOG #018.
// Evidência: docs/ai/breno/evidencias/2026-11-22-defeito-10-level2state.txt

// ---------------------------------------------------------------------------
// 1. Correção em src/br/states/Level2State.java, dentro de render(Graphics g)
// ---------------------------------------------------------------------------
//
// ANTES:
//
//			//MOVIMENTACAO DO ALIEN
//			for (int c = 0; c < aliens.size(); c++) {
//				Alien alien = aliens.get(c);
//				if (alien.isVisible()) {
//					alien.update();
//				}
//				else {
//					aliens.remove(c);
//				}
//			}
//
//			//DESENHANDO E MOVIMENTANDO OS TIROS
//			for (int e = 0; e < shots.size(); e++) {
//				Shot s = shots.get(e);
//				if (s.isVisible()) {
//					s.paint(g);
//					s.update();
//				}
//				else {
//					shots.remove(e);
//				}
//			}
//
// DEPOIS:
//
//			//MOVIMENTACAO DO ALIEN
//			// Remove os invisiveis antes de percorrer a lista: remover dentro
//			// do laco com indice pulava o elemento seguinte (issue #10).
//			aliens.removeIf(alien -> !alien.isVisible());
//			for (int c = 0; c < aliens.size(); c++) {
//				aliens.get(c).update();
//			}
//
//			//DESENHANDO E MOVIMENTANDO OS TIROS
//			shots.removeIf(s -> !s.isVisible());
//			for (int e = 0; e < shots.size(); e++) {
//				Shot s = shots.get(e);
//				s.paint(g);
//				s.update();
//			}

// ---------------------------------------------------------------------------
// 2. Casos novos em src/test/java/br/states/Level2StateTest.java
// ---------------------------------------------------------------------------

    @Nested
    @DisplayName("Remocao de aliens e tiros invisiveis (issue #10)")
    class RemocaoDeInvisiveis {

        private final Graphics g = mock(Graphics.class);

        @BeforeEach
        void prepararRemocao() {
            state.init();
            // a vitoria chama StateManager.setState: ver "ARMADILHA DE ESTADO ESTATICO".
            new StateManager();
        }

        @Test
        @DisplayName("dois aliens invisiveis seguidos saem da lista no mesmo frame")
        void removeAliensInvisiveisConsecutivos() {
            state.aliens.get(0).setVisible(false);
            state.aliens.get(1).setVisible(false);

            state.render(g);

            assertEquals(6, state.aliens.size());
            assertTrue(state.aliens.stream().allMatch(Alien::isVisible));
        }

        @Test
        @DisplayName("vitoria no mesmo frame em que todos os aliens ficam invisiveis")
        void vitoriaNoMesmoFrameQueOsAliensSomem() {
            state.aliens.forEach(alien -> alien.setVisible(false));

            state.render(g);

            assertTrue(state.aliens.isEmpty());
            assertTrue(state.victory);
        }

        @Test
        @DisplayName("tiro logo depois de um tiro removido continua subindo no mesmo frame")
        void tiroSeguinteAoRemovidoContinuaSubindo() {
            state.ship.simpleShot();
            state.ship.simpleShot();
            Shot removido = state.shots.get(0);
            Shot seguinte = state.shots.get(1);
            removido.setVisible(false);
            int yAntes = seguinte.getY();

            state.render(g);

            assertFalse(state.shots.contains(removido));
            assertEquals(yAntes - Shot.getVelocity(), seguinte.getY());
        }
    }
