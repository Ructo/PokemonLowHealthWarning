package pokemonlowhealthwarning.patch;

import com.evacipated.cardcrawl.modthespire.lib.SpirePatch;
import com.megacrit.cardcrawl.cards.DamageInfo;
import com.megacrit.cardcrawl.characters.AbstractPlayer;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.rooms.AbstractRoom;
import pokemonlowhealthwarning.ModFile;
import pokemonlowhealthwarning.util.ProAudio;

import static pokemonlowhealthwarning.ModFile.makeID;
import static pokemonlowhealthwarning.util.Wiz.playAudio;

@SpirePatch(clz = AbstractPlayer.class, method = "damage")
public class HealthWarningPatch {


    public static void Postfix(AbstractPlayer player, DamageInfo info) {
        float healthThreshold = player.maxHealth * 0.2f;

        // Stop audio immediately if the player dies
        if (player.currentHealth <= 0) {
            stopAudio();  // Ensure audio stops when the player dies
        } else {
            checkPlayerHealth(player, healthThreshold);
        }
    }

    @SpirePatch(clz = AbstractPlayer.class, method = "heal")
    public static class HealPatch {
        public static int Postfix(AbstractPlayer player, int healAmount) {
            float healthThreshold = player.maxHealth * 0.2f;

            // Check the player's health after healing
            checkPlayerHealth(player, healthThreshold);

            return healAmount;
        }
    }

    public static void checkPlayerHealth(AbstractPlayer player, float healthThreshold) {
        if (player.currentHealth <= healthThreshold && player.currentHealth > 0 && inCombat() && !ModFile.isPlaying) {
            CardCrawlGame.music.silenceBGM();
            startAudio();
        }

        if (player.currentHealth > healthThreshold || !inCombat()) {
            stopAudio();
        }
    }

    private static boolean inCombat() {
        return AbstractDungeon.getCurrRoom() != null && AbstractDungeon.getCurrRoom().phase == AbstractRoom.RoomPhase.COMBAT;
    }

    private static void startAudio() {
        CardCrawlGame.music.silenceBGM();
        playAudio(ProAudio.warningintro);
        ModFile.isPlaying = true;
    }

    public static void stopAudio() {
        if (ModFile.isPlaying) {
            CardCrawlGame.sound.stop(makeID(ProAudio.warningintro.name()));
            CardCrawlGame.sound.stop(makeID(ProAudio.warningloop.name()));
            CardCrawlGame.music.unsilenceBGM();
            ModFile.isPlaying = false;
        }
    }
    private static String makeID(String id) {
        return "pokemonlowhealthwarning:" + id;
    }
}
