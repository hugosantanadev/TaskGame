package com.gasmtask.shared.exception;

import org.springframework.http.HttpStatus;

/**
 * Códigos de erro estáveis da API. O frontend decide a mensagem exibida a partir do {@code code};
 * o texto padrão serve como {@code detail} do Problem Details.
 */
public enum ErrorCode {

    VALIDATION_FAILED(HttpStatus.BAD_REQUEST, "Há campos inválidos na requisição."),
    MALFORMED_REQUEST(HttpStatus.BAD_REQUEST, "Não foi possível ler o corpo da requisição."),
    UNAUTHENTICATED(HttpStatus.UNAUTHORIZED, "É preciso entrar na sua conta."),
    INVALID_CREDENTIALS(HttpStatus.UNAUTHORIZED, "E-mail ou senha incorretos."),
    INVALID_REFRESH_TOKEN(HttpStatus.UNAUTHORIZED, "Sua sessão terminou. Entre novamente."),
    ACCESS_DENIED(HttpStatus.FORBIDDEN, "Você não tem permissão para esta ação."),
    RESOURCE_NOT_FOUND(HttpStatus.NOT_FOUND, "Recurso não encontrado."),
    METHOD_NOT_ALLOWED(HttpStatus.METHOD_NOT_ALLOWED, "Método HTTP não suportado neste endereço."),
    EMAIL_ALREADY_REGISTERED(HttpStatus.CONFLICT, "Já existe uma conta com este e-mail."),
    DAY_LOCKED(HttpStatus.UNPROCESSABLE_CONTENT, "Hoje e os dias passados não aceitam essa mudança. Ela vale a partir de amanhã."),
    DAILY_LIMIT_REACHED(HttpStatus.UNPROCESSABLE_CONTENT, "Esse dia já atingiu o limite de tarefas."),
    WEEK_NOT_AVAILABLE(HttpStatus.UNPROCESSABLE_CONTENT, "Só dá para planejar a semana atual e a próxima."),
    INVALID_WEEK_START(HttpStatus.BAD_REQUEST, "A semana precisa começar numa segunda-feira."),
    OCCURRENCE_ALREADY_ON_DATE(HttpStatus.CONFLICT, "Essa missão já está planejada nesse dia."),
    OCCURRENCE_NOT_PENDING(HttpStatus.CONFLICT, "Essa tarefa já foi concluída ou perdida."),
    NOT_COMPLETABLE_TODAY(HttpStatus.UNPROCESSABLE_CONTENT, "Uma tarefa só pode ser concluída no próprio dia."),
    PROOF_REQUIRED(HttpStatus.UNPROCESSABLE_CONTENT, "Essa missão exige uma foto como prova."),
    PROOF_ALREADY_ATTACHED(HttpStatus.CONFLICT, "Essa tarefa já tem uma prova."),
    INVALID_IMAGE(HttpStatus.UNSUPPORTED_MEDIA_TYPE, "Envie uma imagem JPEG, PNG ou WebP."),
    TASK_ARCHIVED(HttpStatus.CONFLICT, "Essa missão está arquivada."),
    INVALID_SCHEDULE(HttpStatus.UNPROCESSABLE_CONTENT, "Cada dia da semana pode aparecer só uma vez na recorrência."),
    INSUFFICIENT_COINS(HttpStatus.UNPROCESSABLE_CONTENT, "Moedas insuficientes para essa compra."),
    ITEM_ALREADY_OWNED(HttpStatus.CONFLICT, "Esse item já está na sua coleção."),
    ITEM_NOT_AVAILABLE(HttpStatus.UNPROCESSABLE_CONTENT, "Esse item não está à venda."),
    ITEM_NOT_FOR_ROOM(HttpStatus.UNPROCESSABLE_CONTENT, "Esse item é do personagem, não do quarto."),
    ITEM_NOT_FOR_SLOT(HttpStatus.UNPROCESSABLE_CONTENT, "Esse item não serve nesse lugar do personagem."),
    FREEZE_LIMIT_REACHED(HttpStatus.UNPROCESSABLE_CONTENT, "Você já guardou o máximo de protetores de sequência."),
    CONCURRENT_MODIFICATION(HttpStatus.CONFLICT, "Os dados mudaram enquanto você editava. Tente de novo."),
    PAYLOAD_TOO_LARGE(HttpStatus.CONTENT_TOO_LARGE, "O arquivo enviado é grande demais."),
    UNSUPPORTED_MEDIA_TYPE(HttpStatus.UNSUPPORTED_MEDIA_TYPE, "Formato de conteúdo não suportado."),
    REQUEST_FAILED(HttpStatus.BAD_REQUEST, "Não foi possível processar a requisição."),
    INTERNAL_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "Erro inesperado. Tente novamente em instantes.");

    private final HttpStatus status;
    private final String defaultMessage;

    ErrorCode(HttpStatus status, String defaultMessage) {
        this.status = status;
        this.defaultMessage = defaultMessage;
    }

    public HttpStatus status() {
        return status;
    }

    public String defaultMessage() {
        return defaultMessage;
    }

    /** Código genérico para erros do próprio framework (rota inexistente, método errado etc.). */
    public static ErrorCode fromStatus(int status) {
        return switch (status) {
            case 401 -> UNAUTHENTICATED;
            case 403 -> ACCESS_DENIED;
            case 404 -> RESOURCE_NOT_FOUND;
            case 405 -> METHOD_NOT_ALLOWED;
            case 409 -> CONCURRENT_MODIFICATION;
            case 413 -> PAYLOAD_TOO_LARGE;
            case 415 -> UNSUPPORTED_MEDIA_TYPE;
            default -> status >= 500 ? INTERNAL_ERROR : REQUEST_FAILED;
        };
    }
}
