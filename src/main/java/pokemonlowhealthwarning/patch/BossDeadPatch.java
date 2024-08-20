package pokemonlowhealthwarning.patch;

import com.evacipated.cardcrawl.modthespire.lib.SpirePatch;
import com.evacipated.cardcrawl.modthespire.lib.SpirePrefixPatch;
import com.evacipated.cardcrawl.modthespire.lib.SpireReturn;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.monsters.AbstractMonster;
import com.badlogic.gdx.math.MathUtils;
import pokemonlowhealthwarning.ModFile;

@SpirePatch(clz = AbstractMonster.class, method = "playBossStinger")
public class BossDeadPatch {

    @SpirePrefixPatch
    public static SpireReturn<Void> Prefix() {
        // Play the boss victory stinger sound effect
        System.out.println("BOSSDEADPATCHBEFORE");
        CardCrawlGame.sound.play("BOSS_VICTORY_STINGER");
        System.out.println("BOSSDEADPATCHSTINGER");

        // Play the regular boss stinger for "TheEnding" or choose a random stinger for normal bosses
        if (AbstractDungeon.id.equals("TheEnding")) {
            CardCrawlGame.music.playTempBgmInstantly("STS_EndingStinger_v1.ogg", false);
        } else {
            // Randomly select one of the four possible stingers
            switch (MathUtils.random(0, 3)) {
                case 0:
                    CardCrawlGame.music.playTempBgmInstantly("STS_BossVictoryStinger_1_v3_MUSIC.ogg", false);
                    break;
                case 1:
                    CardCrawlGame.music.playTempBgmInstantly("STS_BossVictoryStinger_2_v3_MUSIC.ogg", false);
                    break;
                case 2:
                    CardCrawlGame.music.playTempBgmInstantly("STS_BossVictoryStinger_3_v3_MUSIC.ogg", false);
                    break;
                case 3:
                    CardCrawlGame.music.playTempBgmInstantly("STS_BossVictoryStinger_4_v3_MUSIC.ogg", false);
                    break;
            }
            System.out.println("BOSSDEADPATCHAFTER");
        }

        // Return early to avoid executing the original logic
        return SpireReturn.Return();
    }
}
