# 🐛 Bugs e Soluções

> Registro de bugs encontrados e resolvidos. Consultar antes de investigar regressões.

---

## Bug #001 — ANR no Startup (App Not Responding)

**Status:** ✅ Resolvido
**Conversa de referência:** f6a055a9-8d9f-4b02-957b-cbe80aa59498
**Data:** 2026-05-03

### Sintoma
App travava (ANR) durante a sequência de inicialização, impossibilitando a abertura.

### Causa
Operação pesada (I/O, rede ou consulta ao banco) sendo executada na **thread principal (Main Thread)** durante o startup.

### Solução
Mover operações bloqueantes para coroutines / threads de background (Dispatchers.IO). Garantir que a Main Thread só faça trabalho de UI.

### Lição
Qualquer consulta ao Parse Server, Room ou operação de I/O **deve** usar `viewModelScope.launch(Dispatchers.IO)` ou equivalente. Nunca chamar diretamente na Main Thread.

---

## Bug #002 — Header Sobrepondo Status Bar do Android

**Status:** ✅ Resolvido
**Conversa de referência:** c2b4455a-0025-4584-b34f-432284c782c7
**Data:** 2026-04-26

### Sintoma
O header da `MainApp` screen ficava posicionado incorretamente, com espaço excessivo ou sobreposição com a barra de status do sistema Android.

### Causa
Falta de tratamento de `WindowInsets` no Composable do header.

### Solução
Aplicar `statusBarsPadding()` ou usar `WindowInsets.statusBars` no modifier do container do header. Garantir alinhamento horizontal com `fillMaxWidth()`.

---

## Bug #003 — WorkDate Incorreta para T3 Após Meia-Noite

**Status:** ✅ Resolvido (lógica documentada em `logica_de_turnos.md`)
**Data:** Identificado na fase inicial do projeto

### Sintoma
Serviços registrados entre `00:00–05:20` (T3 pós meia-noite) apareciam com a data errada nos filtros, "sumindo" quando o usuário filtrava pelo dia anterior.

### Causa
O sistema usava a data do relógio (`createdAt`) em vez da `workDate` para filtrar.

### Solução
Implementar o conceito de `WorkDate`:
- T3 pós meia-noite (00:00–05:20) → `workDate = dia anterior`
- Salvar sempre como `00:00:00` no Parse
- Filtros sempre comparam por `workDate` como String `YYYY-MM-DD`

---

## Bug #004 — "Java home supplied is invalid" no Build

**Status:** ✅ Resolvido
**Data:** Durante setup do ambiente

### Sintoma
`gradlew.bat assembleDebug` falha com erro `Java home supplied is invalid`.

### Causa
Caminho do Java 17 incorreto ou não definido no `gradle.properties`.

### Solução
Atualizar `org.gradle.java.home` em `gradle.properties` com o caminho correto do JDK 17 (usando `\\` duplo no Windows).
Ver detalhes completos em `conhecimento/build_e_deploy.md`.

---

## Template para Novos Bugs

```markdown
## Bug #XXX — [Título Descritivo]

**Status:** 🔴 Aberto | 🟡 Em investigação | ✅ Resolvido
**Conversa de referência:** [conversation-id]
**Data:** YYYY-MM-DD

### Sintoma
[O que o usuário observa]

### Causa
[Root cause identificada]

### Solução
[O que foi feito para resolver]

### Lição
[O que aprendemos para evitar regressão]
```
