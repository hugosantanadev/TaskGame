package com.gasmtask.economy.service;

import com.gasmtask.economy.domain.Wallet;
import com.gasmtask.economy.repository.WalletRepository;
import com.gasmtask.user.domain.UserRegisteredEvent;

import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
class WalletRegistrationListener {

    private final WalletRepository wallets;

    WalletRegistrationListener(WalletRepository wallets) {
        this.wallets = wallets;
    }

    @EventListener
    void onUserRegistered(UserRegisteredEvent event) {
        wallets.save(Wallet.open(event.userId(), event.registeredAt()));
    }
}
