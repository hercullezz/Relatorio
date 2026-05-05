# 🎨 UI Design System — "Industrial Premium"

> Padrão visual do app. Toda tela nova deve seguir este guia.

---

## 1. Identidade Visual

**Tema:** Dark mode com elementos de glassmorphism e gradientes sutis.
**Sensação:** Profissional, robusto, industrial — não genérico.

---

## 2. Paleta de Cores (Referência)

| Uso | Descrição |
|---|---|
| **Background Global** | Gradiente escuro (dark navy → quase preto) aplicado no root |
| **Cards** | Superfície semi-transparente com bordas sutis (glassmorphism) |
| **Accent / CTA** | Azul-elétrico ou âmbar industrial (evitar vermelho/azul genérico) |
| **Texto Primário** | Branco / off-white de alta legibilidade |
| **Texto Secundário** | Cinza claro com opacidade reduzida |
| **Status: Pendente** | Âmbar / laranja |
| **Status: Aprovado** | Verde-esmeralda |
| **Status: Rejeitado** | Vermelho-escuro |

---

## 3. Layout Padrão de Telas

### Estrutura Global
- **Background gradient** aplicado no root da `MainActivity` (todas as telas herdam)
- Scaffolds com `containerColor = Color.Transparent` para mostrar o gradiente global
- **Header compacto** com: turno atual | horário de trabalho | data de trabalho

### Header Global
- Posicionado abaixo do status bar do Android (usar `WindowInsets` / `statusBarsPadding()`)
- Centralizado horizontalmente
- Exibe: Turno (ex: "3º Turno"), WorkTime (ex: "21:30–05:20"), WorkDate (ex: "21/03")

### Cards de Serviço
- Bordas arredondadas
- Leve elevação ou borda sutil
- Agrupados por linha de produção (com cabeçalho colorido por linha)

---

## 4. Componentes Padronizados

### Tela de Login / Cadastro
- Card centralizado sobre gradiente
- Campo de senha com toggle de visibilidade
- Botão principal com gradiente no background

### Painel Admin — Gerenciamento de Usuários
- Layout de **cards** (não lista simples)
- Ações por usuário: Aprovar/Reprovar | Tornar Admin | Reset Senha | Deletar
- Delete via **Cloud Function** do Parse (seguro, server-side)

### Tela de Configurações
- Consistência com o tema "Industrial Premium"
- Seções bem delimitadas

---

## 5. Micro-animações e Interatividade

- Hover/press states em todos os botões (ripple + scale sutil)
- Transições suaves entre telas
- Loading states com indicadores coerentes com o tema

---

## 6. Design de PDF Gerado

Regras de layout para PDFs de relatório:

| Seção | Regra |
|---|---|
| **Gráficos (Topo)** | Título centralizado "GRÁFICOS DE PRODUÇÃO"; grid 2 colunas; nome da linha acima de cada gráfico |
| **Serviços (Corpo)** | Agrupados por Linha de Produção; cabeçalho de linha com fundo Azul Marinho + texto branco |
| **Nomes de Máquina** | Dentro da seção da linha, exibir apenas o nome curto (ex: "Extrusora 01", não "Linha 1 – Extrusora 01") |
| **Redundância** | Não repetir "Linha" se o nome já contém (ex: evitar "LINHA Linha 1") |
| **Fotos** | Máximo 3 por serviço; grid compacto; tratamento de erro se download falhar |

---

## 7. Problemas de Layout Já Resolvidos

| Problema | Solução |
|---|---|
| Header sobrepondo status bar | Aplicar `statusBarsPadding()` ou `WindowInsets.statusBars` no Composable do header |
| Header desalinhado horizontalmente | Wrapper com `fillMaxWidth()` + `wrapContentWidth(Alignment.CenterHorizontally)` |
| Espaço vazio excessivo entre header e conteúdo | Revisar padding/spacer na Column principal da tela |

---

## 8. Indicador de Status do Turno (Simplificado em 2026-05-05)

O status do turno do usuário é indicado diretamente pela cor da fonte do texto do turno (ex: **T1**) no `CompactHeader.kt`.

### Estados e Cores
| Estado | Cor | Condição |
|---|---|---|
| **ATIVO** | Verde (`IndustrialTertiaryDark`) | `currentShiftId == userShiftId` |
| **HORA EXTRA** | Vermelho (`ErrorRed`) | Turno do usuário ainda está na janela `getVisibleShiftInfos()` (+1h) |
| **FORA DO TURNO** | Cinza (`IndustrialSecondaryDark`) | Fora da janela de trabalho permitida |

### Implementação
- **Lógica**: Compara o `shiftId` do usuário logado (Parse) com o turno atual detectado.
- **Atualização**: Automática a cada **60 segundos** via `LaunchedEffect`.
- **Acessibilidade**: As cores foram escolhidas da paleta "Industrial Dark" para garantir contraste sobre o fundo escuro/gradiente do app.
