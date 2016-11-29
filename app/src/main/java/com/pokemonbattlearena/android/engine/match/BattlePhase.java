package com.pokemonbattlearena.android.engine.match;

import android.util.Log;

import com.pokemonbattlearena.android.engine.database.Move;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import static com.pokemonbattlearena.android.engine.Logging.logIsPhaseReady;
import static com.pokemonbattlearena.android.engine.Logging.logQueueCommand;
import static com.pokemonbattlearena.android.engine.Logging.logSetPlayerReady;

public class BattlePhase {

    private transient BattlePokemonPlayer player1;
    private transient BattlePokemonPlayer player2;
    private transient boolean player1Ready;
    private transient boolean player2Ready;
    private List<Command> commands;
    private BattlePhaseResult battlePhaseResult;

    BattlePhase(BattlePokemonPlayer player1, BattlePokemonPlayer player2) {
        this.player1 = player1;
        this.player2 = player2;
        this.player1Ready = false;
        this.player2Ready = false;
        this.commands = new ArrayList<>();
    }

    public List<Command> getCommands() {

        return commands;
    }

    public BattlePhaseResult getBattlePhaseResult() {

        return battlePhaseResult;
    }

    public void setBattlePhaseResult(BattlePhaseResult battlePhaseResult) {
        this.battlePhaseResult = battlePhaseResult;
    }

    public boolean queueCommand(Command command) {

        this.commands.add(command);

        if (command instanceof Switch) {
            Switch s = (Switch) command;
            setPlayerReady(s.getAttackingPlayer());
        } else if (command instanceof Attack) {
            Attack a = (Attack) command;
            setPlayerReady(a.getAttackingPlayer());
        }

        if (logQueueCommand) {
            logQueueCommand(command);
        }
        return isPhaseReady();
    }

    private void setPlayerReady(BattlePokemonPlayer player) {

        if(player.equals(player1)) {
            player1Ready = true;
        } else {
            player2Ready = true;
        }

        if (logSetPlayerReady) {
            logSetPlayerReady(player, player1);
        }
    }

    private boolean isPhaseReady() {

        boolean ready = (player1Ready && player2Ready);

        if(logIsPhaseReady) {
            logIsPhaseReady(ready);
        }

        return ready;
    }
}
