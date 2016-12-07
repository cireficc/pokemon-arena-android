package com.pokemonbattlearena.android.engine.match;

public class Switch extends Command {

    private transient static final String TAG = Switch.class.getName();

    private BattlePokemonPlayer attackingPlayer;
    private int positionToSwitchTo;

    public Switch(BattlePokemonPlayer attacker, int positionToSwitchTo) {
        this.attackingPlayer = attacker;
        this.positionToSwitchTo = positionToSwitchTo;
    }
    public  BattlePokemonPlayer getAttackingPlayer () { return attackingPlayer; }

    public BattlePokemonPlayer getAttackingBattlePlayer(Battle battle) {
        return battle.getPlayerFromId(attackingPlayer.getId());
    }

    public int getPositionToSwitchTo() {
        return positionToSwitchTo;
    }

    public String pokemonSwitchingTo(Battle battle) {
        return getAttackingBattlePlayer(battle).getBattlePokemonTeam().getBattlePokemons().get(positionToSwitchTo).getOriginalPokemon().getName();
    }


    @Override
    public SwitchResult execute(Battle battle) {

        BattlePokemonPlayer attackingPlayer = getAttackingBattlePlayer(battle);
        TargetInfo targetInfo = new TargetInfo(attackingPlayer);
        SwitchResult.Builder builder = new SwitchResult.Builder(targetInfo);

        builder.setPositionOfPokemon(positionToSwitchTo);

        return builder.build();
    }
}
