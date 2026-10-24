package df11zomgraves.ingameinfo.integration;

import df11zomgraves.ingameinfo.tag.TagRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;

import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Locale;
import java.util.Set;

/** Botania mana stored in the player's inventory and accessories. */
public final class BotaniaIntegration {
    private static final String MANA_HANDLER = "vazkii.botania.api.mana.ManaItemHandler";
    private static final String XPLAT = "vazkii.botania.xplat.XplatAbstractions";
    private static final String MANA_POOL = "vazkii.botania.api.mana.ManaPool";
    private static final String MANA_SPREADER = "vazkii.botania.api.mana.ManaSpreader";
    private static final String GENERATING_FLOWER = "vazkii.botania.api.block_entity.GeneratingFlowerBlockEntity";
    private static final String FUNCTIONAL_FLOWER = "vazkii.botania.api.block_entity.FunctionalFlowerBlockEntity";

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

    private enum Device {
        POOL, SPREADER, FLOWER
    }

    private enum Metric {
        CURRENT, MAXIMUM, PERCENT
    }

    private static Object target() {
        final Minecraft minecraft = Minecraft.getInstance();
        final Level level = minecraft.level;
        if (minecraft.hitResult == null || minecraft.hitResult.getType() != HitResult.Type.BLOCK || level == null) {
            return null;
        }
        final BlockPos pos = ((BlockHitResult) minecraft.hitResult).getBlockPos();
        return level.getBlockEntity(pos);
    }

    private static boolean isDevice(final Object blockEntity, final Device device) {
        if (device == Device.POOL) {
            return Reflect.isInstance(blockEntity, MANA_POOL);
        }
        if (device == Device.SPREADER) {
            return Reflect.isInstance(blockEntity, MANA_SPREADER);
        }
        return Reflect.isInstance(blockEntity, GENERATING_FLOWER)
                || Reflect.isInstance(blockEntity, FUNCTIONAL_FLOWER);
    }

    private static double[] targetMana(final Device device) {
        final Object blockEntity = target();
        if (!isDevice(blockEntity, device)) {
            return new double[] { 0.0D, 0.0D };
        }
        Object current = Reflect.call(blockEntity, "getCurrentMana");
        if (current == null) {
            current = Reflect.call(blockEntity, "getMana");
        }
        return new double[] { number(current), number(Reflect.call(blockEntity, "getMaxMana")) };
    }

    private static double number(final Object value) {
        return value instanceof Number ? ((Number) value).doubleValue() : 0.0D;
    }

    public static class TargetMana extends IntegrationTag {
        private final Device device;
        private final Metric metric;

        public TargetMana(final Device device, final Metric metric) {
            super("botania");
            this.device = device;
            this.metric = metric;
        }

        @Override
        protected String value() {
            final double[] mana = targetMana(this.device);
            if (this.metric == Metric.MAXIMUM) {
                return String.format(Locale.ENGLISH, "%.0f", mana[1]);
            }
            if (this.metric == Metric.PERCENT) {
                return String.format(Locale.ENGLISH, "%.1f", mana[1] <= 0.0D ? 0.0D : mana[0] * 100.0D / mana[1]);
            }
            return String.format(Locale.ENGLISH, "%.0f", mana[0]);
        }
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
        TagRegistry.INSTANCE.register(new TargetMana(Device.POOL, Metric.CURRENT).setName("botaniamanapoolover"));
        TagRegistry.INSTANCE.register(new TargetMana(Device.POOL, Metric.MAXIMUM).setName("botaniamanapoolmaxover"));
        TagRegistry.INSTANCE.register(new TargetMana(Device.POOL, Metric.PERCENT).setName("botaniamanapoolpercentover"));
        TagRegistry.INSTANCE.register(new TargetMana(Device.SPREADER, Metric.CURRENT).setName("botaniaspreadermanaover"));
        TagRegistry.INSTANCE.register(new TargetMana(Device.SPREADER, Metric.MAXIMUM).setName("botaniaspreadermaxmanaover"));
        TagRegistry.INSTANCE.register(new TargetMana(Device.SPREADER, Metric.PERCENT).setName("botaniaspreaderpercentover"));
        TagRegistry.INSTANCE.register(new TargetMana(Device.FLOWER, Metric.CURRENT).setName("botaniaflowermanaover"));
        TagRegistry.INSTANCE.register(new TargetMana(Device.FLOWER, Metric.MAXIMUM).setName("botaniaflowermaxmanaover"));
        TagRegistry.INSTANCE.register(new TargetMana(Device.FLOWER, Metric.PERCENT).setName("botaniaflowerpercentover"));
    }
}
