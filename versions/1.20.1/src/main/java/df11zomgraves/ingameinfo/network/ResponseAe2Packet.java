package df11zomgraves.ingameinfo.network;

import java.util.function.Supplier;

import df11zomgraves.ingameinfo.integration.Ae2Integration;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

/** Server answer for {@link RequestAe2Packet}. */
public class ResponseAe2Packet {
	public final String source;
	public final boolean linked;
	public final int channels;
	public final String channelMode;
	public final double storedPower;
	public final double maxStoredPower;
	public final double averageInjection;
	public final double averageUsage;
	public final double channelPower;

	public ResponseAe2Packet() {
		this("none", false, 0, "", 0.0D, 0.0D, 0.0D, 0.0D, 0.0D);
	}

	public ResponseAe2Packet(final String source, final boolean linked, final int channels, final String channelMode,
			final double storedPower, final double maxStoredPower, final double averageInjection,
			final double averageUsage, final double channelPower) {
		this.source = source;
		this.linked = linked;
		this.channels = channels;
		this.channelMode = channelMode;
		this.storedPower = storedPower;
		this.maxStoredPower = maxStoredPower;
		this.averageInjection = averageInjection;
		this.averageUsage = averageUsage;
		this.channelPower = channelPower;
	}

	public void encode(final FriendlyByteBuf buf) {
		buf.writeUtf(this.source, 32);
		buf.writeBoolean(this.linked);
		buf.writeVarInt(this.channels);
		buf.writeUtf(this.channelMode, 32);
		buf.writeDouble(this.storedPower);
		buf.writeDouble(this.maxStoredPower);
		buf.writeDouble(this.averageInjection);
		buf.writeDouble(this.averageUsage);
		buf.writeDouble(this.channelPower);
	}

	public static ResponseAe2Packet decode(final FriendlyByteBuf buf) {
		return new ResponseAe2Packet(buf.readUtf(32), buf.readBoolean(), buf.readVarInt(), buf.readUtf(32),
				buf.readDouble(), buf.readDouble(), buf.readDouble(), buf.readDouble(), buf.readDouble());
	}

	public void handleMessage(final Supplier<NetworkEvent.Context> context) {
		context.get().enqueueWork(() -> Ae2Integration.applyServerInfo(new Ae2Integration.NetworkInfo(
				this.source, this.linked, this.channels, this.channelMode, this.storedPower, this.maxStoredPower,
				this.averageInjection, this.averageUsage, this.channelPower)));
		context.get().setPacketHandled(true);
	}
}
