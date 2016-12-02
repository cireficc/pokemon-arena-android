package com.pokemonbattlearena.android.engine.match;

import com.pokemonbattlearena.android.engine.ai.StatePokemon;
import com.pokemonbattlearena.android.engine.database.Pokemon;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class BattlePokemonTeam {

    public List<BattlePokemon> battlePokemons;
    public int indexOfCurrent = 0;
    public int indexOfFuture = 0;

    public BattlePokemonTeam (StatePokemon[] statePokemons) {

        this.battlePokemons = new ArrayList<>();
        for (StatePokemon sp: statePokemons) {
            this.battlePokemons.add(sp.toBattle());
        }

    }

    public BattlePokemonTeam(PokemonTeam pokemonTeam) {

        this.battlePokemons = new ArrayList<>();
        for (Pokemon p : pokemonTeam.getPokemons()) {
            this.battlePokemons.add(new BattlePokemon(p));
        }
        battlePokemons.get(indexOfCurrent).setAsCurrentPokemon(true);
        battlePokemons.get(indexOfFuture).setAsPokemonOnDeck(true);
    }

    public List<BattlePokemon> getBattlePokemons() {
        return battlePokemons;
    }

    public BattlePokemon getCurrentPokemon() { return battlePokemons.get(indexOfCurrent); }

    public BattlePokemon getPokemonOnDeck() { return battlePokemons.get(indexOfFuture); }

    public void setPokemonOnDeck (int position) {
        indexOfFuture = position;
        battlePokemons.get(indexOfFuture).setAsPokemonOnDeck(true);
    }

    public void switchPokemonAtPosition(int position) {
        battlePokemons.get(indexOfCurrent).setAsCurrentPokemon(false);
        battlePokemons.get(indexOfFuture).setAsPokemonOnDeck(false);
        battlePokemons.get(position).setAsCurrentPokemon(true);
        battlePokemons.get(position).setAsPokemonOnDeck(true);

        indexOfCurrent = position;
        indexOfFuture = position;
    }

    public boolean allFainted() {

        for (BattlePokemon pokemon : battlePokemons) {
            if (!pokemon.isFainted()) {
                return false;
            }
        }

        return true;
    }
}
