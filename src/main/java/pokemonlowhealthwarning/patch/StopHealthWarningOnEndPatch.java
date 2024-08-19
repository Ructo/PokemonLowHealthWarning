package pokemonlowhealthwarning.patch;

import com.evacipated.cardcrawl.modthespire.lib.SpirePatch;
import com.megacrit.cardcrawl.rooms.AbstractRoom;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import pokemonlowhealthwarning.ModFile;

@SpirePatch(clz = AbstractRoom.class, method = "endBattle")
public class StopHealthWarningOnEndPatch {

    public static void Prefix(AbstractRoom __instance) {
        if (ModFile.isPlaying) {
            ModFile.stopHealthWarningMusic();
        }
    }
}
