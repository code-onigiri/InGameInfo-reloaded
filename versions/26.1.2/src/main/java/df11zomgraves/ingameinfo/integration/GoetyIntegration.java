package df11zomgraves.ingameinfo.integration;

import df11zomgraves.ingameinfo.tag.TagRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;

import java.util.Locale;

/** Goety soul energy tags. */
public final class GoetyIntegration {
    private static final String SOUL_HELPER = "com.Polarice3.Goety.utils.SEHelper";

    private GoetyIntegration() {
    }

    private static Player player() {
        return Minecraft.getInstance().player;
    }

    public static class Souls extends IntegrationTag {
        public Souls() {
            super("goety");
        }

        @Override
        protected String value() {
            final Object value = Reflect.callStatic(SOUL_HELPER, "getSESouls", player());
            return value instanceof Number ? String.format(Locale.ENGLISH, "%.0f", ((Number) value).doubleValue()) : "0";
        }
    }

    public static class Active extends IntegrationTag {
        public Active() {
            super("goety");
        }

        @Override
        protected String value() {
            final Object value = Reflect.callStatic(SOUL_HELPER, "getSEActive", player());
            return value instanceof Boolean && (Boolean) value ? "true" : "false";
        }
    }

    public static void register() {
        TagRegistry.INSTANCE.register(new Souls().setName("goetysouls"));
        TagRegistry.INSTANCE.register(new Active().setName("goetyactive"));
    }
}
