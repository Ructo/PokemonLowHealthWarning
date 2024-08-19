package pokemonlowhealthwarning;

import basemod.BaseMod;
import basemod.interfaces.*;
import com.badlogic.gdx.math.MathUtils;
import com.evacipated.cardcrawl.modthespire.lib.SpireInitializer;
import com.megacrit.cardcrawl.actions.AbstractGameAction;
import com.megacrit.cardcrawl.characters.AbstractPlayer;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.monsters.AbstractMonster;
import com.megacrit.cardcrawl.rooms.AbstractRoom;
import com.megacrit.cardcrawl.rooms.MonsterRoom;
import com.megacrit.cardcrawl.rooms.MonsterRoomBoss;
import pokemonlowhealthwarning.util.ProAudio;

import static basemod.BaseMod.logger;
import static com.megacrit.cardcrawl.monsters.AbstractMonster.playBossStinger;
import static pokemonlowhealthwarning.util.Wiz.isInCombat;

@SpireInitializer
public class ModFile implements
        EditCardsSubscriber,
        EditRelicsSubscriber,
        EditStringsSubscriber,
        EditKeywordsSubscriber,
        PostBattleSubscriber,
        OnStartBattleSubscriber,
        OnPlayerTurnStartSubscriber,
        PostDungeonInitializeSubscriber,
        PostUpdateSubscriber,
        AddAudioSubscriber {

    public static final String modID = "pokemonlowhealthwarning";
    public static boolean isPlaying = false;
    public static String currentTempMusicKey = null;

    private static final String[] SPECIAL_TEMP_TRACKS = {
            "SHOP", "SHRINE", "MINDBLOOM", "CREDITS"
    };

    public ModFile() {
        BaseMod.subscribe(this);
    }

    public static void initialize() {
        new ModFile();
    }

    @Override
    public void receiveEditStrings() {
        // Localization loading...
    }

    @Override
    public void receiveAddAudio() {
        for (ProAudio a : ProAudio.values()) {
            BaseMod.addAudio(makeID(a.name()), "pokemonlowhealthwarningResources/audio/" + a.name().toLowerCase() + ".ogg");
        }
    }

    @Override
    public void receiveEditCards() {
        // Implement card edits here if needed
    }

    @Override
    public void receiveEditKeywords() {
        // Implement keyword edits here if needed
    }

    @Override
    public void receiveEditRelics() {
        // Implement relic edits here if needed
    }

    public static String makeID(String idText) {
        return modID + ":" + idText;
    }

    public static void playTempBgm(String key, boolean loop) {
        // Play the temp BGM and track the current key
        CardCrawlGame.music.playTempBgmInstantly(key, loop);
        currentTempMusicKey = key;
    }

    public static void stopTempBgm() {
        // Stop temp BGM and reset the tracking variable
        CardCrawlGame.music.silenceTempBgmInstantly();
        currentTempMusicKey = null;
    }

    @Override
    public void receiveOnBattleStart(AbstractRoom abstractRoom) {
        // Check player health and special case logic at the start of combat
        CardCrawlGame.music.silenceTempBgmInstantly();
        ModFile.isPlaying = false;
        AbstractDungeon.actionManager.addToBottom(checkPlayerHealth());
    }

    @Override
    public void receiveOnPlayerTurnStart() {
        // Check health at the start of each player turn
        checkPlayerHealth();
    }

    @Override
    public void receivePostBattle(AbstractRoom abstractRoom) {
        // Stop health warning music at the end of battle
        stopHealthWarningMusic();
    }

    @Override
    public void receivePostDungeonInitialize() {
        if (CardCrawlGame.isInARun() && AbstractDungeon.currMapNode != null && AbstractDungeon.getCurrRoom() != null && AbstractDungeon.actionManager != null) {
            ModFile.isPlaying = false;
            checkPlayerHealth();
        }
    }

    @Override
    public void receivePostUpdate() {
        if (CardCrawlGame.isInARun() && AbstractDungeon.currMapNode != null && AbstractDungeon.actionManager != null) {
            if (AbstractDungeon.getCurrRoom() == null || !AbstractDungeon.getCurrRoom().phase.equals(AbstractRoom.RoomPhase.COMBAT)) {
                stopHealthWarningMusic();
            }
        }
    }

    // Utility method to check player health and trigger the appropriate action
    public static AbstractGameAction checkPlayerHealth() {
        AbstractPlayer player = AbstractDungeon.player;
        float healthThreshold = player.maxHealth * 0.2f;

        if (player.currentHealth <= healthThreshold && player.currentHealth > 0 && !ModFile.isPlaying) {
            // Trigger the health warning music
            playTempBgm("warningintro", false);
            playHealthWarningMusic();
            System.out.println("Playing warning music");
            ModFile.isPlaying = true;

        } else if (player.currentHealth > healthThreshold && ModFile.isPlaying) {
            // Stop the health warning music if the player’s health recovers
            stopHealthWarningMusic();
            System.out.println("Stopping warning music");
            ModFile.isPlaying = false;
        }
        return null;
    }


    public static boolean isSpecialTempTrackPlaying() {
        // Check if the current playing temp music matches any of the special tracks
        if (currentTempMusicKey != null) {
            for (String specialTrack : SPECIAL_TEMP_TRACKS) {
                if (currentTempMusicKey.equals(specialTrack)) {
                    return true;
                }
            }
        }
        return false;
    }

    public static void playHealthWarningMusic() {
        if (!isSpecialTempTrackPlaying()) {
            CardCrawlGame.music.silenceTempBgmInstantly();  // Silence all temp music first
            ModFile.playTempBgm("warningintro", false);     // Start playing warning music
            ModFile.isPlaying = true;
        }
    }

    public static void stopHealthWarningMusic() {
        if (isPlaying) {
            boolean isFightingLagavulin = false;
            boolean isFightingHexaghost = false;
            boolean isFightingHeart = false;

            // Check for specific elite/boss monsters
            if (AbstractDungeon.getCurrRoom() instanceof MonsterRoom) {
                for (AbstractMonster mo : AbstractDungeon.getCurrRoom().monsters.monsters) {
                    if (mo.name.equals("Lagavulin")) {
                        isFightingLagavulin = true;
                    } else if (mo.name.equals("Hexaghost")) {
                        isFightingHexaghost = true;
                    } else if (mo.name.equals("CorruptHeart")) {
                        isFightingHeart = true;
                    }
                }

                // Handle music logic based on whether the room phase is COMPLETE or still in combat
                if (AbstractDungeon.getCurrRoom().phase != AbstractRoom.RoomPhase.COMPLETE) {
                    if (isFightingLagavulin) {
                        // Resume Elite music for Lagavulin
                        CardCrawlGame.music.silenceTempBgmInstantly();
                        CardCrawlGame.music.playTempBgmInstantly("ELITE");
                    } else if (isFightingHexaghost || isFightingHeart) {
                        // Stop music after custom check for Hexaghost or the Heart
                        CardCrawlGame.music.silenceTempBgmInstantly();
                    } else if (AbstractDungeon.getCurrRoom() instanceof MonsterRoomBoss) {
                        // Play the regular boss music before victory
                        CardCrawlGame.music.silenceTempBgmInstantly();
                        CardCrawlGame.music.playTempBgmInstantly("BOSS_BOTTOM");
                    } else {
                        // Resume normal music for regular rooms
                        CardCrawlGame.music.silenceTempBgmInstantly();
                        CardCrawlGame.music.unsilenceBGM();
                    }
                } else {
                    // If the room phase is COMPLETE and it's not a boss room, resume normal BGM
                    if (!(AbstractDungeon.getCurrRoom() instanceof MonsterRoomBoss)) {
                        CardCrawlGame.music.silenceTempBgmInstantly();
                        CardCrawlGame.music.unsilenceBGM();
                    } else {
                        playBossStinger();
                    }
                }

                // Reset the health warning music state
                isPlaying = false;
            }
        }
    }
}



