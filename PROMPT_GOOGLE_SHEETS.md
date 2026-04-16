# Prompt para Implementação em Google Sheets
## Sistema de Relatório de Manutenção - Fábrica 24h / 3 Turnos

---

## CONTEXTO GERAL DO PROJETO

Estou desenvolvendo um **Sistema de Relatório de Manutenção** para uma fábrica que funciona **24 horas por dia com 3 turnos**. Atualmente tenho um aplicativo Android em Kotlin/Compose que realiza essa função, mas preciso replicar a lógica e funcionalidades em **Google Sheets** para fins de análise e visualização de dados.

O sistema deve permitir que operários registrem serviços de manutenção durante seus turnos de trabalho, com validações de turno, geração de relatórios e análises consolidadas.

---

## ESTRUTURA DE TURNOS DA FÁBRICA

### Horários de Turnos (24 horas)

```
Turno 1 (T1): 05:00 - 13:40
Turno 2 (T2): 13:20 - 22:00
Turno 3 (T3): 21:30 - 05:20 (cruza meia-noite para o dia seguinte)
```

### Overlaps (períodos de transição entre equipes)

- **T1 → T2**: 13:20 - 13:40 (20 minutos de sobreposição)
- **T2 → T3**: 21:30 - 22:00 (30 minutos de sobreposição)

### Conceito Crítico: WorkDate (Data de Trabalho)

**Problema**: O Turno 3 começa em um dia civil e termina no dia seguinte. Um serviço registrado às 02:00 do dia 22/03 pertence, na verdade, ao Turno 3 que começou em 21/03.

**Solução**: Usar o conceito de **WorkDate** (Data de Trabalho), que é SEMPRE a data de início do turno:

- Um serviço registrado às 22:30 de 21/03 → WorkDate = 21/03
- Um serviço registrado às 02:00 de 22/03 → WorkDate = 21/03 (mesmo turno 3)

**Cálculo da WorkDate**:
```
Se Turno 1 (05:00 - 13:40):     WorkDate = Dia Atual
Se Turno 2 (13:20 - 22:00):     WorkDate = Dia Atual
Se Turno 3 (21:30 - 05:20):
   - Entre 21:30 e 23:59:        WorkDate = Dia Atual
   - Entre 00:00 e 05:20:        WorkDate = Dia Anterior
```

---

## USUARIOS E PERMISSÕES

### Perfis de Usuário

1. **Operário**: 
   - Adiciona serviços comprovando trabalho realizado
   - Pode adicionar serviços apenas durante seu turno
   - Pode deixar comentários/observações

2. **Supervisor**: 
   - Valida e aprova serviços adicionados
   - Pode visualizar relatórios consolidados
   - Pode visualizar serviços de outros funcionários

3. **Administrador**:
   - Gerencia usuários (criar, editar, remover)
   - Gera relatórios consolidados
   - Acesso total ao sistema

### Configuração de Acesso

- Cada usuário possui um **ShiftId** configurado (1, 2 ou 3)
- Um smartphone Android é compartilhado entre funcionários de diferentes turnos
- Múltiplos usuários podem usar o mesmo dispositivo – cada um loga com suas credenciais
- O turno é determinado pelo: **ShiftId do usuário + Hora atual**

---

## REGRAS DE NEGÓCIO - ADIÇÃO DE SERVIÇOS

### Restrição Principal: Apenas seu Turno

Um usuário **SÓ PODE** adicionar serviços dentro do seu turno de trabalho:
```
Validação: ShiftId do Usuário = ShiftId Calculado da Hora Atual
```

Se houver tentativa fora do turno, o sistema deve avisar e bloquear.

### Extensão 1: Hora Extra (até +1 hora após turno terminar)

O usuário pode continuar adicionando serviços **até 1 hora após seu turno terminar**:

```
Turno 1 (05:00 - 13:40)  →  Pode adicionar até 14:40
Turno 2 (13:20 - 22:00)  →  Pode adicionar até 23:00
Turno 3 (21:30 - 05:20)  →  Pode adicionar até 06:20
```

Esses registros recebem a flag **overtime = true**.

### Extensão 2: Turno Seguinte (sem tempo de adicionar no próprio turno)

Se o usuário **não registrou** nada durante seu próprio turno, ele pode adicionar serviços no turno seguinte, mas **DEVE**:
- Indicar que é referente ao turno anterior
- Deixar um comentário/nota explicando o atraso
- O sistema permite override manual do shiftId

**Exemplo**:
```
Turno 1 não registrou um serviço com problema de máquina
No Turno 2 (próximo turno), o operário pode registrar:
   - Descrição: "Serviço de manutenção da máquina X"
   - Turno: T1 (manual override)
   - Nota: "Serviço não concluído no turno anterior"
```

---

## ESTRUTURA DE DADOS DO SISTEMA

### Tabela 1: SERVIÇOS_MANUTENÇÃO (MaintenanceItems)

```
Coluna A: ID                              (Auto-gerado, chave primária)
Coluna B: UUID_ÚNICO                      (Identificador único da entrada)
Coluna C: USUÁRIO_ID                      (ID do usuário que adicionou)
Coluna D: MÁQUINA / EQUIPAMENTO           (Nome da máquina/equipamento)
Coluna E: TIPO_SERVIÇO                    (Ex: Limpeza, Lubrificação, Reparo, etc.)
Coluna F: DESCRIÇÃO                       (Descrição detalhada do serviço)
Coluna G: DATA_HORA_REGISTRO              (Timestamp quando foi adicionado: dd/mm/yyyy HH:mm:ss)
Coluna H: WORK_DATE                       (Data de trabalho em formato: dd/mm/yyyy)
Coluna I: TURNO_ID                        (1, 2 ou 3 - pode ser auto-detectado ou manual)
Coluna J: TURNO_NOME                      (T1, T2 ou T3 - derivado)
Coluna K: É_HORA_EXTRA                    (Sim/Não - Se foi registrado após horário normal)
Coluna L: FOTOS_URL                       (Links das fotos separados por vírgula)
Coluna M: STATUS_APROVAÇÃO                (Pendente, Aprovado, Rejeitado)
Coluna N: SUPERVISOR_RESPONSÁVEL          (Usuário que aprovou/rejeitou)
Coluna O: DATA_APROVAÇÃO                  (Quando foi aprovado)
Coluna P: NOTAS_SUPERVISOR                (Comentários da validação)
Coluna Q: DELETADO                        (Sim/Não - Marcação lógica, não deletar)
```

### Tabela 2: USUÁRIOS

```
Coluna A: ID_USUÁRIO                      (Identificador único)
Coluna B: NOME_COMPLETO                   (Nome do funcionário)
Coluna C: EMAIL                           (Email corporativo)
Coluna D: TURNO_ID                        (1, 2 ou 3 - Turno padrão do usuário)
Coluna E: TURNO_NOME                      (T1, T2 ou T3)
Coluna F: PERFIL                          (Operário, Supervisor, Administrador)
Coluna G: STATUS                          (Ativo, Inativo)
Coluna H: DATA_CRIAÇÃO                    (Quando foi criado)
Coluna I: CPF                             (Para identificação)
```

### Tabela 3: MÁQUINAS (Machine Configuration)

```
Coluna A: ID_MÁQUINA                      (Identificador único)
Coluna B: NOME_MÁQUINA                    (Nome/código da máquina)
Coluna C: LINHA_PRODUÇÃO_ID               (ID da linha à qual pertence)
Coluna D: STATUS                          (Ativa, Inativa, Em Reparo)
Coluna E: LOCALIZAÇÃO                     (Setor, galpão, etc.)
```

### Tabela 4: LINHAS DE PRODUÇÃO

```
Coluna A: ID_LINHA                        (Identificador único)
Coluna B: NOME_LINHA                      (Nome da linha de produção)
Coluna C: DESCRIÇÃO                       (Descrição/notas)
```

### Tabela 5: ESTOQUE (Stock Items)

```
Coluna A: ID_ITEM                         (Identificador único)
Coluna B: CÓDIGO_ITEM                     (Código do produto/peça)
Coluna C: DESCRIÇÃO                       (Nome do item)
Coluna D: LOCALIZAÇÃO                     (Onde está armazenado)
Coluna E: QUANTIDADE                      (Em unidades)
Coluna F: DATA_ÚLTIMA_ATUALIZAÇÃO         (Última vez que foi alterado)
```

---

## FUNCIONALIDADES ESPERADAS EM GOOGLE SHEETS

### 1. Dashboard/Painel de Controle

Um painel que mostre:
- **Resumo do turno atual** (hora atual, turno detectado, usuário logado)
- **Total de serviços registrados hoje** (por turno)
- **Serviços pendentes de aprovação** (quantidade)
- **Últimos serviços adicionados** (últimas 5 entradas)
- **Gráfico de serviços por máquina** (hoje)

### 2. Tela de Adição de Servico

Formulário com campos:
- **Máquina** (dropdown com lista de máquinas)
- **Tipo de Serviço** (dropdown: Limpeza, Lubrificação, Reparo, Inspeção, etc.)
- **Descrição** (campo de texto longo)
- **Fotos** (possibilidade de anexar URLs de imagens)
- **Turno** (auto-detect da hora atual, mas permitir override manual)
- **É hora extra?** (checkbox, deve ser pré-preenchido se after+1h)

Validações:
- Bloquear adição se não estiver dentro do horário permitido (turno + 1 hora)
- Exigir nota explicativa se adicionar em turno diferente

### 3. Tela de Histórico e Filtros

Filtros disponíveis:
- **Por Turno Atual**: Exibe serviços do turno atual + 1 hora (visibilidade de turno)
- **Por Dia Específico**: Selecionar data e turno(s) desejados
- **Por Período**: Filtro por intervalo de datas
- **Por Máquina**: Dropdown com máquinas
- **Por Tipo de Serviço**: Dropdown com tipos
- **Por Status de Aprovação**: Pendente, Aprovado, Rejeitado
- **Busca** (pesquisa por máquina ou descrição)

Ordenação:
- Por data (mais recente primeiro)
- Por máquina
- Por turno
- Por status

### 4. Relatório Diário em PDF

Cada turno deve gerar um relatório que inclua:
- **Cabeçalho**: Data do turno, número do turno (T1/T2/T3)
- **Serviços agrupados por máquina**
- **Para cada serviço**: Tipo, descrição, fotos, horário de adição, status
- **Rodapé**: Data de geração, número de página

### 5. Relatório Consolidado

Relatório que cruza dados de:
- Múltiplos dias
- Múltiplos turnos
- Agrupado por máquina, tipo de serviço, ou período
- Com gráficos de tendências

### 6. Aprovação de Serviços (Supervisor)

Tela onde supervisores podem:
- Visualizar serviços pendentes
- Aprovar ou rejeitar
- Deixar notas de validação
- Ver histórico de aprovações

### 7. Gerenciamento de Usuários (Admin)

CRUD de usuários:
- Criar novo usuário (nome, email, turno, perfil)
- Editar usuário (nome, turno, perfil, status)
- Desativar/Ativar usuário
- Ver histórico de atividades do usuário

### 8. Gerenciamento de Máquinas (Admin)

CRUD de máquinas:
- Criar máquina (nome, linha de produção)
- Editar máquina
- Ver histórico de manutenções de cada máquina
- Relatório de máquinas mais mantidas

---

## CÁLCULOS E FÓRMULAS NECESSÁRIAS

### 1. Auto-Detecção de Turno (baseado em hora atual)

```
Dado: Hora Atual (HH:MM em formato 24h)

SE hora >= 05:00 E hora < 13:40 ENTÃO Turno = 1
SE hora >= 13:20 E hora < 22:00 ENTÃO Turno = 2
SE hora >= 21:30 E hora < 05:20 ENTÃO Turno = 3

NOTA: Há overlaps (13:20-13:40 e 21:30-22:00)
      - Nestes períodos, permitir seleção manual do turno
      - Usar turno mais "provável" baseado em contexto
```

### 2. Cálculo da WorkDate

```
Dado: Hora Atual (HH:MM) e Data Civil (DD/MM/YYYY)

SE Turno IN (1, 2):
    WorkDate = Data Civil Atual
SE Turno = 3:
    SE hora >= 21:30:
        WorkDate = Data Civil Atual
    SE hora >= 00:00 E hora < 05:20:
        WorkDate = Data Civil Anterior (subtract 1 day)
```

### 3. Detecção de Hora Extra

```
DadoHoraRegistro = Hora em que o serviço foi adicionado

SE HoraRegistro > HoraSaidaTurnoDoUsuario E 
   HoraRegistro <= HoraSaidaTurnoDoUsuario + 1 Hora ENTÃO
    É_Hora_Extra = SIM
SENÃO
    É_Hora_Extra = NÃO
```

### 4. Filtro de Visibilidade de Turno (com tolerância de 1 hora)

```
Dado: Turno do Usuário, Hora Atual

Turnos visíveis para o usuário:
- Seu turno normal
- +1 hora além do término do turno
- Se passou da 1 hora, mostrar apenas turnos futuros/consolidados

Exemplo:
  Usuário do Turno 1 (05:00-13:40):
    05:00-14:40: Pode ver seu próprio turno
    14:40+: Turno não mais visível (exceto em relatórios consolidados)
```

### 5. Filtro por Data e Turno

```
Filtrar registros onde:
- WORK_DATE = Data Selecionada
- TURNO_ID IN (Turnos selecionados)
- Opcional: STATUS_APROVAÇÃO = Valor desejado

IMPORTANTE: Usar WORK_DATE para comparação, não DATA_HORA_REGISTRO
           Isso evita erros com Turno 3 que cruza meia-noite
```

---

## VALIDAÇÕES CRÍTICAS

### V1: Restrição de Turno

```
SE usuário está adicionando um serviço
E TurnoAtualCalculado ≠ TurnoDoUsuário
E HoraAtual NÃO ESTÁ em (TurnoDoUsuário + 1 Hora)
ENTÃO
    BLOQUEAR adição
    MENSAGEM: "Fora do período permitido para seu turno"
```

### V2: Campos Obrigatórios

```
Para adicionar serviço:
- Máquina: obrigatório
- Tipo de Serviço: obrigatório
- Descrição: obrigatório (mínimo 10 caracteres)
- Se turno ≠ turno do usuário: Nota obrigatória
```

### V3: Integridade de Data

```
SE Data_Registro > Data_Atual ENTÃO
    ERRO: "Data não pode ser futura"

SE Data_Registro < (Turno - 7 dias) ENTÃO
    AVISO: "Registro muito antigo, verifique se não é erro"
```

---

## EXEMPLOS DE USO

### Cenário 1: Operário Adicionando Serviço em Seu Turno

```
Hora: 10:30 de 21/03/2026
Usuário: João (Turno 1)
Turno Detectado: 1 ✓

João clica em "Novo Serviço":
- Máquina: "Máquina A - Linha 1"
- Tipo: "Lubrificação"
- Descrição: "Realizada manutenção preventiva na máquina A"
- É Hora Extra: NÃO (pois ainda está no turno)

Registro criado:
  DATE_TIME: 21/03/2026 10:30:00
  WORK_DATE: 21/03/2026
  TURNO_ID: 1
  STATUS: Pendente
```

### Cenário 2: Operário Fazendo Hora Extra

```
Hora: 14:20 de 21/03/2026
Usuário: Maria (Turno 1)
Turno 1 termina às 13:40, permite até 14:40 para hora extra

Maria clica em "Novo Serviço" (É uma hora extra permitida):
- Máquina: "Máquina C"
- Tipo: "Reparo"
- Descrição: "Reparo no motor da máquina C"
- É Hora Extra: SIM (auto-detectado, mas editável)

Registro criado:
  DATE_TIME: 21/03/2026 14:20:00
  WORK_DATE: 21/03/2026
  TURNO_ID: 1
  IS_OVERTIME: SIM
```

### Cenário 3: Operário Registrando Serviço do Turno Anterior

```
Hora: 14:00 de 22/03/2026
Usuário: Pedro (Turno 2)
Serviço que deveria ter sido registrado no Turno 1 (21/03)

Pedro clica em "Novo Serviço":
- Sistema quer bloquear (pois turno ≠ turno do usuário)
- Pedro deixa a NOTA OBRIGATÓRIA: "Serviço do turno anterior não registrado"
- Pedro seleciona manualmente: TURNO_ID = 1

Registro criado:
  DATE_TIME: 22/03/2026 14:00:00
  WORK_DATE: 21/03/2026 (calculado pela intenção de Pedro)
  TURNO_ID: 1 (manual override)
  STATUS: Pendente (requer aprovação do supervisor)
  NOTA: "Serviço do turno anterior não registrado"
```

### Cenário 4: Serviço do Turno 3 Cruzando Meia-Noite

```
Hora: 02:30 de 22/03/2026
Usuário: Carlos (Turno 3 que começou em 21/03)

Carlos clica em "Novo Serviço":
- Máquina: "Máquina B"
- Tipo: "Inspeção"
- Descrição: "Inspeção de segurança realizada"

Registro criado automaticamente:
  DATE_TIME: 22/03/2026 02:30:00 (hora real)
  WORK_DATE: 21/03/2026 (WorkDate do Turno 3)
  TURNO_ID: 3
  
  Resultado: Quando filtrar por "21/03/2026 Turno 3", este serviço 
  aparecerá corretamente, mesmo tendo sido registrado em 22/03.
```

---

## RECOMENDAÇÕES TÉCNICAS PARA GOOGLE SHEETS

### Estrutura Sugerida

1. **Aba 1: "Dashboard"**
   - Painel resumido com status do dia
   - Gráficos rápidos
   - Botão "Novo Serviço"

2. **Aba 2: "Novo Serviço"**
   - Formulário de entrada de dados
   - Validações em tempo real (Google Sheets Form ou Data Validation)

3. **Aba 3: "Histórico"**
   - Lista de todos os serviços
   - Filtros dinâmicos
   - Opções de edição e exclusão lógica

4. **Aba 4: "Usuários"** (admin)
   - Tabela de usuários
   - CRUD de usuários

5. **Aba 5: "Máquinas"** (admin)
   - Tabela de máquinas
   - Linha de produção

6. **Aba 6: "Estoque"**
   - Tabela de itens de estoque
   - Quantidade disponível

7. **Aba 7: "Relatórios"**
   - Gerar PDF customizado
   - Filtros de data/turno

8. **Aba 8: "Dados_Brutos_Serviços"** (oculta)
   - Fonte de dados para tudo
   - Backup dos serviços

### Integração com Apps Script

Google Apps Script pode:
- Validar dados em tempo real
- Calcular TurnoID e WorkDate automaticamente
- Gerar PDFs formatados
- Enviar notificações por email

---

## OBSERVAÇÕES FINAIS

1. **Fuso Horário**: Sempre usar horário local da fábrica na comparação
2. **Sincronização**: Se houver integração com app Android, sincronizar pelo UUID único
3. **Auditoria**: Manter registro de quem criou/editou/aprovou cada serviço
4. **Performance**: Usar filtros adequados para não sobrecarregar a planilha
5. **Backup**: Realizar backup regular dos dados em outra planilha/banco

---

## DÚVIDAS QUE VOCÊ PODE DETALHAR MELHOR

Para implementação específica em Google Sheets, você pode:
- Especificar qual será o sistema de autenticação de usuários
- Detalhar o formato desejado dos PDFs
- Indicar se haverá integração com o app Android existente
- Especificar limitações de quantidade de dados (quantos registros espera ter?)
- Detalhar o tipo de gráficos/análises desejadas

---

