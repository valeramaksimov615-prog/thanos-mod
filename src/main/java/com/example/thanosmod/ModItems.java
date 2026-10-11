package com.example.thanosmod;

import net.fabricmc.fabric.api.creativetab.v1.CreativeModeTabEvents;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.ToolMaterial;
import net.minecraft.world.item.equipment.ArmorMaterial;
import net.minecraft.world.item.equipment.ArmorType;
import net.minecraft.world.item.equipment.EquipmentAsset;
import net.minecraft.world.item.equipment.EquipmentAssets;

import java.util.EnumMap;
import java.util.function.Function;

public final class ModItems {

    // ---------- Материалы ----------
    public static final ResourceKey<EquipmentAsset> THANOS_ASSET =
            ResourceKey.create(EquipmentAssets.ROOT_ID, ThanosMod.id("thanos"));

    public static final ArmorMaterial THANOS_ARMOR_MATERIAL = new ArmorMaterial(
            50,
            armorValues(),
            25,
            SoundEvents.ARMOR_EQUIP_NETHERITE,
            4.0f,
            0.25f,
            ItemTags.REPAIRS_NETHERITE_ARMOR,
            THANOS_ASSET);

    public static final ToolMaterial THANOS_TOOL_MATERIAL = new ToolMaterial(
            BlockTags.INCORRECT_FOR_NETHERITE_TOOL,
            3500,
            9.0f,
            6.0f,   // бонус к урону (итого у меча ~ 1 + 6 + 3 = 10)
            20,
            ItemTags.NETHERITE_TOOL_MATERIALS);

    private static EnumMap<ArmorType, Integer> armorValues() {
        EnumMap<ArmorType, Integer> map = new EnumMap<>(ArmorType.class);
        map.put(ArmorType.BOOTS, 4);
        map.put(ArmorType.LEGGINGS, 7);
        map.put(ArmorType.CHESTPLATE, 9);
        map.put(ArmorType.HELMET, 4);
        map.put(ArmorType.BODY, 12);
        return map;
    }

    // ---------- Предметы ----------
    public static final Item INFINITY_GAUNTLET = register("infinity_gauntlet", InfinityGauntletItem::new,
            new Item.Properties().stacksTo(1).rarity(Rarity.EPIC).fireResistant()
                    .component(ModComponents.STONE, 0));

    public static final Item THANOS_HELMET = register("thanos_helmet", Item::new,
            new Item.Properties().humanoidArmor(THANOS_ARMOR_MATERIAL, ArmorType.HELMET)
                    .rarity(Rarity.EPIC).fireResistant());
    public static final Item THANOS_CHESTPLATE = register("thanos_chestplate", Item::new,
            new Item.Properties().humanoidArmor(THANOS_ARMOR_MATERIAL, ArmorType.CHESTPLATE)
                    .rarity(Rarity.EPIC).fireResistant());
    public static final Item THANOS_LEGGINGS = register("thanos_leggings", Item::new,
            new Item.Properties().humanoidArmor(THANOS_ARMOR_MATERIAL, ArmorType.LEGGINGS)
                    .rarity(Rarity.EPIC).fireResistant());
    public static final Item THANOS_BOOTS = register("thanos_boots", Item::new,
            new Item.Properties().humanoidArmor(THANOS_ARMOR_MATERIAL, ArmorType.BOOTS)
                    .rarity(Rarity.EPIC).fireResistant());

    public static final Item DOUBLE_EDGED_SWORD = register("double_edged_sword", Item::new,
            new Item.Properties().sword(THANOS_TOOL_MATERIAL, 3.0f, -2.2f)
                    .rarity(Rarity.EPIC).fireResistant());

    private static Item register(String name, Function<Item.Properties, Item> factory, Item.Properties props) {
        ResourceKey<Item> key = ResourceKey.create(Registries.ITEM, ThanosMod.id(name));
        return Registry.register(BuiltInRegistries.ITEM, key, factory.apply(props.setId(key)));
    }

    public static void init() {
        CreativeModeTabEvents.modifyOutputEvent(CreativeModeTabs.COMBAT).register(entries -> {
            entries.accept(INFINITY_GAUNTLET);
            entries.accept(DOUBLE_EDGED_SWORD);
            entries.accept(THANOS_HELMET);
            entries.accept(THANOS_CHESTPLATE);
            entries.accept(THANOS_LEGGINGS);
            entries.accept(THANOS_BOOTS);
        });
    }

    private ModItems() {
    }
}
