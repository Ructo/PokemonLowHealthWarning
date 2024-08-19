package pokemonlowhealthwarning.patch;

import com.evacipated.cardcrawl.modthespire.lib.SpirePatch;
import com.evacipated.cardcrawl.modthespire.lib.SpirePrefixPatch;
import com.evacipated.cardcrawl.modthespire.lib.SpireReturn;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.monsters.AbstractMonster;
import pokemonlowhealthwarning.ModFile;

@SpirePatch(clz = AbstractMonster.class, method = "playBossStinger")
public class BossDeadPatch {

    // Flag to prevent recursive calls and double playing
    private static boolean bossStingerPlaying = false;

    @SpirePrefixPatch
    public static SpireReturn<Void> Prefix() {
        // Check if the boss stinger is already playing to prevent double execution
        if (bossStingerPlaying) {
            return SpireReturn.Return(); // Skip the rest of the logic if already playing
        }

        bossStingerPlaying = true; // Set the flag to indicate that the stinger is playing

        // Stop the health warning music
        ModFile.stopHealthWarningMusic();

        // Play the boss victory stinger
        System.out.println("BOSSDEADPATCHBEFORE");
        CardCrawlGame.sound.play("BOSS_VICTORY_STINGER");
        System.out.println("BOSSDEADPATCHSTINGER");

        // Play the regular boss stinger for "TheEnding" or normal boss stingers
        if (AbstractDungeon.id.equals("TheEnding")) {
            CardCrawlGame.music.playTempBgmInstantly("STS_EndingStinger_v1.ogg", false);
        } else {
            CardCrawlGame.music.playTempBgmInstantly("STS_BossVictoryStinger_1_v3_MUSIC.ogg", false);
            System.out.println("BOSSDEADPATCHAFTER");
        }

        // Return early to avoid executing the original logic
        return SpireReturn.Return();
    }
}
