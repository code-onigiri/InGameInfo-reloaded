package df11zomgraves.ingameinfo.integration;

import df11zomgraves.ingameinfo.tag.TagRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Locale;
import java.util.Set;

/** Botania mana stored in the player's inventory and accessories. */
public final class BotaniaIntegration {
    private static final String MANA_HANDLER = "vazkii.botania.api.mana.ManaItemHandler";
    private static final String XPLAT = "vazkii.botania.xplat.XplatAbstractions";

    private BotaniaIntegration() {
    }

    private static Player player() {
        return Minecraft.getInstance().player;
    }

    private static Object handler() {
        Object value = Reflect.callStatic(MANA_HANDLER, "instance");
        return value != null ? value : Reflect.staticField(MANA_HANDLER, "INSTANCE");
    }

    private static double[] mana() {
        double current = 0.0D;
        double maximum = 0.0D;
        final Object handler = handler();
        final Object xplat = Reflect.staticField(XPLAT, "INSTANCE");
        if (handler == null || xplat == null) {
            return new double[] { current, maximum };
        }
        final Set<ItemStack> seen = Collections.newSetFromMap(new IdentityHashMap<ItemStack, Boolean>());
        for (final String method : new String[] { "getManaItems", "getManaAccesories" }) {
            final Object value = Reflect.call(handler, method, player());
            if (!(value instanceof Iterable<?>)) {
                continue;
            }
            for (final Object element : (Iterable<?>) value) {
                if (!(element instanceof ItemStack)) {
                    continue;
                }
                final ItemStack stack = (ItemStack) element;
                if (stack.isEmpty() || !seen.add(stack)) {
                    continue;
                }
                final Object item = Reflect.call(xplat, "findManaItem", stack);
                final Object itemMana = Reflect.call(item, "getMana");
                final Object itemMax = Reflect.call(item, "getMaxMana");
                if (itemMana instanceof Number) {
                    current += ((Number) itemMana).doubleValue();
                }
                if (itemMax instanceof Number) {
                    maximum += ((Number) itemMax).doubleValue();
                }
            }
        }
        return new double[] { current, maximum };
    }

    public static class Mana extends IntegrationTag {
        public Mana() {
            super("botania");
        }

        @Override
        protected String value() {
            return String.format(Locale.ENGLISH, "%.0f", mana()[0]);
        }
    }

    public static class MaxMana extends IntegrationTag {
        public MaxMana() {
            super("botania");
        }

        @Override
        protected String value() {
            return String.format(Locale.ENGLISH, "%.0f", mana()[1]);
        }
    }

    public static class ManaPercent extends IntegrationTag {
        public ManaPercent() {
            super("botania");
        }

        @Override
        protected String value() {
            final double[] mana = mana();
            return String.format(Locale.ENGLISH, "%.1f", mana[1] <= 0.0D ? 0.0D : mana[0] * 100.0D / mana[1]);
        }
    }

    public static void register() {
        TagRegistry.INSTANCE.register(new Mana().setName("botaniamana"));
        TagRegistry.INSTANCE.register(new MaxMana().setName("botaniamaxmana"));
        TagRegistry.INSTANCE.register(new ManaPercent().setName("botaniamanapercent"));
    }
}
