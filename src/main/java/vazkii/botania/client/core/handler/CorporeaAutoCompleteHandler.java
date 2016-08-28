/**
 * This class was created by <SoundLogic>. It's distributed as
 * part of the Botania Mod. Get the Source Code in github:
 * https://github.com/Vazkii/Botania
 * 
 * Botania is Open Source and distributed under the
 * Botania License: http://botaniamod.net/license.php
 * 
 * File Created @ [June 8, 2015, 12:55:20 AM (GMT)]
 */
package vazkii.botania.client.core.handler;

import net.minecraft.client.gui.GuiChat;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.TabCompleter;
import net.minecraftforge.client.event.GuiOpenEvent;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.ReflectionHelper;
import vazkii.botania.api.corporea.CorporeaHelper;
import vazkii.botania.common.Botania;
import vazkii.botania.common.lib.LibObfuscation;

import java.util.ArrayList;
import java.util.List;
import java.util.TreeSet;
import java.util.stream.Collectors;

public class CorporeaAutoCompleteHandler extends GuiChat.ChatTabCompleter {

	private static final TreeSet<String> itemNames = new TreeSet<>(String::compareToIgnoreCase);

	public static void initItemNames() {
		itemNames.clear();
		List<ItemStack> curList = new ArrayList<>();

		for (Item item : Item.REGISTRY) {
			if(item != null && item.getCreativeTab() != null) {
				curList.clear();
				try {
					item.getSubItems(item, null, curList);
					curList.stream()
							.map(s -> CorporeaHelper.stripControlCodes(s.getDisplayName().trim()))
							.forEach(itemNames::add);
				}
				catch (Exception e) {}
			}
		}
	}

	@SubscribeEvent
	public static void onGuiOpen(GuiScreenEvent.InitGuiEvent.Post evt) {
		if (evt.getGui() instanceof GuiChat) {
			GuiChat gui = ((GuiChat) evt.getGui());
			TabCompleter completer = ReflectionHelper.getPrivateValue(GuiChat.class, gui, LibObfuscation.TAB_COMPLETER);
			if (completer instanceof GuiChat.ChatTabCompleter) {
				Botania.LOGGER.info("Replacing");
				ReflectionHelper.setPrivateValue(GuiChat.class, gui, new CorporeaAutoCompleteHandler((GuiChat.ChatTabCompleter) completer), LibObfuscation.TAB_COMPLETER);
			} else {
				Botania.LOGGER.warn("Couldn't add Corporea Autocomplete to chat GUI");
			}
		}
	}

	private CorporeaAutoCompleteHandler(GuiChat.ChatTabCompleter old) {
		super(ReflectionHelper.getPrivateValue(TabCompleter.class, old, LibObfuscation.TEXT_FIELD));
	}

	// Copy of super, edits noted
	@Override
	public void complete()
	{
		// Botania - Fall back if corporea shouldn't act
		if (!CorporeaHelper.shouldAutoComplete()) {
			super.complete();
			return;
		}

		if (this.didComplete)
		{
			this.textField.deleteFromCursor(0);
			this.textField.deleteFromCursor(this.textField.getNthWordFromPosWS(-1, this.textField.getCursorPosition(), false) - this.textField.getCursorPosition());
			if (this.completionIdx >= this.completions.size())
			{
				this.completionIdx = 0;
			}
		}
		else
		{
			int i = this.textField.getNthWordFromPosWS(-1, this.textField.getCursorPosition(), false);
			this.completions.clear();
			this.completionIdx = 0;
			String s = this.textField.getText().substring(0, this.textField.getCursorPosition());
			this.requestCorporeaCompletions(s); // Botania - Use our own

			if (this.completions.isEmpty())
			{
				return;
			}

			this.didComplete = true;
			this.textField.deleteFromCursor(i - this.textField.getCursorPosition());
		}

		this.textField.writeText(net.minecraft.util.text.TextFormatting.getTextWithoutFormattingCodes(this.completions.get(this.completionIdx++)));
	}

	// Copy of super.requestCompletions. Edits noted
	private void requestCorporeaCompletions(String prefix) {
		if (prefix.length() >= 1)
		{
			net.minecraftforge.client.ClientCommandHandler.instance.autoComplete(prefix);
			this.requestedCompletions = true;           // Botania - move above actual completion because setCompletions needs true
			setCompletions(buildAutoCompletes(prefix)); // Botania - complete corporea
		}
	}

	private String[] buildAutoCompletes(String prefix) {
		if(prefix.isEmpty() || prefix.charAt(0) == '/')
			return new String[0];
		return getNames(prefix);
	}

	private String[] getNames(String prefix) {
		prefix = prefix.trim();
		if(prefix.isEmpty())
			return new String[0];
				
		TreeSet<String> result = new TreeSet<>();
		String[] words = prefix.split(" ");
		int i = words.length - 1;
		String curPrefix = words[i];
		while(i >= 0) {
			result.addAll(getNamesStartingWith(curPrefix.toLowerCase()));
			i--;
			if(i >= 0)
				curPrefix = words[i] + " " + curPrefix;
		}
		Botania.LOGGER.info("Final res: " + result);
		return result.toArray(new String[0]);
	}

	private List<String> getNamesStartingWith(String prefix) {
		Botania.LOGGER.info("Getnamesstartingwith: " + prefix);

		List<String> result = itemNames.tailSet(prefix).stream()
				.filter(s -> s.toLowerCase().startsWith(prefix))
				.collect(Collectors.toList());

		Botania.LOGGER.info("res: " + result);
		return result;
	}

}
