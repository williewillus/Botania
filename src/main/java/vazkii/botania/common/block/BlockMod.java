/**
 * This class was created by <Vazkii>. It's distributed as
 * part of the Botania Mod. Get the Source Code in github:
 * https://github.com/Vazkii/Botania
 * 
 * Botania is Open Source and distributed under the
 * Botania License: http://botaniamod.net/license.php
 * 
 * File Created @ [Jan 14, 2014, 5:31:15 PM (GMT)]
 */
package vazkii.botania.common.block;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.registry.GameRegistry;
import vazkii.botania.api.recipe.IElvenItem;
import vazkii.botania.common.core.BotaniaCreativeTab;
import vazkii.botania.common.item.block.ItemBlockElven;
import vazkii.botania.common.item.block.ItemBlockMod;
import vazkii.botania.common.lib.LibMisc;

public class BlockMod extends Block {

	protected int originalLight;

	public BlockMod(Material par2Material, String name) {
		super(par2Material);
		setUnlocalizedName(name);
		GameRegistry.register(this, new ResourceLocation(LibMisc.MOD_ID, name));
		registerItemForm();
		if(registerInCreative())
			setCreativeTab(BotaniaCreativeTab.INSTANCE);
	}

	public void registerItemForm() {
		GameRegistry.register(this instanceof IElvenItem ? new ItemBlockElven(this) : new ItemBlockMod(this), getRegistryName());
	}

	@Override
	public Block setLightLevel(float p_149715_1_) {
		originalLight = (int) (p_149715_1_ * 15);
		return super.setLightLevel(p_149715_1_);
	}

	protected boolean registerInCreative() {
		return true;
	}


}
