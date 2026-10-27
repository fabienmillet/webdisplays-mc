package net.montoyo.wd.mixins;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.minecraft.client.OptionInstance;
import net.minecraft.client.Options;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.options.OptionsSubScreen;
import net.minecraft.client.gui.screens.options.SoundOptionsScreen;
import net.minecraft.network.chat.Component;
import net.montoyo.wd.client.gui.ScreenVolumeOption;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.util.Arrays;

@Mixin(SoundOptionsScreen.class)
public abstract class SoundOptionsScreenMixin extends OptionsSubScreen {
    public SoundOptionsScreenMixin(Screen lastScreen, Options options, Component title) {
        super(lastScreen, options, title);
    }

    // Appended to the per-category sliders grid (Music, Jukebox/Note Blocks, ...).
    // @ModifyReturnValue chains with other mods doing the same (e.g. Ultimate Car Mod's "Cars");
    // a cancellable @Inject at RETURN would return early and skip their injectors.
    @ModifyReturnValue(method = "getAllSoundOptionsExceptMaster", at = @At("RETURN"))
    private OptionInstance<?>[] webdisplays$addScreenVolume(OptionInstance<?>[] options) {
        OptionInstance<?>[] withScreens = Arrays.copyOf(options, options.length + 1);
        withScreens[options.length] = ScreenVolumeOption.create();
        return withScreens;
    }

    @Override
    public void removed() {
        super.removed();
        ScreenVolumeOption.saveIfChanged();
    }
}
