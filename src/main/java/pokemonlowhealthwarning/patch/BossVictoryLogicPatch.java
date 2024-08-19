package pokemonlowhealthwarning.patch;

import com.evacipated.cardcrawl.modthespire.lib.SpirePatch;
import com.evacipated.cardcrawl.modthespire.lib.SpirePrefixPatch;
import com.megacrit.cardcrawl.actions.GameActionManager;
import com.megacrit.cardcrawl.blights.AbstractBlight;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.rooms.AbstractRoom;
import com.megacrit.cardcrawl.monsters.AbstractMonster;
import com.megacrit.cardcrawl.core.Settings;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.screens.stats.StatsScreen;
import com.megacrit.cardcrawl.unlock.UnlockTracker;

import static com.megacrit.cardcrawl.monsters.AbstractMonster.playBossStinger;

@SpirePatch(
        clz = AbstractMonster.class,
        method = "onBossVictoryLogic"
)
public class BossVictoryLogicPatch {

    @SpirePrefixPatch
    public static void Prefix(AbstractMonster __instance) {
        // Accessing the deathTimer field of AbstractMonster
        for (AbstractMonster monster : AbstractDungeon.getMonsters().monsters) {
            if (Settings.FAST_MODE) {
                monster.deathTimer += 0.7F;
            } else {
                monster.deathTimer += 1.5F;
            }
        }

        // Fade in ambiance for the scene
        AbstractDungeon.scene.fadeInAmbiance();

        // Process boss count and stats if no event is tied to the room
        if (AbstractDungeon.getCurrRoom().event == null) {
            AbstractDungeon.bossCount++;
            StatsScreen.incrementBossSlain();

            // Check for achievements
            if (GameActionManager.turn <= 1) {
                UnlockTracker.unlockAchievement("YOU_ARE_NOTHING");
            }
            if (GameActionManager.damageReceivedThisCombat - GameActionManager.hpLossThisCombat <= 0) {
                UnlockTracker.unlockAchievement("PERFECT");
                CardCrawlGame.perfect++;
            }
        }

        System.out.println("BOSSVICTORYLOGICPATCH");

        // Trigger blights on boss defeat
        for (AbstractBlight blight : AbstractDungeon.player.blights) {
            blight.onBossDefeat();
        }
    }
}
