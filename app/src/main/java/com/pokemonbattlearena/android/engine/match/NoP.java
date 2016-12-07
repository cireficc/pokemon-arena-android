package com.pokemonbattlearena.android.engine.match;

/**
 * Created by nathan on 12/3/16.
 */

public class NoP extends Command{

    BattlePokemonPlayer attackingPlayer;


    public NoP(){};

    public NoP (BattlePokemonPlayer attackingPlayer) {
        this.attackingPlayer = attackingPlayer;
    }

    public BattlePokemonPlayer getAttackingPlayer() {
        return  attackingPlayer;
    }


    public NoPResult execute(Battle battle) {
        NoPResult.Builder builder = new NoPResult.Builder(new TargetInfo());
        return builder.build();

    }

}
