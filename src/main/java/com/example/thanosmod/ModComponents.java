package com.example.thanosmod;

import com.mojang.serialization.Codec;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.codec.ByteBufCodecs;

public final class ModComponents {
    /** Индекс выбранного камня в перчатке. */
    public static final DataComponentType<Integer> STONE = Registry.register(
            BuiltInRegistries.DATA_COMPONENT_TYPE,
            ThanosMod.id("stone"),
            DataComponentType.<Integer>builder()
                    .persistent(Codec.INT)
                    .networkSynchronized(ByteBufCodecs.VAR_INT)
                    .build());

    public static void init() {
    }

    private ModComponents() {
    }
}
