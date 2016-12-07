package com.pokemonbattlearena.android.engine.match;

// TODO: convert to builder pattern, so as not to have ridiculous constructor overloads

public class TargetInfo {

    private BattlePokemonPlayer attackingPlayer;
    private BattlePokemonPlayer defendingPlayer;
    private transient BattlePokemon attackingPokemon;
    private transient BattlePokemon defendingPokemon;

    /*
     * This constructor is used in AttackResult.Builder.
     */
    protected TargetInfo(BattlePokemonPlayer attackingPlayer, BattlePokemonPlayer defendingPlayer,
                         BattlePokemon attackingPokemon, BattlePokemon defendingPokemon) {
        this.attackingPlayer = attackingPlayer;
        this.defendingPlayer = defendingPlayer;
        this.attackingPokemon = attackingPokemon;
        this.defendingPokemon = defendingPokemon;
    }

    /*
     * This constructor is used in SwitchResult.Builder.
     */
    protected TargetInfo(BattlePokemonPlayer attackingPlayer) {
        this.attackingPlayer = attackingPlayer;
    }


    /*
     * This constructor is used in NoPResult.Builder.
     */
    protected TargetInfo() {
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
