package df11zomgraves.ingameinfo.integration;

import df11zomgraves.ingameinfo.tag.TagRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;

import java.util.Locale;

/** Cold Sweat body and world temperature tags. */
public final class ColdSweatIntegration {
    private static final String TEMPERATURE = "com.momosoftworks.coldsweat.api.util.Temperature";
    private static final String TRAIT = "com.momosoftworks.coldsweat.api.util.Temperature$Trait";

    private ColdSweatIntegration() {
    }

    private static Player player() {
        return Minecraft.getInstance().player;
    }

    private static double temperature(final String traitName) {
        final Object trait = Reflect.staticField(TRAIT, traitName);
        final Object value = Reflect.callStatic(TEMPERATURE, "get", player(), trait);
        return value instanceof Number ? ((Number) value).doubleValue() : 0.0D;
    }

    public static class BodyTemperature extends IntegrationTag {
        public BodyTemperature() {
            super("coldsweat");
        }

        @Override
        protected String value() {
            return String.format(Locale.ENGLISH, "%.1f", temperature("BODY"));
        }
    }

    public static class WorldTemperature extends IntegrationTag {
        public WorldTemperature() {
            super("coldsweat");
        }

        @Override
        protected String value() {
            return String.format(Locale.ENGLISH, "%.1f", temperature("WORLD"));
        }
    }

    public static void register() {
        TagRegistry.INSTANCE.register(new BodyTemperature().setName("coldsweat"));
        TagRegistry.INSTANCE.register(new WorldTemperature().setName("coldsweatworld"));
    }
}
