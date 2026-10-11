package com.example.thanosmod;

import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Relative;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import java.util.List;
import java.util.Set;

/**
 * Перчатка Бесконечности.
 *  Shift + ПКМ — переключить камень.
 *  ПКМ         — применить способность выбранного камня.
 */
public class InfinityGauntletItem extends Item {

    public enum Stone {
        SPACE("space", ChatFormatting.BLUE, 0x3377FF, 40),
        MIND("mind", ChatFormatting.YELLOW, 0xFFE62E, 200),
        REALITY("reality", ChatFormatting.RED, 0xFF2222, 400),
        POWER("power", ChatFormatting.DARK_PURPLE, 0xAA33FF, 160),
        TIME("time", ChatFormatting.GREEN, 0x33FF66, 400),
        SOUL("soul", ChatFormatting.GOLD, 0xFF9922, 300),
        SNAP("snap", ChatFormatting.WHITE, 0xFFFFFF, 1200);

        public final String key;
        public final ChatFormatting format;
        public final int color;
        public final int cooldown;

        Stone(String key, ChatFormatting format, int color, int cooldown) {
            this.key = key;
            this.format = format;
            this.color = color;
            this.cooldown = cooldown;
        }

        public MutableComponent display() {
            return Component.translatable("stone.thanosmod." + key).withStyle(format);
        }
    }

    public InfinityGauntletItem(Properties properties) {
        super(properties);
    }

    private static Stone selected(ItemStack stack) {
        int idx = stack.getOrDefault(ModComponents.STONE, 0);
        Stone[] all = Stone.values();
        return all[Math.floorMod(idx, all.length)];
    }

    /** В названии предмета показываем выбранный камень. */
    @Override
    public Component getName(ItemStack stack) {
        return super.getName(stack).copy()
                .append(Component.literal(" [").withStyle(ChatFormatting.GRAY))
                .append(selected(stack).display())
                .append(Component.literal("]").withStyle(ChatFormatting.GRAY));
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        // Переключение камня
        if (player.isShiftKeyDown()) {
            int next = (stack.getOrDefault(ModComponents.STONE, 0) + 1) % Stone.values().length;
            stack.set(ModComponents.STONE, next);
            actionBar(player, Component.translatable("message.thanosmod.selected", Stone.values()[next].display()));
            return InteractionResult.SUCCESS;
        }

        if (level instanceof ServerLevel serverLevel) {
            Stone stone = selected(stack);
            if (activate(serverLevel, player, stone)) {
                player.getCooldowns().addCooldown(stack, stone.cooldown);
                serverLevel.playSound(null, player.blockPosition(), SoundEvents.BEACON_ACTIVATE,
                        SoundSource.PLAYERS, 1.0f, 1.4f);
                burst(serverLevel, player, stone.color);
            }
        }
        return InteractionResult.SUCCESS;
    }

    private static boolean activate(ServerLevel level, Player player, Stone stone) {
        switch (stone) {
            case SPACE:
                return spaceStone(level, player);
            case MIND:
                for (LivingEntity e : nearby(level, player, 24)) {
                    e.addEffect(new MobEffectInstance(MobEffects.GLOWING, 400, 0));
                    e.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 300, 1));
                }
                return true;
            case REALITY:
                player.addEffect(new MobEffectInstance(MobEffects.RESISTANCE, 600, 1));
                player.addEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE, 1200, 0));
                player.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, 1200, 2));
                return true;
            case POWER:
                for (LivingEntity e : nearby(level, player, 8)) {
                    e.hurtServer(level, level.damageSources().playerAttack(player), 20.0f);
                    Vec3 away = new Vec3(e.getX() - player.getX(), 0, e.getZ() - player.getZ()).normalize().scale(1.8);
                    e.push(away.x, 0.6, away.z);
                }
                return true;
            case TIME:
                player.addEffect(new MobEffectInstance(MobEffects.SPEED, 600, 2));
                player.addEffect(new MobEffectInstance(MobEffects.HASTE, 600, 2));
                for (LivingEntity e : nearby(level, player, 16)) {
                    e.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 200, 6));
                }
                return true;
            case SOUL:
                player.heal(player.getMaxHealth());
                player.getFoodData().eat(20, 1.0f);
                player.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 200, 2));
                return true;
            case SNAP:
                return snap(level, player);
        }
        return false;
    }

    private static List<LivingEntity> nearby(ServerLevel level, Player player, double radius) {
        AABB box = player.getBoundingBox().inflate(radius);
        return level.getEntitiesOfClass(LivingEntity.class, box, e -> e != player && e.isAlive());
    }

    private static boolean spaceStone(ServerLevel level, Player player) {
        Vec3 eye = player.getEyePosition();
        Vec3 look = player.getLookAngle();
        Vec3 end = eye.add(look.scale(40));
        BlockHitResult hit = level.clip(new ClipContext(eye, end,
                ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, player));
        Vec3 target = hit.getType() == HitResult.Type.MISS
                ? end
                : hit.getLocation().subtract(look.scale(0.6));
        burst(level, player, Stone.SPACE.color);
        if (!(player instanceof ServerPlayer sp)) return false;
        boolean ok = sp.teleportTo(level, target.x, target.y - player.getEyeHeight(), target.z,
                Set.<Relative>of(), sp.getYRot(), sp.getXRot(), false);
        if (ok) sp.resetFallDistance();
        return ok;
    }

    /** «Щелчок»: случайная половина враждебных мобов вокруг исчезает. Игроков и мирных мобов не трогает. */
    private static boolean snap(ServerLevel level, Player player) {
        AABB box = player.getBoundingBox().inflate(48);
        List<Monster> monsters = level.getEntitiesOfClass(Monster.class, box, Monster::isAlive);
        int removed = 0;
        for (Monster m : monsters) {
            if (player.getRandom().nextBoolean()) {
                level.sendParticles(new DustParticleOptions(0x999999, 1.5f),
                        m.getX(), m.getY() + m.getBbHeight() / 2, m.getZ(), 30, 0.4, 0.6, 0.4, 0.05);
                m.discard();
                removed++;
            }
        }
        actionBar(player, Component.translatable("message.thanosmod.snap", removed));
        return true;
    }

    private static void actionBar(Player player, Component text) {
        if (player instanceof ServerPlayer sp) {
            sp.sendSystemMessage(text, true);
        }
    }

    private static void burst(ServerLevel level, Player player, int color) {
        level.sendParticles(new DustParticleOptions(color, 2.0f),
                player.getX(), player.getY() + 1.0, player.getZ(), 60, 1.0, 1.0, 1.0, 0.1);
    }
}
