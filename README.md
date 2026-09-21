# Trabalho Prático - Qualidade e Teste de Software

> **Disciplina:** Qualidade e Teste de Software  
> **Sistema Sob Teste (SUT):** Space Invaders (Java)  
> **Repositório Original:** [Upstream](https://github.com/repo-software-testing-courses/gameSpaceInvaders)  
> **Repositório Fork / Grupo:** [Trabalho](https://github.com/noidnoidnoid/Trabalho_QTS-SpaceInvaders)

---

## Equipe e Responsabilidades

| Integrante | Matrícula | Responsabilidade Principal | Classe Complexa Atribuída ($\text{CC} \ge 10$) |
| :--- | :--- | :--- | :--- |
| **Breno Carvalho** | 123083015 | `Testador` | `Level2State` |
| **Erick Bessa** | 122083063 | `Testador/Realizador dos slides` | `Level1State` |
| **Leon Stevans** | 123083047 | `Projetista de Teste/Testador` | `Game` |
| **Luiz Eduardo** | 123083019 | `Testador` | `Ship` |
| **Roger Egito** | 220083118 | `Testador` | `AudioPlayer` |

---

## Visão Geral do Sistema Sob Teste (SUT)

O projeto escolhido é o jogo **Space Invaders**, desenvolvido em Java com Arquitetura Orientada a Objetos e manipulação de multithreading para renderização contínua de gráficos em tempo real via Canvas/JFrame (`60 FPS`).

---

## Índice de Artefatos e Entregáveis

Todas as entregas estão organizadas na branch principal (`main` / `master`) conforme os requisitos da disciplina.

### Documentação Geral e Logs
* **Plano de Teste:** [Docs](https://docs.google.com/document/d/1kktI82aWBj7p4GrCMSzNBMWDEArWQh54ub-EJuK4X20/edit?usp=sharing)
* **Registro de Uso de IA:** [AI-LOG](docs/ai/AI-LOG.md)
* **Slides de Apresentação:** [Slides](https://docs.google.com/presentation/d/1qsA4xxGPf2P7VD_Pt7r_hr5ZSvUkgkzlxxyO3kw0Bk0/edit?usp=sharing)

---

### Entrega 1 (Peso 3) — *Prazo: 21/09/2026*
* [x] **Descrição do Escopo:** Incluído no documento do [Plano de Teste](https://docs.google.com/document/d/1kktI82aWBj7p4GrCMSzNBMWDEArWQh54ub-EJuK4X20/edit?usp=sharing).
* [x] **Código-fonte Original:** Preservado na estrutura inicial do repositório.
* [x] **Casos de Teste Unitários Iniciais:** Localizados em [`src/test/java/`](src/test/java/)
  * [x] `Game` - [`GameTest`](src/test/java/br/GameTest.java) · 5 casos (`start()`, `stop()`, `run()` e controle de thread via Reflection)
  * [x] `Ship` — [`ShipTest`](src/test/java/br/characters/ShipTest.java) · 32 casos · **100% de cobertura de arestas** · 89% de escore de mutação  
  * [x] `AudioPlayer`- [`AudioPlayerTest`](src/test/java/br/audio/AudioPlayerTest.java) · 5 casos (`play()`, `stop()` e isolamento da API Sound via Mock/Reflection)
  * [x] `Level1State` - [`Level1StateTest`](src/test/java/br/states/Level1StateTest.java) · 6 casos (`init()`, `update()`, colisão nave-alien e tiro-alien via Reflection)
  * [x] `Level2State` — [`Level2StateTest`](src/test/java/br/states/Level2StateTest.java) · 11 casos (`init()`, `update()`, `checarColisoes()` via `render()`) · cobertura/mutação: Entrega 2
* [ ] **Casos de Testes Manuais:**
  * [x] Cenário exportado do **TestLink** da classe `Game`: [`PDF`](docs/manual-tests/CT01-GameLoop-TestLink.pdf)
  * [x] Caso de teste de sistema da classe `Ship`: [`PDF`](docs/manual-tests/CT02-Ship-CasoDeTesteSistema.pdf) — 20 passos executados em 20/09/2026, 19 passaram e 1 falhou
  * [x] Caso de teste de sistema da classe `AudioPlayer`: [`PDF`](docs/manual-tests/CT04-AudioPlayer-CasoDeTesteSistema.pdf) — 5 passos executados em 21/09/2026, 4 passaram e 1 falhou
  * [x] Caso de teste de sistema da classe `Level1State`: [`PDF`](docs/manual-tests/CT05-Level1State-CasoDeTesteSistema.pdf) — 6 passos executados em 21/09/2026, todos passaram
  * [x] Caso de teste de sistema da classe `Level2State`: [`PDF`](docs/manual-tests/CT03-Level2State-CasoDeTesteSistema.pdf) — 7 passos executados em 21/09/2026, todos passaram
* [x] **Registro de Bugs / Bugs Tracking:** [Aba Issues do Repositório](../../issues?q=is%3Aissue)

---

### Entrega 2 (Peso 5) — *23/11/2026*
* [ ] **Testes Unitários Evoluídos & Testes de Integração:** Isolamento de dependências (`Mocks`/`Stubs`) em `src/test/java/...`.
* [ ] **Métricas de Qualidade ISO 25010:** Relatório e justificativas no [Plano de Teste](https://docs.google.com/document/d/).
* [ ] **Testes Automatizados de Sistema:** Requisitos funcionais e não-funcionais em `src/test/java/system/...`.
* [ ] **Cobertura Estrutural ($\ge 80\%$ Todas-Arestas):** Relatório JaCoCo/HTML disponível em [`docs/reports/coverage/`](docs/reports/coverage/).
* [ ] **Teste Baseado em Defeitos ($\ge 80\%$ Escore de Mutação):** Relatório Pitest disponível em [`docs/reports/mutation/`](docs/reports/mutation/).
* [ ] **Inspeção de Código (SonarQube/SonarCloud):**
  * Evidências antes das correções: [`docs/sonar/before/`](docs/sonar/before/)
  * Evidências após as correções: [`docs/sonar/after/`](docs/sonar/after/)

---

## Ferramentas Utilizadas

* **Linguagem:** Java (JDK 17+)
* **Build & Dependências:** Maven ([`pom.xml`](pom.xml) na raiz) — *a confirmar com o grupo*
* **Framework de Testes:** JUnit 5, Mockito, PIT Mutation Testing, JaCoCo
* **Automação de Sistema:** Selenium / Robot / TestNG
* **Análise Estática:** SonarQube / SonarCloud
* **Gestão de Testes Manuais:** TestLink

---

## Como Executar

Requer **JDK 17+** e **Maven**. Todos os comandos rodam a partir da raiz do repositório —
o SUT carrega as imagens com caminhos relativos (`new File("src/*.png")`), então o
diretório de trabalho importa.

```bash
mvn clean test                                    # executa a suíte de testes
mvn clean test jacoco:report                      # + relatório de cobertura
mvn test-compile org.pitest:pitest-maven:mutationCoverage   # teste de mutação
mvn compile exec:java -Dexec.mainClass=br.Launch  # abre o jogo
```

### Relatórios

Os relatórios são **regerados a cada build** e saem em `target/`, que não é versionado —
HTML gerado em repositório compartilhado só produz conflito de merge.

| Relatório | Caminho gerado |
| :--- | :--- |
| Cobertura (JaCoCo) | `target/site/jacoco/index.html` |
| Mutação (PIT) | `target/pit-reports/index.html` |

Na **hora de entregar**, copiar para os caminhos que este README linka como artefatos:

```bash
mkdir -p docs/reports && rm -rf docs/reports/coverage docs/reports/mutation
cp -r target/site/jacoco docs/reports/coverage
cp -r target/pit-reports docs/reports/mutation
```

### Notas sobre o build

* **Encoding misto.** A maior parte dos `.java` está em **ISO-8859-1**, mas
  `Display.java` e `Background.java` foram convertidos para UTF-8. O `pom.xml` declara
  ISO-8859-1, que é o único valor sob o qual tudo compila — trocar para UTF-8 quebra os
  demais arquivos. Vale padronizar o conjunto em algum momento.
* Os testes rodam em modo *headless* e desenham em `BufferedImage`, sem abrir janela.
* Para adicionar os testes das demais classes, basta criar os arquivos em
  `src/test/java/br/...`. Nenhuma alteração no `pom.xml` é necessária.
