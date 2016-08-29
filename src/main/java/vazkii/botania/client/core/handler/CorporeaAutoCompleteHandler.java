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

import com.google.common.util.concurrent.ListenableFutureTask;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiChat;
import net.minecraft.client.gui.GuiTextField;
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
import java.util.Arrays;
import java.util.List;
import java.util.Queue;
import java.util.TreeSet;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;
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
				ReflectionHelper.setPrivateValue(GuiChat.class, gui, new CorporeaAutoCompleteHandler((GuiChat.ChatTabCompleter) completer), LibObfuscation.TAB_COMPLETER);
			} else {
				Botania.LOGGER.warn("Couldn't add Corporea Autocomplete to chat GUI");
			}
		}
	}

	private String lastAutoCompletePrefix = "";

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
			Botania.LOGGER.info("Entered completions cycle");
			showText();
			this.textField.deleteFromCursor(0);
			Botania.LOGGER.info("Deleting last prefix with size " + lastAutoCompletePrefix.length());
			this.textField.deleteFromCursor(-lastAutoCompletePrefix.length() /*- this.textField.getCursorPosition()*/); // Botania - Item names have spaces, so we can't just delete the last word
			showText();
			if (this.completionIdx >= this.completions.size())
			{
				this.completionIdx = 0;
			}
		}
		else
		{
			Botania.LOGGER.info("Entered completions rebuild");
			showText();
			int i = this.textField.getNthWordFromPosWS(-1, this.textField.getCursorPosition(), false);
			this.completions.clear();
			this.completionIdx = 0;
			String s = this.textField.getText().substring(0, this.textField.getCursorPosition());
			lastAutoCompletePrefix = s;         // Botania - save to use above
			this.requestCorporeaCompletions(s); // Botania - Use our own

			if (this.completions.isEmpty())
			{
				Botania.LOGGER.info("Leaving early due to empty completions");
				showText();
				return;
			}

			this.didComplete = true;
			this.textField.deleteFromCursor(i - this.textField.getCursorPosition());
			Botania.LOGGER.info("Leaving completions rebuild");
			showText();
		}

		Botania.LOGGER.info("Final write");
		showText();
		this.textField.writeText(net.minecraft.util.text.TextFormatting.getTextWithoutFormattingCodes(this.completions.get(this.completionIdx++)));
		showText();
	}

	// Copy of super.requestCompletions. Edits noted
	private void requestCorporeaCompletions(String prefix) {
		if (prefix.length() >= 1)
		{
			net.minecraftforge.client.ClientCommandHandler.instance.autoComplete(prefix);
			String[] completes = buildAutoCompletes(prefix);
			Botania.LOGGER.info("Built completions: {}", Arrays.toString(completes));
			runNextClientTick(() -> setCompletions(completes)); // Botania - complete corporea
			this.requestedCompletions = true;
		}
	}

	// Simulate a ping-pong retrieving completions from the server
	private void runNextClientTick(Runnable r) {
		Queue<FutureTask> q = ReflectionHelper.getPrivateValue(Minecraft.class, Minecraft.getMinecraft(), LibObfuscation.SCHEDULED_TASKS);
		synchronized (q) {
			q.add(ListenableFutureTask.create(Executors.callable(r)));
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
		return result.toArray(new String[0]);
	}

	private List<String> getNamesStartingWith(String prefix) {
		return itemNames.tailSet(prefix).stream()
				.filter(s -> s.toLowerCase().startsWith(prefix))
				.collect(Collectors.toList());
	}

	private void showText() {
		String s = ReflectionHelper.getPrivateValue(GuiTextField.class, textField, LibObfuscation.TEXT);
		Botania.LOGGER.info("Current text in field: {}", s);
	}

}
