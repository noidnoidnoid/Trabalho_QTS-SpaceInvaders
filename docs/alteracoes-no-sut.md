# Alterações no código do jogo (SUT) na Entrega 2

Responsável: Roger Egito (220083118)

Este documento lista toda mudança feita no código do jogo (`src/br/**`) durante a Entrega 2, com o motivo e a issue correspondente. Mudanças só em testes não aparecem aqui.

As mudanças são de quatro tipos:

- **Testabilidade:** abrem uma "costura" (seam) para os testes injetarem dependências falsas. O comportamento do jogo não muda.
- **Correção de defeito:** cada uma tem uma issue e pelo menos um teste que falhava antes da correção e passa depois.
- **Código novo:** a camada web, que permite testes de sistema com Selenium.
- **Correções da inspeção de código (PMD):** problemas de qualidade apontados pela ferramenta, sem mudança de comportamento.

## 1. Testabilidade

| Classe | Mudança | Por quê |
| :--- | :--- | :--- |
| `br.audio.AudioPlayer` | Construtor package-private `AudioPlayer(Clip clip)`. O construtor público passou a delegar para `static Clip loadClip(String path)`. | Os testes passam um `Clip` falso sem abrir o dispositivo de som, e a suíte não precisa mais de reflection sobre o nome do campo privado `clip`, que quebraria com qualquer renomeação. |
| `br.states.MenuState` | Construtor package-private `MenuState(AudioPlayer themeSound, Runnable exitAction)`. O construtor público delega com `new AudioPlayer(...)` e `() -> System.exit(0)`. | A dependência do tema era criada dentro do construtor, e a opção EXIT chamava `System.exit` direto. Os dois impediam o teste unitário isolado: não dava para contar quantas vezes a música toca, e o ramo EXIT encerraria a JVM de teste. `StateManager` continua usando o construtor público, então o jogo não mudou. |
| `br.states.MenuState` | Campos `choice`, `x`, `y`, `movex`, `movey` passaram de `private` para acesso de pacote. | Montar os casos de valor limite (alien encostando em cada parede, opção na ponta da lista) sem reflection. É a mesma decisão já tomada e documentada em `Level2State` na Entrega 1. |

## 2. Correções de defeitos

| Issue | Classe | Defeito | Correção | Teste que comprova |
| :--- | :--- | :--- | :--- | :--- |
| [#2](https://github.com/noidnoidnoid/Trabalho_QTS-SpaceInvaders/issues/2) | `AudioPlayer` | Recurso de áudio ausente lançava `NullPointerException` e derrubava o jogo. | `loadClip` verifica o recurso e devolve `null`, e o jogo segue sem som. | `AudioPlayerTest`, caso D1 |
| [#3](https://github.com/noidnoidnoid/Trabalho_QTS-SpaceInvaders/issues/3) | `AudioPlayer` | Sem dispositivo de áudio, `IllegalArgumentException` impedia o jogo de abrir. | A exceção entra no `catch` junto com as outras falhas de carregamento. | `AudioPlayerTest`, caso D2, e `ThemeAudioIT` CTI-22 |
| [#4](https://github.com/noidnoidnoid/Trabalho_QTS-SpaceInvaders/issues/4) | `AudioPlayer` | `stop()` lançava NPE quando o áudio não carregou. | Guarda de `null`, igual à que `play()` já tinha. | `AudioPlayerTest`, caso D3 |
| [#5](https://github.com/noidnoidnoid/Trabalho_QTS-SpaceInvaders/issues/5) | `AudioPlayer` | Comentários dos argumentos de `AudioFormat` trocados e streams nunca fechados. | Comentários corrigidos e try-with-resources. | `AudioPlayerTest`, "fecha os streams depois de abrir o clip" |
| [#8](https://github.com/noidnoidnoid/Trabalho_QTS-SpaceInvaders/issues/8) | `Shot` | Tiro que sai pelo topo nunca era removido (a condição testava `y > HEIGHT`, mas o tiro sobe). | Condição `y + height < 0`. | `ShotDefectsIT` CTI-18 |
| [#9](https://github.com/noidnoidnoid/Trabalho_QTS-SpaceInvaders/issues/9) | `Shot` | `isVisible` era `static`: um acerto apagava todos os tiros. | Campo de instância. `setVisible` deixou de ser estático. | `ShotDefectsIT` CTI-19 |
| [#11](https://github.com/noidnoidnoid/Trabalho_QTS-SpaceInvaders/issues/11) | `Background` | O fundo era relido do disco a cada frame (cerca de 15 ms de um orçamento de 16,7 ms). | Leitura pela nova classe `br.display.Sprites`, que lê cada arquivo uma vez. | `PerformanceSystemIT` CTS-08 e CTS-10, `SpritesTest` |
| [#12](https://github.com/noidnoidnoid/Trabalho_QTS-SpaceInvaders/issues/12) | `Alien`, `Shot` | Cada alien e cada tiro relia o PNG (pico de 34 ms ao entrar na fase 2). | Mesma correção: `Sprites.read`. | `PerformanceSystemIT` CTS-10 |

**Efeito em teste de outro integrante:** a correção da #9 tornou `Shot.setVisible` um método de instância. O `@BeforeEach` do `ShipTest` (Luiz) chamava `Shot.setVisible(false)` para zerar o estado global entre os testes. Essa linha foi removida, porque o estado deixou de ser global, e o comentário foi atualizado. Nenhum caso do `ShipTest` mudou, e os 32 continuam passando.

**Por que corrigir em `Background`/`Sprites`, e não nos estados:** quem cria um `Background` novo a cada frame são os `render()` de todos os estados (`MenuState`, `HelpState`, `GameOverState`, `YouWinState`, `Level1State`, `Level2State`). Corrigir lá exigiria mexer em classes de outros integrantes. O custo real era a leitura do disco, e não a criação do objeto, então resolver a leitura num só lugar remove a causa sem tocar nessas classes.

**Defeitos registrados e não corrigidos por mim**, porque estão em classes de outros integrantes:

- [#7](https://github.com/noidnoidnoid/Trabalho_QTS-SpaceInvaders/issues/7): `GameTest` dá `HeadlessException` nos 5 casos e quebra `mvn test` para o grupo.
- [#10](https://github.com/noidnoidnoid/Trabalho_QTS-SpaceInvaders/issues/10): os laços de remoção em `Level1State` e `Level2State` pulam o elemento seguinte a cada `remove`.

## 3. Código novo: camada web

| Arquivo | Papel |
| :--- | :--- |
| `src/br/web/WebLaunch.java` | `main` da versão web. Sobe o jogo e o servidor em `http://localhost:8080`. |
| `src/br/web/WebGame.java` | Roda o jogo real sem janela. O loop de 60 FPS é o mesmo de `Game.run()`, desenhando numa imagem em memória. |
| `src/br/web/WebServer.java` | Servidor HTTP do próprio JDK (`com.sun.net.httpserver`): página, frame em JPEG, estado em JSON e teclas. |
| `src/br/web/GameSession.java` | Interface entre o servidor e o jogo, para testar o servidor com um jogo falso. |
| `src/br/web/KeyMapping.java` | Converte o nome da tecla do navegador no código AWT e recusa qualquer outra tecla. |
| `src/br/states/StateSnapshot.java` | Foto do que o jogador vê (tela, opção do menu, nave, aliens, tiros), tirada pelo loop a cada frame. |
| `resources/web/index.html` | Página que mostra o jogo e os campos que o Selenium lê. |

**O que a camada web substitui e o que ela reusa.** Ela reusa sem alteração toda a lógica do jogo: `StateManager`, todos os estados, `KeyManager`, `Ship`, `Alien`, `Shot` e `Background`. Ela substitui só a janela (`Display`) e o destino do desenho de `Game.render()`. Essas duas partes não são exercitadas pelos testes de sistema com Selenium, e isso está declarado no Plano de Teste.

**Dívida técnica assumida.** O loop de `WebGame.run()` é uma cópia do loop de `Game.run()`. Reusar `Game` exigiria mudar o construtor dele, que sempre abre um `JFrame`, e o `GameTest`, que são do Leon. A forma de eliminar a duplicação seria separar em `Game` o loop (atualizar e desenhar num `Graphics` qualquer) da janela, e as duas versões passariam a usar o mesmo loop.

**Diferença intencional em relação ao desktop.** No desktop, as teclas chegam pela thread de eventos do AWT e mexem nos estados ao mesmo tempo que a thread do jogo. Na versão web, as teclas entram numa fila e são aplicadas pela própria thread do jogo no início de cada frame, então só uma thread toca os objetos do jogo.

## 4. Correções da inspeção de código (PMD)

Detalhes, prints de antes e depois e justificativas do que ficou sem correção: [`docs/inspecao/LEIA-ME.md`](inspecao/LEIA-ME.md).

| Classe | Mudança |
| :--- | :--- |
| `MenuState` | Uma declaração por linha (`font1`/`font2` e `x`/`y`/`movex`/`movey`). Os campos `alien` e `background` viraram variáveis locais de `render()`. Comentário explicando o `KeyPressed()` vazio |
| `AudioPlayer` | O `InputStream` do recurso passou a ser aberto no próprio try-with-resources. Chaves nos `if (clip == null)` |
| `WebGame`, `WebServer`, `WebLaunch`, `Sprites` (código novo) | Chaves nos `if`, parâmetro sem uso removido de `requireMethod`, construtor privado em `WebLaunch` |
