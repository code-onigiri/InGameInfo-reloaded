package df11zomgraves.ingameinfo.network;

import df11zomgraves.ingameinfo.reference.Names;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/** Asks the server for AE2 network statistics. {@code pos} may be null. */
public record RequestAe2Packet(BlockPos pos) implements CustomPacketPayload {
    public static final Type<RequestAe2Packet> TYPE =
            new Type<>(Identifier.fromNamespaceAndPath(Names.MODID, "request_ae2"));
    public static final StreamCodec<FriendlyByteBuf, RequestAe2Packet> STREAM_CODEC = StreamCodec.of(
            (buf, packet) -> {
                buf.writeBoolean(packet.pos() != null);
                if (packet.pos() != null) {
                    buf.writeBlockPos(packet.pos());
                }
            },
            buf -> new RequestAe2Packet(buf.readBoolean() ? buf.readBlockPos() : null));

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(final RequestAe2Packet packet, final IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player() instanceof final ServerPlayer player) {
                final df11zomgraves.ingameinfo.integration.Ae2Integration.NetworkInfo info =
                        df11zomgraves.ingameinfo.integration.Ae2Integration.collect(player, packet.pos());
                PacketDistributor.sendToPlayer(player, new ResponseAe2Packet(info.source, info.linked, info.channels,
                        info.channelMode, info.storedPower, info.maxStoredPower, info.averageInjection,
                        info.averageUsage, info.channelPower));
            }
        });
    }
}