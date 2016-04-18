package vazkii.botania.common.item.equipment.tool.elementium;

import net.minecraft.block.Block;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.world.BlockEvent.HarvestDropsEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.oredict.OreDictionary;
import vazkii.botania.api.BotaniaAPI;
import vazkii.botania.common.item.ModItems;
import vazkii.botania.common.item.equipment.tool.manasteel.ItemManasteelPick;
import vazkii.botania.common.item.equipment.tool.terrasteel.ItemTerraPick;
import vazkii.botania.common.lib.LibItemNames;

public class ItemElementiumPick extends ItemManasteelPick {

	public ItemElementiumPick() {
		super(BotaniaAPI.elementiumToolMaterial, LibItemNames.ELEMENTIUM_PICK);
		MinecraftForge.EVENT_BUS.register(this);
	}

	@SubscribeEvent
	public void onHarvestDrops(HarvestDropsEvent event) {
		if(event.getHarvester() != null) {
			ItemStack stack = event.getHarvester().getHeldItemMainhand();
			if(stack != null && (stack.getItem() == this || stack.getItem() == ModItems.terraPick && ItemTerraPick.isTipped(stack))) {
				for(int i = 0; i < event.getDrops().size(); i++) {
					ItemStack drop = event.getDrops().get(i);
					if(drop != null) {
						Block block = Block.getBlockFromItem(drop.getItem());
						if(block != null){
							if(isDisposable(block) || (isSemiDisposable(block) && !event.getHarvester().isSneaking()))
								event.getDrops().remove(i);
						}
					}
				}
			}
		}
	}

	public static boolean isDisposable(Block block) {
		for(int id : OreDictionary.getOreIDs(new ItemStack(block))) {
			String name = OreDictionary.getOreName(id);
			if(BotaniaAPI.disposableBlocks.contains(name))
				return true;
		}
		return false;
	}
	
	public static boolean isSemiDisposable(Block block) {
		for(int id : OreDictionary.getOreIDs(new ItemStack(block))) {
			String name = OreDictionary.getOreName(id);
			if(BotaniaAPI.semiDisposableBlocks.contains(name))
				return true;
		}
		return false;
	}
}
