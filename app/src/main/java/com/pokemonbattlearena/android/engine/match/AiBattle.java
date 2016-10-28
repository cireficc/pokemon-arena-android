package com.pokemonbattlearena.android.engine.match;

import com.pokemonbattlearena.android.engine.database.Database;
import com.pokemonbattlearena.android.engine.database.Move;

public class AiBattle extends Battle {

    private Database database;

    public AiBattle(Database database, PokemonPlayer humanPlayer) {
        super(new AiPlayer(database), humanPlayer);

        this.database = database;
    }

    public Command getNextCommand() {

        BattlePokemon aiPokemon = this.self.getBattlePokemonTeam().getCurrentPokemon();
        BattlePokemon playerPokemon = this.opponent.getBattlePokemonTeam().getCurrentPokemon();
        Move moveToUse = aiPokemon.getMoveSet()[0];

        // TODO: Actually calculate shit using heuristics instead of hard-coding moves

        BattlePokemonTeam humanTeam = this.opponent.getBattlePokemonTeam();
        Move[] humanCurrentPokemonMoves = playerPokemon.getMoveSet();
        int humanCurrentPokemonHp = playerPokemon.getCurrentHp();

        return new Attack(aiPokemon, moveToUse, playerPokemon);
    }
}