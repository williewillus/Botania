package vazkii.botania.common.item.equipment.tool.elementium;

import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.monster.EntityCreeper;
import net.minecraft.entity.monster.EntityPigZombie;
import net.minecraft.entity.monster.EntitySkeleton;
import net.minecraft.entity.monster.EntityZombie;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Enchantments;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.living.LivingDropsEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import vazkii.botania.api.BotaniaAPI;
import vazkii.botania.common.core.helper.ItemNBTHelper;
import vazkii.botania.common.entity.EntityDoppleganger;
import vazkii.botania.common.item.ModItems;
import vazkii.botania.common.item.equipment.tool.manasteel.ItemManasteelAxe;
import vazkii.botania.common.lib.LibItemNames;

import java.util.Random;

public class ItemElementiumAxe extends ItemManasteelAxe {

	public ItemElementiumAxe() {
		super(BotaniaAPI.elementiumToolMaterial, LibItemNames.ELEMENTIUM_AXE);
		MinecraftForge.EVENT_BUS.register(this);
	}

	// Thanks to SpitefulFox for the drop rates
	// https://github.com/SpitefulFox/ForbiddenMagic/blob/master/src/com/spiteful/forbidden/FMEventHandler.java

	@SubscribeEvent
	public void onEntityDrops(LivingDropsEvent event) {
		if(event.isRecentlyHit() && event.getSource().getEntity() != null && event.getSource().getEntity() instanceof EntityPlayer) {
			ItemStack weapon = ((EntityPlayer) event.getSource().getEntity()).getHeldItemMainhand();
			if(weapon != null && weapon.getItem() == this) {
				Random rand = event.getEntityLiving().worldObj.rand;
				int looting = EnchantmentHelper.getEnchantmentLevel(Enchantments.fortune, weapon);

				if(event.getEntityLiving() instanceof EntitySkeleton && rand.nextInt(26) <= 3 + looting)
					addDrop(event, new ItemStack(Items.skull, 1, ((EntitySkeleton)event.getEntityLiving()).getSkeletonType()));
				else if(event.getEntityLiving() instanceof EntityZombie && !(event.getEntityLiving() instanceof EntityPigZombie) && rand.nextInt(26) <= 2 + 2 * looting)
					addDrop(event, new ItemStack(Items.skull, 1, 2));
				else if(event.getEntityLiving() instanceof EntityCreeper && rand.nextInt(26) <= 2 + 2 * looting)
					addDrop(event, new ItemStack(Items.skull, 1, 4));
				else if(event.getEntityLiving() instanceof EntityPlayer && rand.nextInt(11) <= 1 + looting) {
					ItemStack stack = new ItemStack(Items.skull, 1, 3);
					ItemNBTHelper.setString(stack, "SkullOwner", event.getEntityLiving().getName());
					addDrop(event, stack);
				} else if(event.getEntityLiving() instanceof EntityDoppleganger && rand.nextInt(13) < 1 + looting)
					addDrop(event, new ItemStack(ModItems.gaiaHead));
			}
		}
	}

	private void addDrop(LivingDropsEvent event, ItemStack drop) {
		EntityItem entityitem = new EntityItem(event.getEntityLiving().worldObj, event.getEntityLiving().posX, event.getEntityLiving().posY, event.getEntityLiving().posZ, drop);
		entityitem.setPickupDelay(10);
		event.getDrops().add(entityitem);
	}

}
