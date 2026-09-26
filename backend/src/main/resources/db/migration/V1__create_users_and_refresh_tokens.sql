-- Identidade do usuário. E-mail sempre gravado em minúsculas pela aplicação.
create table users (
    id              uuid         primary key,
    email           varchar(254) not null,
    password_hash   varchar(100) not null,
    display_name    varchar(40)  not null,
    time_zone       varchar(64)  not null,
    ranking_visible boolean      not null default true,
    created_at      timestamptz  not null,
    updated_at      timestamptz  not null,
    version         bigint       not null default 0,
    constraint uk_users_email unique (email)
);

-- Refresh tokens opacos. Guardamos só o hash SHA-256; o valor real vive apenas no cookie do navegador.
-- family_id agrupa as rotações de uma mesma sessão (um login = uma família).
create table refresh_tokens (
    id             uuid        primary key,
    family_id      uuid        not null,
    user_id        uuid        not null references users (id) on delete cascade,
    token_hash     varchar(64) not null,
    created_at     timestamptz not null,
    expires_at     timestamptz not null,
    revoked_at     timestamptz,
    revoked_reason varchar(20),
    version        bigint      not null default 0,
    constraint uk_refresh_tokens_token_hash unique (token_hash),
    constraint ck_refresh_tokens_revocation
        check ((revoked_at is null) = (revoked_reason is null))
);

create index idx_refresh_tokens_family_id on refresh_tokens (family_id);
create index idx_refresh_tokens_user_id on refresh_tokens (user_id);
create index idx_refresh_tokens_expires_at on refresh_tokens (expires_at);
