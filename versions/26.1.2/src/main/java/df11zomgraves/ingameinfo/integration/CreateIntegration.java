package df11zomgraves.ingameinfo.integration;

import df11zomgraves.ingameinfo.tag.TagRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;

import java.util.Locale;
import java.util.Map;

/** Create kinetic/stress tags for the targeted block or the ridden contraption. */
public final class CreateIntegration {
    private static final String KINETIC = "com.simibubi.create.content.kinetics.base.KineticBlockEntity";
    private static final String STRESS_GAUGE = "com.simibubi.create.content.kinetics.gauge.StressGaugeBlockEntity";
    private static final String CONTRAPTION_ENTITY = "com.simibubi.create.content.contraptions.AbstractContraptionEntity";

    private CreateIntegration() {
    }

    private static Object targetKinetic() {
        final Minecraft minecraft = Minecraft.getInstance();
        final Level level = minecraft.level;
        if (minecraft.hitResult == null || minecraft.hitResult.getType() != HitResult.Type.BLOCK || level == null) {
            return null;
        }
        final BlockPos pos = ((BlockHitResult) minecraft.hitResult).getBlockPos();
        final BlockEntity blockEntity = level.getBlockEntity(pos);
        return Reflect.isInstance(blockEntity, KINETIC) ? blockEntity : null;
    }

    private static Object riddenContraption() {
        final Player player = Minecraft.getInstance().player;
        final Entity vehicle = player == null ? null : player.getVehicle();
        return Reflect.isInstance(vehicle, CONTRAPTION_ENTITY) ? vehicle : null;
    }

    private static Object contraptionBlockEntity(final Object contraptionEntity) {
        final Object contraption = Reflect.call(contraptionEntity, "getContraption");
        final Object blocks = Reflect.call(contraption, "getBlocks");
        if (!(blocks instanceof Map<?, ?>)) {
            return null;
        }
        for (final Object key : ((Map<?, ?>) blocks).keySet()) {
            if (key instanceof BlockPos) {
                final Object blockEntity = Reflect.call(contraption, "getBlockEntityClientSide", key);
                if (Reflect.isInstance(blockEntity, KINETIC)) {
                    return blockEntity;
                }
            }
        }
        return null;
    }

    private static Object kinetic() {
        final Object target = targetKinetic();
        if (target != null) {
            return target;
        }
        final Object contraption = riddenContraption();
        return contraption == null ? null : contraptionBlockEntity(contraption);
    }

    private static float speed(final Object kinetic) {
        final Object value = Reflect.call(kinetic, "getSpeed");
        return value instanceof Number ? Math.abs(((Number) value).floatValue()) : 0.0F;
    }

    private static boolean isStressGauge(final Object blockEntity) {
        return Reflect.isInstance(blockEntity, STRESS_GAUGE);
    }

    private static float gaugeStress(final Object gauge) {
        final Object value = Reflect.call(gauge, "getNetworkStress");
        return value instanceof Number ? ((Number) value).floatValue() : 0.0F;
    }

    private static float gaugeCapacity(final Object gauge) {
        final Object value = Reflect.call(gauge, "getNetworkCapacity");
        return value instanceof Number ? ((Number) value).floatValue() : 0.0F;
    }

    public static class Rpm extends IntegrationTag {
        public Rpm() {
            super("create");
        }

        @Override
        protected String value() {
            final Object kinetic = kinetic();
            return kinetic == null ? "0.0" : String.format(Locale.ENGLISH, "%.1f", speed(kinetic));
        }
    }

    public static class Kinetic extends IntegrationTag {
        public Kinetic() {
            super("create");
        }

        @Override
        protected String value() {
            return String.valueOf(kinetic() != null);
        }
    }

    public static class Stress extends IntegrationTag {
        public Stress() {
            super("create");
        }

        @Override
        protected String value() {
            final Object blockEntity = kinetic();
            if (blockEntity == null) {
                return "0.0";
            }
            final float stress = isStressGauge(blockEntity) ? gaugeStress(blockEntity) : number("calculateStressApplied", blockEntity);
            return String.format(Locale.ENGLISH, "%.2f", stress);
        }
    }

    public static class StressCapacity extends IntegrationTag {
        public StressCapacity() {
            super("create");
        }

        @Override
        protected String value() {
            final Object blockEntity = kinetic();
            if (blockEntity == null) {
                return "0.0";
            }
            final float capacity = isStressGauge(blockEntity) ? gaugeCapacity(blockEntity) : number("calculateAddedStressCapacity", blockEntity);
            return String.format(Locale.ENGLISH, "%.2f", capacity);
        }
    }

    public static class StressPercent extends IntegrationTag {
        public StressPercent() {
            super("create");
        }

        @Override
        protected String value() {
            final Object blockEntity = kinetic();
            if (blockEntity == null) {
                return "0.0";
            }
            if (isStressGauge(blockEntity)) {
                final float capacity = gaugeCapacity(blockEntity);
                return String.format(Locale.ENGLISH, "%.1f", capacity <= 0 ? 0.0F : gaugeStress(blockEntity) * 100.0F / capacity);
            }
            final Object network = Reflect.call(blockEntity, "getOrCreateNetwork");
            final float capacity = number("calculateCapacity", network);
            final float stress = number("calculateStress", network);
            return String.format(Locale.ENGLISH, "%.1f", capacity <= 0 ? 0.0F : stress * 100.0F / capacity);
        }
    }

    public static class NetworkSize extends IntegrationTag {
        public NetworkSize() {
            super("create");
        }

        @Override
        protected String value() {
            final Object blockEntity = kinetic();
            if (blockEntity == null) {
                return "0";
            }
            final Object network = Reflect.call(blockEntity, "getOrCreateNetwork");
            final Object size = Reflect.call(network, "getSize");
            return size instanceof Number ? String.valueOf(((Number) size).intValue()) : "0";
        }
    }

    private static float number(final String method, final Object target) {
        final Object value = Reflect.call(target, method);
        return value instanceof Number ? ((Number) value).floatValue() : 0.0F;
    }

    public static void register() {
        TagRegistry.INSTANCE.register(new Rpm().setName("createrpm").setAliases("createspeed", "kineticrpm"));
        TagRegistry.INSTANCE.register(new Kinetic().setName("createkinetic").setAliases("kinetic"));
        TagRegistry.INSTANCE.register(new Stress().setName("createstress"));
        TagRegistry.INSTANCE.register(new StressCapacity().setName("createstresscapacity"));
        TagRegistry.INSTANCE.register(new StressPercent().setName("createstresspercent"));
        TagRegistry.INSTANCE.register(new NetworkSize().setName("createnetworksize"));
    }
}
