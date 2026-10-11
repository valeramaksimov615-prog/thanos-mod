package com.example.thanosmod;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EquipmentSlot;

public class ThanosMod implements ModInitializer {
    public static final String MOD_ID = "thanosmod";

    public static Identifier id(String path) {
        return Identifier.fromNamespaceAndPath(MOD_ID, path);
    }

    @Override
    public void onInitialize() {
        ModComponents.init();
        ModItems.init();
        ServerTickEvents.END_SERVER_TICK.register(ThanosMod::armorSetBonus);
    }

    /** Полный сет брони Таноса даёт Силу II и Сопротивление урону I. */
    private static void armorSetBonus(MinecraftServer server) {
        if (server.getTickCount() % 20 != 0) return;
        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            if (player.getItemBySlot(EquipmentSlot.HEAD).is(ModItems.THANOS_HELMET)
                    && player.getItemBySlot(EquipmentSlot.CHEST).is(ModItems.THANOS_CHESTPLATE)
                    && player.getItemBySlot(EquipmentSlot.LEGS).is(ModItems.THANOS_LEGGINGS)
                    && player.getItemBySlot(EquipmentSlot.FEET).is(ModItems.THANOS_BOOTS)) {
                player.addEffect(new MobEffectInstance(MobEffects.STRENGTH, 60, 1, true, false, true));
                player.addEffect(new MobEffectInstance(MobEffects.RESISTANCE, 60, 0, true, false, true));
            }
        }
    }
}
