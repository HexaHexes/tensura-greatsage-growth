package org.bitwisemadness.tensuragreatsagegrowth.mixin;

import dev.dedek.greatsage.client.SageConsoleScreen;
import dev.dedek.greatsage.client.SageMenuScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import org.bitwisemadness.tensuragreatsagegrowth.TensuraGreatSageGrowth;
import org.bitwisemadness.tensuragreatsagegrowth.client.FamilyStrip;
import org.bitwisemadness.tensuragreatsagegrowth.client.FamilyStripLayout;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * The mod's growth strip inside the Sage screens (stack PR C, issue #10; the
 * wrong-screen root cause is documented in issue #17): the addon's node tree
 * cannot be extended (its layout maps are keyed by the final {@code SageNode}
 * enum), so the mod renders its own row of family entries - this hook draws it
 * after the screen's own frame and takes the click first, issuing the server
 * purchase command for a buyable row (the exact path the gametests cover).
 *
 * <p><b>Both screens are targeted</b> (issue #17): the {@code OPEN_MENU}
 * factory picks between them on the server-synced {@code shaderMenu} flag -
 * this pack opens {@link SageConsoleScreen}, which is why alpha.8/alpha.9
 * showed nothing: the strip lived on a screen that never rendered. Both
 * declare the same {@code render} and {@code mouseClicked} signatures and both
 * extend {@code Screen}, so the handlers below merge into either, and the
 * {@code (Screen) (Object) this} access is valid in both.</p>
 *
 * <p>The render inject binds every {@code RETURN} rather than {@code TAIL}:
 * both screens have early returns (close-animation completion), and
 * {@code TAIL} only binds the last one. No {@code @Shadow} members - screen
 * size comes from the public {@code Screen} fields via direct access, the
 * client from {@link Minecraft#getInstance()} (see #15). Client array only.</p>
 */
@Mixin(value = {SageMenuScreen.class, SageConsoleScreen.class})
public class SageScreensStripMixin {

    @Inject(method = "render(Lnet/minecraft/client/gui/GuiGraphics;IIF)V", at = @At("RETURN"))
    private void greatsagegrowth$renderStrip(GuiGraphics gui, int mouseX, int mouseY, float partialTick,
            CallbackInfo ci) {
        Screen screen = (Screen) (Object) this;
        FamilyStrip.render(gui, Minecraft.getInstance().font, screen.width, screen.height);
    }

    @Inject(method = "mouseClicked(DDI)Z", at = @At("HEAD"), cancellable = true)
    private void greatsagegrowth$stripClick(double mouseX, double mouseY, int button,
            CallbackInfoReturnable<Boolean> cir) {
        if (button != 0) {
            return;
        }
        Screen screen = (Screen) (Object) this;
        FamilyStripLayout.Row row = FamilyStripLayout.hit(
                (float) mouseX, (float) mouseY, screen.width, screen.height);
        if (row == null) {
            return;
        }
        TensuraGreatSageGrowth.LOGGER.info("growth strip hit: {},{} -> {}/{}",
                mouseX, mouseY, row.family(), row.kind());
        if (row.kind() != FamilyStripLayout.Row.Kind.BUYABLE) {
            return;
        }
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player != null && minecraft.player.connection != null) {
            minecraft.player.connection.sendCommand(
                    "sagegrowth unlock " + row.family().tensuraPackage());
        }
        cir.setReturnValue(true);
    }
}
