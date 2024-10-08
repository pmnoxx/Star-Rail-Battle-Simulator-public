package characters;

import battleLogic.Battle;
import battleLogic.BattleHelpers;
import battleLogic.FuYuan;
import enemies.AbstractEnemy;
import powers.AbstractPower;
import powers.PowerStat;
import powers.TracePower;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class Lingsha extends AbstractCharacter {
    FuYuan fuYuan;
    AbstractPower damageTrackerPower;
    private static final int fuYuanMaxHitCount = 5;
    private static final int skillHitCountGain = 3;
    private int fuYuanCurrentHitCount = 0;
    private static final int emergencyHealCooldown = 2;
    private int currentEmergencyHealCD = 0;
    private HashMap<AbstractCharacter, Integer> characterTimesDamageTakenMap = new HashMap<>();
    private int fuYuanAttacksMetric = 0;
    private String fuYuanAttacksMetricName = "Number of Fu Yuan Attacks";
    private int numEmergencyHeals = 0;
    private String numEmergencyHealsMetricName = "Number of Emergency Heal Triggers";

    public Lingsha() {
        super("Lingsha", 1358, 679, 437, 98, 80, ElementType.FIRE, 110, 100, Path.ABUNDANCE);

        this.addPower(new TracePower()
                .setStat(PowerStat.HP_PERCENT, 18)
                .setStat(PowerStat.BREAK_EFFECT, 37.3f)
                .setStat(PowerStat.ATK_PERCENT, 10));

        this.hasAttackingUltimate = true;
        this.basicEnergyGain = 30;

        damageTrackerPower = new LingshaEmergencyHealTracker();
        fuYuan = new FuYuan(this);
    }

    @Override
    public float getFinalAttack() {
        if (!Battle.battle.isInCombat) {
            return super.getFinalAttack();
        } else {
            float atkBonus = 0.25f * getTotalBreakEffect();
            if (atkBonus > 50) {
                atkBonus = 50;
            }
            return super.getFinalAttack() + ((baseAtk + lightcone.baseAtk) *  (1 + atkBonus / 100));
        }
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

    public void useSkill() {
        super.useSkill();
        ArrayList<DamageType> types = new ArrayList<>();
        types.add(DamageType.SKILL);
        BattleHelpers.PreAttackLogic(this, types);

        for (AbstractEnemy enemy : Battle.battle.enemyTeam) {
            BattleHelpers.hitEnemy(this, enemy, 0.8f, BattleHelpers.MultiplierStat.ATK, types, TOUGHNESS_DAMAGE_SINGLE_UNIT);
        }
        increaseHitCount(skillHitCountGain);
        Battle.battle.AdvanceEntity(fuYuan, 20);
        fuYuan.speedPriority = 1;
        resetDamageTracker();

        BattleHelpers.PostAttackLogic(this, types);
    }

    public void useUltimate() {
        // only ult if fu yuan isn't about to attack so we don't waste action forward as much
        if (Battle.battle.actionValueMap.get(fuYuan) >= fuYuan.getBaseAV() * 0.5) {
            super.useUltimate();
            ArrayList<DamageType> types = new ArrayList<>();
            types.add(DamageType.ULTIMATE);
            BattleHelpers.PreAttackLogic(this, types);

            for (AbstractEnemy enemy : Battle.battle.enemyTeam) {
                AbstractPower besotted = new Befog();
                enemy.addPower(besotted);
                BattleHelpers.hitEnemy(this, enemy, 1.5f, BattleHelpers.MultiplierStat.ATK, types, TOUGHNESS_DAMAGE_TWO_UNITS);
            }
            Battle.battle.AdvanceEntity(fuYuan, 100);
            fuYuan.speedPriority = 1;

            BattleHelpers.PostAttackLogic(this, types);
        }
    }

    public void FuYuanAttack(boolean useHitCount) {
        fuYuanAttacksMetric++;

        ArrayList<DamageType> types = new ArrayList<>();
        types.add(DamageType.FOLLOW_UP);
        BattleHelpers.PreAttackLogic(this, types);

        for (AbstractEnemy enemy : Battle.battle.enemyTeam) {
            BattleHelpers.hitEnemy(this, enemy, 0.9f, BattleHelpers.MultiplierStat.ATK, types, TOUGHNESS_DAMAGE_SINGLE_UNIT);
        }
        if (useHitCount) {
            decreaseHitCount(1);
        }
        resetDamageTracker();

        BattleHelpers.PostAttackLogic(this, types);
    }

    private void increaseHitCount(int amount) {
        if (fuYuanMaxHitCount <= 0) {
            Battle.battle.actionValueMap.put(fuYuan, fuYuan.getBaseAV());
        }
        int initalStack = fuYuanCurrentHitCount;
        fuYuanCurrentHitCount += amount;
        if (fuYuanCurrentHitCount > fuYuanMaxHitCount) {
            fuYuanCurrentHitCount = fuYuanMaxHitCount;
        }
        Battle.battle.addToLog(String.format("Fu Yuan gained %d hits (%d -> %d)", amount, initalStack, fuYuanCurrentHitCount));
    }

    private void decreaseHitCount(int amount) {
        int initalStack = fuYuanCurrentHitCount;
        fuYuanCurrentHitCount -= amount;
        if (fuYuanCurrentHitCount <= 0) {
            fuYuanCurrentHitCount = 0;
            Battle.battle.actionValueMap.remove(fuYuan);
        }
        Battle.battle.addToLog(String.format("Fu Yuan hits left decreased by %d (%d -> %d)", amount, initalStack, fuYuanCurrentHitCount));
    }

    public void onTurnStart() {
        super.onTurnStart();
        if (currentEmergencyHealCD > 0) {
            currentEmergencyHealCD--;
        }
    }

    public void takeTurn() {
        super.takeTurn();
        if (Battle.battle.numSkillPoints > 0 && fuYuanCurrentHitCount <= fuYuanMaxHitCount - skillHitCountGain) {
            useSkill();
        } else if (Battle.battle.numSkillPoints >= 4) {
            useSkill();
        } else {
            useBasicAttack();
        }
    }

    public void onCombatStart() {
        Battle.battle.actionValueMap.put(fuYuan, fuYuan.getBaseAV());
        increaseHitCount(skillHitCountGain);
        for (AbstractCharacter character : Battle.battle.playerTeam) {
            characterTimesDamageTakenMap.put(character, 0);
            character.addPower(damageTrackerPower);
        }
    }

    public void useTechnique() {
        for (AbstractEnemy enemy : Battle.battle.enemyTeam) {
            AbstractPower befog = new Befog();
            enemy.addPower(befog);
        }
    }

    public void resetDamageTracker() {
        Battle.battle.addToLog("Resetting Lingsha damage tracker due to healing");
        for (Map.Entry<AbstractCharacter, Integer> entry : characterTimesDamageTakenMap.entrySet()) {
            entry.setValue(0);
        }
    }

    public HashMap<String, String> getCharacterSpecificMetricMap() {
        HashMap<String, String> map = super.getCharacterSpecificMetricMap();
        map.put(fuYuanAttacksMetricName, String.valueOf(fuYuanAttacksMetric));
        map.put(numEmergencyHealsMetricName, String.valueOf(numEmergencyHeals));
        return map;
    }

    public ArrayList<String> getOrderedCharacterSpecificMetricsKeys() {
        ArrayList<String> list = super.getOrderedCharacterSpecificMetricsKeys();
        list.add(fuYuanAttacksMetricName);
        list.add(numEmergencyHealsMetricName);
        return list;
    }

    private static class Befog extends AbstractPower {
        public Befog() {
            this.name = this.getClass().getSimpleName();
            this.turnDuration = 2;
            this.type = PowerType.DEBUFF;
        }

        @Override
        public float getConditionalDamageTaken(AbstractCharacter character, AbstractEnemy enemy, ArrayList<DamageType> damageTypes) {
            if (damageTypes.contains(DamageType.BREAK)) {
                return 25f;
            }
            return 0;
        }
    }

    private class LingshaEmergencyHealTracker extends AbstractPower {
        public LingshaEmergencyHealTracker() {
            this.name = this.getClass().getSimpleName();
            this.lastsForever = true;
        }

        public void onAttacked(AbstractCharacter character, AbstractEnemy enemy, ArrayList<AbstractCharacter.DamageType> types) {
            int timesHit = characterTimesDamageTakenMap.get(character);
            timesHit++;
            Battle.battle.addToLog(String.format("%s has been hit %d times since last heal", character.name, timesHit));
            if (timesHit >= 2 && currentEmergencyHealCD <= 0) {
                Battle.battle.addToLog("Triggering Lingsha Emergency Heal");
                numEmergencyHeals++;
                currentEmergencyHealCD = emergencyHealCooldown;
                Lingsha.this.FuYuanAttack(false);
            } else {
                characterTimesDamageTakenMap.put(character, timesHit);
            }
        }
    }
}
