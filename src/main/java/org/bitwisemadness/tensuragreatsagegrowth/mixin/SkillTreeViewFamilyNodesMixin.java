package org.bitwisemadness.tensuragreatsagegrowth.mixin;

import dev.dedek.greatsage.SageNode;
import dev.dedek.greatsage.client.Paint;
import dev.dedek.greatsage.client.SageProgressClient;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import org.bitwisemadness.tensuragreatsagegrowth.FamilyUnlocks;
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
 * (owner rulings on issue #20: parallel to the rank column, starting from the
 * Resistance node, base-mod style, tooltip-based info - and, from the
 * 13:29-13:32 playtest, <b>never drawn over the base mod's tooltips</b>).
 *
 * <p><b>Two injection points enforce the z-order</b>: the nodes and their
 * thread draw right <b>after the view scissor is released</b> (once per frame,
 * before the base's own tooltip pass), and only the <b>hover tooltip</b> draws
 * at {@code RETURN} - so base tooltips always cover our nodes, and our tooltip
 * draws last like theirs.</p>
 *
 * <p>The node recipe is a faithful port of the decompiled
 * {@code SkillTreeView.node()}/{@code tooltip()}: dark fill, additive glow and
 * ring in the state colours (owned -> ACQUIRED, ready -> READY, gated ->
 * LOCKED), concentric inner ring (radius - 2.5) on every node, glyph at {@code 0.75 * zoom}; tooltips carry
 * title, wrapped description, a compact cost line and the base's own
 * "Needs level" phrasing ({@code greatsage.tree.needLevel}).</p>
 */
@Mixin(targets = "dev.dedek.greatsage.client.SkillTreeView")
public class SkillTreeViewFamilyNodesMixin {

    /** SkillTreeView colour constants (inlined - javac rejects uninitialised shadowed finals). */
    private static final int ACQUIRED_COLOR = 14679276;
    private static final int READY_COLOR = 9431488;
    private static final int LOCKED_COLOR = 6256232;
    private static final int NODE_FILL = 465426;
    private static final int TOOLTIP_BG = 267278;
    private static final int TOOLTIP_BODY = 10469295;

    /** One-shot first-frame log (#17-style diagnostics for the tree nodes). */
    private static volatile boolean firstFrameLogged;

    @Shadow
    private float zoom;

    @Shadow
    private float viewLeft;

    @Shadow
    private float viewTop;

    @Shadow
    private float viewWidth;

    @Shadow
    private float viewHeight;

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

    private float[] greatsagegrowth$pos(int index) {
        SageFamily family = SageFamily.values()[index];
        return new float[] {
                FamilyTreeLayout.screen(originX(), FamilyTreeLayout.rawX(family), zoom),
                FamilyTreeLayout.screen(originY(), FamilyTreeLayout.rawY(family), zoom)
        };
    }

    /** Nodes + thread: after the view scissor release, before the base tooltip pass. */
    @Inject(
            method = "render(Lnet/minecraft/client/gui/GuiGraphics;Lnet/minecraft/client/gui/Font;FFFFFDD)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/GuiGraphics;disableScissor()V",
                    shift = At.Shift.AFTER))
    private void greatsagegrowth$renderFamilyNodes(GuiGraphics gui, Font font, float p1, float p2, float p3,
            float p4, float p5, double mouseX, double mouseY, CallbackInfo ci) {
        if (!firstFrameLogged) {
            firstFrameLogged = true;
            float[] first = greatsagegrowth$pos(0);
            TensuraGreatSageGrowth.LOGGER.info(
                    "growth tree nodes first frame: origin=({},{}) zoom={} aspectual=({},{})",
                    (int) originX(), (int) originY(), zoom, (int) first[0], (int) first[1]);
        }
        // the branch starts at the Resistance node (ruling on #20)
        float[] anchor = {
                FamilyTreeLayout.screen(originX(), FamilyTreeLayout.ANCHOR_X, zoom),
                FamilyTreeLayout.screen(originY(), FamilyTreeLayout.ANCHOR_Y, zoom)
        };
        float[] firstNode = greatsagegrowth$pos(0);
        segment(gui, anchor[0], anchor[1], firstNode[0], firstNode[1], ACQUIRED_COLOR, 0.75F);
        for (int i = 0; i + 1 < SageFamily.values().length; i++) {
            float[] a = greatsagegrowth$pos(i);
            float[] b = greatsagegrowth$pos(i + 1);
            segment(gui, a[0], a[1], b[0], b[1], ACQUIRED_COLOR, 0.75F);
        }
        for (int i = 0; i < SageFamily.values().length; i++) {
            float[] p = greatsagegrowth$pos(i);
            drawNode(gui, font, SageFamily.values()[i], p[0], p[1], radius(), zoom);
        }
    }

    /** The hover tooltip only: at RETURN it lands after the base's own tooltip. */
    @Inject(
            method = "render(Lnet/minecraft/client/gui/GuiGraphics;Lnet/minecraft/client/gui/Font;FFFFFDD)V",
            at = @At("RETURN"))
    private void greatsagegrowth$renderFamilyTooltip(GuiGraphics gui, Font font, float p1, float p2, float p3,
            float p4, float p5, double mouseX, double mouseY, CallbackInfo ci) {
        double hover = radius() + (8.0F * zoom);
        for (SageFamily family : SageFamily.values()) {
            float[] p = greatsagegrowth$pos(family.ordinal());
            double dx = mouseX - p[0];
            double dy = mouseY - p[1];
            if ((dx * dx) + (dy * dy) <= hover * hover) {
                tooltip(gui, font, family, (float) mouseX, (float) mouseY);
                return;
            }
        }
    }

    /** The base mod's node recipe: fill, additive glow, ring(s), glyph. */
    private void drawNode(GuiGraphics gui, Font font, SageFamily family, float cx, float cy, float r, float zoom) {
        boolean owned = ClientFamilyUnlocks.isUnlocked(family);
        boolean ready = !owned && SageProgressClient.level() >= family.minLevel()
                && SageProgressClient.xp() >= FamilyUnlocks.XP_COST;
        int tone = owned ? ACQUIRED_COLOR : ready ? READY_COLOR : LOCKED_COLOR;

        gui.fill((int) Math.round(cx - r), (int) Math.round(cy - r),
                (int) Math.round(cx + r), (int) Math.round(cy + r), (240 << 24) | NODE_FILL);

        Paint.beginAdditive();
        float glow = owned ? 0.8F : ready ? 0.55F : 0.22F;
        Paint.glow(gui, cx, cy, r * 2.6F, tone, glow * 0.6F);
        Paint.ring(gui, cx, cy, r, tone, owned ? 0.95F : 0.6F);
        // concentric inner ring: the old y-2.5 offset made the two circles
        // intersect (playtest 13:29-13:32); the base pairs radius with radius-2.5.
        Paint.ring(gui, cx, cy, Math.max(r - 2.5F, 1.5F), tone, owned ? 0.7F : 0.45F);
        Paint.endAdditive(gui);

        String glyph = Component.translatable("greatsagegrowth.tree." + family.tensuraPackage() + ".glyph")
                .getString();
        var pose = gui.pose();
        pose.pushPose();
        pose.translate(cx, cy, 0.0F);
        float scale = 0.75F * zoom;
        pose.scale(scale, scale, 1.0F);
        int glyphColor = (255 << 24) | (owned ? 0xFFFFFF : tone);
        gui.drawString(font, glyph, -font.width(glyph) / 2, -4, glyphColor, false);
        pose.popPose();
    }

    /** The base mod's tooltip: title, wrapped description, cost, base's need-level line. */
    private void tooltip(GuiGraphics gui, Font font, SageFamily family, float mouseX, float mouseY) {
        boolean owned = ClientFamilyUnlocks.isUnlocked(family);
        List<String> lines = new ArrayList<>();
        lines.add(family.displayName());
        wrap(lines, font, Component
                .translatable("greatsagegrowth.tree." + family.tensuraPackage() + ".desc").getString());
        if (owned) {
            lines.add(Component.translatable("greatsagegrowth.unlocked", family.displayName()).getString());
        } else {
            lines.add(Component.translatable("greatsagegrowth.tree.cost", FamilyUnlocks.XP_COST).getString());
            if (SageProgressClient.level() < family.minLevel()) {
                lines.add(Component.translatable("greatsage.tree.needLevel", family.minLevel()).getString());
            }
        }

        int width = 0;
        for (String line : lines) {
            width = Math.max(width, (int) Math.round(font.width(line) * 0.6F));
        }
        width += 10;
        int height = (lines.size() * 8) + 7;
        float x = mouseX + 12.0F;
        if (x + width > viewLeft + viewWidth) {
            x = mouseX - 12.0F - width;
        }
        float y = Math.min(mouseY + 6.0F, viewTop + viewHeight - height - 2.0F);

        gui.fill(Math.round(x), Math.round(y), Math.round(x) + width, Math.round(y) + height,
                (242 << 24) | TOOLTIP_BG);
        int border = (178 << 24) | READY_COLOR;
        gui.fill(Math.round(x), Math.round(y), Math.round(x) + width, Math.round(y) + 1, border);
        gui.fill(Math.round(x), Math.round(y) + height - 1, Math.round(x) + width, Math.round(y) + height, border);

        var pose = gui.pose();
        for (int i = 0; i < lines.size(); i++) {
            pose.pushPose();
            pose.translate(x + 5.0F, y + 4.0F + (i * 8.0F), 0.0F);
            pose.scale(0.6F, 0.6F, 1.0F);
            int color = (255 << 24) | (i == 0 ? ACQUIRED_COLOR : TOOLTIP_BODY);
            gui.drawString(font, lines.get(i), 0, 0, color, false);
            pose.popPose();
        }
    }

    /** Greedy wrap to the addon's tooltip width (WRAP = 250). */
    private void wrap(List<String> out, Font font, String text) {
        int wrapAt = 250;
        StringBuilder current = new StringBuilder();
        for (String word : text.split(" ")) {
            String candidate = current.isEmpty() ? word : current + " " + word;
            if (font.width(candidate) > wrapAt && current.length() > 0) {
                out.add(current.toString());
                current = new StringBuilder(word);
            } else {
                current = new StringBuilder(candidate);
            }
        }
        if (current.length() > 0) {
            out.add(current.toString());
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
}
