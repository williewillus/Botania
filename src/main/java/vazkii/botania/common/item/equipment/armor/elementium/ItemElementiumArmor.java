package vazkii.botania.common.item.equipment.armor.elementium;

import net.minecraft.client.model.ModelBiped;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.translation.I18n;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import vazkii.botania.api.BotaniaAPI;
import vazkii.botania.api.item.IPixieSpawner;
import vazkii.botania.client.lib.LibResources;
import vazkii.botania.client.model.armor.ModelArmorElementium;
import vazkii.botania.common.core.handler.ConfigHandler;
import vazkii.botania.common.item.ModItems;
import vazkii.botania.common.item.equipment.armor.manasteel.ItemManasteelArmor;

import java.util.List;

public abstract class ItemElementiumArmor extends ItemManasteelArmor implements IPixieSpawner {

	public ItemElementiumArmor(EntityEquipmentSlot type, String name) {
		super(type, name, BotaniaAPI.elementiumArmorMaterial);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public ModelBiped provideArmorModelForSlot(ItemStack stack, EntityEquipmentSlot slot) {
		models.put(slot, new ModelArmorElementium(slot));
		return models.get(slot);
	}

	@Override
	public String getArmorTextureAfterInk(ItemStack stack, EntityEquipmentSlot slot) {
		return ConfigHandler.enableArmorModels ? LibResources.MODEL_ELEMENTIUM_NEW : slot == EntityEquipmentSlot.LEGS ? LibResources.MODEL_ELEMENTIUM_1 : LibResources.MODEL_ELEMENTIUM_0;
	}

	@Override
	public boolean getIsRepairable(ItemStack par1ItemStack, ItemStack par2ItemStack) {
		return par2ItemStack.getItem() == ModItems.manaResource && par2ItemStack.getItemDamage() == 7 ? true : super.getIsRepairable(par1ItemStack, par2ItemStack);
	}

	static ItemStack[] armorset;

	@Override
	public ItemStack[] getArmorSetStacks() {
		if(armorset == null)
			armorset = new ItemStack[] {
				new ItemStack(ModItems.elementiumHelm),
				new ItemStack(ModItems.elementiumChest),
				new ItemStack(ModItems.elementiumLegs),
				new ItemStack(ModItems.elementiumBoots)
		};

		return armorset;
	}

	@Override
	public boolean hasArmorSetItem(EntityPlayer player, int i) {
		ItemStack stack = player.inventory.armorInventory[3 - i];
		if(stack == null)
			return false;

		switch(i) {
		case 0: return stack.getItem() == ModItems.elementiumHelm || stack.getItem() == ModItems.elementiumHelmRevealing;
		case 1: return stack.getItem() == ModItems.elementiumChest;
		case 2: return stack.getItem() == ModItems.elementiumLegs;
		case 3: return stack.getItem() == ModItems.elementiumBoots;
		}

		return false;
	}

	@Override
	public String getArmorSetName() {
		return I18n.translateToLocal("botania.armorset.elementium.name");
	}

	@Override
	public void addArmorSetDescription(ItemStack stack, List<String> list) {
		super.addArmorSetDescription(stack, list);
		addStringToTooltip(I18n.translateToLocal("botania.armorset.elementium.desc"), list);
	}

}
