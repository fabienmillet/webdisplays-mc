package net.montoyo.wd.client.gui;

import net.minecraft.client.OptionInstance;
import net.minecraft.client.Options;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.montoyo.wd.config.ClientConfig;

/**
 * "WebDisplays Screens" slider of the Music & Sounds menu (added by SoundOptionsScreenMixin).
 * Screen audio is played by Chromium, outside of Minecraft's sound engine, so it can't have a
 * real SoundSource; this edits audio_max_volume_percent instead, which scales every screen on
 * top of the Master slider. Applied live while dragging, saved when the menu closes.
 */
public class ScreenVolumeOption {
	private static boolean dirty = false;

	public static OptionInstance<Double> create() {
		return new OptionInstance<>(
				"soundCategory.webdisplays.screens",
				OptionInstance.noTooltip(),
				(label, value) -> value == 0.0
						? Options.genericValueLabel(label, CommonComponents.OPTION_OFF)
						: Component.translatable("options.percent_value", label, (int) Math.round(value * 100.0)),
				OptionInstance.UnitDouble.INSTANCE,
				ClientConfig.audioMaxVolumePercent / 100.0,
				value -> {
					ClientConfig.setScreenVolume(value * 100.0, false);
					dirty = true;
				});
	}

	public static void saveIfChanged() {
		if (dirty) {
			dirty = false;
			ClientConfig.setScreenVolume(ClientConfig.audioMaxVolumePercent, true);
		}
	}
}
