package df11zomgraves.ingameinfo.integration;

import df11zomgraves.ingameinfo.tag.TagRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;

import java.util.Locale;

/** Vampirism blood tags. */
public final class VampirismIntegration {
    private static final String API = "de.teamlapen.vampirism.api.VampirismAPI";

    private VampirismIntegration() {
    }

    private static Player player() {
        return Minecraft.getInstance().player;
    }

    private static Object vampire() {
        Object value = Reflect.callStatic(API, "vampirePlayer", player());
        if (value == null) {
            value = Reflect.callStatic(API, "getVampirePlayer", player());
            value = Reflect.call(value, "orElse", (Object) null);
        }
        return value;
    }

    private static Object bloodStats() {
        return Reflect.call(vampire(), "getBloodStats");
    }

    private static double number(final Object value) {
        return value instanceof Number ? ((Number) value).doubleValue() : 0.0D;
    }

    private static double blood() {
        return number(Reflect.call(bloodStats(), "getBloodLevel"));
    }

    private static double maxBlood() {
        return number(Reflect.call(bloodStats(), "getMaxBlood"));
    }

    public static class Blood extends IntegrationTag {
        public Blood() {
            super("vampirism");
        }

        @Override
        protected String value() {
            return String.format(Locale.ENGLISH, "%.0f", blood());
        }
    }

    public static class MaxBlood extends IntegrationTag {
        public MaxBlood() {
            super("vampirism");
        }

        @Override
        protected String value() {
            return String.format(Locale.ENGLISH, "%.0f", maxBlood());
        }
    }

    public static class BloodPercent extends IntegrationTag {
        public BloodPercent() {
            super("vampirism");
        }

        @Override
        protected String value() {
            final double max = maxBlood();
            return String.format(Locale.ENGLISH, "%.1f", max <= 0.0D ? 0.0D : blood() * 100.0D / max);
        }
    }

    public static class VampireLevel extends IntegrationTag {
        public VampireLevel() {
            super("vampirism");
        }

        @Override
        protected String value() {
            return String.format(Locale.ENGLISH, "%.0f", number(Reflect.call(vampire(), "getLevel")));
        }
    }

    public static void register() {
        TagRegistry.INSTANCE.register(new Blood().setName("blood"));
        TagRegistry.INSTANCE.register(new MaxBlood().setName("maxblood"));
        TagRegistry.INSTANCE.register(new BloodPercent().setName("bloodpercent"));
        TagRegistry.INSTANCE.register(new VampireLevel().setName("vampirelevel"));
    }
}
