package df11zomgraves.ingameinfo.integration;

import df11zomgraves.ingameinfo.tag.TagRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;

import java.util.Locale;

/** Paragliders stamina tags. */
public final class ParaglidersIntegration {
    private static final String STAMINA = "tictim.paraglider.api.stamina.Stamina";

    private ParaglidersIntegration() {
    }

    private static Player player() {
        return Minecraft.getInstance().player;
    }

    private static Object stamina() {
        return Reflect.callStatic(STAMINA, "get", player());
    }

    private static double stat(final String method) {
        final Object value = Reflect.call(stamina(), method);
        return value instanceof Number ? ((Number) value).doubleValue() : 0.0D;
    }

    private static String number(final double value) {
        if (Math.abs(value - Math.rint(value)) < 0.0001D) {
            return String.format(Locale.ENGLISH, "%.0f", value);
        }
        return String.format(Locale.ENGLISH, "%.1f", value);
    }

    public static class Stamina extends IntegrationTag {
        public Stamina() {
            super("paraglider");
        }

        @Override
        protected String value() {
            return number(stat("stamina"));
        }
    }

    public static class MaxStamina extends IntegrationTag {
        public MaxStamina() {
            super("paraglider");
        }

        @Override
        protected String value() {
            return number(stat("maxStamina"));
        }
    }

    public static class StaminaPercent extends IntegrationTag {
        public StaminaPercent() {
            super("paraglider");
        }

        @Override
        protected String value() {
            final double max = stat("maxStamina");
            return String.format(Locale.ENGLISH, "%.1f", max <= 0.0D ? 0.0D : stat("stamina") * 100.0D / max);
        }
    }

    public static class Depleted extends IntegrationTag {
        public Depleted() {
            super("paraglider");
        }

        @Override
        protected String value() {
            final Object depleted = Reflect.call(stamina(), "isDepleted");
            if (depleted instanceof Boolean) {
                return (Boolean) depleted ? "true" : "false";
            }
            return stat("stamina") <= 0.0D ? "true" : "false";
        }
    }

    public static void register() {
        TagRegistry.INSTANCE.register(new Stamina().setName("stamina"));
        TagRegistry.INSTANCE.register(new MaxStamina().setName("maxstamina"));
        TagRegistry.INSTANCE.register(new StaminaPercent().setName("staminapencent"));
        TagRegistry.INSTANCE.register(new Depleted().setName("staminadepleted"));
    }
}
