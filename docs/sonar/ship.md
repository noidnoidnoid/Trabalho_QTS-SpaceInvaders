# Inspeção da `Ship` (Luiz)

Classe `br.characters.Ship`, complexidade 14. Ferramenta base: Sonar (perfil comprehensive). Complemento: PMD.

## Antes

7 problemas no Sonar ([servidor](before/02-issues-Ship.png), [Eclipse](before/09-eclipse-problemas-Ship.png)) e 1 no PMD ([print](before/10-pmd-Ship.png)).

| Linha | Problema | Sonar | PMD |
| ---: | :--- | :---: | :---: |
| 5 | Import sem uso (`BufferedImage`) | ✓ | ✓ |
| 17 | Campo deveria ser `final` | ✓ | |
| 18 | Campo deveria ser `final` | ✓ | |
| 34 | `System.out` em vez de logger | ✓ | |
| 35 | `printStackTrace()` | ✓ | |
| 72 | Código comentado | ✓ | |
| 82 | Código comentado | ✓ | |

## Depois

| | Antes | Depois |
| :--- | ---: | ---: |
| Issues no Sonar | 7 | 0 |
| Problemas no PMD | 1 | 0 |
| Testes da `Ship` | 32 passando | 32 passando |
| Cobertura de arestas | 8/8 | 8/8 |
| Mutação | 16/18 (88,9%) | 14/16 (87,5%) |

Prints: [0 issues abertas](after/02-issues-Ship.png), [as 7 marcadas como corrigidas](after/02b-issues-Ship-corrigidas.png), [PMD](after/10-pmd-Ship.png) (relatório restrito à `Ship.java`) e [visão geral](after/01-visao-geral.png).

O que mudou na `Ship.java`:

- import sem uso removido;
- `System.out` e `printStackTrace()` trocados por um logger do Java (`java.util.logging`), com a mesma mensagem e a exceção;
- `movex` virou a constante `MOVEX` (a regra `java:S1170` pede `static` para valor fixo) e `shots` virou `final`;
- 2 blocos de código comentado removidos.

No `ShipTest`, o teste de falha ao carregar a imagem passou a verificar o aviso no log, e não mais o terminal.

**Teste manual (22/11/2026):** jogo aberto com `mvn compile exec:java` depois da correção. A nave aparece, anda com `A`/`D` na mesma velocidade, para nas duas bordas e atira com `Espaço`. O log não teve nenhum aviso da `Ship` (só o aviso de falta de som do WSL).

**Mutantes que sobrevivem (linhas 59 e 63, em `limits()`) são equivalentes:** trocar `>` por `>=` ou `<` por `<=` só muda o caso em que o `if` grava o valor que a variável já tem (700 ou 0), então nenhum teste consegue diferenciar. O total caiu de 18 para 16 porque o PIT não cria mutantes em chamadas de log.

**Quality gate "Failed" na visão geral:** vem do código novo do grupo (17 issues novas e 62% de cobertura no código novo, em arquivos como `WebGame` e `PerformanceSystemIT`). A `Ship` não tem issue nova e tem 100% de cobertura.
