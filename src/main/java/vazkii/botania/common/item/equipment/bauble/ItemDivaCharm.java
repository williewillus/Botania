/**
 * This class was created by <Vazkii>. It's distributed as
 * part of the Botania Mod. Get the Source Code in github:
 * https://github.com/Vazkii/Botania
 *
 * Botania is Open Source and distributed under the
 * Botania License: http://botaniamod.net/license.php
 *
 * File Created @ [May 25, 2014, 10:30:39 PM (GMT)]
 */
package vazkii.botania.common.item.equipment.bauble;

import java.util.List;

import com.google.common.base.Predicates;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.monster.EntityCreeper;
import net.minecraft.entity.monster.IMob;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.AxisAlignedBB;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.living.LivingHurtEvent;

import vazkii.botania.api.item.IBaubleRender;
import vazkii.botania.api.mana.IManaUsingItem;
import vazkii.botania.api.mana.ManaItemHandler;
import vazkii.botania.common.Botania;
import vazkii.botania.common.block.subtile.functional.SubTileHeiseiDream;
import vazkii.botania.common.lib.LibItemNames;
import vazkii.botania.common.lib.LibObfuscation;
import baubles.api.BaubleType;
import baubles.common.lib.PlayerHandler;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.ReflectionHelper;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class ItemDivaCharm extends ItemBauble implements IManaUsingItem, IBaubleRender {

	public ItemDivaCharm() {
		super(LibItemNames.DIVA_CHARM);
		MinecraftForge.EVENT_BUS.register(this);
	}

	@SubscribeEvent
	public void onEntityDamaged(LivingHurtEvent event) {
		if(event.source.getEntity() instanceof EntityPlayer && event.entityLiving instanceof EntityLiving && !event.entityLiving.worldObj.isRemote && Math.random() < 0.6F) {
			EntityPlayer player = (EntityPlayer) event.source.getEntity();
			ItemStack amulet = PlayerHandler.getPlayerBaubles(player).getStackInSlot(0);
			if(amulet != null && amulet.getItem() == this) {
				final int cost = 250;
				if(ManaItemHandler.requestManaExact(amulet, player, cost, false)) {
					final int range = 20;

					List mobs = player.worldObj.getEntitiesWithinAABB(Entity.class, new AxisAlignedBB(event.entity.posX - range, event.entity.posY - range, event.entity.posZ - range, event.entity.posX + range, event.entity.posY + range, event.entity.posZ + range), Predicates.instanceOf(IMob.class));
					if(mobs.size() > 1) {
						if(SubTileHeiseiDream.brainwashEntity((EntityLiving) event.entityLiving, ((List<IMob>) mobs))) {
							if(event.entityLiving instanceof EntityCreeper)
								ReflectionHelper.setPrivateValue(EntityCreeper.class, (EntityCreeper) event.entityLiving, 2, LibObfuscation.TIME_SINCE_IGNITED);
							event.entityLiving.heal(event.entityLiving.getMaxHealth());
							if(event.entityLiving.isDead)
								event.entityLiving.isDead = false;

							ManaItemHandler.requestManaExact(amulet, player, cost, true);
							player.worldObj.playSoundAtEntity(player, "botania:divaCharm", 1F, 1F);

							double x = event.entityLiving.posX;
							double y = event.entityLiving.posY;
							double z = event.entityLiving.posZ;

							for(int i = 0; i < 50; i++)
								Botania.proxy.sparkleFX(event.entityLiving.worldObj, x + Math.random() * event.entityLiving.width, y + Math.random() * event.entityLiving.height, z + Math.random() * event.entityLiving.width, 1F, 1F, 0.25F, 1F, 3);
						}
					}
				}
			}
		}
	}

	@Override
	public BaubleType getBaubleType(ItemStack arg0) {
		return BaubleType.AMULET;
	}

	@Override
	public boolean usesMana(ItemStack stack) {
		return true;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void onPlayerBaubleRender(ItemStack stack, EntityPlayer player, float partialTicks, RenderType type) {
		if(type == RenderType.HEAD) {
			Minecraft.getMinecraft().renderEngine.bindTexture(TextureMap.locationBlocksTexture);
			Helper.rotateIfSneaking(player);
			boolean armor = player.getCurrentArmor(2) != null;
			GlStateManager.rotate(180F, 1F, 0F, 0F);
			GlStateManager.translate(armor ? 0.35F : 0.3F, 0.45F, -0.15F);
			GlStateManager.rotate(90F, 0F, 1F, 0F);

			GlStateManager.scale(0.5F, 0.5F, 0.5F);
			Minecraft.getMinecraft().getRenderItem().renderItem(stack, ItemCameraTransforms.TransformType.NONE);
		}
	}

}
