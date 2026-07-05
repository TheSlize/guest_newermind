package keormel.newermind.client;

import keormel.newermind.NewermindMobs;
import keormel.newermind.entity.GuestEntity;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import software.bernie.geckolib.constant.DataTickets;
import software.bernie.geckolib.core.animatable.model.CoreGeoBone;
import software.bernie.geckolib.core.animation.AnimationState;
import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.model.data.EntityModelData;

public class GuestModel extends GeoModel<GuestEntity> {
    private static final ResourceLocation MODEL = new ResourceLocation(NewermindMobs.MODID, "geo/entity/guest.geo.json");
    private static final ResourceLocation TEXTURE = new ResourceLocation(NewermindMobs.MODID, "textures/entity/guest.png");
    private static final ResourceLocation ANIMATIONS = new ResourceLocation(NewermindMobs.MODID, "animations/entity/guest.animations.json");

    @Override
    public ResourceLocation getModelResource(GuestEntity animatable) {
        return MODEL;
    }

    @Override
    public ResourceLocation getTextureResource(GuestEntity animatable) {
        return TEXTURE;
    }

    @Override
    public ResourceLocation getAnimationResource(GuestEntity animatable) {
        return ANIMATIONS;
    }

    @Override
    public void handleAnimations(GuestEntity animatable, long instanceId, AnimationState<GuestEntity> animationState) {
        super.handleAnimations(animatable, instanceId, animationState);

        for (CoreGeoBone bone : getAnimationProcessor().getRegisteredBones()) {
            bone.setRotX(-bone.getRotX());
            bone.setRotZ(-bone.getRotZ());
            bone.setPosX(-bone.getPosX());
            bone.setPosZ(-bone.getPosZ());
        }

        if (animatable.shouldLockRootYawRoll()) {
            getBone("Guest").ifPresent(bone -> {
                bone.setRotY(0.0F);
                bone.setRotZ(0.0F);
            });
        }
    }

    @Override
    public void setCustomAnimations(GuestEntity animatable, long instanceId, AnimationState<GuestEntity> animationState) {
        super.setCustomAnimations(animatable, instanceId, animationState);
        getBone("Head").ifPresent(head -> {
            EntityModelData entityData = animationState.getData(DataTickets.ENTITY_MODEL_DATA);
            if (entityData != null) {
                head.setRotX(entityData.headPitch() * Mth.DEG_TO_RAD);
                head.setRotY(entityData.netHeadYaw() * Mth.DEG_TO_RAD);
            }
        });
    }
}