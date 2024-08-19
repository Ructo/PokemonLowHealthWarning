package pokemonlowhealthwarning.patch;

import com.badlogic.gdx.audio.Music;
import com.evacipated.cardcrawl.modthespire.lib.SpirePatch;
import com.evacipated.cardcrawl.modthespire.lib.SpirePrefixPatch;
import com.evacipated.cardcrawl.modthespire.lib.SpireReturn;
import com.megacrit.cardcrawl.audio.MainMusic;
import com.megacrit.cardcrawl.audio.TempMusic;

@SpirePatch(
        clz = TempMusic.class,
        method = "getSong"
)
public class LowHealthMusicPatch {

    @SpirePrefixPatch
    public static SpireReturn<Music> Prefix(TempMusic __instance, String key) {
        switch (key) {
            case "warningintro":
                // Return the custom music for low health intro
                return SpireReturn.Return(MainMusic.newMusic("pokemonlowhealthwarningResources/audio/music/warningintro.ogg"));
            case "warningloop":
                // Return the custom music for low health loop
                return SpireReturn.Return(MainMusic.newMusic("pokemonlowhealthwarningResources/audio/music/warningloop.ogg"));
            default:
                // If the key doesn't match, continue with the default behavior
                return SpireReturn.Continue();
        }
    }
}
