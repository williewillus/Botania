/**
 * This class was created by <williewillus>. It's distributed as
 * part of the Botania Mod. Get the Source Code in github:
 * https://github.com/Vazkii/Botania
 *
 * Botania is Open Source and distributed under the
 * Botania License: http://botaniamod.net/license.php
 */
package vazkii.botania.client.core.handler;

import com.google.common.collect.Maps;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.statemap.DefaultStateMapper;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraftforge.client.event.ModelBakeEvent;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.apache.commons.lang3.text.WordUtils;
import vazkii.botania.client.core.helper.IconHelper;
import vazkii.botania.client.model.FloatingFlowerModel;
import vazkii.botania.client.model.PlatformModel;
import vazkii.botania.client.model.PylonItemModel;
import vazkii.botania.common.block.ModBlocks;
import vazkii.botania.common.integration.buildcraft.TriggerManaLevel;
import vazkii.botania.common.item.ItemSparkUpgrade;
import vazkii.botania.common.item.equipment.armor.terrasteel.ItemTerrasteelHelm;
import vazkii.botania.common.item.equipment.bauble.*;
import vazkii.botania.common.item.relic.ItemKingKey;

import java.util.Map;

public class MiscellaneousIcons {

    public static final MiscellaneousIcons INSTANCE = new MiscellaneousIcons();

    public TextureAtlasSprite
        alfPortalTex,
        lightRelayWorldIcon,
        lightRelayWorldIconRed,
        alchemyCatalystOverlay,
        enchanterOverlay,
        manaVoidOverlay,
        manaWater,
        terraPlateOverlay,
        corporeaWorldIcon,
        corporeaWorldIconMaster,
        corporeaIconStar,
        sparkWorldIcon,
        manaDetectorIcon,
        runeAltarTriggerIcon;

    public TextureAtlasSprite[] sparkUpgradeIcons;
    public TextureAtlasSprite[] kingKeyWeaponIcons;
    public Map<TriggerManaLevel.State, TextureAtlasSprite> manaLevelTriggerIcons = Maps.newEnumMap(TriggerManaLevel.State.class);
    public TextureAtlasSprite[] tiaraWingIcons;

    // begin dank_memes
    public TextureAtlasSprite tailIcon = null;
    public TextureAtlasSprite phiFlowerIcon = null;
    public TextureAtlasSprite goldfishIcon = null;
    public TextureAtlasSprite nerfBatIcon = null;
    // end dank_memes

    @SubscribeEvent
    public void onModelBake(ModelBakeEvent evt) {
        // BlockSpecialFlower
        // Ignore all vanilla rules, redirect all blockstates to blockstates/specialFlower.json#normal
        evt.modelManager.getBlockModelShapes().registerBlockWithStateMapper(ModBlocks.specialFlower, new DefaultStateMapper() {
            @Override
            public ModelResourceLocation getModelResourceLocation(IBlockState state) {
                return new ModelResourceLocation("botania:specialFlower");
            }
        });

        // Floating flowers
        evt.modelRegistry.putObject(new ModelResourceLocation("botania:miniIsland", "normal"), FloatingFlowerModel.INSTANCE);
        evt.modelRegistry.putObject(new ModelResourceLocation("botania:miniIsland", "inventory"), FloatingFlowerModel.INSTANCE);
        evt.modelRegistry.putObject(new ModelResourceLocation("botania:floatingSpecialFlower", "normal"), FloatingFlowerModel.INSTANCE);
        evt.modelRegistry.putObject(new ModelResourceLocation("botania:floatingSpecialFlower", "inventory"), FloatingFlowerModel.INSTANCE);

        // Pylon item model
        evt.modelRegistry.putObject(new ModelResourceLocation("botania:pylon", "inventory"), new PylonItemModel());

        // Platforms
        evt.modelRegistry.putObject(new ModelResourceLocation("botania:platform", "normal"), new PlatformModel());
    }

    @SubscribeEvent
    public void onTextureStitch(TextureStitchEvent.Pre evt) {
        alfPortalTex = IconHelper.forName(evt.map, "alfheimPortalInside", "blocks");
        lightRelayWorldIcon = IconHelper.forName(evt.map, "lightRelay1", "blocks");
        lightRelayWorldIconRed = IconHelper.forName(evt.map, "lightRelay3", "blocks");
        alchemyCatalystOverlay = IconHelper.forName(evt.map, "alchemyCatalyst3", "blocks");
        enchanterOverlay = IconHelper.forName(evt.map, "enchanterOverlay", "blocks");
        manaVoidOverlay = IconHelper.forName(evt.map, "manaVoid1", "blocks");
        manaWater = IconHelper.forName(evt.map, "manaWater", "blocks");
        terraPlateOverlay = IconHelper.forName(evt.map, "terraPlateOverlay", "blocks");
        corporeaWorldIcon = IconHelper.forName(evt.map, "corporeaSpark1", "items");
        corporeaWorldIconMaster = IconHelper.forName(evt.map, "corporeaSpark3", "items");
        corporeaIconStar = IconHelper.forName(evt.map, "corporeaSparkStar", "items");
        sparkWorldIcon = IconHelper.forName(evt.map, "spark1", "items");

        sparkUpgradeIcons = new TextureAtlasSprite[ItemSparkUpgrade.VARIANTS];
        for(int i = 0; i < ItemSparkUpgrade.VARIANTS; i++) {
            sparkUpgradeIcons[i] = IconHelper.forName(evt.map, "sparkUpgradeL" + i, "items");
        }

        tailIcon = IconHelper.forName(evt.map, "tail", "items");
        phiFlowerIcon = IconHelper.forName(evt.map, "phiFlower", "items");
        goldfishIcon = IconHelper.forName(evt.map, "goldfish", "items");
        nerfBatIcon = IconHelper.forName(evt.map, "nerfBat", "items");

        kingKeyWeaponIcons = new TextureAtlasSprite[ItemKingKey.WEAPON_TYPES];
        for(int i = 0; i < ItemKingKey.WEAPON_TYPES; i++)
            kingKeyWeaponIcons[i] = IconHelper.forName(evt.map, "gateWeapon" + i, "items");

        manaDetectorIcon = IconHelper.forName(evt.map, "triggers/manaDetector", "items");
        runeAltarTriggerIcon = IconHelper.forName(evt.map, "triggers/runeAltarCanCraft", "items");

        for (TriggerManaLevel.State s : TriggerManaLevel.State.values()) {
            manaLevelTriggerIcons.put(s, IconHelper.forName(evt.map, "triggers/mana" + WordUtils.capitalizeFully(s.name()), "items"));
        }

        stitchBaubleTextures(evt);
    }

    private void  stitchBaubleTextures(TextureStitchEvent evt) {
        ItemTerrasteelHelm.willIcon = IconHelper.forName(evt.map, "willFlame", "items");

        tiaraWingIcons = new TextureAtlasSprite[ItemFlightTiara.WING_TYPES];
        for (int i = 0; i < tiaraWingIcons.length; i++) {
            tiaraWingIcons[i] = IconHelper.forName(evt.map, "flightTiara" + i, "items");
        }

        ItemIcePendant.gemIcon = IconHelper.forName(evt.map, "icePendantGem", "items");
        ItemItemFinder.gemIcon = IconHelper.forName(evt.map, "itemFinderGem", "items");
        ItemLavaPendant.gemIcon = IconHelper.forName(evt.map, "lavaPendantGem", "items");
        ItemSuperLavaPendant.gemIcon = IconHelper.forName(evt.map, "superLavaPendantGem", "items");
    }

    private MiscellaneousIcons() {}
}
