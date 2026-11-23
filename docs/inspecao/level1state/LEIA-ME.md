# Inspeção do código-fonte da `Level1State`

Classe: `br.states.Level1State` (complexidade ciclomática 17 no JaCoCo antes das correções).

## Ferramentas

- **PMD 7.7.0**, pelo `maven-pmd-plugin` 3.26.0, com o conjunto de regras `quickstart` já configurado no `pom.xml`. É a mesma configuração usada nas inspeções da `MenuState`, do `AudioPlayer` ([`../LEIA-ME.md`](../LEIA-ME.md)) e da `Level2State` ([`../level2state/LEIA-ME.md`](../level2state/LEIA-ME.md)). Comando: `mvn pmd:pmd`. Relatório em `target/reports/pmd.html`.
- **SonarQube** como complemento: a análise do Luiz (perfil Sonar way comprehensive, commit `bbb57f3`) já listava 13 problemas na classe ([print](../../sonar/before/03-issues-Level1State.png) e [lista](../../sonar/before/sonar-antes-comprehensive.md)). A tabela abaixo mostra como cada um foi tratado. Não houve nova análise no Sonar depois das correções. A verificação do "depois" é a do PMD.

## Antes das correções

| Arquivo | Conteúdo |
| :--- | :--- |
| [`antes/Level1State-antes.png`](antes/Level1State-antes.png) | Print da seção `Level1State.java` do relatório: 4 problemas de prioridade 3 |
| [`antes/pmd.html`](antes/pmd.html), [`antes/pmd.xml`](antes/pmd.xml) | Relatório completo: 4 problemas na classe e 37 no projeto |

## Problemas e correções

| Linha (antes) | Regra | Problema | Correção |
| :---: | :--- | :--- | :--- |
| 19 | PMD SingularField (3) / Sonar S1450 | Campo `background` só é usado dentro de `render()` | Virou variável local de `render()` |
| 21, 36, 58, 83, 88, 102 | Sonar S125 | Blocos de código comentado (restos da variável `emJogo`, que não existe mais) | Removidos |
| 72 a 81 (79) | Sonar S5413 / issue [#10](https://github.com/noidnoidnoid/Trabalho_QTS-SpaceInvaders/issues/10) | `shots.remove(e)` dentro de um laço com índice pulava o tiro seguinte | `shots.removeIf(s -> !s.isVisible())` antes de percorrer a lista, a mesma correção feita na `Level2State` |
| 84 e 89 | Sonar S1125 | `if (victory == true)` e `if (gameOver == true)` | `if (victory)` e `if (gameOver)` |
| 107 | Sonar S1117 | Variável local `shots` escondia o campo `shots` | Usa o campo, que é a mesma lista (`shots = ship.getShots()` em `init()`) |
| 108 | PMD ForLoopCanBeForeach (3) | Laço com índice que só lê a lista | `for-each` |
| 118 e 121 | PMD UncommentedEmptyMethodBody (3) / Sonar S1186 | `KeyPressed` e `KeyReleased` vazios sem explicação | Comentário: a fase 1 lê o teclado pelo `KeyManager` em `update()` |

A correção da #10 foi feita com um teste que falhava antes (sobrava 1 tiro depois do frame) e passa depois: caso "issue #10" de [`Level1StateTransicoesTest`](../../../src/test/java/br/states/Level1StateTransicoesTest.java). Evidência em [`../../ai/roger/evidencias/2026-11-23-issue10-level1state-antes.txt`](../../ai/roger/evidencias/2026-11-23-issue10-level1state-antes.txt).

Depois das correções, os 25 casos da classe (`Level1StateTest`, `Level1StateCoberturaMutacaoTest` e `Level1StateTransicoesTest`), os testes de integração e os de sistema continuam passando.

## Depois das correções

| Arquivo | Conteúdo |
| :--- | :--- |
| [`depois/Level1State-depois.png`](depois/Level1State-depois.png) | Print: PMD restrito à classe, "PMD found no problems in your source code" |
| [`depois/somente-level1state/`](depois/somente-level1state/pmd.html) | Esse relatório restrito. Gerado com `<includes>` de `br/states/Level1State.java` na configuração do plugin, só para essa execução |
| [`depois/pmd.html`](depois/pmd.html), [`depois/pmd.xml`](depois/pmd.xml) | Relatório completo: 33 problemas no projeto, nenhum na `Level1State` |
