/**
 * This class was created by <Vazkii>. It's distributed as
 * part of the Botania Mod. Get the Source Code in github:
 * https://github.com/Vazkii/Botania
 * 
 * Botania is Open Source and distributed under the
 * Botania License: http://botaniamod.net/license.php
 * 
 * File Created @ [May 15, 2014, 4:25:24 PM (GMT)]
 */
package vazkii.botania.common.block.subtile.functional;

import com.google.common.base.Predicate;
import net.minecraft.entity.Entity;
import net.minecraft.entity.passive.EntityAnimal;
import vazkii.botania.api.lexicon.LexiconEntry;
import vazkii.botania.common.lexicon.LexiconData;

public class SubTileDreadthorn extends SubTileBellethorn {

	@Override
	public int getColor() {
		return 0x260B45;
	}

	@Override
	public Predicate<Entity> getSelector() {
		return var1 -> var1 instanceof EntityAnimal && ((EntityAnimal) var1).getGrowingAge() == 0;
	}

	@Override
	public int getManaCost() {
		return 30;
	}

	@Override
	public LexiconEntry getEntry() {
		return LexiconData.dreadthorne;
	}

}
