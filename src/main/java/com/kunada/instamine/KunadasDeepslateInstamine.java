package com.kunada.instamine;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.player.AttackBlockCallback;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.Identifier;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.tags.ItemTags;

public class KunadasDeepslateInstamine implements ModInitializer {

    // Using Identifier.parse which is the standard for 1.21.11 mappings
    private static final ResourceKey<Enchantment> EFFICIENCY_KEY =
            ResourceKey.create(Registries.ENCHANTMENT, Identifier.parse("minecraft:efficiency"));

    @Override
    public void onInitialize() {
        AttackBlockCallback.EVENT.register((player, level, hand, pos, direction) -> {
            if (level.isClientSide()) return InteractionResult.PASS;

            if (canInstamine(player, level, pos)) {
                level.destroyBlock(pos, true, player);
                return InteractionResult.SUCCESS;
            }

            return InteractionResult.PASS;
        });
    }

    private boolean canInstamine(Player player, Level level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        ItemStack stack = player.getMainHandItem();

        if (!state.is(Blocks.DEEPSLATE)) return false;
        if (!stack.is(ItemTags.PICKAXES)) return false;

        var enchantmentRegistry = level.registryAccess().lookupOrThrow(Registries.ENCHANTMENT);
        var efficiencyEntry = enchantmentRegistry.getOrThrow(EFFICIENCY_KEY);

        int efficiencyLevel = EnchantmentHelper.getItemEnchantmentLevel(efficiencyEntry, stack);
        if (efficiencyLevel < 5) return false;

        if (!player.hasEffect(MobEffects.HASTE)) return false;
        if (player.getEffect(MobEffects.HASTE).getAmplifier() < 1) return false;

        return true;
    }
}