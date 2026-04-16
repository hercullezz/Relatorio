# Resumo Visual - Sistema de Relatório em Fábrica 24h

## Visão Geral Rápida

```
┌─────────────────────────────────────────────────────────┐
│         SISTEMA DE MANUTENÇÃO - FÁBRICA 24h            │
│                   3 Turnos Contínuos                    │
└─────────────────────────────────────────────────────────┘

TURNOS:
├── T1: 05:00 - 13:40
├── T2: 13:20 - 22:00 (overlap com T1: 13:20-13:40)
└── T3: 21:30 - 05:20⭐ (cruza meia-noite)
        └── Overlap com T2: 21:30-22:00

⭐ CRÍTICO: Turno 3 que começa dia X termina dia X+1
```

## Problema Central: WorkDate

```
CENÁRIO CONFUSO:
├─ Serviço às 22:30 de 21/03 → Turno 3 que começou em 21/03 ✓
├─ Serviço às 02:00 de 22/03 → Turno 3 que começou em 21/03 ✓✓
└─ Se filtrar por "22/03", o segundo serviço some! ❌

SOLUÇÃO:
WorkDate = SEMPRE a data de INÍCIO do turno
├─ Serviço às 22:30 de 21/03 → WorkDate = 21/03
└─ Serviço às 02:00 de 22/03 → WorkDate = 21/03 ✓ (MESMO DATA!)
```

## Fluxo de Dados

```
        ┌─────────────────────────────────────────┐
        │              OPERÁRIO                    │
        │  Registra serviço durante seu turno     │
        └────────────────┬──────────────────────────┘
                         │
                         ↓
        ┌─────────────────────────────────────────┐
        │   VALIDAÇÕES AUTOMÁTICAS                │
        ├─────────────────────────────────────────┤
        │ ✓ Usuário está em seu turno?           │
        │ ✓ Estão em horário permitido?          │
        │ ✓ Campos obrigatórios preenchidos?     │
        │ ✓ Máquina existe?                      │
        │ ✓ Data não é futura?                   │
        └────────────────┬──────────────────────────┘
                         │
                         ↓
        ┌─────────────────────────────────────────┐
        │   CÁLCULOS AUTOMÁTICOS                  │
        ├─────────────────────────────────────────┤
        │ 1. TurnoID (baseado em hora)            │
        │ 2. WorkDate (data de trabalho)          │
        │ 3. É_Hora_Extra? (se após fim turno)   │
        │ 4. Timestamp (quando foi registrado)    │
        └────────────────┬──────────────────────────┘
                         │
                         ↓
        ┌─────────────────────────────────────────┐
        │   SALVAR NA BASE DE DADOS               │
        │   Status = "Pendente"                   │
        └────────────────┬──────────────────────────┘
                         │
                         ↓
        ┌─────────────────────────────────────────┐
        │   SUPERVISOR (opcional)                 │
        │  Aprova, Rejeita ou deixa pendente      │
        └────────────────┬──────────────────────────┘
                         │
                         ↓
        ┌─────────────────────────────────────────┐
        │   RELATÓRIOS & ANÁLISES                 │
        ├─────────────────────────────────────────┤
        │ • Relatório diário por turno (PDF)      │
        │ • Histórico com filtros                 │
        │ • Gráficos de tendências                │
        │ • Serviços por máquina                  │
        └─────────────────────────────────────────┘
```

## Regras de Acesso por Turno

```
┌──────────────────────────────────────────────────────────┐
│  Usuário do Turno 1 (05:00 - 13:40)                      │
├──────────────────────────────────────────────────────────┤
│ Pode adicionar serviços:    05:00 - 14:40 (turno + 1h)   │
│ Pode visualizar dados:      Seu turno + 1h               │
│ Após 14:40:                 Acesso bloqueado             │
└──────────────────────────────────────────────────────────┘

┌──────────────────────────────────────────────────────────┐
│  Usuário do Turno 2 (13:20 - 22:00)                      │
├──────────────────────────────────────────────────────────┤
│ Pode adicionar serviços:    13:20 - 23:00 (turno + 1h)   │
│ Pode visualizar dados:      Seu turno + 1h               │
│ Após 23:00:                 Acesso bloqueado             │
└──────────────────────────────────────────────────────────┘

┌──────────────────────────────────────────────────────────┐
│  Usuário do Turno 3 (21:30 - 05:20)                      │
├──────────────────────────────────────────────────────────┤
│ Pode adicionar serviços:    21:30 - 06:20 (turno + 1h)   │
│ Pode visualizar dados:      Seu turno + 1h               │
│ Após 06:20:                 Acesso bloqueado             │
└──────────────────────────────────────────────────────────┘

┌──────────────────────────────────────────────────────────┐
│  Supervisor / Admin (Acesso Total)                       │
├──────────────────────────────────────────────────────────┤
│ ✓ Visualizar todos os serviços                           │
│ ✓ Aprovar/Rejeitar serviços                              │
│ ✓ Gerar relatórios consolidados                          │
│ ✓ Editar/remover serviços (com auditoria)                │
│ ✓ Gerenciar usuários e máquinas                          │
└──────────────────────────────────────────────────────────┘
```

## Estrutura da Base de Dados (5 Tabelas Principais)

```
┌─────────────────────────────────────────────────────────┐
│ TABELA: SERVIÇOS_MANUTENÇÃO (Núcleo do Sistema)        │
├─────────────────────────────────────────────────────────┤
│ ID │ UUID │ USER │ MÁQUINA │ TIPO │ DESCRIÇÃO           │
│ ... │ ... │ ... │ ...     │ ...  │ ...                  │
│ DATA_HORA │ WORK_DATE │ TURNO │ FOTOS │ STATUS          │
│ DATA_APROVAÇÃO │ SUPERVISOR │ NOTAS │ DELETADO          │
└─────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────┐
│ TABELA: USUÁRIOS (Control de acesso)                    │
├─────────────────────────────────────────────────────────┤
│ ID │ NOME │ EMAIL │ TURNO_ID │ PERFIL │ STATUS          │
│    │      │       │          │        │                 │
│ (Perfis: Operário, Supervisor, Admin)                   │
└─────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────┐
│ TABELA: MÁQUINAS (Equipamentos que são mantidos)        │
├─────────────────────────────────────────────────────────┤
│ ID │ NOME │ LINHA_PRODUÇÃO │ STATUS │ LOCALIZAÇÃO       │
└─────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────┐
│ TABELA: LINHAS_PRODUÇÃO (Agrupamento de máquinas)       │
├─────────────────────────────────────────────────────────┤
│ ID │ NOME │ DESCRIÇÃO                                   │
└─────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────┐
│ TABELA: ESTOQUE (Peças e materiais)                     │
├─────────────────────────────────────────────────────────┤
│ ID │ CÓDIGO │ DESCRIÇÃO │ LOCALIZAÇÃO │ QUANTIDADE       │
└─────────────────────────────────────────────────────────┘
```

## Fórmulas Críticas

```
═══════════════════════════════════════════════════════════

1️⃣  DETECÇÃO DE TURNO (baseado em hora atual)

    Hora Atual >= 05:00 E Hora < 13:40  → Turno = 1
    Hora Atual >= 13:20 E Hora < 22:00  → Turno = 2
    Hora Atual >= 21:30 E Hora < 05:20  → Turno = 3
    
    OVERLAP em 13:20-13:40 e 21:30-22:00 (permitir manual)

═══════════════════════════════════════════════════════════

2️⃣  CÁLCULO DE WORKDATE (crítico para T3)

    SE Turno IN (1, 2):
        WorkDate = Data Civil Atual
    
    SE Turno = 3:
        SE Hora >= 21:30:
            WorkDate = Data Civil Atual
        SE Hora >= 00:00 E Hora < 05:20:
            WorkDate = Data Civil ANTERIOR

═══════════════════════════════════════════════════════════

3️⃣  DETECÇÃO DE HORA EXTRA

    HoraFimTurno = [13:40, 22:00, 05:20]
    
    SE HoraAtual > HoraFimTurno E 
       HoraAtual <= HoraFimTurno + 1 HORA:
        É_Hora_Extra = SIM
    SENÃO:
        É_Hora_Extra = NÃO

═══════════════════════════════════════════════════════════
```

## Funcionalidades por Persona

```
╔════════════════════════════════════════════════════════╗
║              O P E R Á R I O                          ║
╠════════════════════════════════════════════════════════╣
║ ✓ Adicionar novo serviço                             ║
║ ✓ Visualizar serviços do turno actual + 1h           ║
║ ✓ Editar serviços próprios (seu turno apenas)        ║
║ ✓ Remover serviços próprios (marca como deletado)    ║
║ ✓ Ver status de aprovação de seus serviços           ║
║ ✓ Gerar relatório PDF do seu turno                   ║
║ ✗ Não vê serviços de outros turnos                   ║
║ ✗ Não aprova serviços                                ║
║ ✗ Não gerencia usuários                              ║
╚════════════════════════════════════════════════════════╝

╔════════════════════════════════════════════════════════╗
║            S U P E R V I S O R                        ║
╠════════════════════════════════════════════════════════╣
║ ✓ Ver TODOS os serviços (sem restrição de turno)     ║
║ ✓ Aprovar/Rejeitar serviços                          ║
║ ✓ Adicionar notas de validação                       ║
║ ✓ Gerar relatórios consolidados                      ║
║ ✓ Visualizar serviços por máquina/período            ║
║ ✓ Ver histórico de aprovações                        ║
║ ✗ Não deleta dados (apenas marca)                    ║
║ ✗ Não gerencia usuários                              ║
╚════════════════════════════════════════════════════════╝

╔════════════════════════════════════════════════════════╗
║          A D M I N I S T R A D O R                    ║
╠════════════════════════════════════════════════════════╣
║ ✓ TUDO que Supervisor faz                            ║
║ ✓ CRUD: Usuários (criar, editar, desativar)          ║
║ ✓ CRUD: Máquinas (criar, editar)                     ║
║ ✓ CRUD: Linhas de Produção                           ║
║ ✓ CRUD: Estoque                                      ║
║ ✓ Ver logs de auditoria                              ║
║ ✓ Fazer backup de dados                              ║
║ ✓ Deletar dados fisicamente (com aviso)              ║
║ ✓ Configurar regras do sistema                       ║
╚════════════════════════════════════════════════════════╝
```

## Exemplos de Filtros Esperados

```
FILTRO 1: "Turno Atual"
Mostra: Serviços do turno atual + próxima 1 hora
Uso: Operário vê seu próprio turno

FILTRO 2: "Dia Específico"
Seleciona: Data + Turno(s) desejados
Combina: Você escolhe T1, T2, T3 ou combinação
Filtra por: WORK_DATE = Data Selecionada

FILTRO 3: "Período"
Seleciona: Data Início até Data Fim
Agrupa: Por máquina ou por turno
Mostra: Relatório consolidado

FILTRO 4: "Por Máquina"
Dropdown: Lista de todas as máquinas
Mostra: Histórico completo de manutenção dessa máquina
Agrupado: Por data/turno

FILTRO 5: "Status de Aprovação"
Opções: Pendente | Aprovado | Rejeitado
Usa: Supervisores e Admins
```

## Validações de Entrada

```
CAMPO: Máquina
├─ Obrigatório: SIM
├─ Tipo: Dropdown (lista de máquinas ativas)
└─ Erro se: Máquina não existe

CAMPO: Tipo de Serviço
├─ Obrigatório: SIM
├─ Tipo: Dropdown
├─ Opções: Limpeza, Lubrificação, Reparo, Inspeção, Outro
└─ Erro se: Vazio

CAMPO: Descrição
├─ Obrigatório: SIM
├─ Tipo: Texto longo (textarea)
├─ Mínimo: 10 caracteres
└─ Erro se: Descrição < 10 caracteres

CAMPO: Turno (auto-detectado)
├─ Auto-preenchido: SIM (baseado em hora)
├─ Editável: SIM (só se em overlap ou com supervisor)
├─ Restrição: Se turno ≠ turno do usuário, exigir NOTA
└─ Erro se: Fora das 24h

CAMPO: Fotos
├─ Obrigatório: NÃO
├─ Tipo: URLs separadas por vírgula
├─ Máximo: 3 fotos
└─ Aviso se: Foto > 5MB

CAMPO: Nota Explicativa
├─ Obrigatório se: Turno ≠ Turno do Usuário
├─ Tipo: Texto
├─ Mínimo: 20 caracteres (se exigida)
└─ Uso: Justificar registro em turno diferente
```

---

## Checklist de Implementação

```
FASE 1: Estrutura Base
☐ Criar 5 tabelas (Serviços, Usuários, Máquinas, Linhas, Estoque)
☐ Implementar autenticação de usuários
☐ Criar Dashboard básico
☐ Implementar cálculos de Turno e WorkDate

FASE 2: Formulário e Validações
☐ Criar formulário de entrada de serviço
☐ Implementar validações (campos obrigatórios, etc)
☐ Auto-detecção de turno e hora extra
☐ Bloquear fora do horário permitido

FASE 3: Filtros e Visualização
☐ Filtro por Turno Atual
☐ Filtro por Dia Específico
☐ Filtro por Período
☐ Filtro por Máquina
☐ Busca por descrição
☐ Ordenação múltipla

FASE 4: Aprovação e Supervisão
☐ Tela de serviços pendentes
☐ Funcionalidade de Aprovação/Rejeição
☐ Campos de nota do supervisor
☐ Histórico de aprovações

FASE 5: Gerenciamento (Admin)
☐ CRUD de Usuários
☐ CRUD de Máquinas
☐ CRUD de Linhas de Produção
☐ CRUD de Estoque
☐ Logs de auditoria

FASE 6: Relatórios
☐ Relatório diário por turno (PDF)
☐ Relatório consolidado (período)
☐ Gráficos de tendências
☐ Relatório por máquina

FASE 7: Polimento
☐ Temas/cores
☐ Responsividade
☐ Performance
☐ Documentação
☐ Treinamento de usuários
```

---

Salvo em: `PROMPT_GOOGLE_SHEETS.md` (arquivo completo com todas as especificações)

