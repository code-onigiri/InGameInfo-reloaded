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

    private static Object attachmentType;

    private static Object thirst() {
        if (Reflect.cls(ATTACHMENT) != null) {
            if (attachmentType == null) {
                final Object supplier = Reflect.staticField(ATTACHMENT, "PLAYER_THIRST");
                attachmentType = supplier == null ? null : Reflect.call(supplier, "get");
            }
            // Never call getData with a null type: Reflect would happily match
            // any single-argument overload and could return unrelated data.
            if (attachmentType != null) {
                final Object data = Reflect.call(player(), "getData", attachmentType);
                if (data != null) {
                    return data;
                }
            }
            // The attachment class exists but produced nothing (renamed members
            // or no data attached); fall through to the capability path instead
            // of silently reporting zero thirst.
        }
        final Object capability = Reflect.staticField(CAPABILITIES, "PLAYER_THIRST");
        if (capability == null) {
            return null;
        }
        // 1.20.1 returns a LazyOptional here; 1.21.1 uses the attachment path above.
        return unwrap(Reflect.call(player(), "getCapability", capability));
    }

    private static Object unwrap(final Object value) {
        if (value == null) {
            return null;
        }
        // Only Optional/LazyOptional-style holders are unwrapped; if the API
        // generation returns the thirst data directly, keep it as is.
        if (Reflect.call(value, "isPresent") instanceof Boolean) {
            return Reflect.call(value, "orElse", (Object) null);
        }
        return value;
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