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