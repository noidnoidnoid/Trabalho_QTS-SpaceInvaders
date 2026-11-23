# Registro do Uso de Inteligência Artificial Generativa (AI-LOG)

Este documento registra de forma transparente e auditável as interações substanciais com ferramentas de Inteligência Artificial Generativa (ex: Gemini, ChatGPT, Claude, Copilot) durante o desenvolvimento do trabalho prático da disciplina de **Qualidade e Teste de Software**.

---

## Histórico de Interações

### Interação #001 — Análise do Edital e Estruturação Inicial do Projeto
* **Data:** 19/09/2026
* **Responsável:** Leon
* **Atividade:** Planejamento inicial, mapeamento do plano de ação do trabalho e criação da estrutura base do repositório (Passo 1).
* **Ferramenta:** Gemini
* **Prompt/Instrução Utilizada:**
  > *"Analise o documento do Trabalho de Qualidade e Teste de software e crie um plano de desenvolvimento do trabalho, passo a passo e em ordem de execução*
* **Resultado:** 
  1. Criação do plano de execução ordenado em 4 fases cobrindo as entregas 1 e 2.
  2. Geração do template estruturado do arquivo `README.md` com índices diretos e tabela de responsabilidades dos 5 integrantes.
  3. Estruturação do documento de histórico `docs/ai/AI-LOG.md` e geração do primeiro registro.
* **Decisão:** Aceito integralmente. O formato e os campos atendem às exigências do edital da disciplina.
* **Validação:** Verificado manualmente se a estrutura criada atende aos requisitos de visibilidade dos artefatos na branch principal, regras de transparência de IA e suporte ao rastreamento de links dos documentos no Google Docs.

### Interação #002 — Criação do Cenário de Teste Manual (TestLink) para a classe Game
* **Data:** 19/09/2026
* **Responsável:** Leon
* **Atividade:** Escrita estruturada do Plano, Suíte e Casos de Teste manuais no TestLink focados no Game Loop e Multithreading.
* **Ferramenta:** Gemini
* **Prompt/Instrução Utilizada:**
  > *"Eu fiquei com a classe Game e vou usá-la pra documentar o cenário de teste no Testlink. Eu já criei o projeto, o plano e a suíte. Me ajude com os passos do caso de teste."*
* **Resultado:** Geração da árvore hierárquica exigida pelo TestLink (Plano de Execução, Suíte ST01 e Caso de Teste CT01.01). Foram criados passos de execução focando na validação do frame rate de 60 FPS, gerenciamento de threads (start/join) e renderização dos gráficos (BufferStrategy).
* **Decisão:** Aceito sem alterações. Os passos gerados refletem fielmente a lógica interna da classe Game.java para um teste de caixa-preta / sistema.
* **Validação:** As informações foram inseridas na interface do TestLink e a formatação de pré-condições e resultados esperados se comportou adequadamente na geração do relatório em PDF da ferramenta.

### Interação #003 — Infraestrutura de Teste e Suíte Unitária da Classe `Ship`
* **Data:** 19/09/2026 a 20/09/2026
* **Responsável:** Luiz Eduardo
* **Atividade:** Configuração do build de testes do projeto (Passo 2) e criação dos casos de teste unitários da classe complexa `Ship`. Levantamento inicial do registro de bugs e dos casos de teste manuais da mesma classe.
* **Ferramenta:** Claude (Claude Code)
* **Prompt/Instrução Utilizada:** Sessão iterativa, com as seguintes instruções na ordem em que foram dadas:
  1. Analisar o repositório do trabalho e levantar o que precisa ser feito nele.
  2. Restringir o escopo à classe `Ship`, atribuída a este integrante, mais a infraestrutura de teste reaproveitável pelo grupo — recusada a proposta da ferramenta de cobrir as cinco classes complexas de uma vez.
  3. Não modificar o SUT: escrever apenas os testes, deixando a refatoração e a correção dos defeitos para etapa posterior.
  4. Priorizar os itens da Entrega 1.
  5. Justificar os 75 arquivos HTML que haviam sido gerados em `docs/reports/` e, diante da explicação, mover a saída dos relatórios para `target/`, evitando conflito de merge entre os cinco integrantes.
* **Resultado:**
  1. `pom.xml` com JUnit 5, Mockito, JaCoCo e PIT, apontando o Maven para a estrutura de diretórios já existente — nenhum arquivo do código original foi movido ou alterado.
  2. `src/test/java/br/characters/ShipTest.java` com 32 casos, baseados em análise de valor limite.
  3. Levantamento de 13 defeitos do SUT para abertura na aba Issues, e o caso de teste de sistema da classe `Ship` em `docs/manual-tests/CT02-Ship-CasoDeTesteSistema.pdf`, no template da disciplina.
* **Decisão:** **Aceito com alterações.** Corrigidos dois defeitos de configuração do build (exclusão do `junit` transitivo do `jlayer` e uso de `@{argLine}` no JaCoCo) e reforçada a suíte em duas iterações: cobertura de linhas de 90% para 100% e escore de mutação de 83% para 89%. Os dois mutantes restantes são **equivalentes por construção** em `limits()`, onde o valor de correção coincide com o de fronteira, de modo que nenhum teste pode distingui-los.
* **Validação:** `mvn clean test` com 32 testes e 0 falhas, reexecutado em ordem aleatória para comprovar o isolamento entre os casos. JaCoCo: **100% de arestas** em `Ship`, contra 4% do projeto inteiro. PIT: **89%**, acima do limiar de 80% exigido na Entrega 2. `git status src/br` não acusa modificação, confirmando a preservação do código original. A execução do jogo revelou um defeito pré-existente que impede a inicialização em máquina sem placa de som, registrado nas Issues.

### Interação #004 — Suíte Unitária da Classe `Level2State` e Validação Real do Build
* **Data:** 21/09/2026
* **Responsável:** Breno Carvalho
* **Atividade:** Criação dos casos de teste unitários da classe `Level2State` (Passo 2, Entrega 1) e execução real do build Maven do projeto, até então nunca rodado de fato nesta máquina.
* **Ferramenta:** Claude (Claude Code)
* **Prompt/Instrução Utilizada:** Sessão iterativa. Um primeiro rascunho do SUT ajustado (`Level2State.java` com os campos `ship`/`shots`/`aliens`/`victory`/`gameOver` rebaixados de `private` para acesso de pacote, no mesmo padrão de testabilidade do `ShipTest`), da suíte `Level2StateTest.java` e do roteiro de teste de sistema `CT03-Level2State-CasoDeTesteSistema.md` havia sido produzido numa conversa anterior, sem que o Maven fosse instalado ou o build fosse de fato executado naquele momento. Nesta sessão, a instrução foi:
  1. Rodar `mvn clean test` e confirmar que `Level2StateTest` passa de verdade.
  2. Corrigir e explicar qualquer falha encontrada.
  3. Não avançar para isolamento de dependências, mutação (PIT) ou cobertura de 80% — escopo da Entrega 2.
* **Resultado:**
  1. `src/test/java/br/states/Level2StateTest.java` com 11 casos, cobrindo `init()` (criação e posicionamento dos 8 aliens, estado inicial de `victory`/`gameOver`, compartilhamento da lista de tiros com a nave), `update()` (movimento via teclas A/D, disparo via space, ausência de efeito sem teclas) e `checarColisoes()` via `render()` (derrota por colisão nave-alien, colisão tiro-alien, vitória sem aliens restantes).
  2. Constatação de que nem o JDK 17 nem o Maven estavam instalados na máquina usada para validar — o ambiente só tinha um JRE 8 antigo — e que a rede local faz inspeção SSL, o que impedia o Maven de baixar dependências do Maven Central mesmo depois do JDK 17 instalado (`PKIX path building failed`).
  3. Passo a passo de instalação do JDK 17 (Temurin) e do Maven, e da importação do certificado raiz da rede no `cacerts` do JDK 17 via `keytool`, para viabilizar o download das dependências.
* **Decisão:** **Aceito sem alterações no SUT.** O build real não acusou nenhuma falha nos 11 casos de `Level2State` nem nos 32 já existentes de `Ship` — não foi necessário corrigir nada no código de teste ou no SUT além do que já havia sido feito no rascunho anterior.
* **Validação:** `mvn clean test` executado de fato (não apenas planejado) após a correção do ambiente: **BUILD SUCCESS**, `Tests run: 43, Failures: 0, Errors: 0, Skipped: 0` (32 de `ShipTest` + 11 de `Level2StateTest`). Isso substitui e corrige o registro da conversa anterior, que havia deixado o Passo 2 da classe `Level2State` como "sem execução real de Maven" — a suíte agora está validada por execução real do build, e não apenas por inspeção de código.

### Interação #005 — Suíte Unitária da Classe Game com Reflexão
* **Data:** 21/09/2026
* **Responsável:** Leon Stevans
* **Atividade:** Criação dos casos de teste unitários da classe `Game` utilizando JUnit 5 e Java Reflection.
* **Ferramenta:** Gemini
* **Prompt/Instrução Utilizada:**
  > *"Eu quero fazer o caso de teste unitário da classe Game."*
* **Resultado:** Geração da suíte `src/test/java/br/GameTest.java`. Foram testados os ciclos de controle de Thread (`start()` e `stop()`), bem como a lógica de `return` caso a thread já estivesse rodando. Para testar o Game Loop de 60FPS (`run()`), foi criada uma estratégia assíncrona (thread desarmadora) para evitar *deadlocks* do Test Runner e utilizar a anotação `@Timeout(2)`. 
* **Decisão:** Aceito sem alterações. Diferente da classe Ship e Level2State em que foi modificado o SUT para abrir acessos *package-private*, a decisão de design de testes dessa vez foi usar *Java Reflection* (`Field.setAccessible(true)`), mantendo todas as variáveis (`thread`, `running`) como `private` no SUT sem ferir o encapsulamento, garantindo total isolamento da lógica testada.
* **Validação:** Rodado via `mvn clean test` com sucesso, resultando no *BUILD SUCCESS* de toda a suíte somada as outras já em operação (agora incluindo Game, Ship e Level2State).

### Interação #006 — Suíte Unitária da Classe AudioPlayer
* **Data:** 21/09/2026
* **Responsável:** Leon Stevans
* **Atividade:** Desenvolvimento dos testes unitários das classes `AudioPlayer`.
* **Ferramenta:** Gemini
* **Prompt/Instrução Utilizada:**
  > *"Agora falta os testes unitários do AudioPlayer"*
* **Resultado:** `AudioPlayerTest.java`: Criado usando *Reflection* para injetar um *Mock* da classe `Clip` nativa da API Java Sound, permitindo cobertura sem depender de arquivos físicos ou hardware de áudio.
* **Decisão:** Aceito integralmente. As estratégias de `mock` e *Reflection* protegem o SUT de refatorações proibidas nesta fase do trabalho.
* **Validação:** Compilado e testado pelo grupo. Testes manuais formatados para PDF e salvos em `docs/manual-tests/`. Todos os 5 integrantes agora possuem suas classes complexas analisadas e com casos de teste gerados, cumprindo os critérios absolutos da Entrega 1 da disciplina.

### Interação #007 — Suíte Unitária da Classe Level1State
* **Data:** 21/09/2026
* **Responsável:** Leon Stevans
* **Atividade:** Desenvolvimento dos testes unitários das classes `Level1State`.
* **Ferramenta:** Gemini
* **Prompt/Instrução Utilizada:**
  > *"Agora falta os testes unitários do AudioPlayer"*
* **Resultado:** `Level1StateTest.java` Criado usando *Reflection* (como em `GameTest`) para contornar a visibilidade `private` dos campos, com 100% de cobertura nos métodos focais de renderização e colisão.
* **Decisão:** Aceito integralmente. As estratégias de `mock` e *Reflection* protegem o SUT de refatorações proibidas nesta fase do trabalho.
* **Validação:** Compilado e testado pelo grupo. Testes manuais formatados para PDF e salvos em `docs/manual-tests/`. Todos os 5 integrantes agora possuem suas classes complexas analisadas e com casos de teste gerados, cumprindo os critérios absolutos da Entrega 1 da disciplina.

### Interação #008: Planejamento da Entrega 2 do Roger
* **Data:** 22/11/2026
* **Responsável:** Roger Egito
* **Atividade:** Plano para os itens assumidos: unitários com isolamento, integração e sistema (funcional e não funcional).
* **Ferramenta:** Claude (Claude Code, modo de planejamento)
* **Prompt/Instrução Utilizada:**
  > *"Quero fazer uma boa Entrega 2. Disse pro meu grupo então que eu faria [...] Então, como faremos isso?"* Seguido de: *"MenuState parece interessante, mas estou ferrando outra pessoa fazendo isso? Precisamos alinhar isso antes de eu seguir."*, *"Faça a refatoração mínima, mas se tiver como documentar isso no lugar apropriado acho que seria bom."* e *"Talvez tenha uma maneira de conseguirmos usar o Selenium nesse projeto?"*
* **Resultado:** Plano em etapas. Descoberta de que a suíte herdada de `AudioPlayer` nunca passou. Análise do código da GenIA-E2ETest mostrando que ela exige HTML. Proposta de camada web para usar Selenium.
* **Decisão:** Alterado. Roger exigiu alinhar a troca para `MenuState` com o grupo antes de começar (o grupo confirmou), trocou a proposta inicial de `java.awt.Robot` por camada web + Selenium e pediu que as mudanças no SUT fossem documentadas (`docs/alteracoes-no-sut.md`).
* **Validação:** `mvn clean test` executado na `main`: 59 testes, 10 erros (`docs/ai/roger/evidencias/2026-11-22-baseline-mvn-test.txt`).

### Interação #009: AudioPlayer e MenuState: defeitos, correção e suítes isoladas
* **Data:** 22/11/2026
* **Responsável:** Roger Egito
* **Atividade:** Testes unitários com isolamento de dependências (`mockStatic`, `mockConstruction`, injeção pelo construtor).
* **Ferramenta:** Claude (Claude Code)
* **Prompt/Instrução Utilizada:** Execução do plano aprovado, no fluxo "a IA gera, o Roger revisa".
* **Resultado:** 3 defeitos de `AudioPlayer` confirmados por testes que falharam contra o código original (#2, #3, #4), correção mínima e suíte com 21 casos. `MenuState` com construtor injetável e suíte com 33 casos (29 depois da revisão do Roger).
* **Decisão:** Rascunhos da IA preservados em `docs/ai/roger/drafts/`. Mudanças entre rascunho e versão final descritas em `docs/ai/roger/revisao.md`. **Alterado pelo Roger na suíte de `MenuState`:** removeu os testes de cor, fonte e posição exatas (decisões de arte, que quebrariam o teste sem defeito) e o teste do `KeyPressed()` vazio. O corte puro deixava o escore em 79,2% e não pegava 2 erros reais (destaque invertido e menu de cabeça para baixo), então foram acrescentadas 2 verificações de comportamento que não fixam cor nem posição. O teste da tecla sem função foi mantido como caso da classe inválida. Nas demais suítes não pediu mudanças. Detalhes em `docs/ai/roger/revisao.md`, seção 5.
* **Validação:** PIT `AudioPlayer` 78,6% na rodada 1 e 100% (13/13) na rodada 2. PIT `MenuState`: 93,8% e 97,9% nas rodadas da IA, e 83,3% bruto (100% ajustado, com os 7 sobreviventes de arte classificados como equivalentes em relação à especificação) na versão final revisada. JaCoCo: arestas 26/26 em `MenuState` e 8/8 em `AudioPlayer` (9/10 depois da correção do PMD, porque o try-with-resources ganhou uma aresta gerada pelo compilador e inalcançável), complexidade ciclomática de `MenuState` = 24.

### Interação #010: Testes de integração, camada web e testes de sistema com Selenium
* **Data:** 22/11/2026
* **Responsável:** Roger Egito
* **Atividade:** 22 casos de integração (failsafe), adaptador web (`src/br/web/`) e 11 casos de sistema com Selenium (RF01 a RF07, RNF01, RNF01b, RNF02).
* **Ferramenta:** Claude (Claude Code)
* **Prompt/Instrução Utilizada:** *"Corrigir faz parte do trabalho? Se sim, corrija agora. Lembre-se de [...] documentar tudo corretamente, incluindo issues."*
* **Resultado:** Defeitos encontrados e registrados nas issues #5 a #12. Correções de `Shot` (#8, #9), `Background` (#11) e `Alien`/`Shot` (#12) com a nova classe `Sprites`.
* **Decisão:** Erros da própria IA corrigidos depois de detectados por teste ou medição (lista em `docs/ai/roger/revisao.md`, seção 4).
* **Validação:** CTI-18 e CTI-19 falharam antes das correções e passam depois. Frame normal da fase 2 caiu de 17 a 28 ms para 3 a 5 ms depois da #11. Suíte de sistema: com o critério estrito, o CTS-10 falhou em 2 de 3 rodadas por picos isolados (41,9 e 33,6 ms). O GC foi descartado por medição (maior pausa de 4,6 ms), e a causa provável, disputa de CPU com o navegador, não foi comprovada (`docs/ai/roger/evidencias/2026-11-22-investigacao-cts10.txt`). Por decisão do Roger, o critério foi revisto para mediana <= 16,7 ms e no máximo 1 segundo acima de 33,3 ms. Esse critério ainda reprova o código antigo. Execuções finais: 3 de 3 rodadas com todos os casos passando (`docs/evidencias/sistema/execucoes-finais/`). Frame mais lento por segundo entre 2,4 e 13,5 ms, média de 60,5 a 60,8 FPS, pior resposta a tecla de 82 a 114 ms.

### Interação #011: Inspeção de código com PMD e texto do Plano de Teste
* **Data:** 22/11/2026
* **Responsável:** Roger Egito
* **Atividade:** Relatório de inspeção do código-fonte e correção dos problemas na classe complexa (`MenuState`) e no `AudioPlayer`. Texto das seções do Roger para o Plano de Teste.
* **Ferramenta:** Claude (Claude Code)
* **Prompt/Instrução Utilizada:**
  > *"Faça um sonar então, prepare o texto para eu copiar e colar no Docs, e me diga em qual Docs eu deveria colar, e aonde."* Depois: *"Sonar é um exemplo. Talvez tenha outro que possamos usar que não precise de Docker? [...] O que for mais fácil."*
* **Resultado:** O Docker não estava instalado de fato, então a IA propôs o PMD, que roda como plugin do Maven. Relatório antes: 5 problemas na `MenuState` e 3 no `AudioPlayer`. Correções feitas nas duas classes e no código da camada web gerado pela IA. Texto do Plano de Teste em `docs/plano-de-teste/colar-no-google-docs-roger.html`, com o local de cada bloco no documento.
* **Decisão:** Roger escolheu a ferramenta pelo critério de facilidade (PMD no lugar do Sonar). Os nomes `KeyPressed` e `KeyReleased` (interface `State`) ficaram sem correção porque a mudança atingiria as classes dos outros integrantes. Os avisos de `volatile` e de IP fixo no código da camada web foram mantidos com justificativa (`docs/inspecao/LEIA-ME.md`).
* **Validação:** PMD depois das correções: nenhum problema nas duas classes (print em `docs/inspecao/depois/`). Total do projeto de 64 para 47 problemas. Suítes da `MenuState` e do `AudioPlayer`, integração e sistema executadas de novo depois das correções.

### Interação #012 — Correção de Execução Headless em `GameTest` (Issue #7)
* **Data:** 22/11/2026
* **Responsável:** Leon Stevans
* **Atividade:** Diagnóstico e correção da falha por `java.awt.HeadlessException` e dependências gráficas nos testes unitários da classe `br.Game` (Issue #7).
* **Ferramenta:** Gemini
* **Prompt/Instrução Utilizada:**
  > *"Atualize o GameTest.java para isolar Display e dependências gráficas em ambiente headless sem alterar Game.java, resolvendo a Issue #7."*
* **Resultado:** Isolamento de `br.display.Display` via `Mockito.mockConstruction` fornecendo `Canvas`, `BufferStrategy`, `Graphics` e `FontMetrics` simulados, além de limpeza controlada da thread e dos mocks no `@AfterEach` e estabilização dos testes de `stop()` e `run()`.
* **Decisão:** **Aceito sem alterações.** O código de produção (`src/br/Game.java`) permaneceu intacto e o encoding `ISO-8859-1` do projeto foi preservado.
* **Validação:** Execução de `mvn test -Dtest=GameTest` concluída com `BUILD SUCCESS` (`Tests run: 5, Failures: 0, Errors: 0, Skipped: 0`).

### Interação #013 — Inspeção de Código da Classe `Ship` com SonarQube ("antes")
* **Data:** 22/11/2026
* **Responsável:** Luiz Eduardo
* **Atividade:** Configuração do SonarQube local, escolha do perfil de regras, análise do projeto e registro do "antes" da inspeção de código (item 6 da Entrega 2).
* **Ferramenta:** Claude (Claude Code)
* **Prompt/Instrução Utilizada:**
  > *"Monte a inspeção de código da classe Ship com o Sonar: suba o servidor, rode a análise e gere os prints e relatórios do 'antes', sem alterar o código do jogo."*

  (síntese das instruções dadas ao longo da sessão)
* **Resultado:** SonarQube Community 26.11 rodando localmente, análise do commit `bbb57f3` com o perfil Sonar way comprehensive, 10 prints e relatórios em `docs/sonar/before/`, plugin SonarQube for IDE no Eclipse em modo conectado e comparação com o PMD do grupo (51 problemas no PMD contra 100 no Sonar, no mesmo código).
* **Decisão:** **Aceito com alterações.** O Luiz escolheu o perfil comprehensive (o padrão deixava três classes sem nenhum problema), definiu o Sonar como base e o PMD do Roger como complemento, pediu textos mais curtos e tirou da entrega o relatório do perfil padrão e dois scripts.
* **Validação:** Contagens dos prints conferidas contra a API do servidor. PMD reexecutado com a configuração do Roger, reproduzindo os 47 problemas do relatório dele. Varredura sem senhas, tokens ou caminhos locais, e todos os links conferidos. Erros da ferramenta encontrados e corrigidos na sessão: a estimativa inicial de complexidade da `Ship` (6) estava errada, porque JaCoCo e Sonar dão 14; o aviso "analysis has warnings" foi primeiro atribuído à configuração de codificação, mas vinha de um defeito real em `Background.java:23`; os primeiros prints saíram cortados e um deles mostrava um token, e foram refeitos.

### Interação #014 — Correção das Issues da Classe `Ship` e Registro do "depois"
* **Data:** 22/11/2026
* **Responsável:** Luiz Eduardo
* **Atividade:** Correção das 7 issues do Sonar na `Ship`, ajuste do teste afetado e registro do "depois" da inspeção (item 6 da Entrega 2).
* **Ferramenta:** Claude (Claude Code)
* **Prompt/Instrução Utilizada:**
  > *"Corrija as 7 issues da Ship sem mudar o comportamento, prove com testes, cobertura e mutação que nada quebrou e deixe tudo na staging para eu revisar antes do commit."*

  (síntese das instruções dadas ao longo da sessão)
* **Resultado:** `Ship.java` com o import sem uso removido, `java.util.logging` no lugar de `System.out` e `printStackTrace()`, a constante `MOVEX`, a lista `shots` como `final` e sem código comentado. `ShipTest` passando a verificar o aviso no log, e não mais o terminal. Prints e resultado em `docs/sonar/after/` e `docs/sonar/ship.md`.
* **Decisão:** **Aceito sem alterações no código.** Na revisão, o Luiz jogou o jogo para conferir a nave e pediu que esse teste manual fosse registrado no `ship.md`. A versão anterior e a nova do teste estão no histórico do Git (commits `d85b947` e `7f41f71`).
* **Validação:** Sonar de 7 para 0 issues (as 7 marcadas como corrigidas no servidor) e PMD de 1 para 0. 32 testes da `Ship` passando, arestas 8/8 e mutação 14/16 (87,5%), com os 2 sobreviventes equivalentes. `mvn test` com 156/156 depois de integrar a correção do Leon. Encoding `ISO-8859-1` preservado. Antes de editar, a ferramenta verificou que deixar `movex` só como `final` violaria a regra `java:S1170`, por isso ele virou uma constante.

### Interação #015 — Evolução da Suíte `GameTest` (Funcional, Estrutural e Mutação)
* **Data:** 22/11/2026
* **Responsável:** Leon Stevans
* **Atividade:** Expansão dos casos de teste de `br.Game` para cobrir as técnicas funcional, estrutural ($\ge 80\%$ todas-arestas) e baseada em defeitos ($\ge 80\%$ de mutação no PIT)[cite: 14, 18].
* **Ferramenta:** Gemini
* **Prompt/Instrução Utilizada:**
  > *"Analise os relatórios do JaCoCo e do PIT gerados para a classe br.Game e indique quais asserções adicionar em GameTest.java para superar 80% de cobertura de mutação sem alterar o SUT."*
* **Resultado:**
  1. Cobertura estrutural no JaCoCo alcançou **100% de instruções (154/154)** e **100% de arestas/branches (14/14)** cobrindo `Game()`, `start()`, `stop()`, `run()`, `update()` e `render()`.
  2. Configuração de `br.Game` e `br.GameTest*` no `pitest-maven` dentro do `pom.xml`.
  3. Reforço das asserções para registro dos dois `KeyListener` (`sm` e `km`), chamada de `render()` e `stop()` no game loop, `join()` e tratamento de `InterruptedException`, elevando o escore de mutação de `br.Game` de 77% (23/30) para **93% (28/30)**[cite: 21, 23, 24].
* **Decisão:** **Aceito sem alterações.** O único mutante remanescente (`changed conditional boundary` em `delta >= 1` na linha 46) foi classificado como equivalente/não determinístico em aritmética contínua de nanossegundos (`System.nanoTime()`).
* **Validação:** Execução de `mvn clean test jacoco:report` (100% branches) e `mvn test-compile org.pitest:pitest-maven:mutationCoverage` (93% mutação em `br.Game`, 89% no projeto geral).

## Interação #016 — 22/11/2026
* **Responsável:** Leon Stevans (`br.Game`)
* **Ferramenta de IA:** Gemini
* **Atividade:** Análise de pendências da Entrega 2, validação da inspeção estática via PMD (`pmd.xml`) e preenchimento da Seção 2.6 do Plano de Teste.
* **Prompt / Solicitação:** Verificação dos itens pendentes da classe `br.Game` para a Entrega 2, orientação para exportação dos relatórios do PIT, análise dos apontamentos gerados pelo plugin `maven-pmd-plugin` em `target/pmd.xml` e apoio na redação do cronograma da Seção 2.6 do Plano de Teste.
* **Saída Gerada:** Checklist de fechamento da Entrega 2, comandos de cópia dos relatórios HTML do PIT (`docs/reports/mutation/`), classificação das 6 violações de estilo reportadas pelo PMD em `Game.java` e tabela de atividades, entregas e estimativas da Seção 2.6.
* **Validação e Ação Tomada:** O relatório `target/pmd.xml` foi executado e inspecionado manualmente, confirmando que os 6 apontamentos (`OneDeclarationPerLine`, `LocalVariableNamingConventions`, `UncommentedEmptyMethodBody` e `ControlStatementBraces`) são apenas regras cosméticas/de estilo do código legado original (`Game.java`), o qual foi mantido inalterado conforme a estratégia de isolamento da suíte `GameTest`. A Seção 2.5, a Seção 2.6 e o histórico de versões (v1.3) foram incorporados ao Plano de Teste e documentados no `README.md`.

### Interação #017 — Medidas das características de qualidade da ISO/IEC 25010:2023
* **Data:** 22/11/2026
* **Responsável:** Breno Carvalho
* **Atividade:** Indicação das medidas de cada atributo de qualidade da ISO 25010, em escala de 0 a 100%, com a justificativa de cada medida (item da Entrega 2).
* **Ferramenta:** Claude (app Claude, com acesso à pasta local do projeto)
* **Prompt/Instrução Utilizada:**
  > *"Por meio da análise do projeto de Space Invaders disponível na pasta do projeto, e do comando da segunda parte do trabalho, indique as medidas de cada atributo de qualidade da ISO 25010 2023 seguindo uma escala de porcentagem (0 a 100). Registre as contribuições substanciais da minha parte em docs/ai/AI-LOG.md. Faça perguntas para reforçar o seu entendimento, além disso, não efetue nenhuma implementação sem antes eu revisar."*

  Respostas do Breno às perguntas da ferramenta, na ordem: medir antes (commit `bbb57f3`) e depois; usar medidas no formato da ISO/IEC 25023 (X = A/B); entregar em Markdown no repositório; registrar no AI-LOG só esta medição (a correção da #10 foi acrescentada depois, na #018); recuperabilidade como "não se aplica"; o "depois" mede a versão web do jogo; registrar a limitação de comparar produtos diferentes.
* **Resultado:** A ferramenta leu o enunciado, o código nas duas versões, os relatórios de JaCoCo, PIT, Sonar e PMD, os casos de teste manuais (CT02 a CT05), as issues #1 a #12 e as evidências de sistema. Com isso, propôs uma medida A/B para cada subcaracterística aplicável e a média por característica. A nota geral foi ponderada pelos níveis de importância da análise feita pelo Breno em 13/11: 49% antes e 83% depois. Documento em [`docs/qualidade/iso25010-medidas.md`](../qualidade/iso25010-medidas.md).
* **Decisão:** **Alterado** pelo Breno em duas rodadas de revisão, antes de qualquer arquivo ser gravado:
  1. Recuperabilidade: a ferramenta perguntou se deveria ser 0% (não há mecanismo de recuperação) ou "não se aplica". O Breno escolheu "não se aplica", porque o jogo não guarda dados.
  2. Na primeira proposta, o "depois" media o jogo desktop. O Breno definiu que o "depois" deveria medir a versão web, que ele considera o novo modelo do jogo. Com isso a ferramenta refez Capacidade de interação, Compatibilidade e Flexibilidade, e a nota geral passou de 76% para 83%.
  3. O Breno aceitou incluir no documento a limitação de que o "antes" (desktop) e o "depois" (web) são produtos diferentes, com a nota que o desktop teria no "depois" (75%).
  4. A análise mostrou que a issue #10 ainda estava aberta na classe do Breno (`Level2State`), e ele decidiu corrigi-la (interação #018).
* **Validação:** Cada A e B foi conferido na fonte: `jacoco.csv` (146/264 arestas), `mutations` do PIT (89%), [`sonar-antes-comprehensive.md`](../sonar/before/sonar-antes-comprehensive.md) e o print do Sonar depois das correções, os relatórios do PMD, a API de issues do GitHub (consultada em 22/11/2026), os XML das 3 execuções finais de sistema e o `desempenho.txt`. As médias e a nota ponderada foram recalculadas por script. A contagem de classes com estado `static` mutável foi feita por script nas duas versões (3 de 18 e 3 de 25). Erros que a ferramenta encontrou e corrigiu na própria análise: o relatório "antes" do PMD já incluía a camada web, então não servia como "antes" e foi trocado pelo Sonar; a #1 já estava fechada no `bbb57f3` e saiu da contagem de defeitos; o efeito de medir o desktop no "depois" foi recalculado (75%, não os 78% da primeira estimativa).

### Interação #018 — Correção da issue #10 no `Level2State`
* **Data:** 22/11/2026
* **Responsável:** Breno Carvalho
* **Atividade:** Correção do defeito dos laços de remoção da classe `Level2State` (issue #10), com testes que falham antes e passam depois.
* **Ferramenta:** Claude (app Claude, com acesso à pasta local do projeto)
* **Prompt/Instrução Utilizada:**
  > *"Corrija o defeito existente na classe"*, seguido de *"Não efetue nenhum commit, apenas implemente"*.

  Respostas do Breno às perguntas da ferramenta: corrigir só o `Level2State` (o `Level1State` é do Erick); usar `removeIf`; escrever primeiro um teste que falha com o código atual e guardar as evidências em `docs/ai/breno/`; validar no ambiente da ferramenta e com `mvn clean test` na máquina do Breno.
* **Resultado:** Os laços de aliens e de tiros de `Level2State.render()` passaram a remover os invisíveis com `removeIf` antes de percorrer a lista. O `Level2StateTest` ganhou a classe aninhada `RemocaoDeInvisiveis`, com 3 casos: dois aliens invisíveis seguidos saem no mesmo frame, a vitória vem no mesmo frame em que todos os aliens somem, e o tiro seguinte a um tiro removido continua subindo. Rascunho preservado em [`docs/ai/breno/drafts/`](breno/drafts/Level2State-issue10.v1-claude.java).
* **Decisão:** **Correção aceita sem alterações; testes alterados depois da primeira execução.** Antes de aplicar, o Breno revisou o diff e os testes e definiu o escopo e a forma da correção. Quando ele rodou a suíte completa na máquina dele, que tem placa de som, a JVM dos testes ficou sem memória. A ferramenta identificou a causa provável: cada `new StateManager()` carrega a música tema (~51 MB) e nunca a libera. O Breno aprovou trocar, nas duas classes aninhadas do `Level2StateTest`, o `new StateManager()` por estados falsos (`usarEstadosFalsos()`, com `restaurarEstados()` no `@AfterEach`). O Breno decidiu não tratar a falha do `AudioPlayerTest` no JDK 23. A issue #10 continua aberta por causa do `Level1State`.
* **Validação:** O Maven não pôde ser usado no ambiente da ferramenta, porque a rede bloqueia o Maven Central. A prova foi feita com o código compilado por `javac` (JDK 21, `--release 17`) e um programa sem JUnit com os mesmos 3 casos. Os 3 falharam com o código da `main` (7 aliens restantes, vitória atrasada, tiro parado em y = 930) e passaram depois da correção. Em seguida, o próprio `Level2StateTest.java` foi compilado com substitutos mínimos do JUnit e do Mockito, e os 3 métodos novos falharam com o código antigo e passaram com o corrigido ([`2026-11-22-defeito-10-level2state.txt`](breno/evidencias/2026-11-22-defeito-10-level2state.txt)). Na máquina do Breno, `mvn test -Dtest=Level2StateTest` deu **14 testes, 0 falhas, BUILD SUCCESS** ([print](breno/evidencias/2026-11-22-mvn-test-level2statetest.png)). A suíte completa (`mvn clean test`) ainda não passa por dois motivos alheios à correção, descritos na evidência: falta de memória causada pela música tema carregada a cada `new StateManager()` com placa de som, e o `AudioPlayerTest.assertNoWarning`, que falha no JDK 23 por causa de um aviso do Mockito.

### Interação #019 — Inspeção de código da classe `Level2State` com PMD
* **Data:** 22/11/2026
* **Responsável:** Breno Carvalho
* **Atividade:** Relatório de inspeção do código-fonte e correção dos problemas da classe complexa `Level2State` (item da Entrega 2), com prints de antes e depois.
* **Ferramenta:** Claude (app Claude, com acesso à pasta local do projeto)
* **Prompt/Instrução Utilizada:**
  > *"O que falta para encerrar minha parte?"*, seguido de *"Sim"* à proposta de começar pela inspeção, com a regra de só aplicar depois da revisão do Breno.
* **Resultado:** A ferramenta rodou o PMD 7.7.0 com a configuração do grupo (`quickstart`) numa cópia e cruzou o resultado com os 11 problemas da classe na análise do Sonar feita pelo Luiz. Depois mostrou a tabela de problemas e o diff da correção para revisão: campo `background` virou variável local, o array usado só para o número 8 virou constante, laços viraram `for-each`, `== true` foi removido, a variável local `shots` que escondia o campo saiu, `size() == 0` virou `isEmpty()` e os métodos vazios ganharam comentário. Os 2 avisos de `remove()` do Sonar já tinham sido resolvidos pela #10, e o de método estático pela #9. Documentação em [`docs/inspecao/level2state/`](../inspecao/level2state/LEIA-ME.md).
* **Decisão:** **Aceito sem alterações no código.** O Breno aprovou o diff e o uso do PMD para os prints, com o Sonar do Luiz como complemento. A ferramenta apontou que a sua própria correção da #10 tinha criado 2 avisos novos (`ForLoopCanBeForeach`), também resolvidos aqui. Os nomes `KeyPressed`/`KeyReleased` ficaram, porque vêm da interface `State`, comum a todos os integrantes.
* **Validação:** Relatórios do PMD gerados com `mvn pmd:pmd` na máquina do Breno: 11 problemas na classe antes e nenhum depois, e o projeto foi de 48 para 37 (prints e relatórios em `docs/inspecao/level2state/antes` e `depois`). O print do "antes" mostrava 10 problemas, e a ferramenta conferiu no XML que o 11º, de prioridade 4, aparece em outra seção do HTML. `mvn test -Dtest=Level2StateTest` depois das correções: 14 testes, 0 falhas. Para comprovar que o comportamento não mudou, as duas versões da classe rodaram em 150 partidas simuladas com teclas aleatórias e sementes fixas (cerca de 140 mil quadros, 61 vitórias e 89 derrotas), com estado idêntico quadro a quadro ([`SimL2.java`](breno/evidencias/prova/br/states/SimL2.java)).

### Interação #020 — Mutação (PIT) e cobertura de arestas da classe `Level2State`
* **Data:** 22/11/2026
* **Responsável:** Breno Carvalho
* **Atividade:** Teste baseado em defeitos (pelo menos 80% de escore de mutação) e teste estrutural (pelo menos 80% no critério todas-arestas) na classe complexa `Level2State` (itens da Entrega 2).
* **Ferramenta:** Claude (app Claude, com acesso à pasta local do projeto)
* **Prompt/Instrução Utilizada:**
  > *"Aprovo sim"* (incluir a `Level2State` no PIT do `pom.xml`) e *"Perfeito, realize essa implementação do pit que te retorno esse resultado"*. Depois, *"aprovo"* para os casos novos.
* **Resultado:** Foram acrescentadas 2 linhas no `pom.xml`: `br.states.Level2State` em `targetClasses` e `br.states.Level2StateTest*` em `targetTests`. A primeira execução do PIT, rodada pelo Breno, deu 19/29 (66%). A ferramenta leu o `mutations.xml` e agrupou os 10 sobreviventes em três lacunas da suíte: nenhum caso conferia o desenho, o movimento dos aliens nem a troca de tela. Antes de mostrar os testes, conferiu com um programa simples que as afirmações valiam para a classe correta. Depois propôs 5 casos na classe aninhada `DesenhoETrocaDeTela`, cada um ligado aos mutantes que deveria matar. Documentação em [`docs/reports/level2state/`](../reports/level2state/LEIA-ME.md).
* **Decisão:** **Aceito sem alterações.** O Breno aprovou a mudança no `pom.xml` e os 5 casos como foram propostos. Na proposta, a ferramenta apontou que os casos de desenho e movimento não fixam posição exata, cor nem velocidade, para não repetir o problema que levou o Roger a remover testes de arte da suíte da `MenuState`.
* **Validação:** Na máquina do Breno, `mvn test -Dtest=Level2StateTest` deu 19 testes, 0 falhas, e o PIT deu **29/29 (100%)**, com test strength de 100% e nenhum mutante sem cobertura. JaCoCo: **34/34 arestas (100%)**. As 2 linhas não cobertas (55/57) são os corpos vazios de `KeyPressed` e `KeyReleased`. Prints e relatórios em `docs/reports/level2state/`.

### Interação #021 — Alinhamento dos pesos das medidas ISO 25010 com a análise de importância do grupo
* **Data:** 23/11/2026
* **Responsável:** Breno Carvalho
* **Atividade:** Correção dos pesos da nota geral das medidas ISO 25010 (interação #017) e revisão da medida de segurança operacional.
* **Ferramenta:** Claude (app Claude)
* **Prompt/Instrução Utilizada:**
  > *"Verifique o item 3 do print publicado. Parece que utilizamos pesos diferentes no nosso projeto. Monte um plano para essa mudança e não realize nenhuma implementação sem minha revisão"*, com o arquivo `importancia-caracteristicas.md` anexado.

  Decisões do Breno ao plano proposto: adotar os pesos do `importancia-caracteristicas.md` e registrar a mudança aqui; o autor da análise publica o arquivo no repositório; segurança operacional em 50%; receber os arquivos editados pelo chat para aplicar.
* **Resultado:** Uma revisão do grupo (item 3) apontou que a nota geral da #017 usava os pesos de uma análise individual de importância do Breno (13/11), e não os da análise oficial do grupo. As duas divergem em 5 características: Compatibilidade 3 → 2, Capacidade de interação 3 → 4, Segurança 2 → 1, Manutenibilidade 4 → 3 e Segurança operacional 1 → 2. A ferramenta recalculou a nota com os pesos do grupo: 49,0% antes e 82,8% depois, os mesmos 49% e 83% depois de arredondar. Também mostrou que a medida de segurança operacional (100%) ignorava o único ponto de atenção da análise do grupo: a música começa sozinha no menu, sem aviso e sem controle de volume. Por fim, apresentou os riscos de manter 100% (contradição com o documento oficial) e de passar a 50% (medida grosseira e números já publicados a refazer).
* **Decisão:** **Alterado.** Pesos trocados pelos do grupo. A segurança operacional passou a 50% (1 de 2 perigos mitigados), e a nota geral foi para **45% antes e 79% depois**; medindo o desktop no "depois", 71%. As 8 outras medidas não mudaram. Arquivos atualizados: `docs/qualidade/iso25010-medidas.md` (com histórico de revisões), README e a seção 2.7 do Plano de Teste. A #017 foi mantida como registro do que foi feito naquele momento.
* **Validação:** Nota geral recalculada por script, a partir das medidas de cada subcaracterística, com os dois conjuntos de pesos e com a segurança operacional em 100% e em 50%. Conferido que os valores arredondados da tabela levam ao mesmo resultado (1223/27 = 45,3% e 2138/27 = 79,2%). Os links para o `importancia-caracteristicas.md` dependem da publicação do arquivo pelo autor, em `docs/iso25010/`.

---

## Modelo para Novas Entradas (Template)

Para registrar interações futuras, copiemos a estrutura abaixo:

```markdown
### Interação #00X — [Título da Atividade]
* **Data:** DD/MM/AAAA
* **Responsável:** [Nome do Integrante]
* **Atividade:** [Ex: Criação dos Casos de Teste Unitários da Classe Game / Configuração JaCoCo]
* **Ferramenta:** [ChatGPT / Gemini / Copilot / Claude]
* **Prompt/Instrução Utilizada:**
  > *"[Insira o prompt exato ou instrução relevante]"*
* **Resultado:** [Breve resumo da resposta gerada pela IA]
* **Decisão:** [Aceito sem alterações / Alterado (detalhar o que mudou) / Rejeitado]
* **Validação:** [Descrever como o grupo testou ou validou o resultado (ex: testes executados, compilação, revisão de código)]
```