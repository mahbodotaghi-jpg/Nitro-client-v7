package dev.nitro.module.impl;

import dev.nitro.module.Module;
import net.fabricmc.fabric.api.event.player.AttackEntityCallback;
import net.minecraft.entity.Entity;
import net.minecraft.entity.decoration.EndCrystalEntity;
import net.minecraft.util.ActionResult;

/**
 * Removes an end crystal on your client the instant you hit it, instead of waiting for the
 * server's response, so you can place the next one immediately.
 */
public class CrystalOptimizer extends Module {
	public CrystalOptimizer() {
		super("Crystal optimizer", "Instantly removes crystals you hit (client side).", Category.COMBAT);
		AttackEntityCallback.EVENT.register((player, world, hand, entity, hit) -> {
			if (enabled && world.isClient && entity instanceof EndCrystalEntity) {
				entity.setRemoved(Entity.RemovalReason.KILLED);
				entity.onRemoved();
			}
			return ActionResult.PASS;
		});
	}
}
