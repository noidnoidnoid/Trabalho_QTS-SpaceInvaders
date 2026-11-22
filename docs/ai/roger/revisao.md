# Revisão das soluções geradas com IA (Roger, Entrega 2)

O enunciado pede que, quando a IA for usada de forma relevante em testes, o grupo preserve a solução inicial da IA, a solução final depois da revisão e a descrição das mudanças. Este arquivo faz isso para as suítes do Roger.

## Onde está cada versão

| Versão | Arquivo |
| :--- | :--- |
| Suíte herdada de `AudioPlayer` (Entrega 1, Gemini) | [`drafts/AudioPlayerTest.v0-gemini.java`](drafts/AudioPlayerTest.v0-gemini.java) |
| Primeiros testes de defeito de `AudioPlayer` (Claude) | [`drafts/AudioPlayerDefectTests.v1-claude.java`](drafts/AudioPlayerDefectTests.v1-claude.java) |
| Suíte de `AudioPlayer` gerada pelo Claude, depois das rodadas de PIT | [`drafts/AudioPlayerTest.v1-claude.java`](drafts/AudioPlayerTest.v1-claude.java) |
| Suíte de `MenuState` gerada pelo Claude, depois das rodadas de PIT | [`drafts/MenuStateTest.v1-claude.java`](drafts/MenuStateTest.v1-claude.java) |
| Suíte de `MenuState` com o primeiro corte proposto pelo Roger (escore 79,2%) | [`drafts/MenuStateTest.v2-corte-do-roger.java`](drafts/MenuStateTest.v2-corte-do-roger.java) |
| Versões finais | [`AudioPlayerTest.java`](../../../src/test/java/br/audio/AudioPlayerTest.java), [`MenuStateTest.java`](../../../src/test/java/br/states/MenuStateTest.java) |

O histórico do Git mostra também cada passo, porque cada etapa foi commitada separadamente.

## 1. Suíte herdada de `AudioPlayer` (v0, Gemini)

**Problema central:** a suíte nunca rodou. Os 5 casos dão erro no `@BeforeEach`, que chama `new AudioPlayer("/caminho_inexistente.mp3")` supondo que o construtor capture a falha. O construtor lança `NullPointerException`. A suíte foi registrada no AI-LOG (#006) como "compilado e testado pelo grupo", mas a execução real (`docs/ai/roger/evidencias/2026-11-22-baseline-mvn-test.txt`) mostra `tests=5 errors=5`. Issue [#6](https://github.com/noidnoidnoid/Trabalho_QTS-SpaceInvaders/issues/6).

Outros problemas encontrados na leitura:

- Injeta o `Clip` por reflection usando o nome do campo privado (`"clip"`). Qualquer renomeação quebra o teste sem erro de compilação.
- O construtor, onde está quase toda a lógica (decodificação, quatro tipos de falha), não tinha nenhum caso.
- Não verificava a ordem `stop`, depois `setFramePosition(0)`, depois `start`, só que as chamadas aconteceram.
- Não pegou `stop()` com `clip == null`, que é um defeito real ([#4](https://github.com/noidnoidnoid/Trabalho_QTS-SpaceInvaders/issues/4)).

## 2. Suíte nova de `AudioPlayer` (Claude)

Ordem de trabalho:

1. **Testes de defeito primeiro, contra o código original.** Três casos usando só a API pública (D1, D2, D3). Os três falharam ([evidência](evidencias/2026-11-22-defeitos-audioplayer-antes.txt)), o que confirma que são defeitos reais e não suposições.
2. **Correção mínima** em `AudioPlayer` (ver `docs/alteracoes-no-sut.md`) e suíte completa com 21 casos.
3. **PIT, rodada 1: 78,6%.** Os 3 mutantes sobreviventes removiam as mensagens de diagnóstico. Leitura: nenhum teste verificava se o jogo avisa por que ficou sem som.
4. **Mudança:** os testes passaram a capturar `System.err` e a exigir a mensagem com o caminho do arquivo. **PIT, rodada 2: 100% (13/13).**

## 3. Suíte de `MenuState` (Claude)

1. Refatoração mínima para injetar o tema e a ação de saída.
2. Suíte com 32 casos por partição de equivalência e valor limite. JaCoCo: 26/26 arestas.
3. **PIT, rodada 1: 93,8%.** Dois sobreviventes removiam `g.setFont(...)`. Nenhum teste olhava a fonte.
4. **Mudança:** o mock de `Graphics` passou a registrar a fonte ativa em cada `drawString`, com um caso novo exigindo 48 pt no título e 24 pt nas opções. **PIT, rodada 2: 97,9% (47/48).** O mutante restante remove `System.exit(0)` do construtor público, que não pode rodar dentro da JVM de teste. Ele é exercitado de ponta a ponta pelo teste de sistema CTS-04, que verifica o código de saída do processo.

## 4. Erros da própria IA pegos pelos testes ou pela execução

Registrados porque mostram que a validação não foi só confirmar o que a IA escreveu:

| O que a IA fez | Como foi descoberto | Correção |
| :--- | :--- | :--- |
| `StateSnapshot.capture()` chamava `getState()` antes de validar o índice | O caso "índice fora da tabela" de `StateSnapshotTest` falhou | Validar o índice antes |
| `WebGame` codificava o JPEG segurando o mesmo lock do loop | Medição manual: 63 a 67 FPS sem a página aberta e 45 a 50 com a página | Copiar o frame sob o lock e codificar fora dele |
| Testes de sistema usavam `Keys.ENTER` | Todo passo com ENTER falhou. Na especificação do WebDriver, `Keys.ENTER` é o Enter do teclado numérico (`NumpadEnter`) | `Keys.RETURN` nos testes. O adaptador passou a aceitar `NumpadEnter`, como o desktop já aceitava (caso CTS-02b) |
| CTS-09 incluía no tempo de resposta a pausa de 50 ms entre apertar e soltar | Resultado oscilando entre 186 e 254 ms | Cronômetro a partir da tecla solta (o menu reage ao soltar) |
| CTI-19 conferia o resultado depois de 2 frames | O teste passou contra o código com defeito. Dois defeitos se anulavam nessa janela (#9 e #10) | Conferir depois de 3 frames |
| RNF01 só pela média de FPS | A média passava (59,5) enquanto o frame mais lento chegava a 50 ms | Critério adicional RNF01b (CTS-10) |
| Profile `system` do `pom.xml` com o padrão `br/system/**/*IT.java` | As execuções com filtro na linha de comando (`-Dit.test`) passavam. A primeira execução sem filtro rodou 0 testes de sistema e mesmo assim deu BUILD SUCCESS | Padrão `**/system/*IT.java`. A partir daí, a verificação final sempre roda o comando exato que o grupo vai usar |
| CTS-10 exigia nenhum frame acima de 33,3 ms, sem tolerância | Depois das correções, falhou em 2 de 3 execuções por picos isolados. A investigação descartou o GC e não confirmou outra causa ([evidência](evidencias/2026-11-22-investigacao-cts10.txt)) | Critério revisto, por decisão do Roger, para mediana <= 16,7 ms e no máximo 1 segundo acima de 33,3 ms. As execuções com o critério antigo foram mantidas como evidência |

## 5. Revisão do Roger

### O que o Roger questionou na suíte de `MenuState`

1. **Testes de desenho.** O Roger apontou que os testes de cor do título, posição centralizada, altura das opções e tamanho de fonte não testam comportamento: se os desenvolvedores mudarem a arte do jogo, esses testes quebram sem que exista defeito. Para ele, bastaria verificar que tudo que deveria aparecer foi desenhado.
2. **Teste da tecla sem função** e **teste do `KeyPressed()` vazio.** O Roger considerou os dois irrelevantes: o que importa é o menu subir e descer com a tecla certa.

### O que foi medido antes de decidir

| Versão | Escore PIT | O que muda |
| :--- | :---: | :--- |
| v1 (Claude) | 97,9% | Testa cor, fonte e posição exatas |
| Sem os 2 testes de tecla | 97,9% | Os dois não matavam nenhum mutante que os outros já não matassem |
| v2: corte do Roger nos testes de desenho | 79,2% | Abaixo dos 80% exigidos, e deixava passar 2 erros reais (abaixo) |
| Final: corte do Roger + 2 verificações de comportamento | 83,3% bruto, 100% ajustado | Ver a classificação dos sobreviventes |

A v2 deixava passar dois mutantes que **não são arte**:

- **Destaque invertido** (linha 94, `i == choice` negado): as duas opções não selecionadas ficam destacadas e a selecionada fica normal. O teste "a selecionada tem cor diferente das outras" não percebe, porque a selecionada continua sendo a única diferente. Foi acrescentada a verificação de que o destaque não usa a cor do texto comum (o título).
- **Menu de cabeça para baixo** (linha 99, `+` trocado por `-`): as opções sobem em vez de descer, e S passa a mover o destaque para cima na tela. Foi acrescentada a verificação de que todo texto fica dentro da tela e de que as opções aparecem de cima para baixo, na ordem do menu.

### Decisões

| Item | Decisão | Motivo |
| :--- | :--- | :--- |
| Testes de cor, fonte e posição exatas | **Removidos** (proposta do Roger) | São decisões de arte. Testar o valor exato faz o teste quebrar sem defeito |
| "Desenha título, opções, fundo e alien" | **Mantido**, reescrito | É o "tudo que deveria renderizar foi renderizado" proposto pelo Roger |
| Destaque diferente das outras opções e do texto comum | **Novo** | Pega o destaque invertido sem fixar cor nenhuma |
| Texto dentro da tela e opções em ordem | **Novo** | Pega o menu de cabeça para baixo sem fixar posição nenhuma |
| `KeyPressed()` não faz nada | **Removido** (proposta do Roger) | Testa um método vazio, e o comportamento "o menu só reage ao soltar a tecla" já é coberto pelo teste de integração CTI-05 |
| Tecla sem função não muda nada | **Mantido** (o Roger concordou depois da discussão) | Não mata mutante extra, mas é o caso da classe inválida na partição de equivalência, que faz parte da técnica funcional avaliada |

### Classificação dos mutantes sobreviventes (versão final)

O escore bruto do PIT conta todo mutante igual. Os sobreviventes abaixo mudam só aspectos que, pela especificação adotada, não são defeito. Por isso foram classificados como **equivalentes em relação à especificação**, o mesmo tratamento dado aos 2 mutantes de `Ship` na Entrega 1.

| Linha | Mutante | Efeito no jogo | Classificação |
| :---: | :--- | :--- | :--- |
| 88 e 98 | Divisão trocada por multiplicação no cálculo de x | Texto deslocado alguns pixels, ainda dentro da tela | Arte (centralização exata) |
| 88 e 98 | Subtração trocada por adição no cálculo de x | Texto deslocado 40 px para a direita, ainda dentro da tela | Arte (centralização exata) |
| 84 | Remove `setColor(Color.WHITE)` do título | O título herda a cor padrão do `Graphics` (no jogo real, preta, e fica quase invisível no fundo escuro) | Arte (cor do título). Classificação discutível, registrada para transparência |
| 85 e 91 | Remove `setFont(...)` | Texto na fonte padrão, menor | Arte (tipografia) |
| 31 | Remove `System.exit(0)` (sem cobertura) | O jogo não fecharia no EXIT | Não testável dentro da JVM de teste. Coberto pelo teste de sistema CTS-04, que verifica o código de saída do processo |

Escore ajustado: 40 mortos de 40 mutantes que representam comportamento especificado e testável em teste unitário = **100%**.

### Demais suítes

Nas suítes de `AudioPlayer`, integração, adaptador web e sistema, o Roger revisou o código e a documentação e não pediu mudanças.
