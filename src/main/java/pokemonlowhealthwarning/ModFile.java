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
    public static boolean isBossStingerPlaying = false;  // Flag for boss stinger
    public static boolean bossBattleEnded = false;       // Prevent health checks after boss defeat
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
        System.out.println("playTempBGM");
        CardCrawlGame.music.playTempBgmInstantly(key, loop);
        currentTempMusicKey = key;
    }

    public static void stopTempBgm() {
        CardCrawlGame.music.silenceTempBgmInstantly();
        currentTempMusicKey = null;
    }

    @Override
    public void receiveOnBattleStart(AbstractRoom abstractRoom) {
        ModFile.isPlaying = false;
        ModFile.isBossStingerPlaying = false;
        ModFile.bossBattleEnded = false;
        AbstractDungeon.actionManager.addToBottom(checkPlayerHealth());
    }

    @Override
    public void receiveOnPlayerTurnStart() {
        if (!bossBattleEnded && !isBossStingerPlaying) {
            checkPlayerHealth();
        }
    }

    @Override
    public void receivePostBattle(AbstractRoom abstractRoom) {
        bossBattleEnded = true;
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
        if (CardCrawlGame.isInARun() && AbstractDungeon.currMapNode != null && AbstractDungeon.actionManager != null && ModFile.isPlaying) {
            if (AbstractDungeon.getCurrRoom() == null || !AbstractDungeon.getCurrRoom().phase.equals(AbstractRoom.RoomPhase.COMBAT)) {
                stopHealthWarningMusic();
            }
        }
    }

    public static AbstractGameAction checkPlayerHealth() {
        if (isBossStingerPlaying || bossBattleEnded) {
            return null;
        }

        AbstractPlayer player = AbstractDungeon.player;
        float healthThreshold = player.maxHealth * 0.2f;

        if (player.currentHealth <= healthThreshold && player.currentHealth > 0 && !ModFile.isPlaying) {
            playHealthWarningMusic();
            ModFile.isPlaying = true;

        } else if (player.currentHealth > healthThreshold && ModFile.isPlaying) {
            stopHealthWarningMusic();
            ModFile.isPlaying = false;
        }
        return null;
    }

    public static void playHealthWarningMusic() {
        if (!isSpecialTempTrackPlaying() && !isBossStingerPlaying && !bossBattleEnded) {
            System.out.println("PlayWarningMusicMain");
            CardCrawlGame.music.silenceTempBgmInstantly();
            ModFile.playTempBgm("warningintro", false);
            ModFile.isPlaying = true;
        }
    }

    public static void stopHealthWarningMusic() {
        if (isPlaying) {
            boolean isFightingLagavulin = false;
            boolean isFightingHexaghost = false;
            boolean isFightingHeart = false;

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

                if (AbstractDungeon.getCurrRoom().phase != AbstractRoom.RoomPhase.COMPLETE) {
                    if (isFightingLagavulin) {
                        CardCrawlGame.music.silenceTempBgmInstantly();
                        CardCrawlGame.music.playTempBgmInstantly("ELITE");
                    } else if (isFightingHexaghost || isFightingHeart) {
                        CardCrawlGame.music.silenceTempBgmInstantly();
                    } else if (AbstractDungeon.getCurrRoom() instanceof MonsterRoomBoss) {
                        CardCrawlGame.music.silenceTempBgmInstantly();
                        CardCrawlGame.music.playTempBgmInstantly("BOSS_BOTTOM");
                        System.out.println("Playing boss music");
                    } else {
                        CardCrawlGame.music.silenceTempBgmInstantly();
                        CardCrawlGame.music.unsilenceBGM();
                    }
                } else {
                    if (!(AbstractDungeon.getCurrRoom() instanceof MonsterRoomBoss)) {
                        CardCrawlGame.music.silenceTempBgmInstantly();
                        CardCrawlGame.music.unsilenceBGM();
                    } else {
                        isBossStingerPlaying = true;
                        System.out.println("BossStingerMonsterBoss");
                        playBossStinger();
                    }
                }

                isPlaying = false;
            }
        }
    }

    public static boolean isSpecialTempTrackPlaying() {
        if (currentTempMusicKey != null) {
            for (String specialTrack : SPECIAL_TEMP_TRACKS) {
                if (currentTempMusicKey.equals(specialTrack)) {
                    return true;
                }
            }
        }
        return false;
    }

    public static void startBossStinger() {
        isBossStingerPlaying = true;
        bossBattleEnded = true;
    }

    public static void resetBossStingerState() {
        isBossStingerPlaying = false;
    }
}
