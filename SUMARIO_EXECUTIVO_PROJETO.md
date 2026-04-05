# Sumário Executivo do Projeto
## Sistema de Relatório de Manutenção - Fábrica 24h

---

## 📋 VISÃO GERAL DO PROJETO

Você desenvolveu um **aplicativo Android em Kotlin/Compose** que gerencia relatórios de manutenção em uma fábrica que funciona 24 horas com 3 turnos contínuos. Agora precisa replicar essa lógica em **Google Sheets** para análise de dados e relatórios.

### Stack Atual (Android)
- **Linguagem**: Kotlin
- **Framework UI**: Jetpack Compose
- **Banco de Dados Local**: Room (SQLite)
- **Backend**: Parse Server
- **Geração de PDF**: PdfDocument Android nativo
- **Versão**: 1.3 (Build 4)
- **Min SDK**: 26 | Target SDK: 36

---

## 🏭 NEGÓCIO: FÁBRICA 24/3 TURNOS

### Horários Operacionais

| Turno | Horário | Duração | Observação |
|-------|---------|---------|-----------|
| **T1** | 05:00 - 13:40 | 8h 40min | Turno matutino |
| **T2** | 13:20 - 22:00 | 8h 40min | Turno vespertino (overlap com T1: 20min) |
| **T3** | 21:30 - 05:20⭐ | 7h 50min | Turno noturno (overlap com T2: 30min) |

⭐ **CRÍTICO**: Turno 3 cruza meia-noite (começa dia X, termina dia X+1)

### Problema Resolvido: WorkDate

**Situação Confusa**: Um serviço registrado às 02:00 de 22/03 (durante Turno 3) deveria aparecer no "dia 21/03 Turno 3", não no dia 22.

**Solução Implementada**: Conceito de **WorkDate** (Data de Trabalho) = data de INÍCIO do turno

```
Serviço às 22:30 de 21/03 → WorkDate = 21/03 ✓
Serviço às 02:00 de 22/03 → WorkDate = 21/03 ✓ (mesmo turno!)
Filtro por "21/03 T3" → Mostra ambos os serviços ✓
```

---

## 👥 PERSONAS E PERMISSÕES

### 1. **OPERÁRIO** (Nível Base)
- **Função**: Registra serviços de manutenção realizados
- **Pode**: 
  - Adicionar serviço dentro de seu turno + 1 hora
  - Visualizar dados apenas do seu turno
  - Editar/remover seus próprios serviços
  - Gerar PDF de seu turno
- **Não Pode**:
  - Ver serviços de outros turnos
  - Aprovar serviços
  - Gerenciar usuários

### 2. **SUPERVISOR** (Nível Intermediário)
- **Função**: Valida e aprova serviços registrados
- **Pode**:
  - Ver TODOS os serviços sem restrição
  - Aprovar/Rejeitar com notas
  - Gerar relatórios consolidados
  - Filtrar por múltiplos critérios
- **Não Pode**:
  - Deletar dados fisicamente
  - Gerenciar usuários

### 3. **ADMINISTRADOR** (Nível Máximo)
- **Função**: Gerencia usuários, máquinas e relatórios
- **Pode**:
  - Tudo que Supervisor faz, mais:
  - CRUD de usuários (criar, editar, desativar)
  - CRUD de máquinas
  - CRUD de estoque
  - Ver logs de auditoria
  - Backup de dados

---

## 🔧 REGRAS DE NEGÓCIO (CRÍTICAS)

### Regra 1: Restrição por Turno
```
Um operário SÓ PODE adicionar serviços durante seu turno de trabalho.
Validação: (TurnoAtualCalculado == TurnoDoOperário) OU (EstáEmHoraExtra)
```

### Regra 2: Hora Extra (até +1 hora após fim do turno)
```
Turno 1: 05:00-13:40  →  Pode registrar até 14:40
Turno 2: 13:20-22:00  →  Pode registrar até 23:00
Turno 3: 21:30-05:20  →  Pode registrar até 06:20

Flag: overtime = true para esses registros
```

### Regra 3: Registrar em Turno Anterior
```
Se não registrou durante seu turno, pode registrar no turno seguinte,
MAS deve indicar que é do turno anterior com nota obrigatória.

Exemplo:
- Operário T1 não registrou durante turno
- No Turno 2, pode registrar com ShiftId=1 (override manual)
- EXIGIR nota: "Serviço esquecido do turno anterior"
```

### Regra 4: Overlaps (períodos de transição)
```
13:20-13:40 (T1 ↔ T2): Permitir seleção manual
21:30-22:00 (T2 ↔ T3): Permitir seleção manual
```

---

## 📊 ESTRUTURA DE DADOS

### Tabela 1: SERVIÇOS_MANUTENÇÃO (16 colunas)
Núcleo do sistema. Cada linha = 1 serviço registrado.

| Campo | Tipo | Obrigatório | Notas |
|-------|------|-------------|-------|
| ID | Int | ✓ | Auto-gerado |
| UUID_ÚNICO | String | ✓ | Para sincronizar com app Android |
| USUÁRIO_ID | Int | ✓ | Quem registrou |
| MÁQUINA | String | ✓ | Nome do equipamento |
| TIPO_SERVIÇO | Enum | ✓ | Limpeza, Lubrificação, Reparo, Inspeção, Outro |
| DESCRIÇÃO | String | ✓ | Descrição detalhada (min 10 caracteres) |
| DATA_HORA_REGISTRO | DateTime | ✓ | Quando foi criado (timestamp) |
| WORK_DATE | Date | ✓ | Data de trabalho (CRÍTICO para T3) |
| TURNO_ID | Int | ✓ | 1, 2 ou 3 |
| É_HORA_EXTRA | Bool | ✓ | true se registrado após fim do turno |
| FOTOS_URL | String | ✗ | URLs separadas por vírgula (max 3) |
| STATUS_APROVAÇÃO | Enum | ✓ | Pendente, Aprovado, Rejeitado |
| SUPERVISOR_ID | Int | ✗ | Quem aprovou |
| DATA_APROVAÇÃO | DateTime | ✗ | Quando foi aprovado |
| NOTAS_SUPERVISOR | String | ✗ | Comentários da validação |
| DELETADO | Bool | ✓ | Marcação lógica (não deletar fisicamente) |

### Tabela 2: USUÁRIOS (7 colunas)
Gerenciamento de acesso.

| Campo | Tipo |
|-------|------|
| ID_USUÁRIO | Int (PK) |
| NOME_COMPLETO | String |
| EMAIL | String |
| TURNO_ID | Int (1-3) |
| PERFIL | Enum: Operário, Supervisor, Admin |
| STATUS | Enum: Ativo, Inativo |
| CPF | String |

### Tabela 3: MÁQUINAS (5 colunas)
Equipamentos que recebem manutenção.

| Campo | Tipo |
|-------|------|
| ID_MÁQUINA | Int (PK) |
| NOME_MÁQUINA | String |
| LINHA_PRODUÇÃO_ID | Int (FK) |
| STATUS | Enum: Ativa, Inativa, Em Reparo |
| LOCALIZAÇÃO | String |

### Tabela 4: LINHAS_PRODUÇÃO (3 colunas)
Agrupamento de máquinas.

| Campo | Tipo |
|-------|------|
| ID_LINHA | Int (PK) |
| NOME_LINHA | String |
| DESCRIÇÃO | String |

### Tabela 5: ESTOQUE (5 colunas)
Peças e materiais disponíveis.

| Campo | Tipo |
|-------|------|
| ID_ITEM | Int (PK) |
| CÓDIGO_ITEM | String |
| DESCRIÇÃO | String |
| LOCALIZAÇÃO | String |
| QUANTIDADE | Int |

---

## 🎯 FUNCIONALIDADES A IMPLEMENTAR EM GOOGLE SHEETS

### Dashboard
- [ ] Resumo do turno atual (hora, turno detectado)
- [ ] Total de serviços registrados hoje (por turno)
- [ ] Serviços com status pendente
- [ ] Últimos 5 serviços adicionados
- [ ] Gráfico de serviços por máquina (hoje)

### Tela de Adição
- [ ] Formulário com campos: Máquina, Tipo, Descrição, Fotos
- [ ] Auto-detecção de turno (com validação de overlap)
- [ ] Auto-detecção de hora extra
- [ ] Validações (campos obrigatórios, caracteres mínimos)
- [ ] Bloqueio fora do horário permitido

### Filtros e Histórico
- [ ] Filtro "Turno Atual" (turno + 1 hora)
- [ ] Filtro "Dia Específico" (data + turnos)
- [ ] Filtro "Período" (data início até fim)
- [ ] Filtro "Por Máquina"
- [ ] Busca livre (máquina ou descrição)
- [ ] Ordenação (data, máquina, turno, status)

### Aprovação (Supervisor)
- [ ] Tela de serviços pendentes
- [ ] Botões Aprovar/Rejeitar
- [ ] Campo de notas do supervisor
- [ ] Histórico de aprovações

### Gerenciamento (Admin)
- [ ] CRUD de usuários
- [ ] CRUD de máquinas
- [ ] CRUD de linhas
- [ ] CRUD de estoque
- [ ] Logs de auditoria

### Relatórios
- [ ] Relatório diário por turno (PDF)
- [ ] Relatório consolidado (múltiplos dias/turnos)
- [ ] Gráficos de tendências
- [ ] Relatório por máquina

---

## 🧮 CÁLCULOS CRÍTICOS

### 1. Auto-Detecção de Turno
```
HoraAtual = hora em formato 24h

SE 05:00 ≤ HoraAtual < 13:40:  Turno = 1
SE 13:20 ≤ HoraAtual < 22:00:  Turno = 2
SE 21:30 ≤ HoraAtual < 05:20:  Turno = 3

ESPECIAL: 13:20-13:40 e 21:30-22:00 (overlaps)
          → Permitir seleção manual
```

### 2. Cálculo de WorkDate (MAIS IMPORTANTE)
```
SE Turno ∈ [1, 2]:
    WorkDate = Dia Civil Atual

SE Turno = 3:
    SE 21:30 ≤ HoraAtual ≤ 23:59:
        WorkDate = Dia Civil Atual
    SE 00:00 ≤ HoraAtual < 05:20:
        WorkDate = Dia Civil ANTERIOR
```

**Exemplo Concreto**:
```
Registros em 22/03 às 02:00 (Turno 3 que começou em 21/03)
→ WorkDate = 21/03 (não 22/03!)
→ Filtro "21/03 T3" mostra este serviço ✓
```

### 3. Detecção de Hora Extra
```
HoraFimTurno = [13:40, 22:00, 05:20] conforme turno

SE HoraRegistro > HoraFimTurno E HoraRegistro ≤ HoraFimTurno + 1h:
    É_Hora_Extra = SIM
SENÃO:
    É_Hora_Extra = NÃO
```

### 4. Visibilidade de Turno (Tolerância +1h)
```
Turno 1: visível de 05:00 até 14:40
Turno 2: visível de 13:20 até 23:00
Turno 3: visível de 21:30 até 06:20 (próximo dia)

Operário vê apenas seu turno durante esse período.
Após a tolerância, acesso bloqueado (exceto em relatórios consolidados).
```

---

## 📱 VALIDAÇÕES ESPERADAS

| Campo | Validação |
|-------|-----------|
| **Máquina** | Obrigatório, Dropdown, Deve existir |
| **Tipo** | Obrigatório, Dropdown |
| **Descrição** | Obrigatório, Mín 10 caracteres, Máx 500 |
| **Turno** | Auto-detectado, Editável com overlap, Exigir nota se ≠ turno do usuário |
| **Fotos** | Opcional, Max 3 URLs, Separadas por vírgula |
| **Data** | Não pode ser futura |
| **Nota** | Obrigatória se turno ≠ turno do usuário |

---

## 📈 EXEMPLO PRÁTICO COMPLETO

**Cenário**: Maria, operária do Turno 3, registra manutenção às 03:00 de 22/03.

```
ENTRADA:
├─ Hora: 03:00 de 22/03/2026
├─ Usuário: Maria (Turno 3, cadastrada)
├─ Máquina: "Máquina B - Linha 2"
├─ Tipo de Serviço: "Lubrificação"
├─ Descrição: "Realizada manutenção preventiva conforme cronograma"
├─ Fotos: [URL_foto1, URL_foto2]
└─ Nota: (nenhuma, pois turno está correto)

CÁLCULOS AUTOMÁTICOS:
├─ TurnoID = 3 ✓ (03:00 está em [21:30, 05:20])
├─ WorkDate = 21/03/2026 ✓ (Turno 3, hora < 05:20 → dia anterior)
├─ É_Hora_Extra = NÃO ✓ (03:00 < 05:20, dentro do turno)
├─ DATA_HORA_REGISTRO = 22/03/2026 03:00:00
├─ STATUS = Pendente
└─ DELETADO = Não

BANCO DE DADOS:
Registro inserido na tabela SERVIÇOS_MANUTENÇÃO
├─ ID: 1245 (auto)
├─ UUID: X7F2A-9K3M-... (único para sincronizar com app)
├─ USUÁRIO_ID: 7 (Maria)
├─ MÁQUINA: "Máquina B"
├─ WORK_DATE: 21/03/2026 (CRÍTICO - não 22/03!)
├─ TURNO_ID: 3
└─ STATUS_APROVAÇÃO: Pendente

VISIBILIDADE:
├─ Maria (T3): Pode ver ✓ (está em seu turno + 1h)
├─ João (T1): Não pode ver (seu turno acabou)
├─ Supervisor: Pode ver ✓ (sem restrição)
├─ Admin: Pode ver ✓ (sem restrição)

FILTRO:
├─ Filtro "21/03 Turno 3" → Mostra este serviço ✓
├─ Filtro "22/03 Turno 3" → NÃO mostra (pois WorkDate=21/03)
└─ Filtro "Por Máquina B" → Mostra este serviço ✓
```

---

## 🚀 ROADMAP DE IMPLEMENTAÇÃO

### **Fase 1: Estrutura Base** (Semana 1)
- [ ] Criar 5 abas com tabelas
- [ ] Auto-increments e validações básicas
- [ ] Autenticação de usuários (simples)

### **Fase 2: Cálculos e Validações** (Semana 1-2)
- [ ] Implementar fórmula de Turno
- [ ] Implementar fórmula de WorkDate
- [ ] Auto-detecção de hora extra
- [ ] Validações de entrada

### **Fase 3: Formulário e UI** (Semana 2)
- [ ] Criar abas de entrada formatadas
- [ ] Data Validation (dropdowns)
- [ ] Mensagens de erro/sucesso

### **Fase 4: Filtros Dinâmicos** (Semana 2-3)
- [ ] 5 tipos de filtro
- [ ] Ordenação múltipla
- [ ] Busca em tempo real

### **Fase 5: Aprovação** (Semana 3)
- [ ] Tela de pendentes
- [ ] Sistema de votação (Aprovar/Rejeitar)
- [ ] Histórico de decisões

### **Fase 6: Relatórios** (Semana 3-4)
- [ ] PDF diário
- [ ] PDF consolidado
- [ ] Gráficos básicos

### **Fase 7: Admin** (Semana 4)
- [ ] CRUD de usuários
- [ ] CRUD de máquinas
- [ ] Logs simples

### **Fase 8: Polimento** (Semana 4-5)
- [ ] Temas/cores
- [ ] Responsividade
- [ ] Performance
- [ ] Documentação

---

## 💡 DIFERENCIAIS TÉCNICOS

🟢 **Já Implementado no App Android**:
- Sistema de Turnos com overlaps
- Conceito de WorkDate para Turno 3
- Sincronização Parse Server
- Geração de PDF customizado
- Sistema de aprovação de supervisor
- Filtros avançados
- CRUD de usuários/máquinas

🟡 **Parcialmente No App**:
- Auditoria (logs básicos)
- Backup de dados
- Gráficos e dashboards visuais

🔴 **Não Implementado**:
- Sistema de estoque integrado
- Relatórios consolidados completos
- Integração com outros sistemas

---

## 📞 COMO USAR OS ARQUIVOS CRIADOS

Você tem 3 arquivos prontos para usar:

1. **`PROMPT_GOOGLE_SHEETS.md`** - Prompt COMPLETO e muito detalhado (usar para prompt complexo)
2. **`RESUMO_VISUAL_GOOGLE_SHEETS.md`** - Diagramas, checklists e visual (usar para entender rápido)
3. **`PROMPT_CONCISO_GOOGLE_SHEETS.txt`** - Versão curta para copy-paste (usar em otra IA)

### Como Usar:

```
OPÇÃO 1 - Chat detalhado:
└─ Copie o PROMPT_GOOGLE_SHEETS.md inteiro
└─ Cole em ChatGPT, Claude, Gemini
└─ Converse iterativamente sobre implementação

OPÇÃO 2 - Visão rápida:
└─ Leia RESUMO_VISUAL_GOOGLE_SHEETS.md
└─ Use para refrescar a memória sobre o projeto
└─ Mostre pro time em reunião (bom para spike)

OPÇÃO 3 - Direto ao ponto:
└─ Copie PROMPT_CONCISO_GOOGLE_SHEETS.txt
└─ Cole em outra IA
└─ Pergunte: "OK, posso começar implementando qual parte?"
```

---

## 🎓 PONTOS-CHAVE A LEMBRAR

✅ **WorkDate é CRÍTICO** - Turno 3 que cruza meia-noite causa 95% dos bugs se mal implementado

✅ **Restrições de Turno são Negócio** - Não é limite técnico, é politica da fábrica

✅ **Overlaps precisam de Input Manual** - 13:20-13:40 e 21:30-22:00 permitem seleção do usuário

✅ **Hora Extra ≠ Outro Turno** - São 2 regras diferentes

✅ **Filtro Usa WorkDate, Não Data de Registro** - Caso contrário, dados desaparecem

✅ **Supervisor Vê TUDO Sem Restrição** - Diferente de Operário que tem limites

---

## ✨ CONCLUSÃO

Você tem um projeto robusto com lógica de negócio bem definida. A maior complexidade está em replicar:

1. O cálculo de **WorkDate para Turno 3** (cruzando meia-noite)
2. A **restrição de acesso por turno** com tolerância de 1 hora
3. O **sistema de aprovação** de supervisor

Todos esses pontos estão bem documentados nos arquivos criados. Use-os para:
- Treinar desenvolvedores
- Comunicar com as IAs
- Documentar decisões arquiteturais
- Implementar a solução em Google Sheets

Sucesso! 🚀

---
**Criado em**: 2 de abril de 2026
**Arquivos Relacionados**: 
- BUSINESS_RULES.txt (regras da fábrica)
- SHIFT_LOGIC_DOCUMENTATION.md (documentação técnica do Turno 3)
- ASSISTANCE.md (configuração do Java)

