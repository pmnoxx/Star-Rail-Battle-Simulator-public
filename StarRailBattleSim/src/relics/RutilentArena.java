package relics;

import characters.AbstractCharacter;
import enemies.AbstractEnemy;
import powers.AbstractPower;
import powers.PermPower;
import powers.PowerStat;

import java.util.ArrayList;

public class RutilentArena extends AbstractRelicSetBonus {
    public RutilentArena(AbstractCharacter owner) {
        super(owner);
    }
    public void onEquip() {
        owner.addPower(PermPower.create(PowerStat.CRIT_CHANCE, 8, "Rutilent Arena Crit Chance Bonus"));
    }

    public void onCombatStart() {
        owner.addPower(new RutilentArenaDamageBonus());
    }

    public String toString() {
        return this.getClass().getSimpleName();
    }

    private static class RutilentArenaDamageBonus extends AbstractPower {
        public RutilentArenaDamageBonus() {
            this.name = this.getClass().getSimpleName();
            this.lastsForever = true;
        }
        @Override
        public float getConditionalDamageBonus(AbstractCharacter character, AbstractEnemy enemy, ArrayList<AbstractCharacter.DamageType> damageTypes) {
            for (AbstractCharacter.DamageType type : damageTypes) {
                if (type == AbstractCharacter.DamageType.BASIC || type == AbstractCharacter.DamageType.SKILL) {
                    return 20;
                }
            }
            return 0;
        }
    }

}
