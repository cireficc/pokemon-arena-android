package com.pokemonbattlearena.android.engine.ai;

import com.pokemonbattlearena.android.engine.database.Move;
import com.pokemonbattlearena.android.engine.match.Attack;
import com.pokemonbattlearena.android.engine.match.Battle;
import com.pokemonbattlearena.android.engine.match.BattlePokemon;
import com.pokemonbattlearena.android.engine.match.BattlePokemonTeam;
import com.pokemonbattlearena.android.engine.match.Command;
import com.pokemonbattlearena.android.engine.match.PokemonPlayer;

public class AiBattle extends Battle {

    protected MiniMax intelligence;

    public AiBattle(PokemonPlayer humanPlayer, AiPlayer aiPlayer) {
        super(humanPlayer, aiPlayer);
      //  BattlePokemonTeam aiPokemon = this.getOpponent().getBattlePokemonTeam();
      //  BattlePokemonTeam playerPokemon = this.getSelf().getBattlePokemonTeam();

        this.intelligence = new MiniMax(this.getOpponent(), this.getSelf());


    }

    public Command getNextCommand() {

        // TODO: Actually calculate shit using heuristics instead of hard-coding moves

//        BattlePokemonTeam humanTeam = this.getOpponent().getBattlePokemonTeam();
//        Move[] humanCurrentPokemonMoves = playerPokemon.getMoveSet();
//        int humanCurrentPokemonHp = playerPokemon.getCurrentHp();

//        return new Attack(aiPokemon, moveToUse, playerPokemon);
        return null;
    }
}
