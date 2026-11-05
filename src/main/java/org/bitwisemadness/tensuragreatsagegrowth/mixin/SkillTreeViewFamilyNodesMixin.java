package org.bitwisemadness.tensuragreatsagegrowth.mixin;

import dev.dedek.greatsage.SageNode;
import dev.dedek.greatsage.client.Paint;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import org.bitwisemadness.tensuragreatsagegrowth.SageFamily;
import org.bitwisemadness.tensuragreatsagegrowth.TensuraGreatSageGrowth;
import org.bitwisemadness.tensuragreatsagegrowth.client.FamilyTreeLayout;
import org.bitwisemadness.tensuragreatsagegrowth.net.ClientFamilyUnlocks;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * The three family upgrades as proper nodes inside the addon's growth tree
 * (owner ruling on issue #20: parallel to the Resistance/Extra/Unique rank
 * column).
 *
 * <p>Placement comes from {@link FamilyTreeLayout}: the same three rows as the
 * rank nodes (y = -2/-4/-6) in the adjacent column at x=2, mapped with the
 * addon's own formula from {@code place()} -
 * {@code origin + raw * (34 * zoom)} - so pan/zoom/fit track exactly. Drawing
 * happens at {@code render}'s RETURN: that method keeps the pose at identity
 * and has already disabled its view scissor, so plain coordinates work. Clicks
 * inject into {@code click} HEAD and mirror its own tolerance
 * ({@code radius() + 3 * zoom}); a hit issues the server purchase command and
 * returns {@code null}, which both screens treat as "nothing selected".</p>
 *
 * <p>State colours are {@code SkillTreeView}'s own constants (values inlined as
 * literals because javac rejects uninitialised shadowed static finals), and the
 * ring goes through the addon's {@code Paint.ring} so it matches the tree's
 * rendering in both shader modes.</p>
 */
@Mixin(targets = "dev.dedek.greatsage.client.SkillTreeView")
public class SkillTreeViewFamilyNodesMixin {

    /** SkillTreeView.ACQUIRED - the teal "owned" tone. */
    private static final int ACQUIRED_COLOR = 14679276;
    /** SkillTreeView.READY - the green "available" tone. */
    private static final int READY_COLOR = 9431488;
    /** SkillTreeView.GOLD - owned emphasis. */
    private static final int GOLD_COLOR = 16765562;

    /** One-shot first-frame log; replaces the removed corner strip diagnostics (#20). */
    private static volatile boolean firstFrameLogged;

    @Shadow
    private float zoom;

    @Shadow
    private float originX() {
        return 0.0F;
    }

    @Shadow
    private float originY() {
        return 0.0F;
    }

    @Shadow
    private float radius() {
        return 0.0F;
    }

    @Shadow
    private void segment(GuiGraphics gui, float x1, float y1, float x2, float y2, int color, float alpha) {
    }

    @Inject(
            method = "render(Lnet/minecraft/client/gui/GuiGraphics;Lnet/minecraft/client/gui/Font;FFFFFDD)V",
            at = @At("RETURN"))
    private void greatsagegrowth$renderFamilyNodes(GuiGraphics gui, Font font, float p1, float p2, float p3,
            float p4, float p5, double mouseX, double mouseY, CallbackInfo ci) {
        float ox = originX();
        float oy = originY();
        float r = radius();
        float[] cx = new float[SageFamily.values().length];
        float[] cy = new float[SageFamily.values().length];
        for (SageFamily family : SageFamily.values()) {
            int i = family.ordinal();
            cx[i] = FamilyTreeLayout.screen(ox, FamilyTreeLayout.rawX(family), zoom);
            cy[i] = FamilyTreeLayout.screen(oy, FamilyTreeLayout.rawY(family), zoom);
        }
        if (!firstFrameLogged) {
            firstFrameLogged = true;
            TensuraGreatSageGrowth.LOGGER.info(
                    "growth tree nodes first frame: origin=({},{}) zoom={} aspectual=({},{})",
                    (int) ox, (int) oy, zoom, (int) cx[0], (int) cy[0]);
        }
        // chain first so the nodes sit on top of their thread
        for (int i = 0; i + 1 < cx.length; i++) {
            segment(gui, cx[i], cy[i], cx[i + 1], cy[i + 1], ACQUIRED_COLOR, 0.75F);
        }
        for (SageFamily family : SageFamily.values()) {
            int i = family.ordinal();
            boolean owned = ClientFamilyUnlocks.isUnlocked(family);
            int tone = owned ? GOLD_COLOR : READY_COLOR;
            Paint.ring(gui, cx[i], cy[i], r, tone, 1.0F);
            String code = shortCode(family);
            gui.drawString(font, code, (int) (cx[i] - (font.width(code) / 2.0F)),
                    (int) (cy[i] - 4), 0xFFE8E8E8, false);
            String name = family.displayName();
            gui.drawString(font, name, (int) (cx[i] - (font.width(name) / 2.0F)),
                    (int) (cy[i] + r + 3), tone | 0xFF000000, false);
            if (!owned) {
                String cost = "Lv " + family.minLevel() + " + 1000 XP";
                gui.drawString(font, cost, (int) (cx[i] - (font.width(cost) / 2.0F)),
                        (int) (cy[i] + r + 13), 0xFF9A9A9A, false);
            }
        }
    }

    @Inject(method = "click(DDFFF)Ldev/dedek/greatsage/SageNode;", at = @At("HEAD"), cancellable = true)
    private void greatsagegrowth$familyNodeClick(double mouseX, double mouseY, float p1, float p2, float p3,
            CallbackInfoReturnable<SageNode> cir) {
        double tolerance = radius() + (3.0F * zoom);
        double tolSq = tolerance * tolerance;
        for (SageFamily family : SageFamily.values()) {
            double dx = mouseX - FamilyTreeLayout.screen(originX(), FamilyTreeLayout.rawX(family), zoom);
            double dy = mouseY - FamilyTreeLayout.screen(originY(), FamilyTreeLayout.rawY(family), zoom);
            if ((dx * dx) + (dy * dy) <= tolSq) {
                boolean owned = ClientFamilyUnlocks.isUnlocked(family);
                TensuraGreatSageGrowth.LOGGER.info("growth tree node hit: {} -> {}", family,
                        owned ? "OWNED" : "BUYABLE");
                if (!owned) {
                    Minecraft minecraft = Minecraft.getInstance();
                    if (minecraft.player != null && minecraft.player.connection != null) {
                        minecraft.player.connection.sendCommand(
                                "sagegrowth unlock " + family.tensuraPackage());
                    }
                }
                cir.setReturnValue(null);
                return;
            }
        }
    }

    /** Two-letter in-ring code: As / Sp / Su (distinct, matching ring size). */
    private static String shortCode(SageFamily family) {
        String pkg = family.tensuraPackage();
        return pkg.substring(0, 1).toUpperCase(java.util.Locale.ROOT) + pkg.substring(1, 2);
    }
}
