package com.pokemonbattlearena.android.engine.match;

class Switch implements Command {

    private transient static final String TAG = Switch.class.getName();

    private BattlePokemonPlayer attackingPlayer;
    private int positionToSwitchTo;

    Switch(BattlePokemonPlayer attacker, int positionToSwitchTo) {
        this.attackingPlayer = attacker;
        this.positionToSwitchTo = positionToSwitchTo;
    }

    public BattlePokemonPlayer getAttackingPlayer() {
        return attackingPlayer;
    }

    public int getPositionToSwitchTo() {
        return positionToSwitchTo;
    }

    @Override
    public SwitchResult execute() {

        TargetInfo targetInfo = new TargetInfo(attackingPlayer);
        SwitchResult.Builder builder = new SwitchResult.Builder(targetInfo);

        builder.setPositionOfPokemon(positionToSwitchTo);

        return builder.build();
    }
}
