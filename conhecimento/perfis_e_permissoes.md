# 👥 Perfis, Permissões e Fluxos de Aprovação

> Regras detalhadas de acesso por perfil e o fluxo completo de aprovação de serviços.

---

## 1. Perfis de Usuário

### 🔵 Operário (Nível Base)

| Ação | Permitido? |
|---|---|
| Registrar serviço (dentro do seu turno ou +1h) | ✅ |
| Ver serviços do próprio turno | ✅ |
| Editar/remover os próprios serviços | ✅ |
| Gerar PDF do próprio turno (até +1h) | ✅ |
| Ver serviços de outros turnos | ❌ |
| Aprovar/Rejeitar serviços | ❌ |
| Gerenciar usuários ou máquinas | ❌ |
| Deletar dados fisicamente | ❌ |

**Restrição temporal:** O operário só tem acesso funcional durante seu turno + 1h de hora extra. Fora disso, o acesso de adição/visualização fica bloqueado.

---

### 🟡 Supervisor (Nível Intermediário)

| Ação | Permitido? |
|---|---|
| Ver TODOS os serviços (sem restrição de turno) | ✅ |
| Filtrar por qualquer critério (data, turno, máquina) | ✅ |
| Aprovar serviços com nota | ✅ |
| Rejeitar serviços com nota obrigatória | ✅ |
| Gerar relatórios consolidados (múltiplos turnos/datas) | ✅ |
| Gerenciar usuários | ❌ |
| Deletar dados fisicamente | ❌ |
| CRUD de máquinas/linhas | ❌ |

---

### 🔴 Administrador (Nível Máximo)

| Ação | Permitido? |
|---|---|
| Tudo que o Supervisor pode | ✅ |
| CRUD de usuários (criar, editar, desativar, deletar) | ✅ |
| CRUD de máquinas e linhas de produção | ✅ |
| CRUD de estoque | ✅ |
| Ver logs de auditoria | ✅ |
| Backup de dados | ✅ |
| Aprovar novos cadastros de usuários | ✅ |
| Conceder/revogar privilégio de Admin | ✅ |

**Ações sensíveis (server-side):**
- **Deleção de usuário:** executada via Cloud Function Parse (não exposta no cliente)
- **Reset de senha:** executada via Cloud Function Parse

---

## 2. Fluxo de Aprovação de Serviços

```
Operário registra serviço
        ↓
status = "Pendente"
        ↓
Supervisor/Admin revisa na tela de pendentes
        ↓
    ┌───────────┐
    │ Aprovar   │ → status = "Aprovado" + supervisorId + approvedAt
    │ Rejeitar  │ → status = "Rejeitado" + nota obrigatória
    └───────────┘
```

### Validações na Revisão (Supervisor)
- `shiftId` do registro é válido (1, 2 ou 3)?
- `workDate` é compatível com `shiftId`?
- Timestamp de criação é consistente?
- Se `overtime = true`: está na janela de +1h?

---

## 3. Fluxo de Cadastro de Novos Usuários

```
Usuário faz cadastro no app
        ↓
status = "Pendente" (não tem acesso ainda)
        ↓
Administrador revisa no Painel Admin
        ↓
    ┌──────────────┐
    │ Aprovar      │ → status = "Ativo" (acesso liberado)
    │ Reprovar     │ → status = "Inativo" / Rejeitado
    └──────────────┘
```

---

## 4. Painel Admin — Gerenciamento de Usuários

**Layout:** Cards por usuário (não lista simples).

**Ações disponíveis por card:**
| Ação | Implementação |
|---|---|
| Aprovar/Reprovar usuário | Atualizar campo `status` no Parse |
| Tornar Admin / Remover Admin | Atualizar campo `role` no Parse |
| Iniciar Reset de Senha | Via Cloud Function Parse |
| Deletar usuário | Via Cloud Function Parse (seguro, server-side) |

---

## 5. Dispositivo Compartilhado

O app foi projetado para um **único dispositivo Android** que circula entre os operários dos turnos:
- Cada operário faz **login com suas próprias credenciais** Parse
- O turno é determinado pelo **usuário logado** + hora atual do dispositivo
- O `shiftId` configurado no servidor para o usuário é a fonte de verdade (não o turno detectado automaticamente, se houver conflito)

---

## 6. Configuração do ShiftId no Servidor

- Cada usuário possui um campo `ShiftId` no Parse (`1`, `2` ou `3`)
- O app consulta esse campo ao fazer login
- Se não houver configuração no servidor → usa detecção automática pelo horário do dispositivo
- Conflito (cliente calcula turno X, servidor tem turno Y) → LOG DE ERRO
