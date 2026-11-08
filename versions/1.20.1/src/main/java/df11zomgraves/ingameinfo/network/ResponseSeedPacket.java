package df11zomgraves.ingameinfo.network;

import java.util.function.Supplier;

import df11zomgraves.ingameinfo.InGameInfoXML;
import df11zomgraves.ingameinfo.tag.TagWorld;
import df11zomgraves.ingameinfo.util.StringConvertUtils;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

public class ResponseSeedPacket {
	public long seed;

	public ResponseSeedPacket() {
		this.seed = 0;
	}

	public ResponseSeedPacket(final long seed) {
		this.seed = seed;
	}

	public void encode(final FriendlyByteBuf buf) {
		buf.writeVarLong(this.seed);
	}

	public static ResponseSeedPacket decode(final FriendlyByteBuf buf) {
		return new ResponseSeedPacket(buf.readVarLong());
	}

	public void handleMessage(Supplier<NetworkEvent.Context> context) {
		context.get().enqueueWork(() -> {
			TagWorld.setSeedReceived(true);
			// A seed set locally via /igi setseed wins over a late response.
			if (!InGameInfoXML.seedSetLocally) {
				InGameInfoXML.seed = seed;
				StringConvertUtils.sendSeedToChat(seed);
			}
		});
		context.get().setPacketHandled(true);
	}
}
