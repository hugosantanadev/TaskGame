package com.gasmtask.chest.domain;

/**
 * Tamanho do baú semanal, pelos dias cumpridos na semana anterior. Com 0 ou 1 dia não há baú. Ouro e lendário
 * trazem um item da loja que a pessoa ainda não tem (até um preço máximo); se ela já tiver comprado quando abrir,
 * o item vira moedas.
 */
public enum ChestTier {
    NONE(0, 0, 0),
    WOOD(15, 15, 0),
    SILVER(30, 30, 0),
    GOLD(50, 50, 40),
    LEGENDARY(80, 80, 120);

    private final int coins;
    private final int xp;
    private final int maxItemPrice;

    ChestTier(int coins, int xp, int maxItemPrice) {
        this.coins = coins;
        this.xp = xp;
        this.maxItemPrice = maxItemPrice;
    }

    public static ChestTier forFulfilledDays(int days) {
        if (days >= 7) {
            return LEGENDARY;
        }
        if (days == 6) {
            return GOLD;
        }
        if (days >= 4) {
            return SILVER;
        }
        return days >= 2 ? WOOD : NONE;
    }

    public int coins() {
        return coins;
    }

    public int xp() {
        return xp;
    }

    /** Preço máximo do item que o baú pode trazer; zero quando não traz item. */
    public int maxItemPrice() {
        return maxItemPrice;
    }

    /** Moedas que substituem o item quando a pessoa já o comprou antes de abrir. */
    public int itemFallbackCoins() {
        return maxItemPrice / 2;
    }
}
