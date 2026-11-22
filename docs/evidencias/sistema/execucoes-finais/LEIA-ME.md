# Execuções finais da suíte de integração e sistema

Três execuções seguidas de `mvn verify -Psystem` (22/11/2026), com o critério revisto do CTS-10.

- Resultado: todos os casos passaram nas 3 execuções (22 de integração, com 1 pulado de propósito porque a máquina tem placa de som, e 11 de sistema). Ver `resumo.txt` e os XML de cada execução.
- Medições de desempenho de cada execução: `execucao-N/desempenho.txt`.
- Screenshots dos casos de sistema (execução 3): `screenshots/`.
- Log de GC de cada processo do jogo (execução 3): `gc-execucao-3/`.

As execuções anteriores, com o critério estrito do CTS-10, estão em `../tres-execucoes-criterio-estrito/`.
