package pokemonlowhealthwarning;

import basemod.BaseMod;
import basemod.interfaces.*;
import com.badlogic.gdx.math.MathUtils;
import com.evacipated.cardcrawl.modthespire.lib.SpireInitializer;
import com.megacrit.cardcrawl.actions.AbstractGameAction;
import com.megacrit.cardcrawl.characters.AbstractPlayer;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.events.AbstractEvent;
import com.megacrit.cardcrawl.events.beyond.MindBloom;
import com.megacrit.cardcrawl.monsters.AbstractMonster;
import com.megacrit.cardcrawl.rooms.*;
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
        } else if (room instanceof MonsterRoom) {
            currentRoomType = "MONSTER";
        } else if (room instanceof ShopRoom) {
            currentRoomType = "SHOP";
        } else if (room instanceof EventRoom) {
            currentRoomType = "EVENT";
        } else if (room instanceof RestRoom) {
            currentRoomType = "REST";
        } else {
            currentRoomType = "OTHER";
        }
    }

    @Override
    public void receiveOnBattleStart(AbstractRoom abstractRoom) {
        isPlaying = false;
        isBossStingerPlaying = false;
        bossBattleEnded = false;
        updateRoomType(abstractRoom);
        checkPlayerHealth();
    }

    @Override
    public void receiveOnPlayerTurnStart() {
        if (!bossBattleEnded && !isBossStingerPlaying) {
            checkPlayerHealth();
        }
    }

    @Override
    public void receivePostBattle(AbstractRoom abstractRoom) {
        stopHealthWarningMusic();
        resetBossStingerState();
    }

    @Override
    public void receivePostDungeonInitialize() {
        if (CardCrawlGame.isInARun() && AbstractDungeon.currMapNode != null && AbstractDungeon.getCurrRoom() != null && AbstractDungeon.actionManager != null) {
            isPlaying = false;
            currentRoomType = null;
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
        bossBattleEnded = false;
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

    private static String getAreaKey() {
        switch (AbstractDungeon.id) {
            case "Exordium":
                return "Exordium";
            case "TheCity":
                return "TheCity";
            case "TheBeyond":
                return "TheBeyond";
            case "TheEnding":
                return "TheEnding";
            default:
                return "Exordium";  // Default music if area isn't found
        }
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
        if (isPlaying) {
            if (AbstractDungeon.getCurrRoom() == null || AbstractDungeon.getCurrRoom().monsters == null) {
                return;  // Early exit if not in a combat room or no monsters are present
            }

            boolean isFightingLagavulin = false;
            boolean isFightingHexaghost = false;
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
                } else if (mo.id.equals("Hexaghost")) {
                    isFightingHexaghost = true;
                } else if (mo.id.equals("CorruptHeart")) {
                    isFightingHeart = true;
                } else if (mo.id.equals("SpireShield") || mo.id.equals("SpireSpear")) {
                    isFightingSpireSpearOrShield = true;
                } else if (mo.type == AbstractMonster.EnemyType.BOSS && AbstractDungeon.getCurrRoom() instanceof EventRoom) {
                    // If it's an event room and the monster is of the boss type
                    isEventRoomBoss = true;
                    if (AbstractDungeon.getCurrRoom().event instanceof MindBloom){
                        isEventMindBloom = true;
                    }
                }
            }

            // Handle transitions if all monsters are dead
            if (allMonstersDead) {
                if (isEventRoomBoss) {
                    // Handle stinger for boss enemies in event rooms (e.g., Mind Bloom boss fight)
                    bossBattleEnded = true;
                    if (!isBossStingerPlaying) {
                        playBossStinger();  // Play the stinger only once
                        isBossStingerPlaying = true;
                    }
                } else if (currentRoomType.equals("BOSS")) {
                    bossBattleEnded = true;
                    if (!isBossStingerPlaying) {
                        playBossStinger();  // Play the stinger only once
                        isBossStingerPlaying = true;
                    }
                } else if (currentRoomType.equals("ELITE")) {
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
                    CardCrawlGame.music.silenceTempBgmInstantly();
                    CardCrawlGame.music.playTempBgmInstantly("STS_EliteBoss_NewMix_v1.ogg");
                } else if (isFightingHexaghost && !isEventMindBloom) {
                    CardCrawlGame.music.silenceTempBgmInstantly();
                    CardCrawlGame.music.playTempBgmInstantly("STS_Boss1_NewMix_v1.ogg");
                } else if (isFightingHeart) {
                    CardCrawlGame.music.silenceTempBgmInstantly();
                    CardCrawlGame.music.playTempBgmInstantly("STS_Boss4_v6.ogg");
                } else if (isFightingSpireSpearOrShield) {
                    // Spire Spear/Shield case: play Act 4 boss music
                    CardCrawlGame.music.silenceTempBgmInstantly();
                    CardCrawlGame.music.playTempBgmInstantly("STS_Act4_BGM_v2.ogg");
                } else if (isEventMindBloom) {
                    // If the player recovers health above the threshold in an event room with a boss, restore event music
                    CardCrawlGame.music.silenceTempBgmInstantly();
                    CardCrawlGame.music.playTempBgmInstantly("MINDBLOOM");
                } else if (currentRoomType.equals("BOSS")) {
                    // Handle normal boss music based on the act number
                    String bossMusicKey = getBossMusicKey();
                    CardCrawlGame.music.silenceTempBgmInstantly();
                    CardCrawlGame.music.playTempBgmInstantly(bossMusicKey);
                } else if (currentRoomType.equals("ELITE")) {
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