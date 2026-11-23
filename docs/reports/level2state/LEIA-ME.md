# Cobertura estrutural e mutação da `Level2State` (Breno)

Responsável: Breno Carvalho (123083015). Classe: `br.states.Level2State` (complexidade 23 no Sonar). Suíte: [`Level2StateTest`](../../../src/test/java/br/states/Level2StateTest.java), com 19 casos.

Metas da Entrega 2 para a classe: pelo menos 80% no critério todas-arestas e pelo menos 80% de escore de mutação.

## Resultado

| Critério | Ferramenta | Resultado | Relatório |
| :--- | :--- | :---: | :--- |
| Todas-arestas | JaCoCo 0.8.15 | **34/34 (100%)** | [`coverage/br.states/Level2State.html`](coverage/br.states/Level2State.html) |
| Escore de mutação | PIT 1.30.0 | **29/29 (100%)**, nenhum mutante sem cobertura | [`mutation/br.states/Level2State.java.html`](mutation/br.states/Level2State.java.html) |

Linhas: 55 de 57. As 2 que faltam são os corpos vazios de `KeyPressed` e `KeyReleased`, que não têm aresta. A fase 2 lê o teclado pelo `KeyManager` em `update()`.

Os relatórios vêm da execução só do `Level2StateTest`, na máquina do Breno, em 22/11/2026. Os números da `Level2State` não dependem das outras suítes.

```
mvn test -Dtest=Level2StateTest            # gera target/site/jacoco
mvn test-compile org.pitest:pitest-maven:mutationCoverage "-DtargetClasses=br.states.Level2State" "-DtargetTests=br.states.Level2StateTest*"
```

Prints: [`mvn-test-level2statetest-19.png`](mvn-test-level2statetest-19.png) (19 testes, 0 falhas) e [`pit-level2state-100.png`](pit-level2state-100.png).

## Como o escore chegou a 100%

A `Level2State` entrou na configuração do PIT no `pom.xml` (`targetClasses` e `targetTests`). A primeira execução, com os 14 casos da época, deu **19/29 (66%)**. Os 10 mutantes que sobreviveram mostraram três lacunas na suíte:

| Linha | Mutante sobrevivente | Lacuna | Caso que passou a matá-lo |
| :---: | :--- | :--- | :--- |
| 67, 70, 74, 88 | Remove o desenho do fundo, da nave, dos aliens ou dos tiros | Nenhum caso conferia o que é desenhado | "desenha o fundo, a nave, os 8 aliens e o tiro em cada frame" (11 chamadas a `drawImage`) |
| 82 | Remove `alien.update()` | Nenhum caso conferia o movimento dos aliens | "os aliens se movem a cada frame" |
| 111 | Inverte a colisão nave-alien | Nenhum caso conferia que a partida continua sem colisão | "sem colisao e com aliens restantes, a partida continua na fase 2" |
| 93, 97 | Inverte `if (victory)` ou `if (gameOver)` | Nenhum caso conferia que a tela não muda sem motivo | O mesmo caso acima (a tela continua `LEVEL2`) |
| 95, 99 | Remove `StateManager.setState(...)` | Os casos olhavam só os campos `victory` e `gameOver` | "vitoria muda para a tela You Win" e "derrota muda para a tela Game Over" |

Os 5 casos novos estão na classe aninhada `DesenhoETrocaDeTela`. Eles usam os estados falsos de `usarEstadosFalsos()`, sem carregar a música do menu. Para não fixar decisões de arte, como posição exata, cor ou velocidade, o caso de desenho só conta os elementos desenhados e o de movimento só verifica que a posição mudou.

Sem os `-DtargetClasses` e `-DtargetTests`, o comando do PIT roda as 5 classes configuradas no `pom.xml`, inclusive a `Level2State`.
