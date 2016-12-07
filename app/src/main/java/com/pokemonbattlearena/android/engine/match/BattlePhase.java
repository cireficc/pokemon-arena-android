package com.pokemonbattlearena.android.engine.match;

import android.util.Log;

import java.util.ArrayList;
import java.util.List;

public class BattlePhase {

    private transient static final String TAG = BattlePhase.class.getName();

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

        Log.i(TAG, "Adding command of type " + command.getClass() + " to command list");
        this.commands.add(command);

        if (command instanceof Switch) {
            Switch s = (Switch) command;
            setPlayerReady(s.getAttackingPlayer());
        } else if (command instanceof Attack) {
            Attack a = (Attack) command;
            setPlayerReady(a.getAttackingPlayer());
        } else if (command instanceof NoP) {
            NoP n = (NoP) command;
            setPlayerReady(n.getAttackingPlayer());
        }

        return isPhaseReady();
    }

    private void setPlayerReady(BattlePokemonPlayer player) {

        Log.i(TAG, "Setting player ready");

        if(player.equals(player1)) {
            Log.i(TAG, "Player 1 ready");
            player1Ready = true;
        } else {
            Log.i(TAG, "Player 2 ready");
            player2Ready = true;
        }
    }

    private boolean isPhaseReady() {

        boolean ready = (player1Ready && player2Ready);
        Log.i(TAG, "Is phase ready (both players ready): " + ready);

        return ready;
    }
}
