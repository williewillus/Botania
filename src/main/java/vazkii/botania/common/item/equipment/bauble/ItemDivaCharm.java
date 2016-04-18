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

import baubles.api.BaubleType;
import baubles.common.lib.PlayerHandler;
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
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.ReflectionHelper;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import vazkii.botania.api.item.IBaubleRender;
import vazkii.botania.api.mana.IManaUsingItem;
import vazkii.botania.api.mana.ManaItemHandler;
import vazkii.botania.api.sound.BotaniaSoundEvents;
import vazkii.botania.common.Botania;
import vazkii.botania.common.block.subtile.functional.SubTileHeiseiDream;
import vazkii.botania.common.lib.LibItemNames;
import vazkii.botania.common.lib.LibObfuscation;

import java.util.List;

public class ItemDivaCharm extends ItemBauble implements IManaUsingItem, IBaubleRender {

	public ItemDivaCharm() {
		super(LibItemNames.DIVA_CHARM);
		MinecraftForge.EVENT_BUS.register(this);
	}

	@SubscribeEvent
	public void onEntityDamaged(LivingHurtEvent event) {
		if(event.getSource().getEntity() instanceof EntityPlayer && event.getEntityLiving() instanceof EntityLiving && !event.getEntityLiving().worldObj.isRemote && Math.random() < 0.6F) {
			EntityPlayer player = (EntityPlayer) event.getSource().getEntity();
			ItemStack amulet = PlayerHandler.getPlayerBaubles(player).getStackInSlot(0);
			if(amulet != null && amulet.getItem() == this) {
				final int cost = 250;
				if(ManaItemHandler.requestManaExact(amulet, player, cost, false)) {
					final int range = 20;

					List mobs = player.worldObj.getEntitiesWithinAABB(Entity.class, new AxisAlignedBB(event.getEntityLiving().posX - range, event.getEntityLiving().posY - range, event.getEntityLiving().posZ - range, event.getEntityLiving().posX + range, event.getEntityLiving().posY + range, event.getEntityLiving().posZ + range), Predicates.instanceOf(IMob.class));
					if(mobs.size() > 1) {
						if(SubTileHeiseiDream.brainwashEntity((EntityLiving) event.getEntityLiving(), ((List<IMob>) mobs))) {
							if(event.getEntityLiving() instanceof EntityCreeper)
								ReflectionHelper.setPrivateValue(EntityCreeper.class, (EntityCreeper) event.getEntityLiving(), 2, LibObfuscation.TIME_SINCE_IGNITED);
							event.getEntityLiving().heal(event.getEntityLiving().getMaxHealth());
							if(event.getEntityLiving().isDead)
								event.getEntityLiving().isDead = false;

							ManaItemHandler.requestManaExact(amulet, player, cost, true);
							player.worldObj.playSound(null, player.posX, player.posY, player.posZ, BotaniaSoundEvents.divaCharm, SoundCategory.PLAYERS, 1F, 1F);

							double x = event.getEntityLiving().posX;
							double y = event.getEntityLiving().posY;
							double z = event.getEntityLiving().posZ;

							for(int i = 0; i < 50; i++)
								Botania.proxy.sparkleFX(event.getEntityLiving().worldObj, x + Math.random() * event.getEntityLiving().width, y + Math.random() * event.getEntityLiving().height, z + Math.random() * event.getEntityLiving().width, 1F, 1F, 0.25F, 1F, 3);
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
	public void onPlayerBaubleRender(ItemStack stack, EntityPlayer player, RenderType type, float partialTicks) {
		if(type == RenderType.HEAD) {
			boolean armor = player.getItemStackFromSlot(EntityEquipmentSlot.HEAD) != null;
			Helper.translateToHeadLevel(player);
			Minecraft.getMinecraft().renderEngine.bindTexture(TextureMap.locationBlocksTexture);
			GlStateManager.scale(0.8F, 0.8F, 0.8F);
			GlStateManager.rotate(90, 0, 0, 1);
			GlStateManager.translate(1.475F, armor ? -0.57F : -0.5F, 0.53F);
			Minecraft.getMinecraft().getRenderItem().renderItem(new ItemStack(this, 1), ItemCameraTransforms.TransformType.THIRD_PERSON_RIGHT_HAND); // todo 1.9
		}
	}

}
