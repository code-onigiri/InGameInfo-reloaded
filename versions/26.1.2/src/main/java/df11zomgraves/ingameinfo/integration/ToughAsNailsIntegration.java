package df11zomgraves.ingameinfo.integration;

import df11zomgraves.ingameinfo.tag.TagRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;

import java.util.Locale;

/** Tough As Nails thirst and body-temperature tags. */
public final class ToughAsNailsIntegration {
    private static final String THIRST_HELPER = "toughasnails.api.thirst.ThirstHelper";
    private static final String TEMPERATURE_HELPER = "toughasnails.api.temperature.TemperatureHelper";

    private ToughAsNailsIntegration() {
    }

    private static Player player() {
        return Minecraft.getInstance().player;
    }

    private static Object thirst() {
        return Reflect.callStatic(THIRST_HELPER, "getThirst", player());
    }

    private static Object temperature() {
        return Reflect.callStatic(TEMPERATURE_HELPER, "getTemperatureForPlayer", player());
    }

    private static String pretty(final Object enumValue) {
        if (!(enumValue instanceof Enum<?>)) {
            return "";
        }
        final String name = ((Enum<?>) enumValue).name().toLowerCase(Locale.ENGLISH);
        return Character.toUpperCase(name.charAt(0)) + name.substring(1);
    }

    public static class Thirst extends IntegrationTag {
        public Thirst() {
            super("toughasnails");
        }

        @Override
        protected String value() {
            final Object value = Reflect.call(thirst(), "getThirst");
            return value instanceof Number ? String.valueOf(((Number) value).intValue()) : "0";
        }
    }

    public static class Hydration extends IntegrationTag {
        public Hydration() {
            super("toughasnails");
        }

        @Override
        protected String value() {
            final Object value = Reflect.call(thirst(), "getHydration");
            return value instanceof Number ? String.format(Locale.ENGLISH, "%.1f", ((Number) value).floatValue()) : "0.0";
        }
    }

    public static class Temperature extends IntegrationTag {
        public Temperature() {
            super("toughasnails");
        }

        @Override
        protected String value() {
            return pretty(temperature());
        }
    }

    public static class TemperatureLevel extends IntegrationTag {
        public TemperatureLevel() {
            super("toughasnails");
        }

        @Override
        protected String value() {
            final Object value = temperature();
            return value instanceof Enum<?> ? String.valueOf(((Enum<?>) value).ordinal()) : "-1";
        }
    }

    public static void register() {
        TagRegistry.INSTANCE.register(new Thirst().setName("thirst"));
        TagRegistry.INSTANCE.register(new Hydration().setName("hydration"));
        TagRegistry.INSTANCE.register(new Temperature().setName("temperature").setAliases("bodytemperature"));
        TagRegistry.INSTANCE.register(new TemperatureLevel().setName("temperaturelevel"));
    }
}
