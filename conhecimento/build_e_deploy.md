# 🔨 Build, Assinatura e Deploy

> Como compilar, assinar e publicar novas versões do app.

---

## 1. Configuração do Ambiente

### Java
- **Versão requerida:** Java 17
- **Configuração:** `gradle.properties` na raiz do projeto
  ```properties
  org.gradle.java.home=C:\\Program Files\\Android\\Android Studio\\jbr
  ```
- **Caminhos comuns do Java 17:**
  - `C:\Program Files\Java\jdk-17.x.x`
  - `C:\Program Files\Android\Android Studio\jbr` ← Android Studio bundled JBR
  - `C:\Program Files\Eclipse Adoptium\...`
  - `C:\Program Files\Amazon Corretto\...`
- Usar barras duplas `\\` no `gradle.properties`

### Problema Resolvido: "Java home supplied is invalid"
Se o build falhar com esse erro, verificar e corrigir o caminho em `gradle.properties`.
Comando para localizar: `where java` no CMD.

---

## 2. Buildar o App

### Debug (para testes)
```powershell
cd c:\Relatorio
.\gradlew.bat assembleDebug
```

### Release (para publicação)
```powershell
cd c:\Relatorio
.\gradlew.bat assembleRelease
```

O APK gerado fica em: `app/build/outputs/apk/release/`

---

## 3. Assinatura do APK

**Keystore:** `relatorio-release.jks` (raiz do projeto — **não commitar senhas**)

A assinatura é configurada no `app/build.gradle.kts`:
```kotlin
signingConfigs {
    create("release") {
        storeFile = file("../relatorio-release.jks")
        // storePassword, keyAlias, keyPassword via variáveis de ambiente ou local.properties
    }
}
```

---

## 4. Versionamento

Dois lugares para atualizar a versão:

**1. `app/build.gradle.kts`:**
```kotlin
versionCode = 11        // Incrementar a cada release
versionName = "2.0"     // Versão semântica visível ao usuário
```

**2. `version.json` (raiz do projeto):**
```json
{
  "versionCode": 11,
  "versionName": "2.0",
  "downloadUrl": "https://github.com/hercullezz/Relatorio/releases/download/v2.0/app-v2.0-release.apk",
  "releaseNotes": "Descrição das mudanças"
}
```

> ⚠️ Sempre manter os dois sincronizados! O app usa `version.json` para auto-update.

---

## 5. Publicar Release no GitHub

1. Buildar e assinar o APK release
2. Renomear o APK para o padrão: `app-vX.X-release.apk`
3. Criar tag no Git: `git tag v2.0 && git push origin v2.0`
4. Criar Release no GitHub com a tag
5. Fazer upload do APK renomeado como asset do release
6. Atualizar `version.json` com a nova URL do download
7. Commitar e fazer push do `version.json` atualizado

### Histórico de APKs Publicados
| Versão | versionCode | Arquivo |
|---|---|---|
| 1.5 | — | app-v1.5-release.apk |
| 1.6 | — | app-v1.6-release.apk |
| 1.7 | — | app-v1.7-release.apk |
| 1.8 | — | app-v1.8-release.apk |
| 1.9 | — | app-v1.9-release.apk |
| **2.0** | **11** | **app-v2.0-release.apk** ← atual |

---

## 6. Git e Repositório

- **Repositório:** `github.com/hercullezz/Relatorio`
- **Arquivos sensíveis no .gitignore:** `local.properties`, `google-services.json`, keystore passwords
- `relatorio-release.jks` — verificar se está no `.gitignore` ou se é intencional tê-lo no repo
