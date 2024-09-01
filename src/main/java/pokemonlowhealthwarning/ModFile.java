package pokemonlowhealthwarning;

import basemod.BaseMod;
import basemod.interfaces.*;
import com.badlogic.gdx.math.MathUtils;
import com.evacipated.cardcrawl.modthespire.lib.SpireInitializer;
import com.megacrit.cardcrawl.actions.AbstractGameAction;
import com.megacrit.cardcrawl.characters.AbstractPlayer;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.events.beyond.MindBloom;
import com.megacrit.cardcrawl.monsters.AbstractMonster;
import com.megacrit.cardcrawl.monsters.exordium.Lagavulin;
import com.megacrit.cardcrawl.rooms.AbstractRoom;
import com.megacrit.cardcrawl.rooms.EventRoom;
import com.megacrit.cardcrawl.rooms.MonsterRoomBoss;
import com.megacrit.cardcrawl.rooms.MonsterRoomElite;
import pokemonlowhealthwarning.patch.HexaghostActivationFieldPatch;
import pokemonlowhealthwarning.patch.LagavulinSleepPatch;
import pokemonlowhealthwarning.util.ProAudio;

import static basemod.BaseMod.logger;

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
    public static String currentRoomType = null;

    private static final String[] SPECIAL_TEMP_TRACKS = {
            "CREDITS"
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

    private void updateRoomType(AbstractRoom room) {
        if (room instanceof MonsterRoomBoss) {
            currentRoomType = "BOSS";
        } else if (room instanceof MonsterRoomElite) {
            currentRoomType = "ELITE";
        } else if (room instanceof EventRoom) {
            currentRoomType = "EVENT";
        } else {
            currentRoomType = null;
        }
    }

    @Override
    public void receiveOnBattleStart(AbstractRoom abstractRoom) {
        resetMusicStates();
        updateRoomType(abstractRoom);
        checkPlayerHealth();
    }

    @Override
    public void receiveOnPlayerTurnStart() {
    }

    @Override
    public void receivePostBattle(AbstractRoom abstractRoom) {
        stopHealthWarningMusic();
        resetMusicStates();
    }

    @Override
    public void receivePostDungeonInitialize() {
        if (CardCrawlGame.isInARun() && AbstractDungeon.currMapNode != null && AbstractDungeon.getCurrRoom() != null && AbstractDungeon.actionManager != null) {
            resetMusicStates();
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

    private void resetMusicStates() {
        isPlaying = false;
        isBossStingerPlaying = false;
        bossBattleEnded = false;
        currentRoomType = null;
        currentTempMusicKey = null;
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

    public static void playBossStinger() {
        if (isBossStingerPlaying) {
            return; // Prevent playing anything else if the boss stinger is playing.
        }

        isBossStingerPlaying = true;

        // Play the boss victory stinger sound effect
        CardCrawlGame.sound.play("BOSS_VICTORY_STINGER");

        if (AbstractDungeon.id.equals("TheEnding")) {
            CardCrawlGame.music.playTempBgmInstantly("STS_EndingStinger_v1.ogg", false);
        } else {
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
        }
    }
        public static AbstractGameAction checkPlayerHealth() {
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

    private static String getBossMusicKey() {
        switch (AbstractDungeon.actNum) {
            case 1:
                return "STS_Boss1_NewMix_v1.ogg";
            case 2:
                return "STS_Boss2_NewMix_v1.ogg";
            case 3:
                return "STS_Boss3_NewMix_v1.ogg";
            case 4:
                return "STS_Boss4_v6.ogg";
            default:
                return "STS_Boss1_NewMix_v1.ogg";  // Default boss music
        }
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
        if (isBossStingerPlaying) {
            return; // Prevent any music from interrupting the boss stinger.
        }
        if (isPlaying) {
            if (AbstractDungeon.getCurrRoom() == null || AbstractDungeon.getCurrRoom().monsters == null) {
                return;  // Early exit if not in a combat room or no monsters are present
            }

            boolean isFightingLagavulin = false;
            boolean isLagavulinAsleep = false;
            boolean isFightingHexaghost = false;
            boolean isHexaghostActivated = false;
            boolean isFightingHeart = false;
            boolean isFightingSpireSpearOrShield = false;
            boolean isEventRoomBoss = false;
            boolean isEventMindBloom = false;
            boolean allMonstersDead = true;

            // Check the status of all monsters
            for (AbstractMonster mo : AbstractDungeon.getCurrRoom().monsters.monsters) {
                if (!mo.isDeadOrEscaped() && !mo.isDying) {
                    allMonstersDead = false;  // At least one monster is still alive
                }

                // Check for specific bosses and elites
                if (mo.id.equals("Lagavulin")) {
                    isFightingLagavulin = true;
                    isLagavulinAsleep = LagavulinSleepPatch.isLagavulinAsleep((Lagavulin) mo); // Check if Lagavulin is asleep
                } else if (mo.id.equals("Hexaghost")) {
                    isFightingHexaghost = true;
                    isHexaghostActivated = HexaghostActivationFieldPatch.isActivated.get(mo);
                } else if (mo.id.equals("CorruptHeart")) {
                    isFightingHeart = true;
                } else if (mo.id.equals("SpireShield") || mo.id.equals("SpireSpear")) {
                    isFightingSpireSpearOrShield = true;
                } else if (AbstractDungeon.getCurrRoom().event instanceof MindBloom) {
                     isEventMindBloom = true;

                }
            }

            // Handle transitions if all monsters are dead
            if (allMonstersDead) {
                if (currentRoomType != null && currentRoomType.equals("ELITE")) {
                    if (isFightingLagavulin) {
                        // Switch back to Exordium music after Lagavulin dies
                        CardCrawlGame.music.silenceTempBgmInstantly();
                        CardCrawlGame.music.unsilenceBGM();
                    } else {
                        // Switch back to area music after other elites die
                        CardCrawlGame.music.silenceTempBgmInstantly();
                        CardCrawlGame.music.unsilenceBGM();
                    }
                } else {
                    // Non-boss or non-elite room: handle music transitions
                    CardCrawlGame.music.silenceTempBgmInstantly();
                    CardCrawlGame.music.unsilenceBGM();
                }
            } else {
                // If monsters are still alive, play the appropriate music for elites or bosses
                if (isFightingLagavulin) {
                    if (!isLagavulinAsleep) {
                        CardCrawlGame.music.silenceTempBgmInstantly();
                        CardCrawlGame.music.playTempBgmInstantly("STS_EliteBoss_NewMix_v1.ogg");
                    } else {
                        CardCrawlGame.music.silenceTempBgmInstantly();
                        CardCrawlGame.music.silenceBGM();
                    }
                } else if (isFightingHexaghost && !isEventMindBloom) {
                    if (isHexaghostActivated) {
                        CardCrawlGame.music.silenceTempBgmInstantly();
                        CardCrawlGame.music.playTempBgmInstantly(getBossMusicKey());
                    } else {
                        CardCrawlGame.music.silenceTempBgmInstantly();
                        CardCrawlGame.music.silenceBGM();
                    }
                } else if (isFightingHeart) {
                    CardCrawlGame.music.silenceTempBgmInstantly();
                    CardCrawlGame.music.playTempBgmInstantly("STS_Boss4_v6.ogg");
                } else if (isFightingSpireSpearOrShield) {
                    CardCrawlGame.music.silenceTempBgmInstantly();
                    CardCrawlGame.music.playTempBgmInstantly("STS_Act4_BGM_v2.ogg");
                } else if (isEventMindBloom) {
                    CardCrawlGame.music.silenceTempBgmInstantly();
                    CardCrawlGame.music.playTempBgmInstantly("MINDBLOOM");
                } else if (currentRoomType != null && currentRoomType.equals("BOSS")) {
                    // Handle normal boss music based on the act number
                    String bossMusicKey = getBossMusicKey();
                    CardCrawlGame.music.silenceTempBgmInstantly();
                    CardCrawlGame.music.playTempBgmInstantly(bossMusicKey);
                } else if (currentRoomType != null && currentRoomType.equals("ELITE")) {
                    // Keep playing elite music until elites are dead
                    CardCrawlGame.music.silenceTempBgmInstantly();
                    CardCrawlGame.music.playTempBgmInstantly("STS_EliteBoss_NewMix_v1.ogg");
                } else {
                    // Handle non-boss, non-elite rooms
                    CardCrawlGame.music.silenceTempBgmInstantly();
                    CardCrawlGame.music.unsilenceBGM();
                }
            }

            // Reset the health warning music state
            isPlaying = false;
        }
    }
}
