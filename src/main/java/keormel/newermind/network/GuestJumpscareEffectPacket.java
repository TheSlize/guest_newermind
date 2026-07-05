package keormel.newermind.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.lang.reflect.InvocationTargetException;
import java.util.function.Supplier;

public record GuestJumpscareEffectPacket(int durationTicks) {
    public static void encode(GuestJumpscareEffectPacket packet, FriendlyByteBuf buffer) {
        buffer.writeVarInt(packet.durationTicks);
    }

    public static GuestJumpscareEffectPacket decode(FriendlyByteBuf buffer) {
        return new GuestJumpscareEffectPacket(buffer.readVarInt());
    }

    public static void handle(GuestJumpscareEffectPacket packet, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> startClientInversion(packet.durationTicks));
        context.setPacketHandled(true);
    }

    private static void startClientInversion(int durationTicks) {
        try {
            Class<?> effectsClass = Class.forName("keormel.newermind.client.GuestClientEffects");
            effectsClass.getMethod("startInversion", int.class).invoke(null, durationTicks);
        } catch (ClassNotFoundException | NoSuchMethodException | IllegalAccessException | InvocationTargetException ignored) {
        }
    }
}
