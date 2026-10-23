package df11zomgraves.ingameinfo.network;

import df11zomgraves.ingameinfo.integration.Ae2Integration;
import df11zomgraves.ingameinfo.reference.Names;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/** Server answer for {@link RequestAe2Packet}. */
public record ResponseAe2Packet(String source, boolean linked, int channels, String channelMode,
        double storedPower, double maxStoredPower, double averageInjection, double averageUsage,
        double channelPower) implements CustomPacketPayload {
    public static final Type<ResponseAe2Packet> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(Names.MODID, "response_ae2"));
    public static final StreamCodec<FriendlyByteBuf, ResponseAe2Packet> STREAM_CODEC = StreamCodec.of(
            (buf, packet) -> {
                buf.writeUtf(packet.source(), 32);
                buf.writeBoolean(packet.linked());
                buf.writeVarInt(packet.channels());
                buf.writeUtf(packet.channelMode(), 32);
                buf.writeDouble(packet.storedPower());
                buf.writeDouble(packet.maxStoredPower());
                buf.writeDouble(packet.averageInjection());
                buf.writeDouble(packet.averageUsage());
                buf.writeDouble(packet.channelPower());
            },
            buf -> new ResponseAe2Packet(buf.readUtf(32), buf.readBoolean(), buf.readVarInt(), buf.readUtf(32),
                    buf.readDouble(), buf.readDouble(), buf.readDouble(), buf.readDouble(), buf.readDouble()));

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(final ResponseAe2Packet packet, final IPayloadContext context) {
        context.enqueueWork(() -> Ae2Integration.applyServerInfo(new Ae2Integration.NetworkInfo(
                packet.source(), packet.linked(), packet.channels(), packet.channelMode(),
                packet.storedPower(), packet.maxStoredPower(), packet.averageInjection(),
                packet.averageUsage(), packet.channelPower())));
    }
}