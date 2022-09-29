package net.pcal.fastback.fabric.mixins;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import net.pcal.fastback.fabric.FabricServerProvider;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(InGameHud.class)
public class InGameHudMixin {

    @Inject(at = @At("TAIL"), method = "render")
    public void render(MatrixStack matrixStack, float tickDelta, CallbackInfo info) {
        final Text text = FabricServerProvider.getInstance().getStatusText();
        if (text == null) return;

        MinecraftClient client = ((InGameHudAccessors)this).getClient();
        MatrixStack matrices = new MatrixStack();
        TextRenderer textRenderer = client.textRenderer;
        int j = textRenderer.getWidth(text);
        int k = 16777215;
        int scaledWidth = client.getWindow().getScaledWidth();
        int scaledHeight = client.getWindow().getScaledHeight();
        textRenderer.drawWithShadow(matrices, text, (float) (scaledWidth - j - 10), (float) (scaledHeight - 15), k);
    }
}
