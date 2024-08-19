package pokemonlowhealthwarning.patch;

import com.badlogic.gdx.math.MathUtils;
import com.evacipated.cardcrawl.modthespire.lib.SpirePatch;
import com.evacipated.cardcrawl.modthespire.lib.SpirePrefixPatch;
import com.evacipated.cardcrawl.modthespire.lib.SpireReturn;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.monsters.AbstractMonster;
import pokemonlowhealthwarning.ModFile;

@SpirePatch(clz = AbstractMonster.class, method = "playBossStinger")
public class BossDeadPatch {

    @SpirePrefixPatch
    public static SpireReturn Prefix() {
        ModFile.isPlaying = false;
        CardCrawlGame.music.silenceTempBgmInstantly();
        CardCrawlGame.music.silenceBGMInstantly();
        CardCrawlGame.sound.play("BOSS_VICTORY_STINGER");

        // Play the regular boss stinger for "TheEnding" or normal boss stingers
        if (AbstractDungeon.id.equals("TheEnding")) {
            CardCrawlGame.music.playTempBgmInstantly("STS_EndingStinger_v1.ogg", false);
        } else {
            CardCrawlGame.music.playTempBgmInstantly("STS_BossVictoryStinger_1_v3_MUSIC.ogg", false);
        }

        // Return early to avoid executing the original logic
        return SpireReturn.Return();
    }
}



