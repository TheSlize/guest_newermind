package keormel.newermind.client;

import keormel.newermind.entity.GuestEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import software.bernie.geckolib.renderer.GeoEntityRenderer;
import software.bernie.geckolib.renderer.layer.AutoGlowingGeoLayer;

public class GuestRenderer extends GeoEntityRenderer<GuestEntity> {
    public GuestRenderer(EntityRendererProvider.Context context) {
        super(context, new GuestModel());
        this.shadowRadius = 0.35F;
        this.withScale(0.8F);
        addRenderLayer(new AutoGlowingGeoLayer<>(this));
    }
}