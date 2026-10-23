package df11zomgraves.ingameinfo.network;

import df11zomgraves.ingameinfo.InGameInfoXML;
import df11zomgraves.ingameinfo.reference.Names;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record ResponseMSPTPacket(double mspt) implements CustomPacketPayload {
	public static final Type<ResponseMSPTPacket> TYPE =
			new Type<>(ResourceLocation.fromNamespaceAndPath(Names.MODID, "response_mspt"));
	public static final StreamCodec<FriendlyByteBuf, ResponseMSPTPacket> STREAM_CODEC = StreamCodec.composite(
			ByteBufCodecs.DOUBLE, ResponseMSPTPacket::mspt,
			ResponseMSPTPacket::new);

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}

	public static void handle(final ResponseMSPTPacket packet, final IPayloadContext context) {
		context.enqueueWork(() -> {
			InGameInfoXML.mspt = packet.mspt();
			InGameInfoXML.tps = (packet.mspt() == -1) ? -1 : Math.min(1000.0 / packet.mspt(), 20);
			InGameInfoXML.serverInstalled = true;
		});
	}
}
