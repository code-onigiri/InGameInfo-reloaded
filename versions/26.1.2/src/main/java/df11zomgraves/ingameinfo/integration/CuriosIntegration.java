package df11zomgraves.ingameinfo.integration;

import df11zomgraves.ingameinfo.tag.TagRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

/** Curios equipped-slot tags. */
public final class CuriosIntegration {
    private static final String CURIOS_API = "top.theillusivec4.curios.api.CuriosApi";

    private CuriosIntegration() {
    }

    private static Object equipped() {
        final Player player = Minecraft.getInstance().player;
        if (player == null) {
            return null;
        }
        final Object lazy = Reflect.callStatic(CURIOS_API, "getCuriosInventory", player);
        final Object handler = Reflect.call(lazy, "orElse", (Object) null);
        return handler == null ? null : Reflect.call(handler, "getEquippedCurios");
    }

    public static class Slots extends IntegrationTag {
        public Slots() {
            super("curios");
        }

        @Override
        protected String value() {
            final Object inventory = equipped();
            final Object slots = Reflect.call(inventory, "getSlots");
            return slots instanceof Number ? String.valueOf(((Number) slots).intValue()) : "0";
        }
    }

    public static class Items extends IntegrationTag {
        public Items() {
            super("curios");
        }

        @Override
        protected String value() {
            final Object inventory = equipped();
            final Object slots = Reflect.call(inventory, "getSlots");
            if (!(slots instanceof Number)) {
                return "";
            }
            final StringBuilder result = new StringBuilder();
            final int count = ((Number) slots).intValue();
            for (int i = 0; i < count; i++) {
                final Object stack = Reflect.call(inventory, "getStackInSlot", i);
                if (!(stack instanceof ItemStack) || ((ItemStack) stack).isEmpty()) {
                    continue;
                }
                final Object name = Reflect.call(stack, "getHoverName");
                final Object text = Reflect.call(name, "getString");
                if (text != null) {
                    if (result.length() > 0) {
                        result.append(", ");
                    }
                    result.append(text);
                }
            }
            return result.toString();
        }
    }

    public static void register() {
        TagRegistry.INSTANCE.register(new Slots().setName("curioslots").setAliases("curioslotcount"));
        TagRegistry.INSTANCE.register(new Items().setName("curioitems").setAliases("curios"));
    }
}
