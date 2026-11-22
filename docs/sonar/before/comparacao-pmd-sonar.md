# Problemas por classe: PMD x SonarQube

Mesmo código (commit `bbb57f3`). PMD 7.7.0, conjunto `quickstart`; SonarQube Community Build 26.11, perfil Sonar way comprehensive.

Fontes: [`pmd-quickstart-bbb57f3.xml`](pmd-quickstart-bbb57f3.xml) (PMD) e [`sonar-antes-comprehensive.md`](sonar-antes-comprehensive.md) (Sonar).

| Classe | Dono | PMD | Sonar | Mesma linha |
| :--- | :--- | ---: | ---: | ---: |
| `Level1State` | Erick | 4 | 13 | 3 |
| `Game` | Leon | 6 | 12 | 3 |
| `Level2State` | Breno | 9 | 11 | 4 |
| `MenuState` | Roger | 5 | 7 | 5 |
| `Ship` | Luiz | 1 | 7 | 1 |
| `AudioPlayer` | Roger | 3 | 1 | 0 |
| `StateManager` |  | 1 | 8 |  |
| `Shot` |  | 2 | 7 |  |
| `KeyManager` |  | 4 | 6 |  |
| `GameOverState` |  | 4 | 5 |  |
| `YouWinState` |  | 4 | 5 |  |
| `Background` |  | 0 | 4 |  |
| `Display` |  | 0 | 4 |  |
| `HelpState` |  | 3 | 4 |  |
| `Alien` |  | 1 | 3 |  |
| `State` |  | 2 | 2 |  |
| `Character` |  | 1 | 1 |  |
| `Launch` |  | 1 | 0 |  |
| **Total (`src/br`)** | | **51** | **100** | |

"Mesma linha": problemas do PMD numa linha em que o Sonar também aponta algo (aproximação).
