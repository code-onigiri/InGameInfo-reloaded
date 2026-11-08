package df11zomgraves.ingameinfo.integration;

import df11zomgraves.ingameinfo.tag.TagRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/** Tinkers' Construct tool tags for the held tool. */
public final class TinkersIntegration {
    private static final String TOOL_STACK = "slimeknights.tconstruct.library.tools.nbt.ToolStack";
    private static final String TOOL_STATS = "slimeknights.tconstruct.library.tools.stat.ToolStats";

    private TinkersIntegration() {
    }

    private static ItemStack cachedStack;
    private static Object cachedTool;
    private static final Map<String, Object> STAT_FIELDS = new HashMap<String, Object>();

    private static Object tool() {
        final Player player = Minecraft.getInstance().player;
        if (player == null) {
            return null;
        }
        final ItemStack stack = player.getMainHandItem();
        if (stack.isEmpty()) {
            cachedStack = null;
            cachedTool = null;
            return null;
        }
        // ToolStack wraps the live stack, so resolving once per held stack is
        // enough; the values read from it (durability etc.) stay current.
        if (stack == cachedStack) {
            return cachedTool;
        }
        Object tool = Reflect.callStatic(TOOL_STACK, "from", stack);
        final Object empty = Reflect.staticField(TOOL_STACK, "EMPTY");
        if (tool != null && empty != null && tool == empty) {
            // ToolStack.from returns EMPTY for non-tool items; treat it as no
            // tool instead of running reflective calls on the placeholder.
            tool = null;
        }
        cachedStack = stack;
        cachedTool = tool;
        return tool;
    }

    private static Object stat(final String name) {
        final Object tool = tool();
        if (tool == null) {
            return null;
        }
        final Object stats = Reflect.call(tool, "getStats");
        if (!STAT_FIELDS.containsKey(name)) {
            STAT_FIELDS.put(name, Reflect.staticField(TOOL_STATS, name));
        }
        final Object stat = STAT_FIELDS.get(name);
        return stat == null ? null : Reflect.call(stats, "get", stat);
    }

    private static int integer(final Object value) {
        return value instanceof Number ? ((Number) value).intValue() : 0;
    }

    private static double decimal(final Object value) {
        return value instanceof Number ? ((Number) value).doubleValue() : 0.0D;
    }

    private static int currentDurability() {
        return integer(Reflect.call(tool(), "getCurrentDurability"));
    }

    private static int maximumDurability() {
        return integer(stat("DURABILITY"));
    }

    private static int modifiers() {
        final Object modifiers = Reflect.call(tool(), "getModifiers");
        if (modifiers instanceof List<?>) {
            return ((List<?>) modifiers).size();
        }
        final Object list = Reflect.call(modifiers, "getModifiers");
        return list instanceof List<?> ? ((List<?>) list).size() : 0;
    }

    public static class Durability extends IntegrationTag {
        public Durability() {
            super("tinkers");
        }

        @Override
        protected String value() {
            return Integer.toString(currentDurability());
        }
    }

    public static class MaxDurability extends IntegrationTag {
        public MaxDurability() {
            super("tinkers");
        }

        @Override
        protected String value() {
            return Integer.toString(maximumDurability());
        }
    }

    public static class DurabilityPercent extends IntegrationTag {
        public DurabilityPercent() {
            super("tinkers");
        }

        @Override
        protected String value() {
            final int maximum = maximumDurability();
            return String.format(Locale.ENGLISH, "%.1f",
                    maximum <= 0 ? 0.0D : currentDurability() * 100.0D / maximum);
        }
    }

    public static class MiningSpeed extends IntegrationTag {
        public MiningSpeed() {
            super("tinkers");
        }

        @Override
        protected String value() {
            return String.format(Locale.ENGLISH, "%.2f", decimal(stat("MINING_SPEED")));
        }
    }

    public static class AttackDamage extends IntegrationTag {
        public AttackDamage() {
            super("tinkers");
        }

        @Override
        protected String value() {
            return String.format(Locale.ENGLISH, "%.2f", decimal(stat("ATTACK_DAMAGE")));
        }
    }

    public static class HarvestLevel extends IntegrationTag {
        public HarvestLevel() {
            super("tinkers");
        }

        @Override
        protected String value() {
            return Integer.toString(integer(Reflect.call(stat("HARVEST_TIER"), "getLevel")));
        }
    }

    public static class Modifiers extends IntegrationTag {
        public Modifiers() {
            super("tinkers");
        }

        @Override
        protected String value() {
            return Integer.toString(modifiers());
        }
    }

    public static class Broken extends IntegrationTag {
        public Broken() {
            super("tinkers");
        }

        @Override
        protected String value() {
            return Boolean.TRUE.equals(Reflect.call(tool(), "isBroken")) ? "true" : "false";
        }
    }

    public static void register() {
        TagRegistry.INSTANCE.register(new Durability().setName("tinkersdurability"));
        TagRegistry.INSTANCE.register(new MaxDurability().setName("tinkersmaxdurability"));
        TagRegistry.INSTANCE.register(new DurabilityPercent().setName("tinkersdurabilitypercent"));
        TagRegistry.INSTANCE.register(new MiningSpeed().setName("tinkersminingspeed"));
        TagRegistry.INSTANCE.register(new AttackDamage().setName("tinkersattackdamage"));
        TagRegistry.INSTANCE.register(new HarvestLevel().setName("tinkersharvestlevel"));
        TagRegistry.INSTANCE.register(new Modifiers().setName("tinkersmodifiers"));
        TagRegistry.INSTANCE.register(new Broken().setName("tinkersbroken"));
    }
}
