# Inspeção do código-fonte com PMD (Roger)

Responsável: Roger Egito (220083118). Classes: `MenuState` (classe complexa, CC 24) e `AudioPlayer`.

## Por que PMD e não SonarQube

O enunciado pede um relatório de inspeção do código "(ex, usando sonar)". O SonarQube precisa de um servidor (Docker ou instalação própria) ou de uma conta no SonarCloud. O PMD faz o mesmo tipo de inspeção estática (code smells, más práticas, código propenso a erro, regras de design) e roda como plugin do Maven, sem servidor nem conta. Por isso qualquer integrante consegue repetir a análise com um comando.

- Ferramenta: PMD 7.7.0, pelo `maven-pmd-plugin` 3.26.0 (configurado no `pom.xml`)
- Conjunto de regras: `quickstart`, o conjunto recomendado pelo próprio PMD para começar
- Escopo: código do jogo (`src/br/**`). Os testes não são inspecionados
- Comando: `mvn pmd:pmd`. Relatório em `target/reports/pmd.html`

## Antes das correções

| Arquivo | Conteúdo |
| :--- | :--- |
| [`antes/MenuState-antes.png`](antes/MenuState-antes.png) | Print: 5 problemas na `MenuState` |
| [`antes/AudioPlayer-antes.png`](antes/AudioPlayer-antes.png) | Print: 3 problemas no `AudioPlayer` |
| [`antes/resumo-antes.png`](antes/resumo-antes.png) | Print: início do relatório (versão do PMD e problemas por prioridade) |
| [`antes/pmd.html`](antes/pmd.html), [`antes/pmd.xml`](antes/pmd.xml) | Relatório completo do projeto: 64 problemas |

## Problemas encontrados e correções

| Classe | Linha | Regra (prioridade) | Problema | Correção |
| :--- | :---: | :--- | :--- | :--- |
| `MenuState` | 16 | OneDeclarationPerLine (4) | `private Font font1, font2;` | Uma declaração por linha |
| `MenuState` | 21 | OneDeclarationPerLine (4) | `int x = 0, y = 0, movex = 3, movey = 3;` | Uma declaração por linha |
| `MenuState` | 23 | SingularField (3) | Campo `alien` só é usado dentro de `render()` | Virou variável local de `render()` |
| `MenuState` | 28 | SingularField (3) | Campo `background` só é usado dentro de `render()` | Virou variável local de `render()` |
| `MenuState` | 109 | UncommentedEmptyMethodBody (3) | `KeyPressed()` vazio sem explicação | Comentário explicando que o menu age ao soltar a tecla |
| `AudioPlayer` | 28 | CloseResource (3) | O `InputStream` do recurso era aberto fora do try-with-resources | Aberto no próprio try-with-resources (fechado em todos os caminhos) |
| `AudioPlayer` | 61 e 68 | ControlStatementBraces (3) | `if (clip == null) return;` sem chaves | Chaves |

Depois de cada correção, as suítes da `MenuState` e do `AudioPlayer` rodaram de novo para garantir que o comportamento não mudou.

**Efeito na cobertura do `AudioPlayer`.** Abrir o recurso no próprio try-with-resources fez o JaCoCo passar de 8/8 para 9/10 arestas, e a complexidade medida de 9 para 10. A aresta nova fica na linha que fecha o try-with-resources externo: o compilador gera ali uma verificação de recurso nulo antes de chamar `close()`. Nesse caminho o recurso nunca é nulo, porque o caso nulo já retornou antes, então a aresta é inalcançável por construção. A alternativa seria voltar a checar o `null` fora do try, que é justamente o formato apontado pelo PMD. Ficou o código que fecha o recurso em todos os caminhos, com a aresta documentada. O escore de mutação continua em 100%.

## Depois das correções

| Arquivo | Conteúdo |
| :--- | :--- |
| [`depois/MenuState-AudioPlayer-depois.png`](depois/MenuState-AudioPlayer-depois.png) | Print: PMD restrito às duas classes, "PMD found no problems in your source code" |
| [`depois/classes-do-roger/`](depois/classes-do-roger/pmd.html) | Esse relatório restrito. Gerado com `<includes>` de `br/states/MenuState.java` e `br/audio/AudioPlayer.java` na configuração do plugin, só para essa execução |
| [`depois/pmd.html`](depois/pmd.html), [`depois/pmd.xml`](depois/pmd.xml) | Relatório completo do projeto: 47 problemas, nenhum em `MenuState` ou `AudioPlayer` |

## Não corrigido, com justificativa

| Onde | Regra | Por que ficou |
| :--- | :--- | :--- |
| `State.java` (afeta `MenuState`) | MethodNamingConventions (1): `KeyPressed` e `KeyReleased` começam com maiúscula | O nome vem da interface `State`, implementada por todos os estados, inclusive os de outros integrantes. Renomear quebraria as classes e os testes deles. Fica como sugestão para o grupo |
| `WebGame.java` (código novo da camada web) | AvoidUsingVolatile (2) | `volatile` é o mecanismo correto para a thread do HTTP enxergar o último estado publicado pela thread do jogo. A regra existe porque `volatile` é fácil de usar errado, e aqui cada campo tem um único escritor |
| `WebServer.java` (código novo da camada web) | AvoidUsingHardCodedIP (3) | `127.0.0.1` é proposital: o servidor só aceita conexões da própria máquina |

No código novo que a IA gerou para a camada web e para `Sprites`, os demais avisos (chaves, parâmetro sem uso, construtor de classe utilitária) também foram corrigidos.
