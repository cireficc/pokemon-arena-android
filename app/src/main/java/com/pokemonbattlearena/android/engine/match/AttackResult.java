package com.pokemonbattlearena.android.engine.match;

import android.util.Log;

import com.pokemonbattlearena.android.engine.database.Move;
import com.pokemonbattlearena.android.engine.database.StatType;
import com.pokemonbattlearena.android.engine.database.StatusEffect;

public class AttackResult extends CommandResult {

    private transient static final String TAG = AttackResult.class.getName();

    private Move moveUsed;

    private boolean moveHit;
    private int damageDone;
    private StatusEffect statusEffectApplied;
    private int statusEffectTurns;
    private boolean succumbedToStatusEffect;
    private boolean unfroze;
    private boolean confused;
    private int confusedTurns;
    private int confusionDamageTaken;
    private int burnDamageTaken;
    private int poisonDamageTaken;
    private boolean flinched;
    private int chargingTurns;
    private int rechargingTurns;
    private int healingDone;
    private int recoilTaken;
    private boolean fainted;
    private int attackStageChange;
    private int defenseStageChange;
    private int spAttackStageChange;
    private int spDefenseStageChange;
    private int speedStageChange;
    private int critStageChange;
    private boolean isHaze;

    private AttackResult(Builder builder) {

        super();

        this.targetInfo = builder.targetInfo;
        this.moveUsed = builder.moveUsed;

        this.moveHit = builder.moveHit;
        this.damageDone = builder.damageDone;
        this.statusEffectApplied = builder.statusEffectApplied;
        this.statusEffectTurns = builder.statusEffectTurns;
        this.succumbedToStatusEffect = builder.succumbedToStatusEffect;
        this.unfroze = builder.unfroze;
        this.confused = builder.confused;
        this.confusedTurns = builder.confusedTurns;
        this.confusionDamageTaken = builder.confusionDamageTaken;
        this.burnDamageTaken = builder.burnDamageTaken;
        this.poisonDamageTaken = builder.poisonDamageTaken;
        this.flinched = builder.flinched;
        this.chargingTurns = builder.chargingTurns;
        this.rechargingTurns = builder.rechargingTurns;
        this.healingDone = builder.healingDone;
        this.recoilTaken = builder.recoilTaken;
        this.fainted = builder.fainted;
        this.attackStageChange = builder.attackStageChange;
        this.defenseStageChange = builder.defenseStageChange;
        this.spAttackStageChange = builder.spAttackStageChange;
        this.spDefenseStageChange = builder.spDefenseStageChange;
        this.speedStageChange = builder.speedStageChange;
        this.critStageChange = builder.critStageChange;
        this.isHaze = builder.isHaze;
    }

    public Move getMoveUsed() {
        return moveUsed;
    }

    public boolean isMoveHit() {
        return moveHit;
    }

    public int getDamageDone() {
        return damageDone;
    }

    public StatusEffect getStatusEffectApplied() {
        return statusEffectApplied;
    }

    public boolean isSuccumbedToStatusEffect() {
        return succumbedToStatusEffect;
    }

    public boolean isUnfroze() {
        return unfroze;
    }

    public boolean isConfused() {
        return confused;
    }

    public int getConfusedTurns() {
        return confusedTurns;
    }

    public int getConfusionDamageTaken() {
        return confusionDamageTaken;
    }

    public int getBurnDamageTaken() {
        return burnDamageTaken;
    }

    public int getPoisonDamageTaken() {
        return poisonDamageTaken;
    }

    public boolean isFlinched() {
        return flinched;
    }

    public int getChargingTurns() {
        return chargingTurns;
    }

    public int getRechargingTurns() {
        return rechargingTurns;
    }

    public int getStatusEffectTurns() {
        return statusEffectTurns;
    }

    public int getHealingDone() {
        return healingDone;
    }

    public int getRecoilTaken() {
        return recoilTaken;
    }

    public boolean isFainted() {
        return fainted;
    }

    public int getAttackStageChange() {
        return attackStageChange;
    }

    public int getDefenseStageChange() {
        return defenseStageChange;
    }

    public int getSpAttackStageChange() {
        return spAttackStageChange;
    }

    public int getSpDefenseStageChange() {
        return spDefenseStageChange;
    }

    public int getSpeedStageChange() {
        return speedStageChange;
    }

    public int getCritStageChange() {
        return critStageChange;
    }

    public boolean isHaze() {
        return isHaze;
    }

    protected static class Builder {

        private TargetInfo targetInfo;
        private Move moveUsed;

        private boolean moveHit;
        private int damageDone;
        private StatusEffect statusEffectApplied;
        private int statusEffectTurns;
        private boolean succumbedToStatusEffect;
        private boolean unfroze;
        private boolean confused;
        private int confusedTurns;
        private int confusionDamageTaken;
        private int burnDamageTaken;
        private int poisonDamageTaken;
        private boolean flinched;
        private int chargingTurns;
        private int rechargingTurns;
        private int healingDone;
        private int recoilTaken;
        private boolean fainted;
        private int attackStageChange;
        private int defenseStageChange;
        private int spAttackStageChange;
        private int spDefenseStageChange;
        private int speedStageChange;
        private int critStageChange;
        private boolean isHaze;

        protected Builder(TargetInfo targetInfo, Move moveUsed) {
            this.targetInfo = targetInfo;
            this.moveUsed = moveUsed;
        }

        public void setMoveHit(boolean moveHit) {
            this.moveHit = moveHit;
        }

        protected Builder setDamageDone(int damageDone) {
            this.damageDone = damageDone;
            return this;
        }

        protected Builder setStatusEffectApplied(StatusEffect statusEffect) {
            this.statusEffectApplied = statusEffect;
            return this;
        }

        protected Builder setStatusEffectTurns(int statusEffectTurns) {
            this.statusEffectTurns = statusEffectTurns;
            return this;
        }

        protected Builder setSuccumbedToStatusEffect(boolean succumbedToStatusEffect) {
            this.succumbedToStatusEffect = succumbedToStatusEffect;
            return this;
        }

        protected Builder setUnfroze(boolean unfroze) {
            this.unfroze = unfroze;
            return this;
        }

        protected Builder setConfused(boolean confused) {
            this.confused = confused;
            return this;
        }

        protected Builder setConfusedTurns(int confusedTurns) {
            this.confusedTurns = confusedTurns;
            return this;
        }

        protected Builder setConfusionDamageTaken(int confusionDamageTaken) {
            this.confusionDamageTaken = confusionDamageTaken;
            return this;
        }

        public int getBurnDamageTaken() {
            return burnDamageTaken;
        }

        public void setBurnDamageTaken(int burnDamageTaken) {
            this.burnDamageTaken = burnDamageTaken;
        }

        public int getPoisonDamageTaken() {
            return poisonDamageTaken;
        }

        public void setPoisonDamageTaken(int poisonDamageTaken) {
            this.poisonDamageTaken = poisonDamageTaken;
        }

        protected Builder setFlinched(boolean flinched) {
            this.flinched = flinched;
            return this;
        }

        protected Builder setChargingTurns(int chargingTurns) {
            this.chargingTurns = chargingTurns;
            return this;
        }

        protected Builder setRechargingTurns(int rechargingTurns) {
            this.rechargingTurns = rechargingTurns;
            return this;
        }

        protected Builder setHealingDone(int healingDone) {
            this.healingDone = healingDone;
            return this;
        }

        protected Builder setRecoilTaken(int recoilTaken) {
            this.recoilTaken = recoilTaken;
            return this;
        }

        protected Builder setFainted(boolean fainted) {
            this.fainted = fainted;
            return this;
        }

        protected Builder setAttackStageChange(int stageChange) {
            this.attackStageChange = stageChange;
            return this;
        }

        protected Builder setDefenseStageChange(int stageChange) {
            this.defenseStageChange = stageChange;
            return this;
        }

        protected Builder setSpAttackStageChange(int stageChange) {
            this.spAttackStageChange = stageChange;
            return this;
        }

        protected Builder setSpDefenseStageChange(int stageChange) {
            this.spDefenseStageChange = stageChange;
            return this;
        }

        protected Builder setSpeedStageChange(int stageChange) {
            this.speedStageChange = stageChange;
            return this;
        }

        protected Builder setCritStageChange(int stageChange) {
            this.critStageChange = stageChange;
            return this;
        }

        protected Builder setIsHaze(boolean isHaze) {
            this.isHaze = isHaze;
            return this;
        }

        protected AttackResult build() {

            Log.i(TAG, "Building AttackResult");
            return new AttackResult(this);
        }
    }
}
