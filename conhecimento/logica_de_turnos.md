# ⏰ Lógica de Turnos e WorkDate

> Arquivo mais crítico do projeto. Toda feature de tempo depende deste conhecimento.

---

## 1. Estrutura de Turnos

| Turno | Início | Fim | Duração | Detalhe |
|---|---|---|---|---|
| **T1** | 05:00 | 13:40 | 8h40min | Matutino |
| **T2** | 13:20 | 22:00 | 8h40min | Vespertino |
| **T3** | 21:30 | 05:20 (+1 dia) | 7h50min | Noturno — **cruza meia-noite** |

### Períodos de Overlap (Transição entre Turnos)
| Período | Turnos Envolvidos | Ação |
|---|---|---|
| 13:20 – 13:40 | T1 ↔ T2 | Seleção **manual** pelo usuário |
| 21:30 – 22:00 | T2 ↔ T3 | Seleção **manual** pelo usuário |

**Regra padrão dos overlaps:** priorizar o turno com hora de INÍCIO mais recente.
- 13:30 → T2 (iniciou 13:20, não 05:00)
- 21:45 → T3 (iniciou 21:30, não 13:20)

---

## 2. WorkDate — O Conceito Central

**WorkDate** = data de início do turno, sempre salva como `YYYY-MM-DD 00:00:00`.

### Por que existe?
Um serviço feito às `02:00 de 22/03` pertence ao **T3 de 21/03** (turno que começou antes da meia-noite). Se usarmos a data do relógio (`createdAt`), o serviço seria agrupado errado.

### Algoritmo de Cálculo

```
SE turno ∈ [T1, T2]:
    workDate = Dia Civil Atual

SE turno = T3:
    SE hora ∈ [21:30, 23:59] → workDate = Dia Civil Atual
    SE hora ∈ [00:00, 05:20] → workDate = Dia Civil ANTERIOR
```

### Exemplos Concretos
```
Serviço às 22:30 de 21/03 (T3) → workDate = 21/03 ✓
Serviço às 02:00 de 22/03 (T3) → workDate = 21/03 ✓ (mesmo turno!)
Serviço às 08:00 de 22/03 (T1) → workDate = 22/03 ✓
```

---

## 3. Hora Extra

Após o fim oficial do turno, o operário pode registrar por mais **1 hora**:

| Turno | Fim Oficial | Fim com Hora Extra |
|---|---|---|
| T1 | 13:40 | **14:40** |
| T2 | 22:00 | **23:00** |
| T3 | 05:20 | **06:20** |

- Registros nesse período recebem `overtime = true`
- **Hora Extra ≠ Turno diferente** — o shiftId continua sendo o do usuário
- PDF também só pode ser gerado nessa janela (turno + 1h)

---

## 4. Registo no Turno Seguinte (Esquecimento)

Se o operário não registrou no próprio turno:
- Pode registrar no **turno seguinte** com `shiftId` manual (override)
- **Nota obrigatória** explicando o atraso (ex: "Serviço esquecido do turno anterior")

---

## 5. Fora de Qualquer Turno (Edge Case)

- Usa **T1 como fallback** + LOG DE ALERTA
- Operador deve revisar e corrigir manualmente

---

## 6. Implementação Técnica (Android/Kotlin)

### Classe Principal
`ShiftManager` — centraliza toda a lógica de turnos.

### Salvamento
`workDate` é salva no Parse sempre com horário **zerado (00:00:00)** para evitar bugs de comparação por milissegundos.

### Filtragem
Sempre comparar como **String no formato `YYYY-MM-DD`**:
```kotlin
ShiftManager.formatWorkDate(millis) // retorna "YYYY-MM-DD" respeitando timezone local
```

### Visibilidade na UI
`ShiftManager.getVisibleShiftInfos()` — retorna os turnos visíveis para o usuário atual (com tolerância de +1h).

### Conflitos Servidor/Cliente
- `shiftId` calculado no cliente ≠ shiftId esperado pelo servidor → **LOG DE ERRO**
- `workDate` não corresponde ao `shiftId` → **REJEITAR**
- Timezone do dispositivo diferente do servidor → **ALERTAR** + sugerir sincronização

---

## 7. Troubleshooting

| Sintoma | Causa | Solução |
|---|---|---|
| Serviços do T3 sumindo no filtro | Filtrando por `createdAt` em vez de `workDate` | Usar `ShiftManager.formatWorkDate(item.workDate)` |
| Serviço no PDF mas não na tela | Lógica de filtro divergente | Unificar usando `getVisibleShiftInfos()` |
| WorkDate errada para T3 pós meia-noite | Timezone incorreto ou horário não zerado | Verificar se workDate é salva como `00:00:00` |
