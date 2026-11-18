package xy177.combatrollantique.client;

import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.util.SoundCategory;
import xy177.combatrollantique.common.RollAttributes;
import xy177.combatrollantique.common.RollContent;
import xy177.combatrollantique.config.RollConfig;

/** Cooldown and charge progression follow Combat Roll 2.0.6. */
public final class RollManager {
    private int timeSinceLastRoll=10;
    private float progress;
    private int cooldownLength, maxRolls=1, availableRolls;
    public boolean isRolling() { return timeSinceLastRoll <= RollConfig.server().roll_duration; }
    public boolean canRoll(EntityPlayerSP player) {
        return !isRolling() && availableRolls > 0 && player.isEntityAlive() && !player.isPlayerSleeping()
            && !player.isSpectator() && player.getHealth() > 0 && player.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).getAttributeValue() > 0;
    }
    public void onRoll() { availableRolls--; timeSinceLastRoll=0; updateCooldownLength(); }
    public void tick(EntityPlayerSP player) {
        maxRolls=(int)RollAttributes.value(player,RollAttributes.COUNT);
        timeSinceLastRoll++;
        if (availableRolls < maxRolls) {
            progress += (float)(RollAttributes.value(player,RollAttributes.RECHARGE)/20F);
            if (progress >= cooldownLength) {
                availableRolls++;
                progress=Math.max(progress-cooldownLength,0);
                updateCooldownLength();
                if (RollConfig.client.playCooldownSound) player.world.playSound(player.posX,player.posY,player.posZ,RollContent.READY,SoundCategory.PLAYERS,1,1,false);
            }
        }
        if (availableRolls == maxRolls) progress=0;
        if (availableRolls > maxRolls) availableRolls=maxRolls;
    }
    private void updateCooldownLength() { cooldownLength=Math.round(RollConfig.server().roll_cooldown*20F); }
    public int elapsed() { return (int)progress; }
    public int total() { return cooldownLength; }
    public int available() { return availableRolls; }
    public int maximum() { return maxRolls; }
}
