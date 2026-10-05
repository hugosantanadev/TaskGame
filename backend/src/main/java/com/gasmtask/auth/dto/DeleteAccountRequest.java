package com.gasmtask.auth.dto;

/** Confirmação da exclusão: a senha atual. */
public record DeleteAccountRequest(String password) {
}
