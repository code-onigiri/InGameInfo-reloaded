package df11zomgraves.ingameinfo.integration;

import df11zomgraves.ingameinfo.tag.TagRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.player.Player;

import java.util.Locale;

/** Iron's Spells 'n Spellbooks mana tags. */
public final class IronsSpellbooksIntegration {
    private static final String CLIENT_MAGIC_DATA = "io.redspace.ironsspellbooks.player.ClientMagicData";
    private static final String ATTRIBUTE_REGISTRY = "io.redspace.ironsspellbooks.api.registry.AttributeRegistry";

    private IronsSpellbooksIntegration() {
    }

    private static Player player() {
        return Minecraft.getInstance().player;
    }

    private static double current() {
        final Object value = Reflect.callStatic(CLIENT_MAGIC_DATA, "getPlayerMana");
        return value instanceof Number ? ((Number) value).doubleValue() : 0.0D;
    }

    private static double maximum() {
        final Player player = player();
        if (player == null) {
            return 0.0D;
        }
        final Object registryObject = Reflect.staticField(ATTRIBUTE_REGISTRY, "MAX_MANA");
        final Object attribute = Reflect.call(registryObject, "get");
        return attribute instanceof Attribute ? player.getAttributeValue((Attribute) attribute) : 0.0D;
    }

    public static class Mana extends IntegrationTag {
        public Mana() {
            super("ironsspellbooks");
        }

        @Override
        protected String value() {
            return String.format(Locale.ENGLISH, "%.0f", current());
        }
    }

    public static class MaxMana extends IntegrationTag {
        public MaxMana() {
            super("ironsspellbooks");
        }

        @Override
        protected String value() {
            return String.format(Locale.ENGLISH, "%.0f", maximum());
        }
    }

    public static class ManaPercent extends IntegrationTag {
        public ManaPercent() {
            super("ironsspellbooks");
        }

        @Override
        protected String value() {
            final double max = maximum();
            return String.format(Locale.ENGLISH, "%.1f", max <= 0 ? 0.0D : current() * 100.0D / max);
        }
    }

    public static void register() {
        TagRegistry.INSTANCE.register(new Mana().setName("ironsmana").setAliases("ironsspellman"));
        TagRegistry.INSTANCE.register(new MaxMana().setName("ironsmaxmana"));
        TagRegistry.INSTANCE.register(new ManaPercent().setName("ironsmanapercent"));
    }
}
