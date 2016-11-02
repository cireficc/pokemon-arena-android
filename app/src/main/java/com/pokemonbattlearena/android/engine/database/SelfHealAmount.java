package com.pokemonbattlearena.android.engine.database;

public enum SelfHealAmount {
    HALF("Half");

    private String amount;

    SelfHealAmount(String amount) {
        this.amount = amount;
    }
}
