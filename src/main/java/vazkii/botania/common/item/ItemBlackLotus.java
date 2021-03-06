/**
 * This class was created by <Vazkii>. It's distributed as
 * part of the Botania Mod. Get the Source Code in github:
 * https://github.com/Vazkii/Botania
 * 
 * Botania is Open Source and distributed under the
 * Botania License: http://botaniamod.net/license.php
 * 
 * File Created @ [Jan 18, 2015, 12:17:10 AM (GMT)]
 */
package vazkii.botania.common.item;

import net.minecraft.client.resources.I18n;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import vazkii.botania.api.internal.VanillaPacketDispatcher;
import vazkii.botania.api.item.IManaDissolvable;
import vazkii.botania.api.mana.IManaPool;
import vazkii.botania.api.sound.BotaniaSoundEvents;
import vazkii.botania.client.core.handler.ModelHandler;
import vazkii.botania.common.lib.LibItemNames;
import vazkii.botania.common.network.PacketBotaniaEffect;
import vazkii.botania.common.network.PacketHandler;

import javax.annotation.Nonnull;
import java.util.List;

public class ItemBlackLotus extends ItemMod implements IManaDissolvable {

	private static final int MANA_PER = 8000;
	private static final int MANA_PER_T2 = 100000;

	public ItemBlackLotus() {
		super(LibItemNames.BLACK_LOTUS);
		setHasSubtypes(true);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void getSubItems(@Nonnull Item item, CreativeTabs tab, List<ItemStack> list) {
		for(int i = 0; i < 2; i++)
			list.add(new ItemStack(item, 1, i));
	}

	@Override
	public boolean hasEffect(ItemStack par1ItemStack) {
		return par1ItemStack.getItemDamage() > 0;
	}

	@Nonnull
	@Override
	public String getUnlocalizedName(ItemStack par1ItemStack) {
		return super.getUnlocalizedName(par1ItemStack) + par1ItemStack.getItemDamage();
	}

	@Override
	public void onDissolveTick(IManaPool pool, ItemStack stack, EntityItem item) {
		if(pool.isFull() || pool.getCurrentMana() == 0)
			return;

		TileEntity tile = (TileEntity) pool;
		boolean t2 = stack.getItemDamage() > 0;

		if(!item.worldObj.isRemote) {
			pool.recieveMana(t2 ? MANA_PER_T2 : MANA_PER);
			stack.stackSize--;
			VanillaPacketDispatcher.dispatchTEToNearbyPlayers(item.worldObj, tile.getPos());
		}

		PacketHandler.sendToNearby(item.worldObj, item, new PacketBotaniaEffect(PacketBotaniaEffect.EffectType.BLACK_LOTUS_DISSOLVE, item.posX, tile.getPos().getY() + 0.5, item.posZ));
		item.playSound(BotaniaSoundEvents.blackLotus, 0.5F, t2 ? 0.1F : 1F);
	}

	@SideOnly(Side.CLIENT)
	@Override
	public void addInformation(ItemStack stack, EntityPlayer player, List<String> list, boolean adv) {
		list.add(I18n.format("botaniamisc.lotusDesc"));
	}

	@SideOnly(Side.CLIENT)
	@Override
	public void registerModels() {
		ModelHandler.registerItemAppendMeta(this, 2, LibItemNames.BLACK_LOTUS);
	}

}
