package com.pokemonbattlearena.android.engine.match;

import android.util.Log;

import com.pokemonbattlearena.android.engine.database.Move;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class BattlePhase {

    private transient static final String TAG = BattlePhase.class.getName();

    private transient BattlePokemonPlayer player1;
    private transient BattlePokemonPlayer player2;
    private transient boolean player1Ready;
    private transient boolean player2Ready;
    private List<Command> commands;
    private BattlePhaseResult battlePhaseResult;

    /*
     * A custom Comparator to determine the order of commands (player actions).
     * Pokemon switching always occurs first. Attack order is determined by the
     * Pokemon's speed - the faster Pokemon attacks first. However, some moves
     * such as Quick Attack will give the attacker priority in the queue.
     */
    private static transient Comparator<Command> commandComparator = new Comparator<Command>() {
        @Override
        public int compare(Command c1, Command c2) {

            Attack a1 = (Attack) c1;
            Attack a2 = (Attack) c2;
            int pokemon1Speed = a1.getAttackingPokemon().getOriginalPokemon().getSpeed();
            int pokemon2Speed = a2.getAttackingPokemon().getOriginalPokemon().getSpeed();

            Log.i(TAG, "Pokemon 1 speed: " + pokemon1Speed + " || Pokemon 2 speed: " + pokemon2Speed);

            // TODO: Use instanceof for custom behavior

            return pokemon2Speed -  pokemon1Speed;
        }
    };

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

    public static Comparator<Command> getCommandComparator() {
        return commandComparator;
    }

    public boolean queueAction(BattlePokemonPlayer attackingPlayer, BattlePokemonPlayer defendingPlayer, Move move) {

        Log.i(TAG, "Queueing action (Move): " + move);

        Attack attack = new Attack(attackingPlayer, defendingPlayer, move);

        Log.i(TAG, "Adding Attack to command list");
        this.commands.add(attack);
        setPlayerReady(attackingPlayer);

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

    protected BattlePhaseResult execute() {

        Log.i(TAG, "Executing current battle phase from BattlePhase");
        Log.i(TAG, "Sorting commands by priority");
        Collections.sort(commands, commandComparator);
        battlePhaseResult = new BattlePhaseResult();

        for (Command command : commands) {
            Log.i(TAG, "Executing command of type: " + command.getClass());

            CommandResult commandResult = command.execute();

            Log.i(TAG, "Adding command result to battle phase result");
            battlePhaseResult.addCommandResult(commandResult);
        }

        return battlePhaseResult;
    }
}
