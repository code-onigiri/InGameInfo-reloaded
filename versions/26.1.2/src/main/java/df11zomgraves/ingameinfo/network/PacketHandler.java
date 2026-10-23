package df11zomgraves.ingameinfo.network;

import df11zomgraves.ingameinfo.reference.Names;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;
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

	public static void sendToServer(final net.minecraft.network.protocol.common.custom.CustomPacketPayload payload) {
		ClientPacketDistributor.sendToServer(payload);
	}

	private static void register(final RegisterPayloadHandlersEvent event) {
		final PayloadRegistrar registrar = event.registrar(PROTOCOL_VERSION);
		registrar.playToServer(RequestSeedPacket.TYPE, RequestSeedPacket.STREAM_CODEC, RequestSeedPacket::handle);
		registrar.playToClient(ResponseSeedPacket.TYPE, ResponseSeedPacket.STREAM_CODEC, ResponseSeedPacket::handle);
		registrar.playToServer(RequestMSPTPacket.TYPE, RequestMSPTPacket.STREAM_CODEC, RequestMSPTPacket::handle);
		registrar.playToClient(ResponseMSPTPacket.TYPE, ResponseMSPTPacket.STREAM_CODEC, ResponseMSPTPacket::handle);
		registrar.playToServer(RequestAe2Packet.TYPE, RequestAe2Packet.STREAM_CODEC, RequestAe2Packet::handle);
		registrar.playToClient(ResponseAe2Packet.TYPE, ResponseAe2Packet.STREAM_CODEC, ResponseAe2Packet::handle);
	}
}