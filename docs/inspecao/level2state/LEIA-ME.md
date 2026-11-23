# Inspeção do código-fonte da `Level2State` (Breno)

Responsável: Breno Carvalho (123083015). Classe: `br.states.Level2State`, a classe complexa do Breno (complexidade 23 no Sonar).

## Ferramentas

- **PMD 7.7.0**, pelo `maven-pmd-plugin` 3.26.0, com o conjunto de regras `quickstart` já configurado no `pom.xml`. É a mesma configuração usada pelo Roger ([`../LEIA-ME.md`](../LEIA-ME.md)). Comando: `mvn pmd:pmd`. Relatório em `target/reports/pmd.html`.
- **SonarQube** como complemento: a análise do Luiz (perfil Sonar way comprehensive, commit `bbb57f3`) já listava 11 problemas na classe ([print](../../sonar/before/04-issues-Level2State.png) e [lista](../../sonar/before/sonar-antes-comprehensive.md)). A tabela abaixo mostra como cada um foi tratado. Não houve nova análise no Sonar depois das correções; a verificação do "depois" é a do PMD.

## Antes das correções

| Arquivo | Conteúdo |
| :--- | :--- |
| [`antes/Level2State-antes.png`](antes/Level2State-antes.png) | Print da seção `Level2State.java` do relatório: os 10 problemas de prioridade 3 |
| [`antes/pmd.html`](antes/pmd.html), [`antes/pmd.xml`](antes/pmd.xml) | Relatório completo: 11 problemas na classe e 48 no projeto. O 11º (`UselessParentheses`, linha 42) tem prioridade 4 e fica em outra seção do HTML |

O relatório "antes" foi gerado com a correção da issue #10 já aplicada (interação #018 do AI-LOG). Na `main` (commit `cdb5130`) eram 9 problemas na classe. A correção da #10 deixou dois laços que passaram a poder ser `for-each`, e por isso o PMD apontou mais dois `ForLoopCanBeForeach`.

## Problemas e correções

| Linha (antes) | Regra | Problema | Correção |
| :---: | :--- | :--- | :--- |
| 25 | PMD SingularField (3) / Sonar S1450 | Campo `background` só é usado dentro de `render()` | Virou variável local de `render()` |
| 39 | Sonar S1197 | `int arrayDeAliens[] = new int[8]` só para guardar o número 8 | Constante `NUMERO_DE_ALIENS` |
| 42 | PMD UselessParentheses (4) | `new Alien((0 + (a * 100)), Game.HEIGHT * 1/4)` | `new Alien(a * 100, Game.HEIGHT / 4)`. Mesmo valor: com inteiros, `HEIGHT * 1 / 4` e `HEIGHT / 4` dão 270 |
| 73, 82, 88, 111, 121, 124 | PMD ForLoopCanBeForeach (3) | Laços com índice que só leem a lista | `for-each`. Nenhum desses laços altera a lista: as remoções acontecem antes, com `removeIf` |
| 95 e 99 | Sonar S1125 | `if (victory == true)` e `if (gameOver == true)` | `if (victory)` e `if (gameOver)` |
| 120 | Sonar S1117 | Variável local `shots` escondia o campo `shots` | Usa o campo, que é a mesma lista (`shots = ship.getShots()` em `init()`) |
| 133 | PMD UseCollectionIsEmpty (3) / Sonar S1155 | `aliens.size() == 0` | `aliens.isEmpty()` |
| 139 e 142 | PMD UncommentedEmptyMethodBody (3) / Sonar S1186 | `KeyPressed` e `KeyReleased` vazios sem explicação | Comentário: a fase 2 lê o teclado pelo `KeyManager` em `update()` |
| (Sonar, `bbb57f3`) | Sonar S5413 (2) | `remove()` dentro de laço com índice | Já resolvido pela correção da issue #10 (`removeIf`) |
| (Sonar, `bbb57f3`) | Sonar S2209 | `shotAux.setVisible(false)` chamava método estático pela instância | Já resolvido pela correção da issue #9 (Roger), que deixou `setVisible` de instância |

Os nomes `KeyPressed` e `KeyReleased`, com inicial maiúscula, ficaram como estão. Eles vêm da interface `State`, e renomear afetaria as classes dos outros integrantes (mesma decisão do Roger).

## Depois das correções

| Arquivo | Conteúdo |
| :--- | :--- |
| [`depois/Level2State-depois.png`](depois/Level2State-depois.png) | Print do relatório: depois de `Level1State.java` vem `StateManager.java`, sem a `Level2State.java` |
| [`depois/pmd.html`](depois/pmd.html), [`depois/pmd.xml`](depois/pmd.xml) | Relatório completo: nenhum problema na `Level2State` e 37 no projeto |
| [`depois/mvn-test-level2statetest-depois.png`](depois/mvn-test-level2statetest-depois.png) | `mvn test -Dtest=Level2StateTest` depois das correções: 14 testes, 0 falhas |

## Como foi verificado que o comportamento não mudou

1. Os 14 testes do `Level2StateTest` passaram depois das correções (print acima).
2. A versão anterior e a corrigida rodaram lado a lado em 150 partidas simuladas, com teclas aleatórias e sementes fixas, e o estado do jogo foi comparado quadro a quadro. O teste somou cerca de 140 mil quadros, 61 vitórias e 89 derrotas, e o estado (nave, aliens, tiros, vitória e derrota) foi idêntico nas duas versões.
