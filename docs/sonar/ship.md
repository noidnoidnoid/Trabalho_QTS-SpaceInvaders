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

Pendente.
