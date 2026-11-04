package org.bitwisemadness.tensuragreatsagegrowth.mixin;

import dev.dedek.greatsage.client.SageMenuScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import org.bitwisemadness.tensuragreatsagegrowth.client.FamilyStrip;
import org.bitwisemadness.tensuragreatsagegrowth.client.FamilyStripLayout;
import org.spongepowered.asm.mixin.Mixin;
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
 *
 * <p>No {@code @Shadow} members (issue #15): {@code width}, {@code height},
 * {@code minecraft} and {@code font} all live on {@code Screen}, not on the
 * target class, and shadowing inherited members is exactly what made this mixin
 * fail to apply in the production pack ({@code "@Shadow field width was not
 * located ... No refMap loaded"}) - a silent FATAL that left the strip missing
 * from every real client. The screen size is instead read with a direct field
 * access on {@code Screen}: ordinary bytecode the mixin transformer never has to
 * resolve. The client comes from {@link Minecraft#getInstance()}. Both
 * injection targets, {@code render} and {@code mouseClicked}, are declared by
 * {@code SageMenuScreen} itself.</p>
 */
@Mixin(SageMenuScreen.class)
public class SageMenuScreenMixin {

    @Inject(method = "render(Lnet/minecraft/client/gui/GuiGraphics;IIF)V", at = @At("TAIL"))
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
        if (row == null || row.kind() != FamilyStripLayout.Row.Kind.BUYABLE) {
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
