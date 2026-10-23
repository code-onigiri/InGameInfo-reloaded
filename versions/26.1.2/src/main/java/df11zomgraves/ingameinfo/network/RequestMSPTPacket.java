package df11zomgraves.ingameinfo.network;

import df11zomgraves.ingameinfo.reference.Names;
import df11zomgraves.ingameinfo.util.MathUtils;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record RequestMSPTPacket() implements CustomPacketPayload {
	public static final Type<RequestMSPTPacket> TYPE =
			new Type<>(Identifier.fromNamespaceAndPath(Names.MODID, "request_mspt"));
	public static final StreamCodec<FriendlyByteBuf, RequestMSPTPacket> STREAM_CODEC =
			StreamCodec.unit(new RequestMSPTPacket());

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}

	public static void handle(final RequestMSPTPacket packet, final IPayloadContext context) {
		context.enqueueWork(() -> {
			if (!(context.player() instanceof ServerPlayer player))
				return;
			final MinecraftServer server = player.level().getServer();
			if (server == null)
				return;
			final long[] times = server.getTickTime(player.level().dimension());
			if (times != null) {
				final double mspt = MathUtils.mean(times) * 1.0E-6D;
				PacketDistributor.sendToPlayer(player, new ResponseMSPTPacket(mspt));
			}
		});
	}
}
