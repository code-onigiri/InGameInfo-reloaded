package df11zomgraves.ingameinfo.network;

import df11zomgraves.ingameinfo.reference.Names;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

public final class PacketHandler {
	private static final String PROTOCOL_VERSION = "1";

	private PacketHandler() {
	}

	public static void init(IEventBus modEventBus) {
		modEventBus.addListener(PacketHandler::register);
	}

	private static void register(final RegisterPayloadHandlersEvent event) {
		final PayloadRegistrar registrar = event.registrar(PROTOCOL_VERSION);
		registrar.playToServer(RequestSeedPacket.TYPE, RequestSeedPacket.STREAM_CODEC, RequestSeedPacket::handle);
		registrar.playToClient(ResponseSeedPacket.TYPE, ResponseSeedPacket.STREAM_CODEC, ResponseSeedPacket::handle);
		registrar.playToServer(RequestMSPTPacket.TYPE, RequestMSPTPacket.STREAM_CODEC, RequestMSPTPacket::handle);
		registrar.playToClient(ResponseMSPTPacket.TYPE, ResponseMSPTPacket.STREAM_CODEC, ResponseMSPTPacket::handle);
	}
}
