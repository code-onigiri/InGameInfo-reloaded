package df11zomgraves.ingameinfo.integration;

import df11zomgraves.ingameinfo.tag.TagRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.world.level.Level;

import java.util.Locale;

/** Serene Seasons current-season tags. */
public final class SereneSeasonsIntegration {
    private static final String HELPER = "sereneseasons.api.season.SeasonHelper";

    private SereneSeasonsIntegration() {
    }

    private static Object state() {
        final Level level = Minecraft.getInstance().level;
        return level == null ? null : Reflect.callStatic(HELPER, "getSeasonState", level);
    }

    private static String pretty(final Object enumValue) {
        if (!(enumValue instanceof Enum<?>)) {
            return "";
        }
        final String name = ((Enum<?>) enumValue).name().toLowerCase(Locale.ENGLISH);
        final StringBuilder result = new StringBuilder();
        for (final String part : name.split("_")) {
            if (result.length() > 0) {
                result.append(' ');
            }
            result.append(Character.toUpperCase(part.charAt(0))).append(part.substring(1));
        }
        return result.toString();
    }

    private static String string(final Object state, final String method) {
        return pretty(Reflect.call(state, method));
    }

    public static class Season extends IntegrationTag {
        public Season() {
            super("sereneseasons");
        }

        @Override
        protected String value() {
            return string(state(), "getSeason");
        }
    }

    public static class SubSeason extends IntegrationTag {
        public SubSeason() {
            super("sereneseasons");
        }

        @Override
        protected String value() {
            return string(state(), "getSubSeason");
        }
    }

    public static class TropicalSeason extends IntegrationTag {
        public TropicalSeason() {
            super("sereneseasons");
        }

        @Override
        protected String value() {
            return string(state(), "getTropicalSeason");
        }
    }

    public static class Day extends IntegrationTag {
        public Day() {
            super("sereneseasons");
        }

        @Override
        protected String value() {
            final Object value = Reflect.call(state(), "getDay");
            return value instanceof Number ? String.valueOf(((Number) value).intValue()) : "0";
        }
    }

    public static class Ticks extends IntegrationTag {
        public Ticks() {
            super("sereneseasons");
        }

        @Override
        protected String value() {
            final Object value = Reflect.call(state(), "getSeasonCycleTicks");
            return value instanceof Number ? String.valueOf(((Number) value).intValue()) : "0";
        }
    }

    public static class Progress extends IntegrationTag {
        public Progress() {
            super("sereneseasons");
        }

        @Override
        protected String value() {
            final Object state = state();
            final Object ticks = Reflect.call(state, "getSeasonCycleTicks");
            final Object duration = Reflect.call(state, "getCycleDuration");
            if (!(ticks instanceof Number) || !(duration instanceof Number) || ((Number) duration).doubleValue() <= 0) {
                return "0.0";
            }
            return String.format(Locale.ENGLISH, "%.1f", ((Number) ticks).doubleValue() * 100.0D / ((Number) duration).doubleValue());
        }
    }

    public static void register() {
        TagRegistry.INSTANCE.register(new Season().setName("season"));
        TagRegistry.INSTANCE.register(new SubSeason().setName("subseason").setAliases("seasonphase"));
        TagRegistry.INSTANCE.register(new TropicalSeason().setName("tropicalseason"));
        TagRegistry.INSTANCE.register(new Day().setName("seasonday"));
        TagRegistry.INSTANCE.register(new Ticks().setName("seasonticks"));
        TagRegistry.INSTANCE.register(new Progress().setName("seasonprogress").setAliases("seasonpercent"));
    }
}
