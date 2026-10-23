package df11zomgraves.ingameinfo.network;

import df11zomgraves.ingameinfo.reference.Names;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record RequestSeedPacket() implements CustomPacketPayload {
	public static final Type<RequestSeedPacket> TYPE =
			new Type<>(Identifier.fromNamespaceAndPath(Names.MODID, "request_seed"));
	public static final StreamCodec<FriendlyByteBuf, RequestSeedPacket> STREAM_CODEC =
			StreamCodec.unit(new RequestSeedPacket());

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}

	public static void handle(final RequestSeedPacket packet, final IPayloadContext context) {
		context.enqueueWork(() -> {
			if (context.player() instanceof ServerPlayer player) {
				final long seed = player.level().getSeed();
				PacketDistributor.sendToPlayer(player, new ResponseSeedPacket(seed));
			}
		});
	}
}
