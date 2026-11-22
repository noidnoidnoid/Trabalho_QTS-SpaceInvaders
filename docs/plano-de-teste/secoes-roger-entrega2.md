# Seções do Plano de Teste: unitários com isolamento, integração e sistema (Entrega 2)

Responsável: Roger Egito (220083118)

> Versão em Markdown das seções do Roger no Plano de Teste. A versão pronta para colar no Google Docs, com a indicação de onde colar cada bloco, é [`colar-no-google-docs-roger.html`](colar-no-google-docs-roger.html). Os números e casos abaixo vêm das execuções registradas em `docs/ai/roger/evidencias/` e `docs/evidencias/sistema/`.

## 1. Escopo

| Nível | Itens sob teste | Fora do escopo |
| :--- | :--- | :--- |
| Unitário | `MenuState` (complexidade ciclomática 24), `AudioPlayer` (10), `Sprites`, `StateSnapshot`, `WebServer`, `KeyMapping` | Classes dos outros integrantes |
| Integração | `StateManager` + estados + `KeyManager` + `Ship` + `Alien` + `Shot` + `Background`, e `AudioPlayer` + MP3 real + decodificador mp3spi + mixer real | Janela (`Display`) |
| Sistema | Jogo inteiro rodando como processo, operado pelo navegador | `Display` e `Game.render()` da versão desktop (ver limitações) |

A `MenuState` foi assumida como classe complexa do Roger na Entrega 2, porque a professora apontou que `AudioPlayer` (complexidade 6 na Entrega 1, 9 depois da correção) fica abaixo do mínimo de 10. A complexidade 24 foi medida pelo contador `COMPLEXITY` do JaCoCo.

## 2. Ferramentas

| Ferramenta | Uso |
| :--- | :--- |
| JUnit 5 | Todos os níveis |
| Mockito 5 (`mock`, `mockStatic`, `mockConstruction`) | Isolamento nos unitários |
| Maven Surefire / Failsafe | Unitários (`mvn test`) e integração (`mvn verify`) separados por convenção de nome (`*Test` e `*IT`) |
| Selenium WebDriver 4 + Chrome headless | Testes de sistema (`mvn verify -Psystem`) |
| JaCoCo | Cobertura de arestas e complexidade ciclomática |
| PIT | Escore de mutação |
| GitHub Issues | Registro dos defeitos encontrados (#2 a #12) |
| PMD 7.7.0 | Inspeção estática do código (`mvn pmd:pmd`) |

**Por que Selenium exigiu uma camada web.** O jogo é uma aplicação desktop em AWT que desenha num `Canvas`. Selenium só controla navegadores. A ferramenta GenIA-E2ETest também foi avaliada: a etapa de extração dela lê o HTML da página e gera seletores XPath, então também exige uma página web. Para usar Selenium de verdade, foi criada uma camada web (`src/br/web/`) que roda a lógica real do jogo sem janela e a publica numa página HTML. A lógica do jogo não foi alterada por essa camada.

## 3. Estratégia de isolamento nos testes unitários

| Dependência | Problema para o teste | Técnica |
| :--- | :--- | :--- |
| `AudioSystem` (API estática do Java Sound) | Exige placa de som, e as falhas (sem dispositivo, arquivo inválido) não acontecem sob demanda | `mockStatic(AudioSystem.class)` |
| `Clip` | Toca som de verdade | Mock injetado pelo construtor package-private de `AudioPlayer` |
| `AudioPlayer` dentro de `MenuState` | Criado no construtor, tocaria música durante os testes | Mock injetado pelo construtor package-private de `MenuState` |
| `System.exit` na opção EXIT | Encerraria a JVM de teste | `Runnable` injetado no mesmo construtor |
| `StateManager` (estático) | Trocar de tela inicializa as fases reais | Array público `StateManager.states` preenchido com mocks e restaurado no `@AfterEach` |
| `Background` e `Alien` em `render()` | Leem PNG do disco | `mockConstruction` |
| `Graphics` | Desenho não tem retorno | Mock que registra cor, fonte e posição de cada `drawString` |

## 3b. Casos de teste unitários

#### Casos de teste unitários da MenuState

Técnicas: partição de equivalência e análise de valor limite (funcional), com cobertura de todas as arestas (estrutural). Resultado: 29 casos, 26 de 26 arestas, escore de mutação de 83,3% bruto e 100% ajustado.

| Grupo | Caso | Técnica |
| :--- | :--- | :--- |
| Ciclo de vida | O construtor não toca a música | Partição (estado inicial) |
| Ciclo de vida | Estado inicial: START selecionado, alien em (0,0) indo para baixo e para a direita | Partição (estado inicial) |
| Ciclo de vida | init() toca o tema a cada chamada | Partição |
| Animação | Um tick soma o deslocamento nos dois eixos | Partição |
| Animação | Parede direita: x final 750 não inverte, 751 inverte | Valor limite |
| Animação | Parede de baixo: y final 1000 não inverte, 1001 inverte | Valor limite |
| Animação | Parede esquerda: x final 0 não inverte, -1 inverte | Valor limite |
| Animação | Topo: y final 0 não inverte, -1 inverte | Valor limite |
| Animação | Canto inferior direito inverte os dois eixos | Valor limite (combinação) |
| Navegação | W sobe uma opção, e do topo vai para a última (3 casos) | Partição e valor limite (pontas da lista) |
| Navegação | S desce uma opção, e da última volta para o topo (3 casos) | Partição e valor limite (pontas da lista) |
| Navegação | Tecla sem função no menu não muda nada | Partição (classe inválida) |
| Seleção | ENTER em START abre a fase 1 | Partição |
| Seleção | ENTER em HELP abre a ajuda | Partição |
| Seleção | ENTER em EXIT encerra o jogo | Partição |
| Seleção | Opção fora do intervalo não faz nada | Estrutural (aresta default do switch) |
| Desenho | A opção selecionada aparece com cor diferente das outras e do texto comum (3 casos) | Partição |
| Desenho | Desenha o título, as três opções, o fundo e o alien na posição da animação | Partição |
| Desenho | Todo texto fica dentro da tela, e as opções aparecem de cima para baixo na ordem do menu | Partição |

Os testes de desenho verificam só comportamento. Cor, fonte e posição exatas são decisões de arte e não são testadas, para que uma mudança visual não quebre os testes sem existir defeito. Os 7 mutantes que sobrevivem mudam apenas esses aspectos (centralização exata, cor do título e fonte) e foram classificados como equivalentes em relação à especificação.

#### Casos de teste unitários do AudioPlayer

21 casos, 9 de 10 arestas (a aresta restante é gerada pelo compilador no fechamento do try-with-resources e é inalcançável, porque o caminho só chega ali com o recurso não nulo), escore de mutação de 100%. A suíte da Entrega 1 foi substituída, porque seus 5 casos davam erro na preparação e nunca chegaram a executar (issue #6).

| Grupo | Casos | Técnica |
| :--- | :--- | :--- |
| Defeitos | Recurso inexistente (#2), máquina sem placa de som (#3) e stop() sem áudio carregado (#4) não derrubam o jogo. Os três falharam antes da correção | Partição (entradas inválidas) |
| play() e stop() | Sem clip, parado e tocando. Ordem parar, voltar ao início e tocar | Partição e estrutural |
| Carregamento com sucesso | Formato PCM de 16 bits pedido para mono e estéreo, streams fechados depois de abrir | Partição |
| Falhas de carregamento | Formato não suportado, conversão não suportada, sem dispositivo, linha ocupada, linha recusa abrir, erro de leitura | Partição (uma classe por tipo de falha) |
| Arquivos reais | Recurso inexistente e arquivo que não é áudio | Partição |

## 4. Casos de teste de integração

Técnica: teste baseado em cenário de uso, com objetos reais e apenas o hardware substituído (som e janela). O loop é dirigido pelos mesmos métodos, na mesma ordem, de `Game.update()` e `Game.render()`.

| ID | Integração exercitada | Cenário | Resultado esperado |
| :--- | :--- | :--- | :--- |
| CTI-01 | StateManager + MenuState + AudioPlayer | Início do jogo | Tela de menu e tema tocado uma vez |
| CTI-02 | StateManager + MenuState + Level1State + Ship | ENTER em START | Fase 1 com a nave em x = 400 e um alien |
| CTI-03 | StateManager + MenuState + HelpState | S, ENTER e depois ENTER em RETURN | Ajuda e volta ao menu, reiniciando o tema |
| CTI-04 | StateManager + MenuState | W em START, ENTER | Pedido de encerramento |
| CTI-05 | StateManager + MenuState | ENTER pressionado sem soltar | Nada acontece (o menu reage ao soltar) |
| CTI-06 | StateManager + KeyManager + MenuState | A, D e Espaço no menu | Opção e tela não mudam |
| CTI-07 | KeyManager + Level1State + Ship | D pressionado | Nave anda 6 px a partir do segundo frame (latência de 1 frame, consequência da ordem em `Game.update()`) |
| CTI-08 / 09 | KeyManager + Level1State + Ship | A ou D segurado | Nave para em x = 0 ou x = 700 |
| CTI-10 | KeyManager + Level1State + Ship | D solto | Nave para no frame seguinte |
| CTI-11 | KeyManager + Level1State + Ship | A e D juntos | Movimentos se anulam |
| CTI-12 | KeyManager + Level1State + Ship + Shot | Espaço segurado | Um tiro por frame, saindo da posição da nave |
| CTI-13 | Level1State + Ship + Shot + Alien + StateManager + Level2State | Tiro acerta o alien | Fase 2 com 8 aliens e sem tiros |
| CTI-14 / 16 | Level1State ou Level2State + Ship + Alien + StateManager | Alien encosta na nave | Game Over |
| CTI-15 | Level2State + Ship + Shot + Alien + StateManager | Último alien destruído | You Win |
| CTI-17 | StateManager + GameOverState + MenuState | ENTER em MENU no Game Over | Volta ao menu |
| CTI-18 | Level1State + Ship + Shot | Tiro sai pelo topo | Tiro removido da lista (falhou antes da correção da #8) |
| CTI-19 | Level2State + Ship + Shot + Alien | Um tiro acerta, outro está em voo | O tiro em voo continua (falhou antes da correção da #9) |
| CTI-20 / 21 | AudioPlayer + MP3 real + mp3spi + mixer real | Com placa de som | Tema carregado, `play()` toca e `stop()` para |
| CTI-22 | AudioPlayer + mixer real | Sem placa de som | Jogo segue sem áudio (correção da #3) |

## 5. Casos de teste de sistema

Técnica: teste funcional caixa-preta, derivado dos requisitos abaixo. O jogo não tem especificação escrita, então os requisitos foram levantados por engenharia reversa do comportamento e do código. Cada teste sobe o jogo como processo separado, abre a página no Chrome e só interage pelo teclado do navegador. O resultado é lido no que a página mostra.

### Requisitos funcionais

- **RF01** O menu abre com START selecionado, e W/S percorrem as opções, voltando ao início depois da última.
- **RF02** ENTER em START abre a fase 1.
- **RF03** ENTER em HELP abre a ajuda, e ENTER em RETURN volta ao menu.
- **RF04** ENTER em EXIT encerra o jogo.
- **RF05** A e D movem a nave sem sair da tela.
- **RF06** Um tiro que acerta o alien da fase 1 abre a fase 2.
- **RF07** Um alien que alcança a nave encerra a partida (Game Over).

### Requisitos não funcionais (Eficiência de Desempenho, ISO/IEC 25010)

- **RNF01 (comportamento temporal):** média de pelo menos 57 FPS durante 10 s na fase 2 (meta do jogo de 60 FPS, com 5% de tolerância).
- **RNF01b (fluidez):** em 10 s na fase 2, amostrando o frame mais lento de cada segundo, a mediana das amostras cabe no orçamento de 16,7 ms e no máximo 1 segundo tem frame acima de 33,3 ms (um frame perdido). Histórico do critério:
  - Foi incluído depois das primeiras execuções: a média de FPS passava enquanto o frame mais lento chegava a 50 ms, porque o loop compensa os frames atrasados e a média esconde as travadas.
  - A primeira versão exigia nenhum frame acima de 33,3 ms. Depois das correções das issues #11 e #12, os frames ficaram entre 3 e 10 ms, mas um pico isolado de cerca de 35 ms ainda aparecia em aproximadamente 1 de cada 8 execuções. O coletor de lixo foi descartado por medição (maior pausa de 4,6 ms). O pico só aparece com o Chrome e o Selenium rodando na mesma máquina, então a causa provável é o sistema operacional tirar a thread do jogo do processador. Essa causa não foi comprovada.
  - O critério revisto ainda reprova o código anterior à correção, em que todos os segundos amostrados ficavam entre 17 e 28 ms.
- **RNF02 (tempo de resposta):** uma tecla solta no navegador altera a página em até 200 ms. O limite é uma escolha nossa, baseada na regra prática de que respostas abaixo de 200 ms parecem imediatas num menu. Não é um número de norma.

| ID | Requisito | Passos | Resultado esperado |
| :--- | :--- | :--- | :--- |
| CTS-01 | RF01 | Abrir a página, S, S, S, W | START, HELP, EXIT, START, EXIT |
| CTS-02 | RF02 | ENTER | Tela LEVEL1 |
| CTS-02b | RF02 | ENTER do teclado numérico | Tela LEVEL1 (o desktop aceita os dois Enter) |
| CTS-03 | RF03 | S, ENTER, ENTER | HELP e depois MENU |
| CTS-04 | RF04 | W, ENTER | Processo do jogo termina com código 0 e a página mostra "desconectado" |
| CTS-05 | RF06 | ENTER, segurar Espaço | Fase 2 com 8 aliens e nave em x = 400 |
| CTS-06 | RF05 | Na fase 2, segurar D e depois A | Nave para em 700 e em 0, e continua parada com a tecla segurada |
| CTS-07 | RF07 | ENTER e não atirar | Game Over em até 90 s |
| CTS-08 | RNF01 | Fase 2, amostrar FPS por 10 s | Média >= 57 |
| CTS-09 | RNF02 | 12 vezes: soltar S e medir até a opção mudar | Pior caso <= 200 ms |
| CTS-10 | RNF01b | Fase 2, amostrar o frame mais lento de cada segundo por 10 s | Mediana <= 16,7 ms e no máximo 1 segundo acima de 33,3 ms |

## 5b. Inspeção de código

A inspeção usou o PMD 7.7.0 (plugin do Maven, conjunto de regras quickstart), que faz o mesmo tipo de análise estática do SonarQube sem exigir servidor nem conta. Na MenuState foram encontrados 5 problemas (duas declarações múltiplas na mesma linha, dois campos que deveriam ser variáveis locais e um método vazio sem explicação) e no AudioPlayer 3 (um recurso aberto fora do try-with-resources e dois if sem chaves). Todos foram corrigidos, e a nova execução do PMD não encontrou problemas nas duas classes. Os nomes KeyPressed e KeyReleased, que vêm da interface State, não foram renomeados, porque a mudança atingiria as classes dos outros integrantes. Os prints de antes e depois estão em docs/inspecao/ no repositório.

Detalhes, prints e relatórios: [`docs/inspecao/`](../inspecao/LEIA-ME.md).

## 6. Critérios de entrada e saída

- **Entrada:** `mvn test` compila e roda sem erro nas classes do escopo. Chrome instalado, para os testes de sistema.
- **Saída:** todos os casos do escopo passam em 3 execuções seguidas da suíte de sistema, arestas >= 80% e escore de mutação >= 80% em `MenuState` e `AudioPlayer`, e cada defeito encontrado está registrado numa issue.

## 7. Limitações

- Os testes de sistema exercitam a versão web. A janela desktop (`Display`) e o desenho de `Game.render()` no `Canvas` não são cobertos por eles. Foram cobertos pelos testes manuais da Entrega 1.
- A posição da nave só é publicada na fase 2, porque a `Level1State` mantém a nave privada e pertence a outro integrante. Por isso o RF05 é verificado na fase 2.
- As medidas de desempenho dependem da máquina. Os testes registram os valores medidos em `desempenho.txt`, além de aprovar ou reprovar.
- `WebGame` aparece com 0% no relatório do JaCoCo, porque só é exercitado pelos testes de sistema, que rodam num processo separado e não instrumentado.
