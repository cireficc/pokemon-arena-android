package com.pokemonbattlearena.android.engine.ai;

import com.pokemonbattlearena.android.engine.database.Move;
import com.pokemonbattlearena.android.engine.match.Battle;
import com.pokemonbattlearena.android.engine.match.BattlePokemon;
import com.pokemonbattlearena.android.engine.match.BattlePokemonTeam;
import com.pokemonbattlearena.android.engine.match.Command;
import com.pokemonbattlearena.android.engine.match.PokemonPlayer;

public class AiBattle extends Battle {

    protected MiniMax intelligence;
    protected int maxAIHP;
    protected int maxHuHP;


    public AiBattle(PokemonPlayer humanPlayer, AiPlayer aiPlayer) {
        super(humanPlayer, aiPlayer);
        BattlePokemonTeam aiPokemon = this.getOpponent().getBattlePokemonTeam();
        BattlePokemonTeam playerPokemon = this.getSelf().getBattlePokemonTeam();
        maxAIHP = calculateTeamHP(aiPokemon);
        maxHuHP = calculateTeamHP(playerPokemon);
    }

    public void buildIntelligence(Battle battle, boolean haveToSwitch) {
        this.intelligence = new MiniMax(
                this.getOpponent(),
                this.getSelf(),
                maxAIHP,
                maxHuHP,
                this.getCurrentBattlePhase().getCommands().get(0),
                battle,
                haveToSwitch);
    }

    public Command showIntelligence() {

        return intelligence.choose().getCommand();
    }

    public static int calculateTeamHP(BattlePokemonTeam team) {
        int totalHP = 0;
        for (BattlePokemon bp : team.getBattlePokemons()) {
            totalHP += bp.getCurrentHp();
        }
        return totalHP;
    }
}
