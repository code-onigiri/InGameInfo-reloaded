package df11zomgraves.ingameinfo.network;

import java.util.function.Supplier;

import df11zomgraves.ingameinfo.integration.Ae2Integration;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.PacketDistributor;

/** Asks the server for AE2 network statistics. {@code pos} may be null. */
public class RequestAe2Packet {
	private final BlockPos pos;

	public RequestAe2Packet() {
		this(null);
	}

	public RequestAe2Packet(final BlockPos pos) {
		this.pos = pos;
	}

	public void encode(final FriendlyByteBuf buf) {
		buf.writeBoolean(this.pos != null);
		if (this.pos != null) {
			buf.writeBlockPos(this.pos);
		}
	}

	public static RequestAe2Packet decode(final FriendlyByteBuf buf) {
		return new RequestAe2Packet(buf.readBoolean() ? buf.readBlockPos() : null);
	}

	public void handleMessage(final Supplier<NetworkEvent.Context> context) {
		context.get().enqueueWork(() -> {
			final ServerPlayer player = context.get().getSender();
			if (player == null) {
				return;
			}
			final Ae2Integration.NetworkInfo info = Ae2Integration.collect(player, this.pos);
			PacketHandler.INSTANCE.send(PacketDistributor.PLAYER.with(() -> player),
					new ResponseAe2Packet(info.source, info.linked, info.channels, info.channelMode,
							info.storedPower, info.maxStoredPower, info.averageInjection, info.averageUsage,
							info.channelPower));
		});
		context.get().setPacketHandled(true);
	}
}
