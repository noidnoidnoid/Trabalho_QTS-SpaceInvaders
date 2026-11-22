"""Gera um retrato (baseline) da analise do SonarQube local em Markdown.

Uso: python3 -I sonar_baseline.py <senha_admin> <arquivo_saida.md> <rotulo> [chave_projeto]
"""
import base64
import json
import sys
import urllib.parse
import urllib.request
from collections import Counter, defaultdict
from datetime import datetime

HOST = "http://localhost:9000"
PROJECT = "space-invaders-comprehensive"


def get(path, auth, **params):
    url = f"{HOST}{path}?{urllib.parse.urlencode(params)}"
    req = urllib.request.Request(url, headers={"Authorization": f"Basic {auth}"})
    with urllib.request.urlopen(req, timeout=30) as resp:
        return json.load(resp)


def main():
    global PROJECT
    password, out_path, label = sys.argv[1], sys.argv[2], sys.argv[3]
    if len(sys.argv) > 4:
        PROJECT = sys.argv[4]
    auth = base64.b64encode(f"admin:{password}".encode()).decode()

    # Metricas que existem nesta versao do servidor (evita 404 por chave desconhecida).
    wanted = ["ncloc", "complexity", "coverage", "branch_coverage", "violations",
              "bugs", "vulnerabilities", "code_smells", "security_hotspots",
              "duplicated_lines_density", "sqale_index", "reliability_rating",
              "security_rating", "sqale_rating"]
    available = set()
    page = 1
    while True:
        r = get("/api/metrics/search", auth, ps=500, p=page)
        available |= {m["key"] for m in r["metrics"]}
        if page * 500 >= r["total"]:
            break
        page += 1
    keys = [k for k in wanted if k in available]

    proj = get("/api/measures/component", auth, component=PROJECT, metricKeys=",".join(keys))
    pm = {m["metric"]: m.get("value", "-") for m in proj["component"]["measures"]}

    tree = get("/api/measures/component_tree", auth, component=PROJECT, qualifiers="FIL",
               metricKeys="violations,complexity,branch_coverage,ncloc", ps=500)

    issues = []
    page = 1
    while True:
        r = get("/api/issues/search", auth, components=PROJECT, ps=500, p=page,
                issueStatuses="OPEN,CONFIRMED")
        issues += r["issues"]
        if page * 500 >= r["paging"]["total"]:
            break
        page += 1

    analyses = get("/api/project_analyses/search", auth, project=PROJECT, ps=1)
    when = analyses["analyses"][0]["date"] if analyses["analyses"] else "?"

    rating = {"1.0": "A", "2.0": "B", "3.0": "C", "4.0": "D", "5.0": "E"}

    def sev(issue):
        impacts = issue.get("impacts") or []
        if impacts:
            return "/".join(f'{i["softwareQuality"].lower()}:{i["severity"].lower()}' for i in impacts)
        return issue.get("severity", "?").lower()

    by_file = defaultdict(list)
    for i in issues:
        by_file[i["component"].split(":", 1)[1]].append(i)

    lines = [f"# SonarQube: {label}", "",
             "- Servidor: SonarQube Community Build 26.11.0.134175 (local), scanner Maven 5.9.0.7382",
             f"- Data da análise no servidor: `{when}`",
             f"- Gerado em: `{datetime.now().isoformat(timespec='seconds')}`",
             f"- Projeto: `{PROJECT}` (código em `src/br/**`, testes em `src/test/java/**`)", "",
             "## Visão geral do projeto", "", "| Métrica | Valor |", "| :--- | ---: |"]
    names = {"ncloc": "Linhas de código", "complexity": "Complexidade ciclomática (total)",
             "coverage": "Cobertura (%)", "branch_coverage": "Cobertura de arestas/condições (%)",
             "violations": "Issues abertas", "bugs": "Bugs", "vulnerabilities": "Vulnerabilidades",
             "code_smells": "Code smells", "security_hotspots": "Security hotspots",
             "duplicated_lines_density": "Duplicação (%)", "sqale_index": "Dívida técnica (min)",
             "reliability_rating": "Nota de confiabilidade", "security_rating": "Nota de segurança",
             "sqale_rating": "Nota de manutenibilidade"}
    for k in keys:
        v = pm.get(k, "-")
        if k.endswith("_rating"):
            v = rating.get(v, v)
        lines.append(f"| {names.get(k, k)} | {v} |")

    lines += ["", "## Por arquivo", "",
              "| Arquivo | Issues | Complexidade | Cobertura de arestas (%) | Linhas |",
              "| :--- | ---: | ---: | ---: | ---: |"]
    rows = []
    for c in tree["components"]:
        m = {x["metric"]: x.get("value", "-") for x in c.get("measures", [])}
        rows.append((c["path"], int(m.get("violations", 0) or 0), m.get("complexity", "-"),
                     m.get("branch_coverage", "-"), m.get("ncloc", "-")))
    for path, v, cx, bc, nl in sorted(rows, key=lambda r: (-r[1], r[0])):
        lines.append(f"| `{path}` | {v} | {cx} | {bc} | {nl} |")

    lines += ["", "## Regras mais violadas", "", "| Regra | Ocorrências |", "| :--- | ---: |"]
    for rule, n in Counter(i["rule"] for i in issues).most_common(15):
        lines.append(f"| `{rule}` | {n} |")

    lines += ["", "## Issues por arquivo", ""]
    for path in sorted(by_file, key=lambda p: (-len(by_file[p]), p)):
        lines += [f"### `{path}` ({len(by_file[path])})", "",
                  "| Linha | Regra | Impacto | Mensagem |", "| ---: | :--- | :--- | :--- |"]
        for i in sorted(by_file[path], key=lambda i: i.get("line", 0)):
            msg = i["message"].replace("|", "\\|")
            lines.append(f'| {i.get("line", "-")} | `{i["rule"]}` | {sev(i)} | {msg} |')
        lines.append("")

    with open(out_path, "w", encoding="utf-8") as fh:
        fh.write("\n".join(lines) + "\n")
    print(f"{len(issues)} issues abertas; arquivo: {out_path}")


if __name__ == "__main__":
    main()
