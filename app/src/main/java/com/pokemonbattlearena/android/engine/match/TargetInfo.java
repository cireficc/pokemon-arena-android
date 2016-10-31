package com.pokemonbattlearena.android.engine.match;

public class TargetInfo {

    private BattlePokemonPlayer attackingPlayer;
    private BattlePokemonPlayer defendingPlayer;
    private transient BattlePokemon attackingPokemon;
    private transient BattlePokemon defendingPokemon;

    protected TargetInfo(BattlePokemonPlayer attackingPlayer, BattlePokemonPlayer defendingPlayer,
                         BattlePokemon attackingPokemon, BattlePokemon defendingPokemon) {
        this.attackingPlayer = attackingPlayer;
        this.defendingPlayer = defendingPlayer;
        this.attackingPokemon = attackingPokemon;
        this.defendingPokemon = defendingPokemon;
    }

    public BattlePokemonPlayer getAttackingPlayer() {
        return attackingPlayer;
    }

    public BattlePokemonPlayer getDefendingPlayer() {
        return defendingPlayer;
    }

    public BattlePokemon getAttackingPokemon() {
        return attackingPokemon;
    }

    public BattlePokemon getDefendingPokemon() {
        return defendingPokemon;
    }
}
