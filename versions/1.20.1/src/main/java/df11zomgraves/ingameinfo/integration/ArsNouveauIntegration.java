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

    private static long cachedTick = Long.MIN_VALUE;
    private static double cachedCurrent;
    private static double cachedMaximum;

    // Mana changes arrive on tick boundaries, so resolve the reflective calls
    // once per tick and share the result between the mana/max/percent tags.
    private static void refresh() {
        final Player player = player();
        final long tick = player != null && player.level() != null ? player.level().getGameTime() : Long.MIN_VALUE;
        if (tick != Long.MIN_VALUE && tick == cachedTick) {
            return;
        }
        final Object current = Reflect.callStatic(MANA_UTIL, "getCurrentMana", player);
        final Object maximum = Reflect.callStatic(MANA_UTIL, "getMaxMana", player);
        cachedCurrent = current instanceof Number ? ((Number) current).doubleValue() : 0.0D;
        cachedMaximum = maximum instanceof Number ? ((Number) maximum).doubleValue() : 0.0D;
        cachedTick = tick;
    }

    private static double current() {
        refresh();
        return cachedCurrent;
    }

    private static double maximum() {
        refresh();
        return cachedMaximum;
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
