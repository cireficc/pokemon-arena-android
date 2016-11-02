package com.pokemonbattlearena.android.engine.ai;

import com.pokemonbattlearena.android.engine.match.Battle;
import com.pokemonbattlearena.android.engine.match.PokemonPlayer;

public class AiBattle extends Battle {

    protected MiniMax intelligence;

    public AiBattle(PokemonPlayer humanPlayer, AiPlayer aiPlayer) {
        super(humanPlayer, aiPlayer);
      //  BattlePokemonTeam aiPokemon = this.getOpponent().getBattlePokemonTeam();
      //  BattlePokemonTeam playerPokemon = this.getSelf().getBattlePokemonTeam();
        this.intelligence = new MiniMax(this.getOpponent(), this.getSelf());
    }

    public String showIntelligence() {

        return intelligence.choose().getValue().toString();
    }
}
