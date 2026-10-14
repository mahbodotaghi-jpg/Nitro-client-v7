package dev.nitro.module.impl;

import dev.nitro.module.Module;
import dev.nitro.setting.NumSetting;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.RespawnAnchorBlock;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.item.Items;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import org.lwjgl.glfw.GLFW;

/**
 * Hold the macro key (default G, rebindable in Controls) while looking at a respawn anchor:
 * it switches to glowstone and charges it, then switches to your "explode slot" and detonates it.
 */
public class AnchorMacro extends Module {
	private final KeyBinding macroKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
		"key.nitro.anchor", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_G, "category.nitro"));

	private final NumSetting chargeDelay = add(new NumSetting("Charge delay (ticks)", 1, 0, 10, true));
	private final NumSetting explodeDelay = add(new NumSetting("Switch delay (ticks)", 2, 0, 10, true));
	private final NumSetting explodeSlot = add(new NumSetting("Explode slot", 9, 1, 9, true));
	private int cooldown;

	public AnchorMacro() {
		super("Anchor macro", "Hold G on an anchor: charge, switch, explode.", Category.COMBAT);
	}

	@Override
	public void onTick() {
		if (mc.player == null || mc.world == null || mc.interactionManager == null || mc.currentScreen != null) return;
		if (!macroKey.isPressed()) { cooldown = 0; return; }
		if (cooldown > 0) { cooldown--; return; }
		if (!(mc.crosshairTarget instanceof BlockHitResult hit) || hit.getType() != HitResult.Type.BLOCK) return;

		BlockState state = mc.world.getBlockState(hit.getBlockPos());
		if (!state.isOf(Blocks.RESPAWN_ANCHOR)) return;

		int charges = state.get(RespawnAnchorBlock.CHARGES);
		if (charges == 0) {
			int slot = findGlowstone();
			if (slot == -1) return;
			mc.player.getInventory().selectedSlot = slot;
			use(hit);
			cooldown = chargeDelay.i();
		} else {
			mc.player.getInventory().selectedSlot = explodeSlot.i() - 1;
			use(hit);
			cooldown = explodeDelay.i();
		}
	}

	private int findGlowstone() {
		for (int i = 0; i < 9; i++) if (mc.player.getInventory().getStack(i).isOf(Items.GLOWSTONE)) return i;
		return -1;
	}

	private void use(BlockHitResult hit) {
		mc.interactionManager.interactBlock(mc.player, Hand.MAIN_HAND, hit);
		mc.player.swingHand(Hand.MAIN_HAND);
	}
}
