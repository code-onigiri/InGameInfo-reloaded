package df11zomgraves.ingameinfo.network;

import df11zomgraves.ingameinfo.InGameInfoXML;
import df11zomgraves.ingameinfo.reference.Names;
import df11zomgraves.ingameinfo.util.StringConvertUtils;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record ResponseSeedPacket(long seed) implements CustomPacketPayload {
	public static final Type<ResponseSeedPacket> TYPE =
			new Type<>(ResourceLocation.fromNamespaceAndPath(Names.MODID, "response_seed"));
	public static final StreamCodec<FriendlyByteBuf, ResponseSeedPacket> STREAM_CODEC = StreamCodec.composite(
			ByteBufCodecs.VAR_LONG, ResponseSeedPacket::seed,
			ResponseSeedPacket::new);

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}

	public static void handle(final ResponseSeedPacket packet, final IPayloadContext context) {
		context.enqueueWork(() -> {
			InGameInfoXML.seed = packet.seed();
			StringConvertUtils.sendSeedToChat(packet.seed());
		});
	}
}
