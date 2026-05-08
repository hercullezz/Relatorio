# 🗺️ MAPA DO CONHECIMENTO — Sistema de Relatório de Manutenção

> **Como usar:** Cole este arquivo no início de cada conversa para que a IA tenha contexto completo sem precisar ler arquivos individuais. Consulte os arquivos na pasta `conhecimento/` apenas para detalhes aprofundados.

---

## 📌 IDENTIDADE DO PROJETO

| Campo | Valor |
|---|---|
| **Nome** | Sistema de Relatório de Manutenção |
| **Tipo** | App Android nativo |
| **Linguagem** | Kotlin |
| **UI Framework** | Jetpack Compose |
| **Banco Local** | Room (SQLite) |
| **Backend** | Parse Server |
| **Geração de PDF** | PdfDocument Android nativo |
| **Versão Atual** | 2.1 (versionCode 12) |
| **Min SDK / Target SDK** | 26 / 36 |
| **Repositório** | github.com/hercullezz/Relatorio |
| **APK mais recente** | app-v2.1-release.apk |
| **Estilo Visual** | "Industrial Premium" — dark mode, gradientes, glassmorphism |

---

## 🏭 CONTEXTO DO NEGÓCIO

Fábrica que opera **24h/dia com 3 turnos contínuos**. Um único dispositivo Android é compartilhado entre os operários de cada turno.

### Turnos
| Turno | Início | Fim | Duração | Detalhe |
|---|---|---|---|---|
| T1 | 05:00 | 13:40 | 8h40min | Matutino |
| T2 | 13:20 | 22:00 | 8h40min | Vespertino (overlap 20min com T1) |
| T3 | 21:30 | 05:20+1 | 7h50min | Noturno — **cruza meia-noite** |

### Overlaps de Turno
- `13:20–13:40` → T1 ↔ T2 (seleção manual pelo usuário)
- `21:30–22:00` → T2 ↔ T3 (seleção manual pelo usuário)

### ⭐ Conceito Crítico: WorkDate
**WorkDate** = data de INÍCIO do turno (não a data do relógio).
- T3 às 02:00 de 22/03 → `workDate = 21/03` (turno começou no dia anterior)
- Filtros SEMPRE usam `workDate`, nunca `createdAt`

---

## 👥 PERFIS DE USUÁRIO

| Perfil | Acesso | Pode Gerar PDF? |
|---|---|---|
| **Operário** | Só vê serviços do próprio turno | Sim, apenas do seu turno (até +1h após fim) |
| **Supervisor** | Vê TODOS os serviços | Sim, consolidado |
| **Administrador** | Tudo do Supervisor + CRUD usuários/máquinas/estoque | Sim, completo |

### Regra de Hora Extra (+1h)
Após o fim oficial do turno, o operário pode registrar/gerar PDF por mais **1 hora**:
- T1: até `14:40` | T2: até `23:00` | T3: até `06:20`
- Registros nesse período recebem flag `overtime = true`

---

## 🗄️ ESTRUTURA DE DADOS (Resumo)

| Tabela | Descrição | Campos-chave |
|---|---|---|
| `SERVIÇOS_MANUTENÇÃO` | Core do sistema | shiftId, workDate, overtime, approvalStatus, deleted |
| `USUÁRIOS` | Controle de acesso | shiftId, role (Operário/Supervisor/Admin), status |
| `MÁQUINAS` | Equipamentos | nome, linhaId, status |
| `LINHAS_PRODUÇÃO` | Agrupamento de máquinas | — |
| `ESTOQUE` | Peças/materiais | código, quantidade |

> Deleção é sempre **lógica** (flag `deleted=true`), nunca física.

---

## 🔧 FUNCIONALIDADES IMPLEMENTADAS

- [x] Sistema de turnos com overlaps e WorkDate para T3
- [x] Restrição de acesso por turno (operário só vê o seu)
- [x] Hora extra (+1h) com flag `overtime`
- [x] Registro no turno seguinte (com nota obrigatória)
- [x] Sincronização com Parse Server
- [x] Geração de PDF customizado (agrupado por linha, fotos em grid)
- [x] Sistema de aprovação (Supervisor: aprovar/rejeitar com nota)
- [x] CRUD de usuários com painel Admin (cards, aprovação, reset senha, delete via Cloud Function)
- [x] Login / Cadastro (UI "Industrial Premium" com gradiente)
- [x] Tela de Configurações
- [x] Auto-update via `version.json` + GitHub Releases
- [x] Header global compacto com turno/horário/data

## 🚧 NÃO IMPLEMENTADO / EM PROGRESSO

- [ ] Sistema de estoque integrado ao app
- [ ] Relatórios consolidados completos (múltiplos dias/turnos)
- [ ] Dashboards com gráficos de tendências
- [ ] Auditoria completa (logs detalhados)

---

## 🐛 PROBLEMAS CONHECIDOS / RESOLVIDOS RECENTEMENTE

| Problema | Status | Arquivo de Detalhe |
|---|---|---|
| ANR no startup (crash ao iniciar o app) | ✅ Resolvido | `conhecimento/bugs_e_solucoes.md` |
| Header com espaçamento incorreto no status bar Android | ✅ Resolvido | `conhecimento/ui_design_system.md` |
| WorkDate incorreta para T3 cruzando meia-noite | ✅ Resolvido | `conhecimento/logica_de_turnos.md` |
| Java Home inválido no Gradle build | ✅ Resolvido | `conhecimento/build_e_deploy.md` |

---

## 🗂️ MAPA DA PASTA `conhecimento/`

| Arquivo | Conteúdo |
|---|---|
| `conhecimento/logica_de_turnos.md` | Cálculo de turno, WorkDate, overlaps, hora extra — a lógica mais crítica |
| `conhecimento/ui_design_system.md` | Paleta "Industrial Premium", componentes, padrões de layout Compose |
| `conhecimento/arquitetura_e_dados.md` | Estrutura de pastas do projeto, camadas (ViewModel, Repository, Parse), tabelas |
| `conhecimento/build_e_deploy.md` | Como buildar, assinar APK, fazer release no GitHub, Java path |
| `conhecimento/bugs_e_solucoes.md` | Registro de bugs resolvidos e soluções aplicadas |
| `conhecimento/perfis_e_permissoes.md` | Regras detalhadas por perfil de usuário e fluxos de aprovação |

---

## 🗂️ ARTEFATOS GOOGLE SHEETS (Ferramentas Separadas)

> Estes arquivos são **prompts prontos** para copiar e colar em outras IAs (ChatGPT, Gemini) para implementação da versão Google Sheets do sistema. Não são documentação do app Android.

| Arquivo | Uso |
|---|---|
| `PROMPT_GOOGLE_SHEETS.md` | Prompt completo e detalhado — usar para conversa iterativa com IA |
| `RESUMO_VISUAL_GOOGLE_SHEETS.md` | Versão com diagramas e checklists — útil para visão rápida ou reunião |
| `PROMPT_CONCISO_GOOGLE_SHEETS.txt` | Versão curta para copy-paste direto em outra IA |

---

## ⚡ REGRAS DE OURO (Nunca Esquecer)

1. **WorkDate ≠ Data do Relógio** para T3 após meia-noite
2. **Filtros sempre por `workDate`**, nunca por `createdAt`
3. **Overlaps exigem seleção manual** do usuário (13:20–13:40 e 21:30–22:00)
4. **Hora Extra ≠ Outro Turno** — são regras distintas
5. **Deleção sempre lógica** (`deleted = true`)
6. **Supervisor vê tudo**, Operário vê só o seu turno
7. Qualquer mudança nos horários de turno → atualizar `BUSINESS_RULES.txt` + `conhecimento/logica_de_turnos.md`

---

*Última atualização: 2026-05-05 | Versão do app: 2.0*

> **Arquivos removidos (conteúdo migrado para `conhecimento/`):** `ASSISTANCE.md`, `NEXT_STEPS.md`, `SHIFT_LOGIC_DOCUMENTATION.md`, `SUMARIO_EXECUTIVO_PROJETO.md`
