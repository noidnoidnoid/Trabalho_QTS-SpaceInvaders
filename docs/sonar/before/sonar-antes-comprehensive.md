# SonarQube: baseline ANTES das correções (perfil Sonar way comprehensive)

- Servidor: SonarQube Community Build 26.11.0.134175 (local), scanner Maven 5.9.0.7382
- Data da análise no servidor: `2026-11-22T02:12:26-0300`
- Gerado em: `2026-11-22T02:12:55`
- Projeto: `space-invaders-comprehensive` (código em `src/br/**`, testes em `src/test/java/**`)

## Visão geral do projeto

| Métrica | Valor |
| :--- | ---: |
| Linhas de código | 1180 |
| Complexidade ciclomática (total) | 189 |
| Cobertura (%) | 41.4 |
| Cobertura de arestas/condições (%) | 33.9 |
| Issues abertas | 106 |
| Bugs | 2 |
| Vulnerabilidades | 6 |
| Code smells | 98 |
| Security hotspots | 0 |
| Duplicação (%) | 20.9 |
| Dívida técnica (min) | 622 |
| Nota de confiabilidade | C |
| Nota de segurança | B |
| Nota de manutenibilidade | A |

## Por arquivo

| Arquivo | Issues | Complexidade | Cobertura de arestas (%) | Linhas |
| :--- | ---: | ---: | ---: | ---: |
| `src/br/states/Level1State.java` | 13 | 16 | 75.0 | 83 |
| `src/br/Game.java` | 12 | 14 | 0.0 | 76 |
| `src/br/states/Level2State.java` | 11 | 23 | 94.1 | 111 |
| `src/br/states/StateManager.java` | 8 | 8 | - | 46 |
| `src/br/characters/Ship.java` | 7 | 14 | 100.0 | 65 |
| `src/br/characters/Shot.java` | 7 | 10 | 50.0 | 54 |
| `src/br/states/MenuState.java` | 7 | 22 | 0.0 | 106 |
| `src/br/input/KeyManager.java` | 6 | 8 | 0.0 | 24 |
| `src/br/states/GameOverState.java` | 5 | 15 | 0.0 | 81 |
| `src/br/states/YouWinState.java` | 5 | 15 | 0.0 | 81 |
| `src/br/display/Background.java` | 4 | 2 | - | 23 |
| `src/br/display/Display.java` | 4 | 5 | - | 47 |
| `src/br/states/HelpState.java` | 4 | 15 | 0.0 | 79 |
| `src/br/characters/Alien.java` | 3 | 10 | 50.0 | 56 |
| `src/br/states/State.java` | 2 | 0 | - | 9 |
| `src/br/audio/AudioPlayer.java` | 1 | 5 | 0.0 | 41 |
| `src/br/characters/Character.java` | 1 | 6 | - | 23 |
| `pom.xml` | 0 | - | - | 168 |
| `src/br/Launch.java` | 0 | 1 | - | 7 |

## Regras mais violadas

| Regra | Ocorrências |
| :--- | ---: |
| `java:S9438` | 17 |
| `java:S1186` | 15 |
| `java:S125` | 11 |
| `java:S1450` | 7 |
| `java:S3010` | 7 |
| `java:S1659` | 7 |
| `java:S4507` | 6 |
| `java:S106` | 5 |
| `java:S1125` | 4 |
| `java:S2696` | 4 |
| `java:S1128` | 3 |
| `java:S5413` | 3 |
| `java:S1117` | 2 |
| `java:S100` | 2 |
| `java:S1130` | 1 |

## Issues por arquivo

### `src/br/states/Level1State.java` (13)

| Linha | Regra | Impacto | Mensagem |
| ---: | :--- | :--- | :--- |
| 19 | `java:S1450` | maintainability:low | Remove the "background" field and declare it as a local variable in the relevant methods. |
| 21 | `java:S125` | maintainability:medium | This block of commented-out lines of code should be removed. |
| 36 | `java:S125` | maintainability:medium | This block of commented-out lines of code should be removed. |
| 58 | `java:S125` | maintainability:medium | This block of commented-out lines of code should be removed. |
| 79 | `java:S5413` | maintainability:medium | Verify that "remove()" is used correctly. |
| 83 | `java:S125` | maintainability:medium | This block of commented-out lines of code should be removed. |
| 84 | `java:S1125` | maintainability:low | Remove the unnecessary boolean literal. |
| 88 | `java:S125` | maintainability:medium | This block of commented-out lines of code should be removed. |
| 89 | `java:S1125` | maintainability:low | Remove the unnecessary boolean literal. |
| 102 | `java:S125` | maintainability:medium | This block of commented-out lines of code should be removed. |
| 107 | `java:S1117` | maintainability:medium | Rename "shots" which hides the field declared at line 17. |
| 118 | `java:S1186` | maintainability:high | Add a nested comment explaining why this method is empty, throw an UnsupportedOperationException or complete the implementation. |
| 121 | `java:S1186` | maintainability:high | Add a nested comment explaining why this method is empty, throw an UnsupportedOperationException or complete the implementation. |

### `src/br/Game.java` (12)

| Linha | Regra | Impacto | Mensagem |
| ---: | :--- | :--- | :--- |
| 12 | `java:S9438` | maintainability:medium/reliability:low | Make this private field "final", it is never reassigned. |
| 16 | `java:S1659` | maintainability:low | Declare "HEIGHT" on a separate line. |
| 18 | `java:S9438` | maintainability:medium/reliability:low | Make this private field "final", it is never reassigned. |
| 19 | `java:S9438` | maintainability:medium/reliability:low | Make this private field "final", it is never reassigned. |
| 35 | `java:S117` | maintainability:low | Rename this local variable to match the regular expression '^[a-z][a-zA-Z0-9]*$'. |
| 36 | `java:S2184` | reliability:low | Cast one of the operands of this division operation to a "double". |
| 50 | `java:S125` | maintainability:medium | This block of commented-out lines of code should be removed. |
| 51 | `java:S8346` | maintainability:high/reliability:medium | Decrement operator (--) should not be used with floating point variables |
| 57 | `java:S1186` | maintainability:high | Add a nested comment explaining why this method is empty, throw an UnsupportedOperationException or complete the implementation. |
| 86 | `java:S125` | maintainability:medium | This block of commented-out lines of code should be removed. |
| 101 | `java:S2142` | reliability:medium | Either re-interrupt this method or rethrow the "InterruptedException" that can be caught here. |
| 102 | `java:S4507` | security:low | Make sure this debug feature is deactivated before delivering the code in production. |

### `src/br/states/Level2State.java` (11)

| Linha | Regra | Impacto | Mensagem |
| ---: | :--- | :--- | :--- |
| 25 | `java:S1450` | maintainability:low | Remove the "background" field and declare it as a local variable in the relevant methods. |
| 39 | `java:S1197` | maintainability:low | Move the array designators [] to the type. |
| 85 | `java:S5413` | maintainability:medium | Verify that "remove()" is used correctly. |
| 97 | `java:S5413` | maintainability:medium | Verify that "remove()" is used correctly. |
| 102 | `java:S1125` | maintainability:low | Remove the unnecessary boolean literal. |
| 106 | `java:S1125` | maintainability:low | Remove the unnecessary boolean literal. |
| 127 | `java:S1117` | maintainability:medium | Rename "shots" which hides the field declared at line 23. |
| 136 | `java:S2209` | maintainability:medium | Change this instance-reference to a static reference. |
| 140 | `java:S1155` | maintainability:low | Use isEmpty() to check whether the collection is empty or not. |
| 146 | `java:S1186` | maintainability:high | Add a nested comment explaining why this method is empty, throw an UnsupportedOperationException or complete the implementation. |
| 149 | `java:S1186` | maintainability:high | Add a nested comment explaining why this method is empty, throw an UnsupportedOperationException or complete the implementation. |

### `src/br/states/StateManager.java` (8)

| Linha | Regra | Impacto | Mensagem |
| ---: | :--- | :--- | :--- |
| 11 | `java:S2386` | maintainability:low | Make this member "protected". |
| 33 | `java:S3010` | maintainability:medium | Remove this assignment of "states". |
| 34 | `java:S3010` | maintainability:medium | Remove this assignment of "states". |
| 35 | `java:S3010` | maintainability:medium | Remove this assignment of "states". |
| 36 | `java:S3010` | maintainability:medium | Remove this assignment of "states". |
| 37 | `java:S3010` | maintainability:medium | Remove this assignment of "states". |
| 38 | `java:S3010` | maintainability:medium | Remove this assignment of "states". |
| 50 | `java:S1186` | maintainability:high | Add a nested comment explaining why this method is empty, throw an UnsupportedOperationException or complete the implementation. |

### `src/br/characters/Ship.java` (7)

| Linha | Regra | Impacto | Mensagem |
| ---: | :--- | :--- | :--- |
| 5 | `java:S1128` | maintainability:low | Remove this unused import 'java.awt.image.BufferedImage'. |
| 17 | `java:S9438` | maintainability:medium/reliability:low | Make this private field "final", it is never reassigned. |
| 18 | `java:S9438` | maintainability:medium/reliability:low | Make this private field "final", it is never reassigned. |
| 34 | `java:S106` | maintainability:medium | Replace this use of System.out by a logger. |
| 35 | `java:S4507` | security:low | Make sure this debug feature is deactivated before delivering the code in production. |
| 72 | `java:S125` | maintainability:medium | This block of commented-out lines of code should be removed. |
| 82 | `java:S125` | maintainability:medium | This block of commented-out lines of code should be removed. |

### `src/br/characters/Shot.java` (7)

| Linha | Regra | Impacto | Mensagem |
| ---: | :--- | :--- | :--- |
| 15 | `java:S9438` | maintainability:medium/reliability:low | Make this private field "final", it is never reassigned. |
| 15 | `java:S9438` | maintainability:medium/reliability:low | Make this private field "final", it is never reassigned. |
| 15 | `java:S1659` | maintainability:low | Declare "height" on a separate line. |
| 26 | `java:S3010` | maintainability:medium | Remove this assignment of "isVisible". |
| 34 | `java:S106` | maintainability:medium | Replace this use of System.out by a logger. |
| 35 | `java:S4507` | security:low | Make sure this debug feature is deactivated before delivering the code in production. |
| 48 | `java:S2696` | maintainability:high | Make the enclosing method "static" or remove this set. |

### `src/br/states/MenuState.java` (7)

| Linha | Regra | Impacto | Mensagem |
| ---: | :--- | :--- | :--- |
| 15 | `java:S9438` | maintainability:medium/reliability:low | Make this private field "final", it is never reassigned. |
| 16 | `java:S1659` | maintainability:low | Declare "font2" on a separate line. |
| 18 | `java:S1659` | maintainability:low | Declare "y" and all following declarations on a separate line. |
| 20 | `java:S1450` | maintainability:low | Remove the "alien" field and declare it as a local variable in the relevant methods. |
| 22 | `java:S9438` | maintainability:medium/reliability:low | Make this private field "final", it is never reassigned. |
| 24 | `java:S1450` | maintainability:low | Remove the "background" field and declare it as a local variable in the relevant methods. |
| 98 | `java:S1186` | maintainability:high | Add a nested comment explaining why this method is empty, throw an UnsupportedOperationException or complete the implementation. |

### `src/br/input/KeyManager.java` (6)

| Linha | Regra | Impacto | Mensagem |
| ---: | :--- | :--- | :--- |
| 10 | `java:S9438` | maintainability:medium/reliability:low | Make this private field "final", it is never reassigned. |
| 12 | `java:S1659` | maintainability:low | Declare "d" and all following declarations on a separate line. |
| 15 | `java:S2696` | maintainability:high | Make the enclosing method "static" or remove this set. |
| 16 | `java:S2696` | maintainability:high | Make the enclosing method "static" or remove this set. |
| 17 | `java:S2696` | maintainability:high | Make the enclosing method "static" or remove this set. |
| 33 | `java:S1186` | maintainability:high | Add a nested comment explaining why this method is empty, throw an UnsupportedOperationException or complete the implementation. |

### `src/br/states/GameOverState.java` (5)

| Linha | Regra | Impacto | Mensagem |
| ---: | :--- | :--- | :--- |
| 13 | `java:S9438` | maintainability:medium/reliability:low | Make this private field "final", it is never reassigned. |
| 14 | `java:S1659` | maintainability:low | Declare "font2" on a separate line. |
| 17 | `java:S1450` | maintainability:low | Remove the "background" field and declare it as a local variable in the relevant methods. |
| 26 | `java:S1186` | maintainability:high | Add a nested comment explaining why this method is empty, throw an UnsupportedOperationException or complete the implementation. |
| 62 | `java:S1186` | maintainability:high | Add a nested comment explaining why this method is empty, throw an UnsupportedOperationException or complete the implementation. |

### `src/br/states/YouWinState.java` (5)

| Linha | Regra | Impacto | Mensagem |
| ---: | :--- | :--- | :--- |
| 13 | `java:S9438` | maintainability:medium/reliability:low | Make this private field "final", it is never reassigned. |
| 14 | `java:S1659` | maintainability:low | Declare "font2" on a separate line. |
| 17 | `java:S1450` | maintainability:low | Remove the "background" field and declare it as a local variable in the relevant methods. |
| 26 | `java:S1186` | maintainability:high | Add a nested comment explaining why this method is empty, throw an UnsupportedOperationException or complete the implementation. |
| 62 | `java:S1186` | maintainability:high | Add a nested comment explaining why this method is empty, throw an UnsupportedOperationException or complete the implementation. |

### `src/br/display/Background.java` (4)

| Linha | Regra | Impacto | Mensagem |
| ---: | :--- | :--- | :--- |
| 15 | `java:S9438` | maintainability:medium/reliability:low | Make this private field "final", it is never reassigned. |
| 16 | `java:S9438` | maintainability:medium/reliability:low | Make this private field "final", it is never reassigned. |
| 23 | `java:S106` | maintainability:medium | Replace this use of System.out by a logger. |
| 24 | `java:S4507` | security:low | Make sure this debug feature is deactivated before delivering the code in production. |

### `src/br/display/Display.java` (4)

| Linha | Regra | Impacto | Mensagem |
| ---: | :--- | :--- | :--- |
| 15 | `java:S9438` | maintainability:medium/reliability:low | Make this private field "final", it is never reassigned. |
| 16 | `java:S9438` | maintainability:medium/reliability:low | Make this private field "final", it is never reassigned. |
| 29 | `java:S3252` | maintainability:high | Use static access with "javax.swing.WindowConstants" for "EXIT_ON_CLOSE". |
| 49 | `java:S106` | maintainability:medium | Replace this use of System.out by a logger. |

### `src/br/states/HelpState.java` (4)

| Linha | Regra | Impacto | Mensagem |
| ---: | :--- | :--- | :--- |
| 13 | `java:S9438` | maintainability:medium/reliability:low | Make this private field "final", it is never reassigned. |
| 17 | `java:S1450` | maintainability:low | Remove the "background" field and declare it as a local variable in the relevant methods. |
| 25 | `java:S1186` | maintainability:high | Add a nested comment explaining why this method is empty, throw an UnsupportedOperationException or complete the implementation. |
| 59 | `java:S1186` | maintainability:high | Add a nested comment explaining why this method is empty, throw an UnsupportedOperationException or complete the implementation. |

### `src/test/java/br/GameTest.java` (4)

| Linha | Regra | Impacto | Mensagem |
| ---: | :--- | :--- | :--- |
| 8 | `java:S1128` | maintainability:low | Remove this unused import 'org.mockito.Mockito.verify'. |
| 88 | `java:S125` | maintainability:medium | This block of commented-out lines of code should be removed. |
| 89 | `java:S5785` | maintainability:medium | Use assertSame instead. |
| 139 | `java:S2925` | maintainability:medium | Remove this use of "Thread.sleep()". |

### `src/br/characters/Alien.java` (3)

| Linha | Regra | Impacto | Mensagem |
| ---: | :--- | :--- | :--- |
| 5 | `java:S1128` | maintainability:low | Remove this unused import 'java.awt.image.BufferedImage'. |
| 33 | `java:S106` | maintainability:medium | Replace this use of System.out by a logger. |
| 34 | `java:S4507` | security:low | Make sure this debug feature is deactivated before delivering the code in production. |

### `src/br/states/State.java` (2)

| Linha | Regra | Impacto | Mensagem |
| ---: | :--- | :--- | :--- |
| 9 | `java:S100` | maintainability:low | Rename this method name to match the regular expression '^[a-z][a-zA-Z0-9]*$'. |
| 10 | `java:S100` | maintainability:low | Rename this method name to match the regular expression '^[a-z][a-zA-Z0-9]*$'. |

### `src/br/audio/AudioPlayer.java` (1)

| Linha | Regra | Impacto | Mensagem |
| ---: | :--- | :--- | :--- |
| 35 | `java:S4507` | security:low | Make sure this debug feature is deactivated before delivering the code in production. |

### `src/br/characters/Character.java` (1)

| Linha | Regra | Impacto | Mensagem |
| ---: | :--- | :--- | :--- |
| 33 | `java:S1186` | maintainability:high | Add a nested comment explaining why this method is empty, throw an UnsupportedOperationException or complete the implementation. |

### `src/test/java/br/audio/AudioPlayerTest.java` (1)

| Linha | Regra | Impacto | Mensagem |
| ---: | :--- | :--- | :--- |
| 31 | `java:S1130` | maintainability:low | Remove the declaration of thrown exception 'java.lang.Exception', as it cannot be thrown from method's body. |

### `src/test/java/br/characters/ShipTest.java` (1)

| Linha | Regra | Impacto | Mensagem |
| ---: | :--- | :--- | :--- |
| 129 | `java:S2093` | maintainability:high | Change this "try" to a try-with-resources. |

