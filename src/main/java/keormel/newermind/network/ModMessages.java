package keormel.newermind.network;

import keormel.newermind.NewermindMobs;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;

public final class ModMessages {
    private static final String PROTOCOL_VERSION = "1";
    private static int packetId;

    private static final SimpleChannel CHANNEL = NetworkRegistry.ChannelBuilder.named(new ResourceLocation(NewermindMobs.MODID, "main")).networkProtocolVersion(() -> PROTOCOL_VERSION).clientAcceptedVersions(PROTOCOL_VERSION::equals).serverAcceptedVersions(PROTOCOL_VERSION::equals).simpleChannel();

    private ModMessages() {
    }

    public static void register() {
        CHANNEL.messageBuilder(GuestJumpscareEffectPacket.class, packetId++, NetworkDirection.PLAY_TO_CLIENT).encoder(GuestJumpscareEffectPacket::encode).decoder(GuestJumpscareEffectPacket::decode).consumerMainThread(GuestJumpscareEffectPacket::handle).add();
    }

    public static void sendToPlayer(ServerPlayer player, GuestJumpscareEffectPacket packet) {
        CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), packet);
    }
}
