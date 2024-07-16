package characters;

import battleLogic.Battle;
import battleLogic.BattleHelpers;
import enemies.AbstractEnemy;
import powers.AbstractPower;
import powers.PermPower;
import powers.TempPower;

import java.util.ArrayList;

public class Huohuo extends AbstractCharacter {
    TempPower tailAtkBonus;
    HuohuoTalentPower talentPower = new HuohuoTalentPower();
    private int talentCounter = 0;

    public Huohuo() {
        super("Huohuo", 1358, 602, 509, 98, 80, ElementType.WIND, 140, 100);

        tailAtkBonus = new TempPower();
        tailAtkBonus.bonusAtkPercent = 40;
        tailAtkBonus.turnDuration = 2;
        tailAtkBonus.name = "Tail Atk Bonus";

        PermPower tracesPower = new PermPower();
        tracesPower.name = "Traces Stat Bonus";
        tracesPower.bonusHPPercent = 28;
        tracesPower.bonusFlatSpeed = 5;
        this.addPower(tracesPower);
    }

    public void useSkill() {
        super.useSkill();
        talentCounter = 2;
        for (AbstractCharacter character : Battle.battle.playerTeam) {
            character.addPower(talentPower);
        }
    }
    public void useBasicAttack() {
        super.useBasicAttack();
        float baseDamage = (0.5f * getFinalHP());
        ArrayList<DamageType> types = new ArrayList<>();
        types.add(DamageType.BASIC);
        BattleHelpers.PreAttackLogic(this, types);

        if (Battle.battle.enemyTeam.size() >= 3) {
            int middleIndex = Battle.battle.enemyTeam.size() / 2;
            BattleHelpers.hitEnemy(this, Battle.battle.enemyTeam.get(middleIndex), baseDamage, types);
        } else {
            AbstractEnemy enemy = Battle.battle.enemyTeam.get(0);
            BattleHelpers.hitEnemy(this, enemy, baseDamage, types);
        }
        BattleHelpers.PostAttackLogic(this, types);
    }

    public void useUltimate() {
        super.useUltimate();
        for (AbstractCharacter character : Battle.battle.playerTeam) {
            if (character != this) {
                character.increaseEnergy(character.maxEnergy * 0.2f, false);
                character.addPower(tailAtkBonus);
            }
        }
    }

    public void takeTurn() {
        if (Battle.battle.numSkillPoints > 0 && talentCounter <= 0) {
            useSkill();
        } else {
            useBasicAttack();
        }
    }

    public String toString() {
        return name;
    }

    public void onCombatStart() {
        talentCounter = 1;
        for (AbstractCharacter character : Battle.battle.playerTeam) {
            character.addPower(talentPower);
        }
    }

    public void onTurnStart() {
        talentCounter--;
        if (talentCounter <= 0) {
            for (AbstractCharacter character : Battle.battle.playerTeam) {
                character.removePower(talentPower);
            }
        }
    }

    private class HuohuoTalentPower extends AbstractPower {
        public HuohuoTalentPower() {
            this.name = this.getClass().getSimpleName();
            lastsForever = true;
        }

        @Override
        public void onTurnStart() {
            Huohuo.this.increaseEnergy(1);
        }
    }
}
