# 🏗️ Arquitetura e Estrutura de Dados

> Mapa técnico do projeto: camadas, pastas e modelo de dados.

---

## 1. Estrutura de Pastas (Android)

```
c:\Relatorio\
├── app/
│   ├── build.gradle.kts          # Dependências e config do módulo app
│   └── src/main/
│       ├── AndroidManifest.xml
│       ├── java/com/...          # Código Kotlin
│       └── res/                  # Resources (layouts, strings, drawables)
├── build.gradle.kts              # Config do projeto raiz
├── gradle.properties             # Configurações do Gradle (Java home, etc.)
├── settings.gradle.kts
├── version.json                  # Controle de versão para auto-update
├── relatorio-release.jks         # Keystore de assinatura do APK
├── google-services.json          # Config Firebase (se usado)
├── BUSINESS_RULES.txt            # Regras de negócio da fábrica (fonte da verdade)
├── inicio.md                     # ← Mapa de conhecimento (este sistema)
└── conhecimento/                 # Documentação por tema
```

---

## 2. Camadas da Arquitetura (MVVM)

```
UI (Composables / Screens)
    ↕
ViewModel (lógica de estado, expõe StateFlow/LiveData)
    ↕
Repository (abstração de dados)
    ↕
┌─────────────────┬──────────────────┐
│  Room (SQLite)  │  Parse Server    │
│  (cache local)  │  (backend nuvem) │
└─────────────────┴──────────────────┘
```

**Parse Server** é o backend principal. Room é usado para cache e operação offline.

---

## 3. Modelo de Dados

### Tabela: SERVIÇOS_MANUTENÇÃO (Core)

| Campo | Tipo | Obrigatório | Notas |
|---|---|---|---|
| `id` | Int | ✓ | Auto-gerado (local) |
| `uuid` | String | ✓ | Chave de sincronização com Parse |
| `userId` | String | ✓ | Parse User ID |
| `machine` | String | ✓ | Nome do equipamento |
| `type` | Enum | ✓ | Corretiva, Preventiva, Informação, Limpeza, Lubrificação, Reparo, Inspeção, Outro |
| `description` | String | ✓ | Mín 10 chars, Máx 500 |
| `photoUris` | String | ✗ | URLs separadas por vírgula (máx 3) |
| `createdAt` | DateTime | ✓ | Timestamp real da criação |
| `workDate` | Date | ✓ | **Data do turno** (salva como 00:00:00) |
| `shiftId` | Int | ✓ | 1, 2 ou 3 |
| `overtime` | Boolean | ✓ | true se registrado após fim do turno |
| `approvalStatus` | Enum | ✓ | Pendente, Aprovado, Rejeitado |
| `supervisorId` | String | ✗ | Parse User ID do aprovador |
| `approvedAt` | DateTime | ✗ | Timestamp da aprovação |
| `supervisorNotes` | String | ✗ | Comentário do supervisor |
| `deleted` | Boolean | ✓ | Flag de deleção lógica (**nunca deletar fisicamente**) |

### Tabela: USUÁRIOS

| Campo | Tipo | Notas |
|---|---|---|
| `objectId` | String | Parse User ID (PK) |
| `username` | String | Usado como login |
| `name` | String | Nome completo |
| `email` | String | — |
| `shiftId` | Int | 1, 2 ou 3 (configurado no servidor) |
| `role` | Enum | Operário, Supervisor, Administrador |
| `status` | Enum | Ativo, Inativo, Pendente |
| `cpf` | String | — |

### Tabela: MÁQUINAS

| Campo | Tipo | Notas |
|---|---|---|
| `objectId` | String | Parse Object ID |
| `name` | String | Nome da máquina |
| `productionLineId` | String | FK para Linha |
| `status` | Enum | Ativa, Inativa, Em Reparo |
| `location` | String | Localização física |

### Tabela: LINHAS_PRODUÇÃO

| Campo | Tipo |
|---|---|
| `objectId` | String |
| `name` | String |
| `description` | String |

### Tabela: ESTOQUE

| Campo | Tipo |
|---|---|
| `objectId` | String |
| `code` | String |
| `description` | String |
| `location` | String |
| `quantity` | Int |

---

## 4. Sincronização Parse Server

### Dados Enviados ao Parse por Serviço
```
shiftId, workDate, type, machine, description,
photoUris, overtime, createdAt, userId
```

### Validações no Servidor
- `workDate` deve corresponder ao `shiftId`
- `shiftId` calculado no cliente deve bater com o do servidor → senão LOG DE ERRO
- Timezone do cliente ≠ servidor → ALERTAR

### Cloud Functions Parse (Implementadas)
- **Deleção de usuário**: feita via Cloud Function (server-side) para segurança — não exposto diretamente no cliente

---

## 5. Auto-Update do App

Arquivo `version.json` na raiz do projeto é publicado junto com o APK no GitHub Releases.

```json
{
  "versionCode": 11,
  "versionName": "2.0",
  "downloadUrl": "https://github.com/hercullezz/Relatorio/releases/download/v2.0/app-v2.0-release.apk",
  "releaseNotes": "..."
}
```

O app consulta este arquivo na inicialização e sugere atualização se `versionCode` for maior que o atual.

---

## 6. Classe ShiftManager (Central)

Responsável por toda lógica de turnos:
- `getCurrentShift()` → retorna o turno atual baseado na hora
- `calculateWorkDate()` → retorna a workDate correta (com lógica do T3)
- `formatWorkDate(millis)` → formata como `YYYY-MM-DD` respeitando timezone
- `getVisibleShiftInfos()` → retorna turnos visíveis para o usuário (com +1h)
- `isOvertime()` → verifica se está na janela de hora extra
