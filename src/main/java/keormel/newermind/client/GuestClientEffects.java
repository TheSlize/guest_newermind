package keormel.newermind.client;

import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.systems.RenderSystem;
import keormel.newermind.NewermindMobs;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderGuiEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = NewermindMobs.MODID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class GuestClientEffects {
    private static final String TEXTURE_PREFIX = NewermindMobs.MODID + "_guest_inversion";

    private static ResourceLocation inversionTextureLocation;
    private static DynamicTexture inversionTexture;
    private static int inversionTicks;
    private static int inversionDurationTicks;
    private static int captureWidth;
    private static int captureHeight;
    private static boolean capturePending;
    private static boolean inversionActive;

    private GuestClientEffects() {
    }

    public static void startInversion(int durationTicks) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level == null) {
            return;
        }

        releaseTexture();
        inversionTicks = Math.max(1, durationTicks);
        inversionDurationTicks = inversionTicks;
        capturePending = true;
        inversionActive = true;
    }

    @SubscribeEvent
    public static void onRenderGuiPre(RenderGuiEvent.Pre event) {
        if (!capturePending) {
            return;
        }

        capturePending = false;
        captureInvertedFrame();
    }

    @SubscribeEvent
    public static void onRenderGuiPost(RenderGuiEvent.Post event) {
        if (!inversionActive || inversionTextureLocation == null || inversionTicks <= 0) {
            return;
        }

        float alpha = Math.min(1.0F, inversionTicks / (float) Math.max(1, inversionDurationTicks));
        renderInversionOverlay(event.getGuiGraphics(), event.getWindow().getGuiScaledWidth(), event.getWindow().getGuiScaledHeight(), alpha);
    }

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END || !inversionActive) {
            return;
        }

        inversionTicks--;
        if (inversionTicks <= 0) {
            releaseTexture();
            capturePending = false;
            inversionActive = false;
        }
    }

    private static void captureInvertedFrame() {
        Minecraft minecraft = Minecraft.getInstance();
        RenderTarget target = minecraft.getMainRenderTarget();
        captureWidth = target.width;
        captureHeight = target.height;
        if (captureWidth <= 0 || captureHeight <= 0) {
            return;
        }

        NativeImage image = new NativeImage(captureWidth, captureHeight, false);
        target.bindRead();
        image.downloadTexture(0, true);
        target.unbindRead();
        image.flipY();
        invertImage(image);

        inversionTexture = new DynamicTexture(image);
        inversionTextureLocation = minecraft.getTextureManager().register(TEXTURE_PREFIX, inversionTexture);
    }

    private static void invertImage(NativeImage image) {
        for (int y = 0; y < image.getHeight(); y++) {
            for (int x = 0; x < image.getWidth(); x++) {
                int color = image.getPixelRGBA(x, y);
                image.setPixelRGBA(x, y, (color & 0xFF000000) | (~color & 0x00FFFFFF));
            }
        }
    }

    private static void renderInversionOverlay(GuiGraphics guiGraphics, int width, int height, float alpha) {
        RenderSystem.disableDepthTest();
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, alpha);
        guiGraphics.blit(inversionTextureLocation, 0, 0, width, height, 0.0F, 0.0F, captureWidth, captureHeight, captureWidth, captureHeight);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.disableBlend();
        RenderSystem.enableDepthTest();
    }

    private static void releaseTexture() {
        Minecraft minecraft = Minecraft.getInstance();
        if (inversionTextureLocation != null) {
            minecraft.getTextureManager().release(inversionTextureLocation);
            inversionTextureLocation = null;
            inversionTexture = null;
        } else if (inversionTexture != null) {
            inversionTexture.close();
            inversionTexture = null;
        }
    }
}
