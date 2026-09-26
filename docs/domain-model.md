# Modelo de dados

Uma **missão** é o que a pessoa quer fazer ("Estudar Java, 5× por semana, 08:00"). Uma **ocorrência** é uma instância datada dela ("Estudar Java, seg 28/09, 08:00"). É a ocorrência que é concluída, perdida e recompensada. A descrição de cada entidade está na seção 4 de [architecture.md](architecture.md).

## Visão completa do MVP

```mermaid
erDiagram
    USER ||--o{ TASK : "define"
    USER ||--o{ WEEKLY_PLAN : "planeja"
    USER ||--|| WALLET : "tem"
    USER ||--|| STREAK : "tem"
    USER ||--o{ DAILY_RESULT : "fecha"
    USER ||--o{ COIN_TRANSACTION : "movimenta"
    USER ||--o{ REFRESH_TOKEN : "autentica"
    USER ||--o{ USER_ACHIEVEMENT : "desbloqueia"
    USER ||--o{ INVENTORY_ITEM : "possui"
    USER ||--|| ROOM : "tem"
    USER ||--|| CHARACTER : "tem"
    TASK ||--o{ TASK_SCHEDULE : "recorre em"
    TASK ||--o{ TASK_OCCURRENCE : "gera"
    WEEKLY_PLAN ||--o{ TASK_OCCURRENCE : "agrupa"
    TASK_OCCURRENCE ||--o| PROOF : "comprovada por"
    TASK_OCCURRENCE ||--o{ COIN_TRANSACTION : "recompensa"
    ACHIEVEMENT ||--o{ USER_ACHIEVEMENT : "concede"
    STORE_ITEM ||--o{ INVENTORY_ITEM : "origina"
    INVENTORY_ITEM ||--o| COIN_TRANSACTION : "pago por"
    ROOM ||--o{ ROOM_ITEM : "contém"
    INVENTORY_ITEM ||--o| ROOM_ITEM : "posto em"
    CHARACTER ||--o{ CHARACTER_EQUIPMENT : "veste"
    INVENTORY_ITEM ||--o| CHARACTER_EQUIPMENT : "equipado em"
```

## Tabelas já criadas

Criadas pela migration `V1__create_users_and_refresh_tokens.sql`, na Fase 1.

```mermaid
erDiagram
    USERS ||--o{ REFRESH_TOKENS : "abre sessões"
    USERS {
        uuid id PK
        varchar email UK "sempre em minúsculas"
        varchar password_hash "BCrypt com prefixo {bcrypt}"
        varchar display_name
        varchar time_zone "IANA, ex.: America/Recife"
        boolean ranking_visible
        timestamptz created_at
        timestamptz updated_at
        bigint version "lock otimista"
    }
    REFRESH_TOKENS {
        uuid id PK
        uuid family_id "uma família = um login"
        uuid user_id FK
        varchar token_hash UK "SHA-256 do token"
        timestamptz created_at
        timestamptz expires_at
        timestamptz revoked_at
        varchar revoked_reason "ROTATED, LOGOUT ou REUSE_DETECTED"
        bigint version
    }
```


- **users.** O e-mail é único e gravado em minúsculas pela aplicação. A senha existe só como hash BCrypt com prefixo de algoritmo, o que permite trocar de algoritmo no futuro sem invalidar as senhas atuais. O fuso horário é parte do domínio: "hoje", "amanhã" e "semana" são sempre calculados nele.
- **refresh_tokens.** O token real só existe no cookie do navegador; o banco guarda o hash SHA-256. Cada login abre uma família, e cada renovação troca o token por outro da mesma família. Se um token já trocado reaparece, a família inteira é revogada. Uma check constraint garante que `revoked_at` e `revoked_reason` sejam preenchidos juntos. Os tokens somem em cascata com o usuário, e um job diário apaga os expirados há mais de uma semana.

## Convenções

- Ids são UUID gerados pela aplicação: não expõem volume nem facilitam enumerar recursos.
- Instantes usam `timestamptz`. O plano semanal (Fase 2) usará `date` e `time` locais, interpretados no fuso do usuário.
- O schema é versionado pelo Flyway em SQL; o Hibernate só valida o mapeamento (`ddl-auto: validate`).
- Entidades com `@Version` usam lock otimista: duas edições simultâneas não se sobrescrevem em silêncio (a API responde 409).
