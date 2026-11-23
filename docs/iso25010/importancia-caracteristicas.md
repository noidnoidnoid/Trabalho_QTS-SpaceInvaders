# Importância das Características de Qualidade do Produto (ISO/IEC 25010:2023)

**Sistema analisado:** Space Invaders em Java, desenvolvido por terceiros em 2021 como projeto de uma disciplina de Programação Orientada a Objetos. Nosso grupo não desenvolveu o jogo. Nós o utilizamos como sistema sob teste.

## 1. Contexto de uso

O Space Invaders é um jogo de tiro 2D single-player para desktop. O jogador controla uma nave com as teclas A, D e Espaço, navega pelos menus com W, S e Enter e enfrenta duas fases: a primeira com 1 alien e a segunda com 8. As partidas duram poucos minutos e nada é salvo entre elas.

Os elementos do sistema que guiaram a análise foram:

- **Usuários:** jogadores casuais e, hoje, estudantes que usam o código em disciplinas de teste de software.
- **Requisito central:** o jogo roda em tempo real, com a meta de 60 FPS definida no método Game.run().
- **Dados tratados:** nenhum dado pessoal. O jogo só lê as imagens e músicas que vêm com ele, não usa rede e não grava arquivos.
- **Restrições técnicas:** Java com Swing/AWT, janela de tamanho fixo (800×1080 px) e imagens carregadas por caminho relativo à pasta do projeto.

Como o jogo não envolve dinheiro, dados pessoais, saúde ou obrigações legais, consideramos que nenhuma falha dele causaria dano grave. Por isso nenhuma característica recebeu nível 5.

## 2. Nível de importância de cada característica

Escala utilizada: **1** não afeta o sucesso do sistema, **2** desejável mas pode ser negligenciada, **3** deve ser atendida em nível aceitável, **4** falhas comprometem o uso do sistema, **5** falhas podem causar danos graves.

| Característica | Nível | Justificativa |
| :--- | :---: | :--- |
| **Adequação Funcional** | 4 | O jogo existe para uma única tarefa: mover a nave, atirar nos aliens e vencer ou perder. Se a colisão ou a troca de fase falhar, a partida perde o sentido. Encontramos um exemplo disso no nosso caso de teste CT02: como o atributo isVisible da classe Shot é estático, quando um tiro acerta um alien todos os tiros da tela somem juntos. |
| **Eficiência de Desempenho** | 4 | Em um jogo de ação em tempo real, se um frame atrasa, a nave responde tarde aos comandos do jogador. A meta é de 60 FPS, mas a cada frame o jogo recarrega a imagem de fundo do disco (new Background() dentro de render()), um custo desnecessário que ameaça essa meta. Além disso, os tiros que saem da tela nunca são removidos da lista, que cresce enquanto o jogador atira. |
| **Compatibilidade** | 2 | O jogo não troca dados com nenhum outro sistema, então a interoperabilidade praticamente não se aplica. Resta a coexistência com outros programas abertos no computador do jogador. O loop principal roda sem pausas e ocupa um núcleo do processador, o que é desejável corrigir, mas não impede ninguém de jogar. |
| **Capacidade de Interação** | 4 | Para o jogador, a interação é o próprio jogo. No código original, o menu não respondia ao teclado por causa de um problema de foco da janela (Issue #1, corrigida pelo nosso grupo), o que deixava o jogo inutilizável. Além disso, a tela HELP conta apenas a história e não explica quais teclas usar. |
| **Confiabilidade** | 4 | Um jogo que não abre perde o jogador na hora. A classe AudioPlayer não trata a exceção lançada quando o computador não tem dispositivo de áudio, e por isso o jogo nem chega a abrir nessa situação, como registramos durante os testes. Não é nível 5 porque o jogo não salva progresso: um travamento custa no máximo uma partida curta. |
| **Segurança** | 1 | O jogo não coleta dados pessoais, não tem login, não usa rede e não grava arquivos. Como não há informação a proteger, falhas de segurança não afetam o sucesso do sistema. |
| **Manutenibilidade** | 3 | Não afeta quem joga, mas importa para quem lê e altera o código, que hoje são estudantes de disciplinas de teste. O uso de variáveis estáticas globais (em KeyManager e StateManager, por exemplo) dificultou os testes unitários e nos obrigou a usar Reflection em algumas classes de teste. |
| **Flexibilidade** | 3 | O jogo nunca foi distribuído para jogadores comuns, então basta um nível aceitável. Mesmo assim, há limitações: as imagens são lidas com new File("src/..."), o que faz o jogo funcionar só quando executado a partir da pasta do projeto, e a janela fixa de 1080 px de altura não cabe inteira em um monitor Full HD. |
| **Proteção (Safety)** | 2 | O jogo não tem efeitos piscantes que ofereçam risco a pessoas fotossensíveis. O único ponto de atenção é a música, que começa automaticamente no menu e não tem controle de volume no jogo. É desejável cuidar disso, mas hoje o risco é baixo. |

## 3. Conflito entre características

**Eficiência de Desempenho × Compatibilidade.** Para manter os 60 FPS, o loop de Game.run() verifica o relógio o tempo todo, sem nenhuma pausa. Isso deixa o intervalo entre frames preciso, mas ocupa um núcleo inteiro do processador e prejudica os outros programas abertos.

A solução óbvia seria colocar um Thread.sleep() no loop para liberar o processador. O problema é que o sleep pode durar bem mais do que o pedido: num teste rápido no computador de um integrante, Thread.sleep(1) durou em média cerca de 15 ms, quase um frame inteiro (16,7 ms). Isso poderia atrasar frames e causar travadas.

Como o jogador percebe uma travada na hora, mas dificilmente percebe o uso do processador durante uma partida curta, priorizamos o desempenho (nível 4) sobre a compatibilidade (nível 2).
