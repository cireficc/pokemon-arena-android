package com.pokemonbattlearena.android.engine.ai;

import com.pokemonbattlearena.android.engine.database.Move;
import com.pokemonbattlearena.android.engine.match.Battle;
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

    public Command showIntelligence() {

        return intelligence.choose().getCommand();
    }
}
