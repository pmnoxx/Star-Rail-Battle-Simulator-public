package lightcones.harmony;

import battleLogic.Battle;
import characters.AbstractCharacter;
import lightcones.AbstractLightcone;
import powers.PermPower;
import powers.PowerStat;
import powers.TempPower;

public class PastSelfInMirror extends AbstractLightcone {

    public PastSelfInMirror(AbstractCharacter owner) {
        super(1058, 529, 529, owner);
    }

    @Override
    public void onEquip() {
        this.owner.addPower(PermPower.create(PowerStat.BREAK_EFFECT, 60, "Past Self in Mirror Break Effect Boost"));
    }

    @Override
    public void onCombatStart() {
        Battle.battle.playerTeam.stream()
                .filter(c -> c.usesEnergy)
                .forEach(c -> c.increaseEnergy(10));
    }

    @Override
    public void onUseUltimate() {
        for (AbstractCharacter character : Battle.battle.playerTeam) {
            character.addPower(TempPower.create(PowerStat.DAMAGE_BONUS, 24, 3, "Past Self in Mirror Damage Boost"));
        }
        if (this.owner.getTotalBreakEffect() > 150) {
            Battle.battle.generateSkillPoint(this.owner, 1);
        }
    }
}
