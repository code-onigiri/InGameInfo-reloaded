package df11zomgraves.ingameinfo.integration;

import df11zomgraves.ingameinfo.tag.TagRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;

import java.util.Locale;

/** Ars Nouveau player mana tags. */
public final class ArsNouveauIntegration {
    private static final String MANA_UTIL = "com.hollingsworth.arsnouveau.api.util.ManaUtil";

    private ArsNouveauIntegration() {
    }

    private static Player player() {
        return Minecraft.getInstance().player;
    }

    private static double current() {
        final Object value = Reflect.callStatic(MANA_UTIL, "getCurrentMana", player());
        return value instanceof Number ? ((Number) value).doubleValue() : 0.0D;
    }

    private static double maximum() {
        final Object value = Reflect.callStatic(MANA_UTIL, "getMaxMana", player());
        return value instanceof Number ? ((Number) value).doubleValue() : 0.0D;
    }

    public static class Mana extends IntegrationTag {
        public Mana() {
            super("arsnouveau");
        }

        @Override
        protected String value() {
            return String.format(Locale.ENGLISH, "%.0f", current());
        }
    }

    public static class MaxMana extends IntegrationTag {
        public MaxMana() {
            super("arsnouveau");
        }

        @Override
        protected String value() {
            return String.format(Locale.ENGLISH, "%.0f", maximum());
        }
    }

    public static class ManaPercent extends IntegrationTag {
        public ManaPercent() {
            super("arsnouveau");
        }

        @Override
        protected String value() {
            final double max = maximum();
            return String.format(Locale.ENGLISH, "%.1f", max <= 0 ? 0.0D : current() * 100.0D / max);
        }
    }

    public static void register() {
        TagRegistry.INSTANCE.register(new Mana().setName("arsmana"));
        TagRegistry.INSTANCE.register(new MaxMana().setName("arsmaxmana"));
        TagRegistry.INSTANCE.register(new ManaPercent().setName("arsmanapercent"));
    }
}
