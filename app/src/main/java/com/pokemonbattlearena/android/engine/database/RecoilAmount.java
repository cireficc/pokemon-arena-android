package com.pokemonbattlearena.android.engine.database;

/**
 * Created by TV on 10/26/2016.
 */

public enum RecoilAmount {
    ONETHIRD("OneThird"),
    ONEFOURTH("OneFourth");

    private String amount;

    RecoilAmount(String amount) {
        this.amount = amount;
    }
}
