# Medidas das características de qualidade (ISO/IEC 25010:2023)

Responsável: Breno Carvalho (123083015). Item da Entrega 2: "Indicação das medidas de cada atributo de qualidade da ISO 25010 seguindo uma escala. Justificar as decisões para indicação das medidas."

Este documento mede **o quanto** o Space Invaders atende cada característica, numa escala de 0% a 100%. A **importância** de cada característica já foi definida antes, numa análise separada (escala de 1 a 5), e aqui ela entra só como peso da nota geral.

## 1. O que foi medido

| Coluna | Produto | Versão |
| :--- | :--- | :--- |
| **Antes** | Jogo desktop (janela Swing/AWT, `br.Launch`) | Commit `bbb57f3`, fim da Entrega 1, antes de qualquer correção da Entrega 2 |
| **Depois** | Versão web do jogo (`br.web.WebLaunch`, em `http://localhost:8080`) | `main` no commit `cdb5130`, mais a correção da issue [#10](https://github.com/noidnoidnoid/Trabalho_QTS-SpaceInvaders/issues/10) no `Level2State` (22/11/2026) |

A versão web roda a mesma lógica do jogo (`StateManager`, estados, `Ship`, `Alien`, `Shot`, `KeyManager`). Ela troca só a janela pela página do navegador ([`alteracoes-no-sut.md`](../alteracoes-no-sut.md), seção 3). O grupo a tratou como o novo modelo do jogo, e é nela que rodam os testes de sistema com Selenium.

## 2. Método

1. Cada subcaracterística aplicável recebe uma medida no formato da ISO/IEC 25023, **X = A / B**, entre 0 e 1. Por exemplo: funções sem defeito aberto ÷ funções implementadas. A e B vêm sempre de uma evidência do repositório (relatório, teste, issue ou código).
2. A nota da característica é a **média simples** das subcaracterísticas aplicáveis, em porcentagem.
3. Uma subcaracterística que não faz sentido para o jogo fica como **"não se aplica" (N/A)** e sai da média. Uma que se aplica mas não tem medida objetiva fica como **"não medida"** e também sai da média. As duas situações são justificadas na seção da característica.
4. A **nota geral** é a média das 9 notas, ponderada pelos níveis de importância da análise anterior (1 a 5).
5. Quando uma lista de itens é avaliada (por exemplo, falhas injetadas), cada item vale 1 se atendido e 0 se não. Para a coexistência, um item vale a fração de execuções que passaram.

## 3. Resumo

| Característica | Peso (importância) | Antes | Depois |
| :--- | :---: | :---: | :---: |
| Adequação funcional | 4 | 86% | 90% |
| Eficiência de desempenho | 4 | 15% | 89% |
| Compatibilidade | 3 | 33% | 79% |
| Capacidade de interação | 3 | 43% | 70% |
| Confiabilidade | 4 | 8% | 72% |
| Segurança (*security*) | 2 | 83% | 92% |
| Manutenibilidade | 4 | 72% | 80% |
| Flexibilidade | 3 | 50% | 88% |
| Segurança operacional (*safety*) | 1 | 100% | 100% |
| **Nota geral (média ponderada)** | | **49%** | **83%** |

As maiores mudanças vieram das correções da Entrega 2 (desempenho e confiabilidade) e da interface web (interação e flexibilidade). A capacidade de interação continua baixa porque nenhuma correção tratou de pausa, placar, vidas ou efeitos sonoros.

## 4. Medidas por característica

### 4.1 Adequação funcional (peso 4): 86% → 90%

Funções especificadas (17), tiradas das histórias de usuário dos casos de teste manuais (CT02 a CT05) e dos requisitos RF01 a RF07 do plano de teste de sistema: navegar no menu, iniciar, ajuda, sair, mover a nave, nave limitada à tela, atirar, vários tiros ao mesmo tempo, tiro some ao sair pelo topo, colisão nave-alien (Game Over), colisão tiro-alien, só o tiro que acertou some, fase 1 → fase 2, fase 2 com 8 aliens, vitória, música tema e **efeito sonoro de tiro**.

| Subcaracterística | Medida (A / B) | Antes | Depois | Evidência |
| :--- | :--- | :---: | :---: | :--- |
| Completude funcional | funções implementadas / especificadas | 16/17 (94%) | 16/17 (94%) | CT04, passo 4: não há som de tiro |
| Correção funcional | funções sem defeito aberto / implementadas | 12/16 (75%) | 14/16 (88%) | Antes: #8, #9, #10 e `Ship.getBounds()`. Depois: #10 continua no `Level1State` e `getBounds()` continua com altura 930 |
| Adequação funcional | funções sem comportamento inadequado ao objetivo / implementadas | 14/16 (88%) | 14/16 (88%) | Aliens da fase 2 perdem a formação (CT03, passo 8) e o tiro não tem limite de cadência (`Ship.simpleShot()`) |

**Justificativa.** O jogo entrega quase tudo o que promete, e por isso a nota é alta desde o início. O ganho vem das correções das issues #8 e #9 (tiros) e da #10 no `Level2State`. Não chega a 100% porque falta o som de tiro, a #10 segue no `Level1State` e a caixa de colisão da nave continua errada. Esse último defeito não aparece na tela, porque a área excedente fica abaixo dela (CT02).

### 4.2 Eficiência de desempenho (peso 4): 15% → 89%

| Subcaracterística | Medida (A / B) | Antes | Depois | Evidência |
| :--- | :--- | :---: | :---: | :--- |
| Comportamento temporal | média de (FPS médio ÷ 60, no máximo 1) e (segundos com frame mais lento ≤ 16,7 ms ÷ segundos amostrados) | (55/60 + 0/10) / 2 = 46% | (100% + 30/30) / 2 = 100% | Antes: 53 a 57 FPS e todos os segundos com frame entre 17 e 28 ms ([`desempenho-render.txt`](../ai/roger/evidencias/2026-11-22-desempenho-render.txt), [`secoes-roger-entrega2.md`](../plano-de-teste/secoes-roger-entrega2.md)). Depois: 60,5 a 60,8 FPS e frame mais lento entre 2,4 e 13,5 ms nas 3 execuções finais ([`execucoes-finais/`](../evidencias/sistema/execucoes-finais/)) |
| Utilização de recursos | recursos usados de forma adequada / recursos avaliados (disco por frame, memória dos tiros, CPU ociosa entre frames) | 0/3 (0%) | 2/3 (67%) | Issues #8, #11 e #12 corrigidas. O laço continua em espera ativa (`Game.run()` e `WebGame.run()`, sem pausa entre frames) |
| Capacidade | tiros liberados da memória ao sair da tela / tiros que saem da tela | 0% | 100% | Antes nenhum tiro era removido (#8). Depois: `ShotDefectsIT`, CTI-18 |

**Justificativa.** No código original o fundo era relido do disco a cada frame (cerca de 15 ms de um orçamento de 16,7 ms), e os tiros nunca saíam da lista. As correções das issues #8, #11 e #12 colocaram o jogo dentro do orçamento com folga. A tecla mais lenta levou de 82 a 114 ms para responder, abaixo do limite de 200 ms (CTS-09). Essa medida não entra na conta porque não existe para o "antes". Falta 11% porque o laço ainda ocupa um núcleo inteiro de CPU.

### 4.3 Compatibilidade (peso 3): 33% → 79%

| Subcaracterística | Medida (A / B) | Antes | Depois | Evidência |
| :--- | :--- | :---: | :---: | :--- |
| Coexistência | itens atendidos / 3: (1) libera o áudio ao fechar, (2) não monopoliza a CPU, (3) cumpre o orçamento de frame com outro programa pesado rodando junto (fração de execuções) | 1/3 (33%) | (1 + 0 + 8/11) / 3 = 58% | (1) CT04, passo 5. (2) espera ativa nos dois laços. (3) Com Chrome e Selenium na mesma máquina, nenhum frame acima de 33,3 ms em 8 de 11 execuções: 1 de 3 no critério estrito e 7 de 8 no lote do CTS-10 ([`investigacao-cts10.txt`](../ai/roger/evidencias/2026-11-22-investigacao-cts10.txt)). Antes o orçamento estourava até sem concorrência |
| Interoperabilidade | interfaces de troca de dados que funcionam com o navegador / interfaces | N/A | 4/4 (100%) | `GET /`, `GET /state`, `GET /frame.jpg` e `POST /key` ([`WebServerTest`](../../src/test/java/br/web/WebServerTest.java) e os 11 casos de sistema com Selenium) |

**Justificativa.** O jogo desktop não troca dados com nenhum outro sistema, então a interoperabilidade não se aplica no "antes". A versão web troca dados com o navegador por HTTP/JSON, e isso funciona. A coexistência continua sendo o ponto fraco: a espera ativa disputa CPU com outros programas, e os picos isolados do CTS-10 só aparecem com o navegador rodando junto.

### 4.4 Capacidade de interação (peso 3): 43% → 70%

| Subcaracterística | Medida (A / B) | Antes | Depois | Evidência |
| :--- | :--- | :---: | :---: | :--- |
| Reconhecibilidade da adequação | opções do menu cujo nome diz o que fazem / opções | 3/3 | 3/3 | START, HELP e EXIT |
| Aprendizagem | controles explicados ao jogador / controles (W, S, Enter, A, D, Espaço) | 0/6 | 6/6 | Antes a Ajuda (`HelpState`) mostra só a história. Depois o painel da página lista os 6 ([`index.html`](../../resources/web/index.html)) |
| Operabilidade | funções de controle disponíveis / esperadas (pausar, voltar ao menu durante a partida, sair, recomeçar depois do fim, ajuda) | 3/5 | 3/5 | Não há pausa nem saída durante as fases (`Level1State` e `Level2State` não tratam nenhuma tecla de saída) |
| Proteção contra erros do usuário | proteções presentes / esperadas (tecla inválida ignorada, confirmação antes de sair) | 1/2 | 1/2 | `KeyManager` ignora códigos fora de 0 a 255; a página só envia as teclas do jogo; EXIT fecha sem confirmar |
| Engajamento do usuário | elementos presentes / esperados (placar, vidas, efeitos sonoros, música, progressão de fases) | 2/5 | 2/5 | `explosion.mp3` existe em `resources/audio/`, mas nenhum código o toca |
| Inclusividade | itens atendidos / 4 (destaque que não depende só de cor, idioma único, teclas remapeáveis, jogável só com teclado) | 1/4 | 2/4 | Depois, o campo "Opção do menu" escreve a opção selecionada. A página está em português e o jogo em inglês (START, GAME OVER) |
| Assistência ao usuário | tarefas que o jogo explica / tarefas (mover, atirar, navegar no menu, vencer a fase, sair da partida) | 0/5 | 3/5 | Painel da página |
| Autodescrição | informações de estado visíveis / esperadas (tela atual, opção selecionada, resultado da partida) | 2/3 | 3/3 | O campo "Tela" mostra MENU, LEVEL1, LEVEL2 e outras |

**Justificativa.** O jogo em si não mudou para o jogador. Quem melhorou a nota foi a página web, que explica os controles e mostra o estado da partida. Continua faltando o que um jogo do gênero costuma ter: pausa, placar, vidas e efeitos sonoros.

### 4.5 Confiabilidade (peso 4): 8% → 72%

| Subcaracterística | Medida (A / B) | Antes | Depois | Evidência |
| :--- | :--- | :---: | :---: | :--- |
| Ausência de falhas | defeitos do SUT corrigidos / defeitos conhecidos | 0/11 (0%) | 9/11 (82%) | Defeitos conhecidos: #2, #3, #4, #5, #8, #9, #10 no `Level1State`, #10 no `Level2State`, #11, #12 e `Ship.getBounds()`. Ficam abertos a #10 no `Level1State` e o `getBounds()`. As issues #6 e #7 eram defeitos das suítes de teste, não do jogo. A #1 já estava corrigida no `bbb57f3` |
| Disponibilidade | situações em que o jogo inicia / situações (com áudio, sem dispositivo de áudio, sem o MP3, executado fora da raiz) | 1/4 (25%) | 3/4 (75%) | Issues #2 e #3 e `ThemeAudioIT` CTI-22. Fora da raiz, `Background.print()` lança `NullPointerException` porque a imagem não carrega |
| Tolerância a falhas | falhas tratadas sem encerrar o jogo / falhas consideradas (MP3 ausente, sem dispositivo de áudio, `stop()` sem áudio carregado, imagem ausente, exceção dentro do laço) | 0/5 (0%) | 3/5 (60%) | Issues #2, #3 e #4 corrigidas. Os laços de `Game.run()` e `WebGame.run()` não têm `try/catch` |
| Recuperabilidade | N/A | | | O jogo não guarda dados. A única recuperação é abrir o jogo de novo, e perde-se só a partida em andamento |

**Justificativa.** O original nem abria numa máquina sem placa de som. As correções do `AudioPlayer` e dos tiros resolveram a maior parte dos defeitos encontrados. Ainda restam a falta de imagem e qualquer exceção no laço, que encerram a partida. Observação de método: o 0% do "antes" na ausência de falhas acontece por construção, porque os defeitos contados foram justamente os que o grupo encontrou nesse código.

### 4.6 Segurança (*security*) (peso 2): 83% → 92%

| Subcaracterística | Medida (A / B) | Antes | Depois | Evidência |
| :--- | :--- | :---: | :---: | :--- |
| Integridade | entradas externas validadas / entradas externas | 1/1 (100%) | 1/1 (100%) | Antes, só o teclado (`KeyManager`). Depois, o servidor HTTP aceita só as teclas do jogo (400 para as outras, `KeyMapping`), responde 405 a método errado e só aceita conexões de `127.0.0.1` |
| Resistência | arquivos sem vulnerabilidade apontada pelo Sonar / arquivos | 12/18 (67%) | 21/25 (84%) | Antes: 6 ocorrências da regra `java:S4507` (`printStackTrace`) ([`sonar-antes-comprehensive.md`](../sonar/before/sonar-antes-comprehensive.md)). Depois: 4 ([`sonar/after/01-visao-geral.png`](../sonar/after/01-visao-geral.png)). Nota de segurança B nas duas análises |
| Confidencialidade, não repúdio, responsabilização, autenticidade | N/A | | | Não há dados pessoais, contas, login nem persistência |

**Justificativa.** É um jogo local, sem dados a proteger. A camada web abre uma porta HTTP, mas só na própria máquina e só para as teclas do jogo.

### 4.7 Manutenibilidade (peso 4): 72% → 80%

| Subcaracterística | Medida (A / B) | Antes | Depois | Evidência |
| :--- | :--- | :---: | :---: | :--- |
| Modularidade | classes sem estado `static` mutável / classes | 15/18 (83%) | 22/25 (88%) | Continuam `KeyManager.a/d/space`, `StateManager.states/currentState` e `Shot.velocity`. Saiu `Shot.isVisible` (#9) |
| Reusabilidade | 1 − duplicação (Sonar) | 79% | 85% | 20,9% → 14,7% |
| Analisabilidade | 1 − apontamentos do Sonar por linha de código | 1 − 106/1.180 = 91% | 1 − 120/1.700 = 93% | 120 = 4 de segurança + 19 de confiabilidade + 97 de manutenibilidade. O Sonar arredonda o tamanho para 1,7 mil linhas |
| Testabilidade | cobertura de arestas do projeto | 34% (Sonar) | 55% (JaCoCo, 146/264) | [`docs/reports/coverage/`](../reports/coverage/). Mutação de 89% nas 4 classes analisadas pelo PIT ([`docs/reports/mutation/`](../reports/mutation/)) |
| Modificabilidade | não medida | | | Não há medida objetiva igual para o antes e o depois. Nota de manutenibilidade do Sonar: A nas duas análises |

**Justificativa.** A Entrega 2 reduziu a duplicação, aumentou a cobertura e tirou um estado global. A nota não sobe mais porque o estado `static` restante força o uso de reflection ou de campos de pacote nos testes, e o laço de `WebGame.run()` é uma cópia do de `Game.run()`, dívida assumida em [`alteracoes-no-sut.md`](../alteracoes-no-sut.md).

### 4.8 Flexibilidade (peso 3): 50% → 88%

| Subcaracterística | Medida (A / B) | Antes | Depois | Evidência |
| :--- | :--- | :---: | :---: | :--- |
| Adaptabilidade | ambientes em que roda / ambientes considerados (a partir da raiz, fora da raiz, tela 1920×1080, navegador) | 1/4 (25%) | 3/4 (75%) | Antes: janela fixa de 800×1080 px (`setResizable(false)`), que não cabe numa tela de 1080 px com a barra de título (dedução a partir do código, não testada). Depois: a imagem ocupa no máximo 90% da altura da janela do navegador (`#frame { height: min(90vh, 1080px) }`). Nas duas versões, os caminhos `src/*.png` exigem rodar a partir da raiz |
| Instalabilidade | pré-requisitos documentados no README / pré-requisitos reais | 3/4 (75%) | 4/4 (100%) | Antes, o dispositivo de áudio era obrigatório e não estava documentado (CT02 a CT05). Depois: JDK 17, Maven, raiz e o comando `WebLaunch` com o endereço da página |
| Escalabilidade, substituibilidade | N/A | | | Jogo local de um jogador, sem requisito de carga nem de substituir outro produto |

**Justificativa.** A versão web resolveu o tamanho de tela e permite jogar no navegador. Continua faltando um pacote executável (JAR), e o jogo só roda a partir da raiz do repositório.

### 4.9 Segurança operacional (*safety*) (peso 1): 100% → 100%

| Subcaracterística | Medida (A / B) | Antes | Depois | Evidência |
| :--- | :--- | :---: | :---: | :--- |
| Identificação de riscos e aviso de perigo | perigos identificados que estão mitigados / perigos identificados | 1/1 | 1/1 | Único perigo plausível: conteúdo piscante (fotossensibilidade). Não existe: o único movimento contínuo é o deslocamento dos sprites |
| Restrição operacional, falha segura, integração segura | N/A | | | O sistema não controla hardware nem interage com o ambiente físico |

**Justificativa.** Nenhuma falha do jogo põe pessoas ou bens em risco. A medida é pouco informativa, coerente com a importância 1.

## 5. Limitações

- **O "antes" e o "depois" medem produtos diferentes.** O "antes" é o jogo desktop e o "depois" é a versão web. Parte da melhora vem das correções (desempenho, confiabilidade, adequação funcional) e parte vem da página nova (interação, flexibilidade, interoperabilidade). Além disso, a camada web foi criada como adaptador para os testes com Selenium, e o próprio grupo registra que a janela (`Display`) e o `Game.render()` do desktop não são exercitados por esses testes ([`alteracoes-no-sut.md`](../alteracoes-no-sut.md), seção 3). Medindo o desktop no "depois", Capacidade de interação, Compatibilidade e Flexibilidade ficariam em 43%, 58% e 63%, e a nota geral cairia de 83% para 75%.
- **As medidas de desempenho do "antes"** foram tiradas na camada web rodando a lógica antiga (FPS no menu e frame da fase 2), porque o desktop não publicava essas medidas.
- **Algumas medidas são listas de verificação** definidas pelo grupo, como os elementos de engajamento ou os ambientes de adaptabilidade. A escolha dos itens tem peso no resultado e está descrita em cada tabela.
- **A analisabilidade** trata cada apontamento do Sonar como uma linha afetada. É uma aproximação.
- **A tela de 1920×1080** no desktop foi deduzida a partir do código e não foi testada.
- **A correção da #10, a inspeção da `Level2State`** ([`docs/inspecao/level2state/`](../inspecao/level2state/LEIA-ME.md), 11 problemas resolvidos, comportamento preservado) **e os 5 casos novos que levaram a classe a 100% de arestas e de mutação** ([`docs/reports/level2state/`](../reports/level2state/LEIA-ME.md)) entraram depois da última análise do Sonar e das execuções finais de sistema. A cobertura e os relatórios citados são da `main` no commit `cdb5130`. Por isso as medidas de analisabilidade, resistência e testabilidade não contam essas melhorias.

## 6. Evidências usadas

- Enunciado do trabalho (Entrega 2) e ISO/IEC 25010:2023 (segunda edição, 2023-11).
- Código em `src/br/` no commit `bbb57f3` e na `main` (`cdb5130`).
- Casos de teste manuais em [`docs/manual-tests/`](../manual-tests/): CT02 a CT05.
- Issues [#1 a #12](https://github.com/noidnoidnoid/Trabalho_QTS-SpaceInvaders/issues?q=is%3Aissue), consultadas em 22/11/2026.
- JaCoCo ([`docs/reports/coverage/jacoco.csv`](../reports/coverage/jacoco.csv)), PIT ([`docs/reports/mutation/`](../reports/mutation/)), Sonar ([`docs/sonar/`](../sonar/README.md)) e PMD ([`docs/inspecao/`](../inspecao/LEIA-ME.md)).
- Testes de sistema e desempenho: [`docs/evidencias/sistema/`](../evidencias/sistema/) e [`docs/plano-de-teste/secoes-roger-entrega2.md`](../plano-de-teste/secoes-roger-entrega2.md).
- Correção da #10 no `Level2State`: [`docs/ai/breno/evidencias/2026-11-22-defeito-10-level2state.txt`](../ai/breno/evidencias/2026-11-22-defeito-10-level2state.txt).
- Uso de IA: [`docs/ai/AI-LOG.md`](../ai/AI-LOG.md), interações #017 e #018.
