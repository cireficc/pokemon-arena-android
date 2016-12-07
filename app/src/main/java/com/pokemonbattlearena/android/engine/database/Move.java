package com.pokemonbattlearena.android.engine.database;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.field.types.StringBytesType;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable(tableName = "moves")
public class Move {

    protected transient final static String ID_FIELD_NAME = "id";
    protected transient final static String NAME_FIELD_NAME = "name";
    protected transient final static String DESCRIPTION_FIELD_NAME = "description";
    protected transient final static String TYPE_1_FIELD_NAME = "type_1";
    protected transient final static String CATEGORY_FIELD_NAME = "category";
    protected transient final static String POWER_FIELD_NAME = "power";
    protected transient final static String ACCURACY_FIELD_NAME = "accuracy";
    protected transient final static String POWER_POINTS_FIELD_NAME = "power_points";
    protected transient final static String STATUS_EFFECT_FIELD_NAME = "status_effect";
    protected transient final static String STATUS_EFFECT_CHANCE_FIELD_NAME = "status_effect_chance";
    protected transient final static String STAGE_CHANGE_STAT_FIELD_NAME = "stage_change_stat";
    protected transient final static String STAGE_CHANGE_FIELD_NAME = "stage_change";
    protected transient final static String STAGE_CHANGE_CHANCE_FIELD_NAME = "stage_change_chance";
    protected transient final static String CAN_FLINCH_FIELD_NAME = "can_flinch";
    protected transient final static String MIN_HITS_FIELD_NAME = "min_hits";
    protected transient final static String MAX_HITS_FIELD_NAME = "max_hits";
    protected transient final static String CHARGING_TURNS_FIELD_NAME = "charging_turns";
    protected transient final static String RECHARGE_TURNS_FIELD_NAME = "recharge_turns";
    protected transient final static String SELF_HEAL_FIELD_NAME = "self_heal";
    protected transient final static String SELF_HEAL_TYPE_FIELD_NAME = "self_heal_type";
    protected transient final static String SELF_HEAL_AMOUNT_FIELD_NAME = "self_heal_amount";
    protected transient final static String RECOIL_FIELD_NAME = "recoil";
    protected transient final static String RECOIL_AMOUNT_FIELD_NAME = "recoil_amount";
    protected transient final static String CRASH_FIELD_NAME = "crash";

    @DatabaseField(generatedId = true, columnName = ID_FIELD_NAME)
    int id;
    @DatabaseField(columnName = NAME_FIELD_NAME)
    private String name;
    @DatabaseField(columnName = DESCRIPTION_FIELD_NAME)
    private transient String description;
    @DatabaseField(columnName = TYPE_1_FIELD_NAME)
    private String type1;
    @DatabaseField(columnName = CATEGORY_FIELD_NAME)
    private String category;
    @DatabaseField(columnName = POWER_FIELD_NAME)
    private int power;
    @DatabaseField(columnName = ACCURACY_FIELD_NAME)
    private int accuracy;
    @DatabaseField(columnName = POWER_POINTS_FIELD_NAME)
    private int powerPoints;
    @DatabaseField(columnName = STATUS_EFFECT_FIELD_NAME)
    private String statusEffect;
    @DatabaseField(columnName = STATUS_EFFECT_CHANCE_FIELD_NAME)
    private int statusEffectChance;
    @DatabaseField(columnName = STAGE_CHANGE_STAT_FIELD_NAME)
    private String stageChangeStat;
    @DatabaseField(columnName = STAGE_CHANGE_FIELD_NAME)
    private int stageChange;
    @DatabaseField(columnName = STAGE_CHANGE_CHANCE_FIELD_NAME)
    private int stageChangeChance;
    @DatabaseField(columnName = CAN_FLINCH_FIELD_NAME)
    private boolean canFlinch;
    @DatabaseField(columnName = MIN_HITS_FIELD_NAME)
    private int minHits;
    @DatabaseField(columnName = MAX_HITS_FIELD_NAME)
    private int maxHits;
    @DatabaseField(columnName = CHARGING_TURNS_FIELD_NAME)
    private int chargingTurns;
    @DatabaseField(columnName = RECHARGE_TURNS_FIELD_NAME)
    private int rechargeTurns;
    @DatabaseField(columnName = SELF_HEAL_FIELD_NAME)
    private int selfHeal;
    @DatabaseField(columnName = SELF_HEAL_TYPE_FIELD_NAME)
    private String selfHealType;
    @DatabaseField(columnName = SELF_HEAL_AMOUNT_FIELD_NAME)
    private String selfHealAmount;
    @DatabaseField(columnName = RECOIL_FIELD_NAME)
    private int recoil;
    @DatabaseField(columnName = RECOIL_AMOUNT_FIELD_NAME)
    private String recoilAmount;
    @DatabaseField(columnName = CRASH_FIELD_NAME)
    private int crash;

    public Move() {
        // Constructor for ORMLite
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public String getType1() {
        return type1;
    }

    public ElementalType getElementalType1() {
        return ElementalType.valueOf(this.type1.toUpperCase());
    }

    public String getCategory() {
        return category;
    }

    public MoveType getMoveType() {

        return MoveType.valueOf(this.category.toUpperCase());
    }

    public int getPower() {
        return power;
    }

    public int getAccuracy() {
        return accuracy;
    }

    public int getPowerPoints() {
        return powerPoints;
    }

    public String getStatusEffectString() {
        return statusEffect;
    }

    public StatusEffect getStatusEffect() {
        try {
            return StatusEffect.valueOf(this.statusEffect.toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    public int getStatusEffectChance() {
        return statusEffectChance;
    }

    public String getStageChangeStat() {
        return stageChangeStat;
    }

    public StatType getStageChangeStatType() {
        return StatType.valueOf(this.stageChangeStat.toUpperCase());
    }

    public int getStageChange() {
        return stageChange;
    }

    public int getStageChangeChance() {
        return stageChangeChance;
    }

    public boolean canFlinch() { return canFlinch; }

    public int getMinHits() {
        return minHits;
    }

    public int getMaxHits() {
        return maxHits;
    }

    public int getChargingTurns() {
        return chargingTurns;
    }

    public boolean isChargingMove() {
        return chargingTurns > 0;
    }

    public int getRechargeTurns() {
        return rechargeTurns;
    }

    public boolean isRechargeMove() {
        return rechargeTurns > 0;
    }

    public int getSelfHeal() {
        return selfHeal;
    }

    public boolean isSelfHeal() { return selfHeal > 0; }

    public SelfHealType getSelfHealType() {
        try {
            return SelfHealType.valueOf(this.selfHealType.toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    public SelfHealAmount getSelfHealAmount() {
        try {
            return SelfHealAmount.valueOf(this.selfHealAmount.toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    public boolean isRecoil() {
        return recoil > 0;
    }

    public RecoilAmount getRecoilAmount() {
        try {
            return RecoilAmount.valueOf(this.recoilAmount.toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    public boolean isCrash() {
        return crash > 0;
    }

    public String toString() {

        StringBuilder sb = new StringBuilder();
        sb.append("Move: (" + id + ") " + name + " [" + type1 + "]");
        sb.append("\n");
        sb.append(" - Description: " + description + "\n");
        sb.append(" - Category: " + category + "\n");
        sb.append(" - Power: " + power + "\n");
        sb.append(" - Accuracy: " + accuracy + "\n");
        sb.append(" - PowerPoints: " + powerPoints + "\n");
        sb.append(" - StatusEffect: " + statusEffect + "\n");
        sb.append(" - StatusEffectChance: " + statusEffectChance + "\n");
        sb.append(" - StageChangeStat: " + stageChangeStat + "\n");
        sb.append(" - StageChange: " + stageChange + "\n");
        sb.append(" - StageChangeChance: " + stageChangeChance + "\n");
        sb.append(" - CanFlinch: " + canFlinch + "\n");
        sb.append(" - MaxHits: " + maxHits + "\n");
        sb.append(" - MinHits: " + minHits + "\n");
        sb.append(" - ChargingTurns: " + chargingTurns + "\n");
        sb.append(" - RechargingTurns: " + rechargeTurns + "\n");
        sb.append(" - SelfHealType: " + selfHealType + "\n");
        sb.append(" - SelfHealAmount: " + selfHealAmount + "\n");
        sb.append(" - Recoil?: " + recoil + "\n");
        sb.append(" - RecoilAmount: " + recoilAmount + "\n");
        sb.append(" - Crash?: " + crash + "\n");


        return sb.toString();
    }
}
