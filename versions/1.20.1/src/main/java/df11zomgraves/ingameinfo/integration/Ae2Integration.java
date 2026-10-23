package df11zomgraves.ingameinfo.integration;

import java.util.List;
import java.util.Locale;

import df11zomgraves.ingameinfo.network.PacketHandler;
import df11zomgraves.ingameinfo.network.RequestAe2Packet;
import df11zomgraves.ingameinfo.tag.TagRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.BlockHitResult;

/**
 * Applied Energistics 2 tags.
 *
 * <p>Cell contents and the charge of powered items are part of the item stack and can be read
 * on the client. Network-wide values (channels, stored AE, power usage) only exist on the
 * server, so the client asks the server for them through a small custom payload. The query is
 * answered for a held, linked wireless terminal or for the AE2 block the player is looking at.</p>
 */
public final class Ae2Integration {
    private static final String WIRELESS_TERMINAL = "appeng.items.tools.powered.WirelessTerminalItem";
    private static final String GRID_HELPER = "appeng.api.networking.GridHelper";
    private static final String NETWORK_STATUS = "appeng.menu.me.networktool.NetworkStatus";
    private static final String STORAGE_CELLS = "appeng.api.storage.StorageCells";
    private static final String POWER_STORAGE = "appeng.api.implementations.items.IAEItemPowerStorage";

    private static volatile boolean active = false;
    private static volatile NetworkInfo latest = NetworkInfo.NONE;
    private static int tickCounter = 0;

    private Ae2Integration() {
    }

    /** Client-side request/reply cache. */
    public static final class NetworkInfo {
        public static final NetworkInfo NONE = new NetworkInfo("none", false, 0, "", 0.0D, 0.0D, 0.0D, 0.0D, 0.0D);

        public final String source;
        public final boolean linked;
        public final int channels;
        public final String channelMode;
        public final double storedPower;
        public final double maxStoredPower;
        public final double averageInjection;
        public final double averageUsage;
        public final double channelPower;

        public NetworkInfo(final String source, final boolean linked, final int channels, final String channelMode,
                final double storedPower, final double maxStoredPower, final double averageInjection,
                final double averageUsage, final double channelPower) {
            this.source = source;
            this.linked = linked;
            this.channels = channels;
            this.channelMode = channelMode;
            this.storedPower = storedPower;
            this.maxStoredPower = maxStoredPower;
            this.averageInjection = averageInjection;
            this.averageUsage = averageUsage;
            this.channelPower = channelPower;
        }
    }

    public static void register() {
        active = true;
        TagRegistry.INSTANCE.register(new NetworkSource().setName("ae2network"));
        TagRegistry.INSTANCE.register(new Channels().setName("ae2channels").setAliases("ae2channel"));
        TagRegistry.INSTANCE.register(new ChannelMode().setName("ae2channelmode"));
        TagRegistry.INSTANCE.register(new GridPower().setName("ae2gridpower"));
        TagRegistry.INSTANCE.register(new GridMaxPower().setName("ae2gridmaxpower"));
        TagRegistry.INSTANCE.register(new GridPowerUsage().setName("ae2gridpowerusage"));
        TagRegistry.INSTANCE.register(new GridPowerInjection().setName("ae2gridpowerinjection"));
        TagRegistry.INSTANCE.register(new ChannelPower().setName("ae2channelpower"));

        TagRegistry.INSTANCE.register(new CellUsed().setName("ae2cellused"));
        TagRegistry.INSTANCE.register(new CellTotal().setName("ae2celltotal"));
        TagRegistry.INSTANCE.register(new CellTypes().setName("ae2celltypes"));
        TagRegistry.INSTANCE.register(new CellMaxTypes().setName("ae2cellmaxtypes"));
        TagRegistry.INSTANCE.register(new CellCount().setName("ae2cellcount"));
        TagRegistry.INSTANCE.register(new CellPercent().setName("ae2cellpercent"));
        TagRegistry.INSTANCE.register(new CellStatus().setName("ae2cellstatus"));

        TagRegistry.INSTANCE.register(new ItemPower().setName("ae2power"));
        TagRegistry.INSTANCE.register(new ItemMaxPower().setName("ae2maxpower"));
        TagRegistry.INSTANCE.register(new ItemPowerPercent().setName("ae2powerpercent"));
    }

    /** Called once per client tick from the HUD ticker. */
    public static void clientTick() {
        if (!active) {
            return;
        }
        final Player player = Minecraft.getInstance().player;
        if (player == null) {
            latest = NetworkInfo.NONE;
            return;
        }
        if (++tickCounter < 20) {
            return;
        }
        tickCounter = 0;
        BlockPos target = null;
        if (Minecraft.getInstance().hitResult instanceof final BlockHitResult hit) {
            target = hit.getBlockPos();
        }
        try {
            PacketHandler.sendToServer(new RequestAe2Packet(target));
        } catch (final Throwable ignored) {
            // The server does not have the mod installed; keep showing client-side item tags.
            active = false;
        }
    }

    public static void applyServerInfo(final NetworkInfo info) {
        latest = info == null ? NetworkInfo.NONE : info;
    }

    /**
     * Server-side collection for a request. The block position is only used when no linked
     * wireless terminal is found in the player's inventory.
     */
    public static NetworkInfo collect(final ServerPlayer player, final BlockPos requestedPos) {
        Object grid = linkedGrid(player);
        String source = "wireless";
        if (grid == null && requestedPos != null && player.blockPosition().distSqr(requestedPos) <= 64.0D) {
            grid = blockGrid(player, requestedPos);
            source = "block";
        }
        if (grid == null) {
            // Aiming at a specific block is awkward while watching the HUD, so fall back to
            // the closest AE2 node around the player. This is what people usually mean by
            // "my network" while standing in their base.
            grid = nearbyGrid(player);
            source = "nearby";
        }
        if (grid == null) {
            return NetworkInfo.NONE;
        }
        final Object status = Reflect.callStatic(NETWORK_STATUS, "fromGrid", grid);
        if (status == null) {
            return NetworkInfo.NONE;
        }
        final Object pathing = Reflect.call(grid, "getPathingService");
        final Object channelMode = Reflect.call(pathing, "getChannelMode");
        final int channels = (int) number(Reflect.call(status, "getChannelsUsed"));
        return new NetworkInfo(source, true, channels,
                channelMode == null ? "" : String.valueOf(channelMode).toLowerCase(Locale.ENGLISH),
                number(Reflect.call(status, "getStoredPower")),
                number(Reflect.call(status, "getMaxStoredPower")),
                number(Reflect.call(status, "getAveragePowerInjection")),
                number(Reflect.call(status, "getAveragePowerUsage")),
                number(Reflect.call(status, "getChannelPower")));
    }

    private static Object linkedGrid(final ServerPlayer player) {
        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            final ItemStack stack = player.getInventory().getItem(i);
            if (stack.isEmpty() || !Reflect.isInstance(stack.getItem(), WIRELESS_TERMINAL)) {
                continue;
            }
            final Object grid = Reflect.call(stack.getItem(), "getLinkedGrid", stack, player.level(), player);
            if (grid != null) {
                return grid;
            }
        }
        return null;
    }

    private static Object nearbyGrid(final ServerPlayer player) {
        final net.minecraft.server.level.ServerLevel level = player.serverLevel();
        final BlockPos center = player.blockPosition();
        for (int radius = 1; radius <= 8; radius++) {
            for (int dx = -radius; dx <= radius; dx++) {
                for (int dz = -radius; dz <= radius; dz++) {
                    if (Math.max(Math.abs(dx), Math.abs(dz)) != radius) {
                        continue;
                    }
                    for (int dy = -4; dy <= 4; dy++) {
                        final BlockPos pos = center.offset(dx, dy, dz);
                        if (level.getBlockEntity(pos) == null) {
                            continue;
                        }
                        final Object grid = blockGrid(player, pos);
                        if (grid != null) {
                            return grid;
                        }
                    }
                }
            }
        }
        return null;
    }

    private static Object blockGrid(final ServerPlayer player, final BlockPos pos) {
        final Object host = Reflect.callStatic(GRID_HELPER, "getNodeHost", player.serverLevel(), pos);
        if (host == null) {
            return null;
        }
        Object node = Reflect.call(host, "getGridNode");
        if (node == null) {
            for (final net.minecraft.core.Direction direction : net.minecraft.core.Direction.values()) {
                node = Reflect.call(host, "getGridNode", direction);
                if (node != null) {
                    break;
                }
            }
        }
        return node == null ? null : Reflect.call(node, "getGrid");
    }

    private static Object cell() {
        final Player player = Minecraft.getInstance().player;
        if (player == null) {
            return null;
        }
        for (final ItemStack stack : new ItemStack[]{player.getMainHandItem(), player.getOffhandItem()}) {
            if (stack.isEmpty()) {
                continue;
            }
            final Object cell = Reflect.callStatic(STORAGE_CELLS, "getCellInventory", stack, null);
            if (cell != null) {
                return cell;
            }
        }
        return null;
    }

    private static Object poweredItem() {
        final Player player = Minecraft.getInstance().player;
        if (player == null) {
            return null;
        }
        for (final ItemStack stack : new ItemStack[]{player.getMainHandItem(), player.getOffhandItem()}) {
            if (!stack.isEmpty() && Reflect.isInstance(stack.getItem(), POWER_STORAGE)) {
                return stack;
            }
        }
        return null;
    }

    private static long number(final Object value) {
        return value instanceof final Number number ? number.longValue() : 0L;
    }

    private static double decimal(final Object value) {
        return value instanceof final Number number ? number.doubleValue() : 0.0D;
    }

    private static String format(final double value) {
        final double abs = Math.abs(value);
        if (abs >= 1.0E18D) {
            return String.format(Locale.ENGLISH, "%.1fE", value / 1.0E18D);
        }
        if (abs >= 1.0E15D) {
            return String.format(Locale.ENGLISH, "%.1fP", value / 1.0E15D);
        }
        if (abs >= 1.0E12D) {
            return String.format(Locale.ENGLISH, "%.1fT", value / 1.0E12D);
        }
        if (abs >= 1.0E9D) {
            return String.format(Locale.ENGLISH, "%.1fG", value / 1.0E9D);
        }
        if (abs >= 1.0E6D) {
            return String.format(Locale.ENGLISH, "%.1fM", value / 1.0E6D);
        }
        if (Math.abs(value - Math.rint(value)) < 0.05D) {
            return String.format(Locale.ENGLISH, "%.0f", value);
        }
        return String.format(Locale.ENGLISH, "%.1f", value);
    }

    private static String percent(final double used, final double total) {
        return total <= 0.0D ? "0.0" : String.format(Locale.ENGLISH, "%.1f", used * 100.0D / total);
    }

    public static class NetworkSource extends IntegrationTag {
        public NetworkSource() {
            super("ae2");
        }

        @Override
        protected String value() {
            return latest.source;
        }
    }

    public static class Channels extends IntegrationTag {
        public Channels() {
            super("ae2");
        }

        @Override
        protected String value() {
            return latest.linked ? Integer.toString(latest.channels) : "0";
        }
    }

    public static class ChannelMode extends IntegrationTag {
        public ChannelMode() {
            super("ae2");
        }

        @Override
        protected String value() {
            return latest.channelMode.isEmpty() ? "none" : latest.channelMode;
        }
    }

    public static class GridPower extends IntegrationTag {
        public GridPower() {
            super("ae2");
        }

        @Override
        protected String value() {
            return format(latest.storedPower);
        }
    }

    public static class GridMaxPower extends IntegrationTag {
        public GridMaxPower() {
            super("ae2");
        }

        @Override
        protected String value() {
            return format(latest.maxStoredPower);
        }
    }

    public static class GridPowerUsage extends IntegrationTag {
        public GridPowerUsage() {
            super("ae2");
        }

        @Override
        protected String value() {
            return format(latest.averageUsage);
        }
    }

    public static class GridPowerInjection extends IntegrationTag {
        public GridPowerInjection() {
            super("ae2");
        }

        @Override
        protected String value() {
            return format(latest.averageInjection);
        }
    }

    public static class ChannelPower extends IntegrationTag {
        public ChannelPower() {
            super("ae2");
        }

        @Override
        protected String value() {
            return format(latest.channelPower);
        }
    }

    public static class CellUsed extends IntegrationTag {
        public CellUsed() {
            super("ae2");
        }

        @Override
        protected String value() {
            return Long.toString(number(Reflect.call(cell(), "getUsedBytes")));
        }
    }

    public static class CellTotal extends IntegrationTag {
        public CellTotal() {
            super("ae2");
        }

        @Override
        protected String value() {
            return Long.toString(number(Reflect.call(cell(), "getTotalBytes")));
        }
    }

    public static class CellTypes extends IntegrationTag {
        public CellTypes() {
            super("ae2");
        }

        @Override
        protected String value() {
            return Long.toString(number(Reflect.call(cell(), "getStoredItemTypes")));
        }
    }

    public static class CellMaxTypes extends IntegrationTag {
        public CellMaxTypes() {
            super("ae2");
        }

        @Override
        protected String value() {
            return Long.toString(number(Reflect.call(cell(), "getTotalItemTypes")));
        }
    }

    public static class CellCount extends IntegrationTag {
        public CellCount() {
            super("ae2");
        }

        @Override
        protected String value() {
            return Long.toString(number(Reflect.call(cell(), "getStoredItemCount")));
        }
    }

    public static class CellPercent extends IntegrationTag {
        public CellPercent() {
            super("ae2");
        }

        @Override
        protected String value() {
            final Object cell = cell();
            return percent(number(Reflect.call(cell, "getUsedBytes")), number(Reflect.call(cell, "getTotalBytes")));
        }
    }

    public static class CellStatus extends IntegrationTag {
        public CellStatus() {
            super("ae2");
        }

        @Override
        protected String value() {
            final Object status = Reflect.call(cell(), "getStatus");
            return status == null ? "none" : String.valueOf(status).toLowerCase(Locale.ENGLISH);
        }
    }

    public static class ItemPower extends IntegrationTag {
        public ItemPower() {
            super("ae2");
        }

        @Override
        protected String value() {
            final Object stack = poweredItem();
            return format(decimal(Reflect.call(stack == null ? null : ((ItemStack) stack).getItem(), "getAECurrentPower", stack)));
        }
    }

    public static class ItemMaxPower extends IntegrationTag {
        public ItemMaxPower() {
            super("ae2");
        }

        @Override
        protected String value() {
            final Object stack = poweredItem();
            return format(decimal(Reflect.call(stack == null ? null : ((ItemStack) stack).getItem(), "getAEMaxPower", stack)));
        }
    }

    public static class ItemPowerPercent extends IntegrationTag {
        public ItemPowerPercent() {
            super("ae2");
        }

        @Override
        protected String value() {
            final Object stack = poweredItem();
            if (stack == null) {
                return "0.0";
            }
            final Object item = ((ItemStack) stack).getItem();
            return percent(decimal(Reflect.call(item, "getAECurrentPower", stack)),
                    decimal(Reflect.call(item, "getAEMaxPower", stack)));
        }
    }
}