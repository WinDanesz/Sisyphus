package com.windanesz.sisyphus.client;

import com.windanesz.sisyphus.CommonProxy;
import com.windanesz.sisyphus.Settings;
import com.windanesz.sisyphus.client.gui.GuiEditHangingSign;
import com.windanesz.sisyphus.client.particle.ParticleCherryPetal;
import com.windanesz.sisyphus.client.render.*;
import com.windanesz.sisyphus.client.render.entity.*;
import com.windanesz.sisyphus.entity.item.EntityBambooChestRaft;
import com.windanesz.sisyphus.entity.item.EntityBambooRaft;
import com.windanesz.sisyphus.entity.living.*;
import com.windanesz.sisyphus.entity.projectile.EntityPiercingArrow;
import com.windanesz.sisyphus.entity.projectile.EntityThrowingDisc;
import com.windanesz.sisyphus.item.ItemLeatherHorseArmor;
import com.windanesz.sisyphus.packet.PacketHangingSignEditor;
import com.windanesz.sisyphus.packet.PacketServerSettings;
import com.windanesz.sisyphus.packet.ServerSettings;
import com.windanesz.sisyphus.registry.ModBlocks;
import com.windanesz.sisyphus.registry.ModEnchantments;
import com.windanesz.sisyphus.registry.ModItems;
import com.windanesz.sisyphus.tileentity.*;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.RenderTippedArrow;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.enchantment.EnumEnchantmentType;
import net.minecraft.entity.monster.EntityPigZombie;
import net.minecraft.init.Blocks;
import net.minecraft.tileentity.TileEntityFlowerPot;
import net.minecraft.tileentity.TileEntitySign;
import net.minecraft.world.ColorizerGrass;
import net.minecraft.world.World;
import net.minecraft.world.biome.BiomeColorHelper;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.client.registry.RenderingRegistry;

import java.util.Arrays;

public class ClientProxy extends CommonProxy {

    @Override
    public void init() {
        Block bubbleColumn = ModBlocks.get("bubble_column");
        if (bubbleColumn != Blocks.AIR) {
            // A bubble column is the colour of the water of the biome, as water is
            Minecraft.getMinecraft().getBlockColors().registerBlockColorHandler((state, world, pos, tintIndex) ->
                    world != null && pos != null ? BiomeColorHelper.getWaterColorAtPos(world, pos) : -1, bubbleColumn);
        }
        Block petals = ModBlocks.get("pink_petals");
        if (petals != Blocks.AIR) {
            // The stems under pink petals are the colour of the grass around them
            Minecraft.getMinecraft().getBlockColors().registerBlockColorHandler((state, world, pos, tintIndex) -> tintIndex != 1 ? 0xFFFFFF
                    : world != null && pos != null ? BiomeColorHelper.getGrassColorAtPos(world, pos) : ColorizerGrass.getGrassColor(0.5D, 1.0D), petals);
        }
        if (ModEnchantments.diggerType != null) {
            // The books of Efficiency, Fortune and Silk Touch stay in the creative tab of tools, where their old kind put them
            EnumEnchantmentType[] relevant = CreativeTabs.TOOLS.getRelevantEnchantmentTypes();
            EnumEnchantmentType[] with = Arrays.copyOf(relevant, relevant.length + 1);
            with[relevant.length] = ModEnchantments.diggerType;
            CreativeTabs.TOOLS.setRelevantEnchantmentTypes(with);
        }
        if (Settings.items.enableLeatherHorseArmor) {
            // The leather horse armor item is drawn in the colour it is dyed in
            Minecraft.getMinecraft().getItemColors().registerItemColorHandler(
                    (stack, tintIndex) -> tintIndex == 0 ? ItemLeatherHorseArmor.getColor(stack) : 0xFFFFFF, ModItems.get("leather_horse_armor"));
        }
    }

    @Override
    public void spawnCherryPetal(World world, double x, double y, double z) {
        Minecraft.getMinecraft().effectRenderer.addEffect(new ParticleCherryPetal(world, x, y, z));
    }

    @Override
    public void handleServerSettingsPacket(PacketServerSettings.Message message) {
        Minecraft.getMinecraft().addScheduledTask(() -> {
            ServerSettings.setFromServer(message.showBrushingInJei, message.discWindUp);
            ClientServerSettings.apply();
        });
    }

    @Override
    public void handleHangingSignEditorPacket(PacketHangingSignEditor.Message message) {
        Minecraft mc = Minecraft.getMinecraft();
        mc.addScheduledTask(() -> {
            if (mc.player != null) {
                mc.displayGuiScreen(new GuiEditHangingSign(message.pos, message.wood));
            }
        });
    }

    @Override
    public void construct() {
        // The game loads its resources before preInit, and the pack has to be among them, so it is put there now and fills itself later
        GeneratedOrePack.install();
    }

    @Override
    public void registerRenderers() {
        // Before the resources are loaded the first time, so that the deepslate ores of other mods have their pictures
        GeneratedOrePack.install();
        if (Settings.buildingBlocks.enableSigns) {
            // Replaces the vanilla renderer, so that dyed text and the new wood types are drawn
            ClientRegistry.bindTileEntitySpecialRenderer(TileEntitySign.class, new RenderColoredSign());
        }
        if (Settings.isContentEnabled("hanging_signs")) {
            ClientRegistry.bindTileEntitySpecialRenderer(TileEntityHangingSign.class, new RenderHangingSign());
        }
        if (Settings.functionalBlocks.enableLectern) {
            ClientRegistry.bindTileEntitySpecialRenderer(TileEntityLectern.class, new RenderLectern());
        }
        if (Settings.functionalBlocks.enableBell) {
            ClientRegistry.bindTileEntitySpecialRenderer(TileEntityBell.class, new RenderBell());
        }
        if (Settings.functionalBlocks.enableCampfire) {
            ClientRegistry.bindTileEntitySpecialRenderer(TileEntityCampfire.class, new RenderCampfire());
        }
        // Draws the plants of other mods and of this mod that flower pots of the game hold, which their block model has no model for
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityFlowerPot.class, new RenderFlowerPot());
        if (Settings.archaeology.enableArchaeology) {
            ClientRegistry.bindTileEntitySpecialRenderer(TileEntityBrushable.class, new RenderBrushable());
            ClientRegistry.bindTileEntitySpecialRenderer(TileEntityDecoratedPot.class, new RenderDecoratedPot());
        }
        if (Settings.isContentEnabled("bamboo_wood")) {
            RenderingRegistry.registerEntityRenderingHandler(EntityBambooRaft.class, manager -> new RenderBambooRaft(manager, false));
            RenderingRegistry.registerEntityRenderingHandler(EntityBambooChestRaft.class, manager -> new RenderBambooRaft(manager, true));
        }
        if (Settings.mobs.enableCats) {
            RenderingRegistry.registerEntityRenderingHandler(EntityCat.class, RenderCat::new);
        }
        if (Settings.mobs.enableFoxes) {
            RenderingRegistry.registerEntityRenderingHandler(EntityFox.class, RenderFox::new);
        }
        if (Settings.mobs.enablePandas) {
            RenderingRegistry.registerEntityRenderingHandler(EntityPanda.class, RenderPanda::new);
        }
        if (Settings.mobs.enableGoats) {
            RenderingRegistry.registerEntityRenderingHandler(EntityGoat.class, RenderGoat::new);
        }
        if (Settings.mobs.enableAxolotls) {
            RenderingRegistry.registerEntityRenderingHandler(EntityAxolotl.class, RenderAxolotl::new);
        }
        if (Settings.mobs.enableBrownMooshroom) {
            RenderingRegistry.registerEntityRenderingHandler(EntityBrownMooshroom.class, RenderBrownMooshroom::new);
        }
        if (Settings.mobs.enableWanderingTrader) {
            RenderingRegistry.registerEntityRenderingHandler(EntityWanderingTrader.class, RenderWanderingTrader::new);
            RenderingRegistry.registerEntityRenderingHandler(EntityTraderLlama.class, RenderTraderLlama::new);
        }
        if (Settings.mobs.enablePiglins) {
            RenderingRegistry.registerEntityRenderingHandler(EntityPiglin.class, RenderPiglin::new);
            // The zombie pigman is drawn as the zombified piglin
            RenderingRegistry.registerEntityRenderingHandler(EntityPigZombie.class, RenderZombifiedPiglin::new);
        }
        if (Settings.mobs.enableStriders) {
            RenderingRegistry.registerEntityRenderingHandler(EntityStrider.class, RenderStrider::new);
        }
        if (Settings.mobs.enableHoglins) {
            RenderingRegistry.registerEntityRenderingHandler(EntityHoglin.class, RenderHoglin<EntityHoglin>::new);
            RenderingRegistry.registerEntityRenderingHandler(EntityZoglin.class, RenderHoglin<EntityZoglin>::new);
        }
        if (Settings.items.crossbow.enabled) {
            RenderingRegistry.registerEntityRenderingHandler(EntityPiercingArrow.class, RenderTippedArrow::new);
        }
        if (Settings.items.throwingDiscs.enabled) {
            RenderingRegistry.registerEntityRenderingHandler(EntityThrowingDisc.class,
                    manager -> new RenderThrowingDisc(manager, Minecraft.getMinecraft().getRenderItem()));
        }
        if (!Settings.mobs.enableFish) {
            return;
        }
        RenderingRegistry.registerEntityRenderingHandler(EntityCod.class, RenderCod::new);
        RenderingRegistry.registerEntityRenderingHandler(EntitySalmon.class, RenderSalmon::new);
        RenderingRegistry.registerEntityRenderingHandler(EntityPufferfish.class, RenderPufferfish::new);
        RenderingRegistry.registerEntityRenderingHandler(EntityTropicalFish.class, RenderTropicalFish::new);
    }
}