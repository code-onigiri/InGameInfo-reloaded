package df11zomgraves.ingameinfo.integration;

import df11zomgraves.ingameinfo.tag.TagRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;

import java.util.Locale;

/** Mekanism radiation tags. */
public final class MekanismIntegration {
    private static final String MANAGER = "mekanism.api.radiation.IRadiationManager";
    private static final String ATTACHMENTS = "mekanism.common.registries.MekanismAttachmentTypes";
    private static final String CAPABILITIES = "mekanism.common.capabilities.Capabilities";
    private static final String CLIENT_RADIATION = "mekanism.common.lib.radiation.ClientRadiation";
    private static final String RADIATION_MANAGER = "mekanism.common.lib.radiation.RadiationManager";

    private MekanismIntegration() {
    }

    private static Player player() {
        return Minecraft.getInstance().player;
    }

    private static Object manager() {
        return Reflect.staticField(MANAGER, "INSTANCE");
    }

    private static double number(final Object value) {
        return value instanceof Number ? ((Number) value).doubleValue() : 0.0D;
    }

    private static double personal() {
        final Player player = player();
        if (player == null) {
            return 0.0D;
        }

        final Object attachment = Reflect.staticField(ATTACHMENTS, "RADIATION");
        if (attachment != null) {
            final Object value = Reflect.call(player, "getData", attachment);
            if (value instanceof Number) {
                return ((Number) value).doubleValue();
            }
        }

        final Object capability = Reflect.staticField(CAPABILITIES, "RADIATION_ENTITY");
        if (capability == null) {
            return 0.0D;
        }
        Object optional = Reflect.call(player, "getCapability", capability, null);
        if (optional == null) {
            optional = Reflect.call(player, "getCapability", capability);
        }
        final Object entity = Reflect.call(optional, "orElse", (Object) null);
        return number(Reflect.call(entity, "getRadiation"));
    }

    private static double environmental() {
        if (Reflect.cls(CLIENT_RADIATION) != null) {
            return number(Reflect.callStatic(CLIENT_RADIATION, "getClientEnvironmentalRadiation"));
        }
        return number(Reflect.call(Reflect.callStatic(RADIATION_MANAGER, "get"), "getClientEnvironmentalRadiation"));
    }

    private static double maximum() {
        if (Reflect.cls(CLIENT_RADIATION) != null) {
            return number(Reflect.callStatic(CLIENT_RADIATION, "getClientMaxMagnitude"));
        }
        return number(Reflect.call(Reflect.callStatic(RADIATION_MANAGER, "get"), "getClientMaxMagnitude"));
    }

    private static String severity() {
        final Object scale;
        if (Reflect.cls(CLIENT_RADIATION) != null) {
            scale = Reflect.callStatic(CLIENT_RADIATION, "getClientScale");
        } else {
            scale = Reflect.call(Reflect.callStatic(RADIATION_MANAGER, "get"), "getClientScale");
        }
        return scale == null ? "none" : scale.toString().toLowerCase(Locale.ENGLISH);
    }

    private static boolean enabled() {
        final Object value = Reflect.call(manager(), "isRadiationEnabled");
        return value instanceof Boolean && (Boolean) value;
    }

    private static String dose(final double value) {
        if (!Double.isFinite(value) || value == 0.0D) {
            return "0.000";
        }
        if (Math.abs(value) < 0.01D) {
            return String.format(Locale.ENGLISH, "%.2E", value);
        }
        return String.format(Locale.ENGLISH, "%.3f", value);
    }

    public static class Personal extends IntegrationTag {
        public Personal() {
            super("mekanism");
        }

        @Override
        protected String value() {
            return dose(personal());
        }
    }

    public static class Environmental extends IntegrationTag {
        public Environmental() {
            super("mekanism");
        }

        @Override
        protected String value() {
            return dose(environmental());
        }
    }

    public static class Maximum extends IntegrationTag {
        public Maximum() {
            super("mekanism");
        }

        @Override
        protected String value() {
            return dose(maximum());
        }
    }

    public static class Severity extends IntegrationTag {
        public Severity() {
            super("mekanism");
        }

        @Override
        protected String value() {
            return severity();
        }
    }

    public static class Enabled extends IntegrationTag {
        public Enabled() {
            super("mekanism");
        }

        @Override
        protected String value() {
            return enabled() ? "true" : "false";
        }
    }

    public static class Percent extends IntegrationTag {
        public Percent() {
            super("mekanism");
        }

        @Override
        protected String value() {
            final double maximum = maximum();
            return String.format(Locale.ENGLISH, "%.1f",
                    maximum <= 0.0D ? 0.0D : environmental() * 100.0D / maximum);
        }
    }

    public static void register() {
        TagRegistry.INSTANCE.register(new Personal().setName("mekanismradiation"));
        TagRegistry.INSTANCE.register(new Environmental().setName("mekanismradiationlevel"));
        TagRegistry.INSTANCE.register(new Maximum().setName("mekanismradiationmax"));
        TagRegistry.INSTANCE.register(new Severity().setName("mekanismradiationseverity"));
        TagRegistry.INSTANCE.register(new Enabled().setName("mekanismradiationenabled"));
        TagRegistry.INSTANCE.register(new Percent().setName("mekanismradiationpercent"));
    }
}
