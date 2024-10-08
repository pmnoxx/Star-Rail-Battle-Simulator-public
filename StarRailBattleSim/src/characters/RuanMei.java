package characters;

import battleLogic.Battle;
import battleLogic.BattleHelpers;
import enemies.AbstractEnemy;
import powers.AbstractPower;
import powers.PermPower;
import powers.PowerStat;
import powers.TracePower;

import java.util.ArrayList;

public class RuanMei extends AbstractCharacter {
    PermPower skillPower;
    AbstractPower ultPower = new RuanMeiUltPower();
    private int skillCounter = 0;
    private int ultCounter = 0;
    public static final String NAME = "Ruan Mei";
    public static final String SKILL_POWER_NAME = "RuanMeiSkillPower";
    public static final String ULT_POWER_NAME = "RuanMeiUltPower";
    public static final String ULT_DEBUFF_NAME = "RuanMeiUltDebuff";

    public RuanMei() {
        super(NAME, 1087, 660, 485, 104, 80, ElementType.ICE, 130, 100, Path.HARMONY);

        this.addPower(new TracePower()
                .setStat(PowerStat.BREAK_EFFECT, 37.3f)
                .setStat(PowerStat.DEF_PERCENT, 22.5f)
                .setStat(PowerStat.FLAT_SPEED, 5));

        this.skillPower = (PermPower) new PermPower(SKILL_POWER_NAME)
                .setStat(PowerStat.DAMAGE_BONUS, 68)
                .setStat(PowerStat.WEAKNESS_BREAK_EFF, 50);
    }

    public void useSkill() {
        super.useSkill();
        skillCounter = 3;
        for (AbstractCharacter character : Battle.battle.playerTeam) {
            character.addPower(skillPower);
        }
    }
    public void useBasicAttack() {
        super.useBasicAttack();
        ArrayList<DamageType> types = new ArrayList<>();
        types.add(DamageType.BASIC);
        BattleHelpers.PreAttackLogic(this, types);

        if (Battle.battle.enemyTeam.size() >= 3) {
            int middleIndex = Battle.battle.enemyTeam.size() / 2;
            BattleHelpers.hitEnemy(this, Battle.battle.enemyTeam.get(middleIndex), 1.0f, BattleHelpers.MultiplierStat.ATK, types, TOUGHNESS_DAMAGE_SINGLE_UNIT);
        } else {
            AbstractEnemy enemy = Battle.battle.enemyTeam.get(0);
            BattleHelpers.hitEnemy(this, enemy, 1.0f, BattleHelpers.MultiplierStat.ATK, types, TOUGHNESS_DAMAGE_SINGLE_UNIT);
        }
        BattleHelpers.PostAttackLogic(this, types);
    }

    public void useUltimate() {
        super.useUltimate();
        ultCounter = 2;
        for (AbstractCharacter character : Battle.battle.playerTeam) {
            character.addPower(ultPower);
        }
    }

    public void onTurnStart() {
        super.onTurnStart();
        increaseEnergy(5);
        if (skillCounter > 0) {
            skillCounter--;
            if (skillCounter <= 0) {
                for (AbstractCharacter character : Battle.battle.playerTeam) {
                    character.removePower(skillPower);
                }
            }
        }
        if (ultCounter > 0) {
            ultCounter--;
            if (ultCounter <= 0) {
                for (AbstractCharacter character : Battle.battle.playerTeam) {
                    character.removePower(ultPower);
                }
            }
        }
    }

    public void takeTurn() {
        super.takeTurn();
        if (Battle.battle.numSkillPoints > 0 && skillCounter <= 0) {
            useSkill();
        } else {
            useBasicAttack();
        }
    }

    public void onCombatStart() {
        for (AbstractCharacter character : Battle.battle.playerTeam) {
            character.addPower(PermPower.create(PowerStat.BREAK_EFFECT, 20, "Ruan Mei Break Buff"));

            if (character != this) {
                character.addPower(PermPower.create(PowerStat.SPEED_PERCENT, 10, "Ruan Mei Speed Buff"));
            }
        }
    }

    public void onWeaknessBreak(AbstractEnemy enemy) {
        BattleHelpers.breakDamageHitEnemy(this, enemy, 1.2f);
    }

    public void useTechnique() {
        useSkill();
        Battle.battle.generateSkillPoint(this, 1);
    }

    public static class RuanMeiUltDebuff extends AbstractPower {

        public boolean triggered = false;
        public AbstractCharacter owner;
        public RuanMeiUltDebuff(AbstractCharacter owner) {
            this.name = ULT_DEBUFF_NAME;
            this.lastsForever = true;
            this.type = PowerType.DEBUFF;
            this.owner = owner;
        }
    }

    private class RuanMeiUltPower extends PermPower {
        public RuanMeiUltPower() {
            super(ULT_DEBUFF_NAME);
            this.setStat(PowerStat.RES_PEN, 25);
        }

        @Override
        public void onAttack(AbstractCharacter character, ArrayList<AbstractEnemy> enemiesHit, ArrayList<AbstractCharacter.DamageType> types) {
            for (AbstractEnemy enemy : enemiesHit) {
                if (!enemy.hasPower(ULT_DEBUFF_NAME)) {
                    AbstractPower debuff = new RuanMeiUltDebuff(RuanMei.this);
                    enemy.addPower(debuff);
                }
            }
        }
    }
}
