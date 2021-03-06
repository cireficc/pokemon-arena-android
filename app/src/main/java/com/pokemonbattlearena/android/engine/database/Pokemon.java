package com.pokemonbattlearena.android.engine.database;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import java.util.List;

@DatabaseTable(tableName = "pokemons")
public class Pokemon {

    protected final static String ID_FIELD_NAME = "id";
    protected final static String NAME_FIELD_NAME = "name";
    protected final static String TYPE_1_FIELD_NAME = "type_1";
    protected final static String TYPE_2_FIELD_NAME = "type_2";
    protected final static String HP_FIELD_NAME = "hp";
    protected final static String ATTACK_FIELD_NAME = "attack";
    protected final static String DEFENSE_FIELD_NAME = "defense";
    protected final static String SPECIAL_ATTACK_FIELD_NAME = "special_attack";
    protected final static String SPECIAL_DEFENSE_FIELD_NAME = "special_defense";
    protected final static String SPEED_FIELD_NAME = "speed";
    protected final static String LEGENDARY_FIELD_NAME = "legendary";

    @DatabaseField(generatedId = true, columnName = ID_FIELD_NAME)
    int id;
    @DatabaseField(columnName = NAME_FIELD_NAME)
    private String name;
    @DatabaseField(columnName = TYPE_1_FIELD_NAME)
    private String type1;
    @DatabaseField(columnName = TYPE_2_FIELD_NAME)
    private String type2;
    @DatabaseField(columnName = HP_FIELD_NAME)
    private int hp;
    @DatabaseField(columnName = ATTACK_FIELD_NAME)
    private int attack;
    @DatabaseField(columnName = DEFENSE_FIELD_NAME)
    private int defense;
    @DatabaseField(columnName = SPECIAL_ATTACK_FIELD_NAME)
    private int specialAttack;
    @DatabaseField(columnName = SPECIAL_DEFENSE_FIELD_NAME)
    private int specialDefense;
    @DatabaseField(columnName = SPEED_FIELD_NAME)
    private int speed;
    @DatabaseField(columnName = LEGENDARY_FIELD_NAME)
    private int legendary;

    protected List<Move> activeMoveList;

    public Pokemon() {
        // Constructor for ORMLite
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getType1() {
        return type1;
    }

    public String getType2() {
        return type2;
    }

    public ElementalType getElementalType1() {
        return ElementalType.valueOf(this.type1.toUpperCase());
    }

    public ElementalType getElementalType2() {
        try {
            return ElementalType.valueOf(this.type2.toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    public int getHp() {
        return hp;
    }

    public int getAttack() {
        return attack;
    }

    public int getDefense() {
        return defense;
    }

    public int getSpecialAttack() {
        return specialAttack;
    }

    public int getSpecialDefense() {
        return specialDefense;
    }

    public int getSpeed() {
        return speed;
    }

    public boolean isLegendary() {
        return legendary > 0;
    }

    public void setActiveMoveList(List<Move> moves) {
        this.activeMoveList = moves;
    }

    public List<Move> getActiveMoveList() {
        return activeMoveList;
    }

    @Override
    public String toString() {

        StringBuilder sb = new StringBuilder();
        sb.append("Pokemon: (" + id + ") " + name + " [" + type1);
        if (type2.isEmpty()) {
            sb.append("]");
        } else {
            sb.append(", " + type2 + "]");
        }
        sb.append("\n");
        sb.append(" - HP: " + hp + "\n");
        sb.append(" - Attack: " + attack + "\n");
        sb.append(" - Defense: " + defense + "\n");
        sb.append(" - SpecialAttack: " + specialAttack + "\n");
        sb.append(" - SpecialDefense: " + specialDefense + "\n");
        sb.append(" - Speed: " + speed + "\n");
        sb.append(" - Legendary: " + isLegendary() + "\n");
        sb.append(" - Moves: " + getActiveMoveList());
        return sb.toString();
    }
}
