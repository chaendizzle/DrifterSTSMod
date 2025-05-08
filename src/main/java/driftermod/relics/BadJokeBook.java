package driftermod.relics;

import basemod.AutoAdd;
import com.badlogic.gdx.math.MathUtils;
import com.megacrit.cardcrawl.actions.animations.TalkAction;
import com.megacrit.cardcrawl.actions.common.ApplyPowerAction;
import com.megacrit.cardcrawl.actions.common.RelicAboveCreatureAction;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.core.Settings;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.monsters.AbstractMonster;
import com.megacrit.cardcrawl.powers.VulnerablePower;
import com.megacrit.cardcrawl.relics.AbstractRelic;
import com.megacrit.cardcrawl.rooms.MonsterRoom;
import com.megacrit.cardcrawl.vfx.UpgradeShineEffect;
import com.megacrit.cardcrawl.vfx.cardManip.ShowCardBrieflyEffect;

import static driftermod.DrifterMod.makeID;

@AutoAdd.Seen
public class BadJokeBook extends BaseRelic {
    private static final String NAME = "badjokebook";
    public static final String ID = makeID(NAME);
    private static final RelicTier RARITY = RelicTier.COMMON;
    private static final LandingSound SOUND = LandingSound.CLINK;

    private static final int USES = 2;
    private static final int VULN_AMOUNT = 1;

    private static final String[] WORD_ENDINGS = new String[]{"er", "ir", "or", "ur", "re"};

    public BadJokeBook() {
        super(ID, NAME, RARITY, SOUND);
    }

    private String validCardPrefix(AbstractCard c)
    {
        for (String wordEnding : WORD_ENDINGS) {
            String name = c.name.replaceAll("[^A-za-z ]", "").trim();
            if (name.toLowerCase().endsWith(wordEnding.toLowerCase())) {
                return name.substring(0, name.length() - wordEnding.length());
            }
        }
        return null;
    }

    @Override
    public String getUpdatedDescription() {
        return DESCRIPTIONS[0] + DESCRIPTIONS[1] + USES + DESCRIPTIONS[2] + VULN_AMOUNT + DESCRIPTIONS[3];
    }

    @Override
    public void onPlayCard(AbstractCard card, AbstractMonster m) {
        if (!(AbstractDungeon.getCurrRoom() instanceof MonsterRoom)) {
            return;
        }
        String prefix = validCardPrefix(card);
        if (counter > 0 && prefix != null) {
            addToTop(new RelicAboveCreatureAction(AbstractDungeon.player, this));
            addToBot(new TalkAction(true, prefix + " 'er?\nI barely even know 'er!", 3.0f, 3.0f));
            for (AbstractMonster mo : AbstractDungeon.getMonsters().monsters) {
                if (!mo.isDeadOrEscaped()) {
                    addToBot(new ApplyPowerAction(mo, AbstractDungeon.player, new VulnerablePower(mo, VULN_AMOUNT, false)));
                }
            }
            counter--;
            if (counter == 0) {
                grayscale = true;
            }
        }
    }

    @Override
    public void onEquip() {
        int effectCount = 0;
        for (AbstractCard c : AbstractDungeon.player.masterDeck.group) {
            if (!c.canUpgrade() || validCardPrefix(c) == null) continue;
            if (++effectCount <= 20) {
                float x = MathUtils.random(0.1f, 0.9f) * (float)Settings.WIDTH;
                float y = MathUtils.random(0.2f, 0.8f) * (float)Settings.HEIGHT;
                AbstractDungeon.effectList.add(new ShowCardBrieflyEffect(c.makeStatEquivalentCopy(), x, y));
                AbstractDungeon.topLevelEffects.add(new UpgradeShineEffect(x, y));
            }
            c.upgrade();
            AbstractDungeon.player.bottledCardUpgradeCheck(c);
        }
        counter = USES;
    }

    @Override
    public void atBattleStart() {
        counter = USES;
        grayscale = false;
    }

    @Override
    public void onObtainCard(AbstractCard c) {
        if (c.canUpgrade() && !c.upgraded && validCardPrefix(c) != null) {
            c.upgrade();
        }
    }

    @Override
    public void onPreviewObtainCard(AbstractCard c) {
        this.onObtainCard(c);
    }

    @Override
    public boolean canSpawn() {
        return Settings.isEndless || AbstractDungeon.floorNum <= 48;
    }

    @Override
    public AbstractRelic makeCopy()
    {
        return new BadJokeBook();
    }
}
