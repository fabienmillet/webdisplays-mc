package net.montoyo.wd.config.annoconfg;

import net.montoyo.wd.config.annoconfg.handle.UnsafeHandle;
import net.neoforged.neoforge.common.ModConfigSpec;

import java.util.function.Supplier;

public class ConfigEntry {
	UnsafeHandle handle;
	Supplier<?> supplier;
	// the spec value itself, when writable (null for converted values such as String[])
	ModConfigSpec.ConfigValue<?> value;
	
	public ConfigEntry(UnsafeHandle handle, Supplier<?> supplier) {
		this.handle = handle;
		this.supplier = supplier;
	}
}
