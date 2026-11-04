package org.bitwisemadness.tensuragreatsagegrowth.mixin;

import dev.dedek.greatsage.client.SageMenuScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import org.bitwisemadness.tensuragreatsagegrowth.client.FamilyStrip;
import org.bitwisemadness.tensuragreatsagegrowth.client.FamilyStripLayout;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * The mod's growth strip inside the Sage menu (stack PR C, issue #10): the
 * addon's tree itself is keyed by its final SageNode enum - layout, glyphs and
 * hubs are all enum maps that cannot be extended - so this hooks the screen
 * instead: the strip renders after the screen's own frame and takes the click
 * first, issuing the server purchase command for a buyable row (the exact path
 * the gametests cover). Client array only.
 */
@Mixin(SageMenuScreen.class)
public class SageMenuScreenMixin {

    @Shadow
    protected int width;

    @Shadow
    protected int height;

    @Shadow
    protected Minecraft minecraft;

    @Shadow
    protected Font font;

    @Inject(method = "render(Lnet/minecraft/client/gui/GuiGraphics;IIF)V", at = @At("TAIL"))
    private void greatsagegrowth$renderStrip(GuiGraphics gui, int mouseX, int mouseY, float partialTick,
            CallbackInfo ci) {
        FamilyStrip.render(gui, this.font, this.width, this.height);
    }

    @Inject(method = "mouseClicked(DDI)Z", at = @At("HEAD"), cancellable = true)
    private void greatsagegrowth$stripClick(double mouseX, double mouseY, int button, CallbackInfoReturnable<Boolean> cir) {
        if (button != 0) {
            return;
        }
        FamilyStripLayout.Row row = FamilyStripLayout.hit((float) mouseX, (float) mouseY, this.width, this.height);
        if (row == null || row.kind() != FamilyStripLayout.Row.Kind.BUYABLE) {
            return;
        }
        if (this.minecraft != null && this.minecraft.player != null && this.minecraft.player.connection != null) {
            this.minecraft.player.connection.sendCommand(
                    "sagegrowth unlock " + row.family().tensuraPackage());
        }
        cir.setReturnValue(true);
    }
}
