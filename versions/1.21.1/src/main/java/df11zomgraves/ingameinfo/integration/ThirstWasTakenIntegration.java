package df11zomgraves.ingameinfo.integration;

import df11zomgraves.ingameinfo.tag.TagRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;

import java.util.Locale;

/** Thirst Was Taken thirst, hydration, and exhaustion tags. */
public final class ThirstWasTakenIntegration {
    private static final String ATTACHMENT = "dev.ghen.thirst.foundation.common.capability.ModAttachment";
    private static final String CAPABILITIES = "dev.ghen.thirst.foundation.common.capability.ModCapabilities";

    private ThirstWasTakenIntegration() {
    }

    private static Player player() {
        return Minecraft.getInstance().player;
    }

    private static Object thirst() {
        if (Reflect.cls(ATTACHMENT) != null) {
            final Object supplier = Reflect.staticField(ATTACHMENT, "PLAYER_THIRST");
            final Object type = Reflect.call(supplier, "get");
            return Reflect.call(player(), "getData", type);
        }
        final Object capability = Reflect.staticField(CAPABILITIES, "PLAYER_THIRST");
        // 1.20.1 returns a LazyOptional here; 1.21.1 uses the attachment path above.
        return Reflect.call(Reflect.call(player(), "getCapability", capability), "orElse", (Object) null);
    }

    private static double number(final Object value) {
        return value instanceof Number ? ((Number) value).doubleValue() : 0.0D;
    }

    public static class Thirst extends IntegrationTag {
        public Thirst() {
            super("thirst");
        }

        @Override
        protected String value() {
            return String.format(Locale.ENGLISH, "%.0f", number(Reflect.call(thirst(), "getThirst")));
        }
    }

    public static class Quenched extends IntegrationTag {
        public Quenched() {
            super("thirst");
        }

        @Override
        protected String value() {
            return String.format(Locale.ENGLISH, "%.0f", number(Reflect.call(thirst(), "getQuenched")));
        }
    }

    public static class Exhaustion extends IntegrationTag {
        public Exhaustion() {
            super("thirst");
        }

        @Override
        protected String value() {
            return String.format(Locale.ENGLISH, "%.1f", number(Reflect.call(thirst(), "getExhaustion")));
        }
    }

    public static void register() {
        TagRegistry.INSTANCE.register(new Thirst().setName("twtthirst"));
        TagRegistry.INSTANCE.register(new Quenched().setName("twtquenched"));
        TagRegistry.INSTANCE.register(new Exhaustion().setName("twtexhaustion"));
    }
}