# Três execuções da suíte de sistema com o critério estrito do CTS-10

Executadas em 22/11/2026, depois das correções das issues #11 e #12, com o CTS-10 ainda na primeira versão (nenhum frame acima de 33,3 ms em 10 s).

Resultado: todos os casos passaram nas 3 execuções, menos o CTS-10, que falhou nas execuções 1 e 2 com um único pico cada (41,9 ms e 33,6 ms). Os demais segundos ficaram entre 2,5 e 8,3 ms.

Esses dados motivaram a investigação em `docs/ai/roger/evidencias/2026-11-22-investigacao-cts10.txt` e a revisão do critério descrita em `src/test/java/br/system/PerformanceSystemIT.java`. As execuções com o critério revisto estão em `../execucoes-finais/`.
