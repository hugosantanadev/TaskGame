package com.gasmtask.economy.controller;

import com.gasmtask.economy.dto.CoinTransactionResponse;
import com.gasmtask.economy.dto.WalletResponse;
import com.gasmtask.economy.service.WalletService;
import com.gasmtask.shared.security.AuthenticatedUser;
import com.gasmtask.shared.web.PageResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/wallet")
@Tag(name = "Carteira")
public class WalletController {

    private final WalletService walletService;

    public WalletController(WalletService walletService) {
        this.walletService = walletService;
    }

    @GetMapping
    @Operation(summary = "Saldo e totais ganhos e gastos")
    public WalletResponse summary(@AuthenticationPrincipal AuthenticatedUser user) {
        return walletService.summary(user.id());
    }

    @GetMapping("/transactions")
    @Operation(summary = "Extrato paginado, do mais recente ao mais antigo")
    public PageResponse<CoinTransactionResponse> transactions(@AuthenticationPrincipal AuthenticatedUser user,
                                                              @RequestParam(defaultValue = "0") int page,
                                                              @RequestParam(defaultValue = "20") int size) {
        return walletService.transactions(user.id(), page, size);
    }
}
