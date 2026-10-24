package df11zomgraves.ingameinfo.handler;

import org.lwjgl.glfw.GLFW;

import df11zomgraves.ingameinfo.reference.Names;
import net.minecraft.client.KeyMapping;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;

@EventBusSubscriber(modid = Names.MODID, value = Dist.CLIENT, bus = EventBusSubscriber.Bus.MOD)
public class KeyInputHandler {
	public static final KeyMapping KEY_TOGGLE = new KeyMapping(Names.Keys.TOGGLE, GLFW.GLFW_KEY_F10, Names.Keys.CATEGORY);
	public static final KeyMapping KEY_RELOAD = new KeyMapping(Names.Keys.RELOAD, GLFW.GLFW_KEY_F12, Names.Keys.CATEGORY);
	public static final KeyMapping KEY_EDITOR = new KeyMapping(Names.Keys.EDITOR, GLFW.GLFW_KEY_F9, Names.Keys.CATEGORY);
	public static final KeyMapping KEY_DEBUG = new KeyMapping(Names.Keys.DEBUG, GLFW.GLFW_KEY_F8, Names.Keys.CATEGORY);

	@SubscribeEvent
	public static void registerBindings(RegisterKeyMappingsEvent event) {
		event.register(KEY_TOGGLE);
		event.register(KEY_RELOAD);
		event.register(KEY_EDITOR);
		event.register(KEY_DEBUG);
	}
}

