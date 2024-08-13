package characters;

import battleLogic.Battle;
import battleLogic.BattleHelpers;
import enemies.AbstractEnemy;
import powers.AbstractPower;
import powers.PermPower;
import powers.TempPower;

import java.util.ArrayList;
import java.util.HashMap;

public class Feixiao extends AbstractCharacter {

    PermPower ultBreakEffBuff;
    private int numFUAs = 0;
    private int numStacks;
    private int wastedStacks;
    private String numFUAsMetricName = "Follow up Attacks used";
    private String numStacksMetricName = "Amount of Talent Stacks gained";
    private String wastedStacksMetricName = "Amount of overcapped Stacks";
    public int stackCount = 0;
    public final int stackThreshold = 2;
    private boolean FUAReady = true;

    public Feixiao() {
        super("Feixiao", 1048, 602, 388, 112, 80, ElementType.WIND, 12, 75);
        this.currentEnergy = 0;
        this.ultCost = 6;
        PermPower tracesPower = new PermPower();
        tracesPower.name = "Traces Stat Bonus";
        tracesPower.bonusAtkPercent = 28f;
        tracesPower.bonusCritChance = 12f;
        tracesPower.bonusDefPercent = 12.5f;
        this.addPower(tracesPower);
        this.isDPS = true;
        this.hasAttackingUltimate = true;

        ultBreakEffBuff = new PermPower();
        ultBreakEffBuff.bonusWeaknessBreakEff = 100;
        ultBreakEffBuff.name = "Fei Ult Break Eff Buff";
    }

    // override normal energy gain to do nothing
    public void increaseEnergy(float amount, boolean ERRAffected) {

    }

    public void increaseStack(int amount) {
        int initialStack = stackCount;
        stackCount += amount;
        Battle.battle.addToLog(String.format("%s gained %d Stack (%d -> %d)", name, amount, initialStack, stackCount));
        if (stackCount >= stackThreshold) {
            int energyGain = stackCount / stackThreshold;
            gainStackEnergy(energyGain);
        }
    }

    public void gainStackEnergy(int energyGain) {
        numStacks += energyGain;
        float initialEnergy = currentEnergy;
        currentEnergy += energyGain;
        if (currentEnergy > maxEnergy) {
            currentEnergy = maxEnergy;
            wastedStacks += energyGain;
        }
        stackCount = stackCount % stackThreshold;
        Battle.battle.addToLog(String.format("%s gained %d Energy (%.1f -> %.1f)", name, energyGain, initialEnergy, currentEnergy));
    }

    public void useSkill() {
        super.useSkill();
        ArrayList<DamageType> types = new ArrayList<>();
        types.add(DamageType.SKILL);
        BattleHelpers.PreAttackLogic(this, types);

        TempPower atkBonus = new TempPower();
        atkBonus.bonusAtkPercent = 48;
        atkBonus.turnDuration = 3;
        atkBonus.name = "Fei Atk Bonus";
        addPower(atkBonus);

        AbstractEnemy enemy;
        if (Battle.battle.enemyTeam.size() >= 3) {
            int middleIndex = Battle.battle.enemyTeam.size() / 2;
            enemy = Battle.battle.enemyTeam.get(middleIndex);
        } else {
            enemy = Battle.battle.enemyTeam.get(0);
        }
        BattleHelpers.hitEnemy(this, enemy, 2.0f, BattleHelpers.MultiplierStat.ATK, types, TOUGHNESS_DAMAGE_TWO_UNITS);

        BattleHelpers.PostAttackLogic(this, types);

        ArrayList<AbstractEnemy> enemiesHit = new ArrayList<>();
        enemiesHit.add(enemy);
        useFollowUp(enemiesHit);
    }
    public void useBasicAttack() {
        super.useBasicAttack();
        ArrayList<DamageType> types = new ArrayList<>();
        types.add(DamageType.BASIC);
        BattleHelpers.PreAttackLogic(this, types);

        AbstractEnemy enemy;
        if (Battle.battle.enemyTeam.size() >= 3) {
            int middleIndex = Battle.battle.enemyTeam.size() / 2;
            enemy = Battle.battle.enemyTeam.get(middleIndex);
        } else {
            enemy = Battle.battle.enemyTeam.get(0);
        }
        BattleHelpers.hitEnemy(this, enemy, 1.0f, BattleHelpers.MultiplierStat.ATK, types, TOUGHNESS_DAMAGE_SINGLE_UNIT);

        BattleHelpers.PostAttackLogic(this, types);
    }

    public void useFollowUp(ArrayList<AbstractEnemy> enemiesHit) {
        int middleIndex = enemiesHit.size() / 2;
        AbstractEnemy enemy = enemiesHit.get(middleIndex);
        moveHistory.add(MoveType.FOLLOW_UP);
        numFUAs++;
        Battle.battle.addToLog(name + " used Follow Up");

        TempPower dmgBonus = new TempPower();
        dmgBonus.bonusDamageBonus = 60;
        dmgBonus.turnDuration = 2;
        dmgBonus.name = "Fei Damage Bonus";
        addPower(dmgBonus);

        ArrayList<DamageType> types = new ArrayList<>();
        types.add(DamageType.FOLLOW_UP);
        BattleHelpers.PreAttackLogic(this, types);

        BattleHelpers.hitEnemy(this, enemy, 1.1f, BattleHelpers.MultiplierStat.ATK, types, TOUGHNESS_DAMAGE_HALF_UNIT);

        BattleHelpers.PostAttackLogic(this, types);
    }

    public void useUltimate() {
        AbstractEnemy enemy;
        if (Battle.battle.enemyTeam.size() >= 3) {
            int middleIndex = Battle.battle.enemyTeam.size() / 2;
            enemy = Battle.battle.enemyTeam.get(middleIndex);
        } else {
            enemy = Battle.battle.enemyTeam.get(0);
        }

        if (Battle.battle.hasCharacter(Robin.NAME)) {
            if (!this.hasPower(Robin.ULT_POWER_NAME)) {
                return;
            }
        }

        if (Battle.battle.hasCharacter(Sparkle.NAME)) {
            if (!this.hasPower(Sparkle.SKILL_POWER_NAME) || !this.hasPower(Sparkle.ULT_POWER_NAME)) {
                return;
            }
        }

        if (Battle.battle.hasCharacter(Bronya.NAME)) {
            if (!this.hasPower(Bronya.SKILL_POWER_NAME) || !this.hasPower(Bronya.ULT_POWER_NAME)) {
                return;
            }
        }

        if (Battle.battle.hasCharacter(RuanMei.NAME)) {
            if (!this.hasPower(RuanMei.ULT_POWER_NAME)) {
                return;
            }
        }

        if (Battle.battle.hasCharacter(Pela.NAME)) {
            if (!enemy.hasPower(Pela.ULT_DEBUFF_NAME)) {
                return;
            }
        }

        if (Battle.battle.hasCharacter(Hanya.NAME)) {
            if (!this.hasPower(Hanya.ULT_BUFF_NAME)) {
                return;
            }
        }

        addPower(ultBreakEffBuff);

        int numHits = 6;
        super.useUltimate();
        ArrayList<DamageType> types = new ArrayList<>();
        types.add(DamageType.ULTIMATE);
        types.add(DamageType.FOLLOW_UP);
        BattleHelpers.PreAttackLogic(this, types);

        for (int i = 0; i < numHits; i++) {
            BattleHelpers.hitEnemy(this, enemy, 0.9f, BattleHelpers.MultiplierStat.ATK, types, TOUGHNESS_DAMAGE_HALF_UNIT);
        }
        BattleHelpers.hitEnemy(this, enemy, 1.6f, BattleHelpers.MultiplierStat.ATK, types, TOUGHNESS_DAMAGE_HALF_UNIT);

        BattleHelpers.PostAttackLogic(this, types);
        removePower(ultBreakEffBuff);
    }

    public void takeTurn() {
        super.takeTurn();
        if (Battle.battle.hasCharacter(Bronya.NAME)) {
            if (!this.hasPower(Bronya.SKILL_POWER_NAME)) {
                useBasicAttack();
            } else {
                if (Battle.battle.numSkillPoints > 1) {
                    useSkill();
                } else {
                    useBasicAttack();
                }
            }
        } else {
            if (Battle.battle.numSkillPoints > 1) {
                useSkill();
            } else {
                useBasicAttack();
            }
        }
    }

    public void onTurnStart() {
        if (currentEnergy >= ultCost) {
            useUltimate(); // check for ultimate activation at start of turn as well
        }
        FUAReady = true;
    }

    public void onCombatStart() {
        gainStackEnergy(3);
        for (AbstractCharacter character : Battle.battle.playerTeam) {
            AbstractPower feiPower = new FeiTalentPower();
            character.addPower(feiPower);
        }
        addPower(new FeiCritDmgPower());
    }

    public void useTechnique() {
        if (Battle.battle.usedEntryTechnique) {
            return;
        } else {
            Battle.battle.usedEntryTechnique = true;
        }
        ArrayList<DamageType> types = new ArrayList<>();
        BattleHelpers.PreAttackLogic(this, types);

        for (AbstractEnemy enemy : Battle.battle.enemyTeam) {
            BattleHelpers.hitEnemy(this, enemy, 2.0f, BattleHelpers.MultiplierStat.ATK, types, TOUGHNESS_DAMAGE_SINGLE_UNIT);
        }

        BattleHelpers.PostAttackLogic(this, types);
        gainStackEnergy(1);
    }

    public HashMap<String, String> getCharacterSpecificMetricMap() {
        HashMap<String, String> map = super.getCharacterSpecificMetricMap();
        map.put(numFUAsMetricName, String.valueOf(numFUAs));
        map.put(numStacksMetricName, String.valueOf(numStacks));
        map.put(wastedStacksMetricName, String.valueOf(wastedStacks));
        return map;
    }

    public ArrayList<String> getOrderedCharacterSpecificMetricsKeys() {
        ArrayList<String> list = super.getOrderedCharacterSpecificMetricsKeys();
        list.add(numFUAsMetricName);
        list.add(numStacksMetricName);
        list.add(wastedStacksMetricName);
        return list;
    }

    private class FeiTalentPower extends AbstractPower {
        public FeiTalentPower() {
            this.name = this.getClass().getSimpleName();
            this.lastsForever = true;
        }

        @Override
        public void onAttack(AbstractCharacter character, ArrayList<AbstractEnemy> enemiesHit, ArrayList<DamageType> types) {
            if (!Feixiao.this.hasPower(ultBreakEffBuff.name)) {
                Feixiao.this.increaseStack(1);
            }
            if (!(character instanceof Feixiao)) {
                if (FUAReady) {
                    Feixiao.this.useFollowUp(enemiesHit);
                    FUAReady = false;
                }
            }
        }
    }

    private class FeiCritDmgPower extends AbstractPower {
        public FeiCritDmgPower() {
            this.name = this.getClass().getSimpleName();
            this.lastsForever = true;
        }

        @Override
        public float getConditionalCritDamage(AbstractCharacter character, AbstractEnemy enemy, ArrayList<AbstractCharacter.DamageType> damageTypes) {
            for (AbstractCharacter.DamageType type : damageTypes) {
                if (type == AbstractCharacter.DamageType.FOLLOW_UP) {
                    return 36;
                }
            }
            return 0;
        }
    }
}
