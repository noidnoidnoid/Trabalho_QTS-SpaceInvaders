// Captura telas do SonarQube local para o relatorio de inspecao (Entrega 2, item 6).
// Gera sempre as mesmas telas, com o mesmo tamanho, para comparar "antes" e "depois".
//
// Uso (a partir de qualquer pasta):
//   SONAR_PASSWORD=<senha do admin> NODE_PATH=<pasta com node_modules/playwright> \
//     node docs/sonar/scripts/capturar-prints.cjs <antes|depois> [chave_do_projeto]
//
// As imagens vao para docs/sonar/before/ (antes) ou docs/sonar/after/ (depois).
const fs = require("fs");
const path = require("path");
const { chromium } = require("playwright");

const HOST = process.env.SONAR_HOST || "http://localhost:9000";
const label = process.argv[2];
const project = process.argv[3] || "space-invaders-comprehensive";

if (!["antes", "depois"].includes(label) || !process.env.SONAR_PASSWORD) {
  console.error("uso: SONAR_PASSWORD=... node capturar-prints.cjs <antes|depois> [chave_do_projeto]");
  process.exit(2);
}

// Classe sob teste de cada integrante (ver README.md da raiz).
const classes = [
  ["Ship", "src/br/characters/Ship.java"],
  ["Level1State", "src/br/states/Level1State.java"],
  ["Level2State", "src/br/states/Level2State.java"],
  ["Game", "src/br/Game.java"],
  ["AudioPlayer", "src/br/audio/AudioPlayer.java"],
];

const pad = (n) => String(n).padStart(2, "0");
const shots = [
  ["01-visao-geral", `/dashboard?id=${project}`],
  ...classes.flatMap(([name, file], i) => {
    const base = `/project/issues?id=${project}&files=${encodeURIComponent(file)}`;
    const shotsForClass = [[`${pad(i + 2)}-issues-${name}`, `${base}&issueStatuses=OPEN%2CCONFIRMED`]];
    // No "depois", tambem as issues que o Sonar marcou como corrigidas (status Fixed).
    if (label === "depois") shotsForClass.push([`${pad(i + 2)}b-issues-${name}-corrigidas`, `${base}&issueStatuses=FIXED`]);
    return shotsForClass;
  }),
  [`${pad(classes.length + 2)}-complexidade-por-arquivo`, `/component_measures?id=${project}&metric=complexity&view=list`],
  [`${pad(classes.length + 3)}-perfil-de-regras`, `/project/information?id=${project}`],
];

const VIEWPORT = { width: 1440, height: 900 };

// O Sonar rola o conteudo dentro de um painel interno, entao fullPage nao basta:
// aumenta a altura da janela ate o maior painel rolavel caber inteiro.
async function fitToContent(page) {
  for (let i = 0; i < 4; i++) {
    const extra = await page.evaluate(() => {
      let max = document.documentElement.scrollHeight - window.innerHeight;
      for (const el of document.querySelectorAll("*")) {
        const oy = getComputedStyle(el).overflowY;
        if ((oy === "auto" || oy === "scroll") && el.scrollHeight > el.clientHeight + 1) {
          max = Math.max(max, el.scrollHeight - el.clientHeight);
        }
      }
      return max;
    });
    if (extra <= 0) return;
    const { width, height } = page.viewportSize();
    await page.setViewportSize({ width, height: Math.min(height + extra, 8000) });
    await page.waitForTimeout(800);
  }
}

(async () => {
  // Segue as pastas previstas no README da raiz: docs/sonar/before e docs/sonar/after.
  const outDir = path.join(__dirname, "..", label === "antes" ? "before" : "after");
  fs.mkdirSync(outDir, { recursive: true });

  const browser = await chromium.launch();
  const context = await browser.newContext({ viewport: VIEWPORT });

  // Login pela API: a sessao fica nos cookies do mesmo contexto do navegador.
  const login = await context.request.post(`${HOST}/api/authentication/login`, {
    form: { login: "admin", password: process.env.SONAR_PASSWORD },
  });
  if (!login.ok()) throw new Error(`login falhou: HTTP ${login.status()}`);

  const page = await context.newPage();
  for (const [name, url] of shots) {
    await page.setViewportSize(VIEWPORT);
    await page.goto(HOST + url);
    await page.waitForLoadState("networkidle");
    await page.waitForTimeout(1500); // graficos e contadores terminam de desenhar

    if (name.includes("-issues-")) {
      // Abre o filtro "File": com zero resultados, e o unico lugar que mostra qual arquivo foi filtrado.
      const facet = page.getByRole("button", { name: /^File/ }).first();
      if (await facet.count()) {
        await facet.click();
        await page.waitForTimeout(1200);
      }
    }

    if (name.endsWith("visao-geral")) {
      // Com mais de uma analise, o painel abre em "New Code"; o retrato completo e "Overall Code".
      await page.getByRole("tab", { name: "Overall Code" }).click();
      await page.waitForTimeout(1500);
    }

    await fitToContent(page);
    const file = path.join(outDir, `${name}.png`);
    let clip;
    if (name.endsWith("perfil-de-regras")) {
      // Recorta so o quadro "About this Project": o painel ao lado mostra o token do badge.
      clip = await page.evaluate(() => {
        const h = [...document.querySelectorAll("h1,h2,h3,h4")].find((e) => e.textContent.trim() === "About this Project");
        if (!h) return null;
        let el = h;
        while (el.parentElement && el.getBoundingClientRect().height < 400) el = el.parentElement;
        const r = el.getBoundingClientRect();
        return { x: 0, y: 0, width: Math.ceil(r.right + 24), height: Math.ceil(r.bottom + 24) };
      });
      if (!clip) throw new Error("quadro 'About this Project' nao encontrado; print nao gerado para nao expor o token do badge");
    }
    await page.screenshot(clip ? { path: file, clip } : { path: file, fullPage: true });

    const shown = (await page.textContent("body")).match(/(\d+) of (\d+) shown/);
    console.log("salvo:", path.basename(file), shown ? `(${shown[0]})` : "");
  }
  await browser.close();
})().catch((e) => {
  console.error("FALHOU:", e.message);
  process.exit(1);
});
