package df11zomgraves.ingameinfo.integration;

import df11zomgraves.ingameinfo.tag.TagRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.energy.IEnergyStorage;

import java.util.Locale;

/** Forge Energy tags for the held item (works with Mekanism, Thermal, IE, ...). */
public final class EnergyIntegration {
    private EnergyIntegration() {
    }

    private static IEnergyStorage energy() {
        final Player player = Minecraft.getInstance().player;
        return player == null ? null : player.getMainHandItem().getCapability(ForgeCapabilities.ENERGY).orElse(null);
    }

    public static class Energy extends IntegrationTag {
        public Energy() {
            super("energy");
        }

        @Override
        protected String value() {
            final IEnergyStorage storage = energy();
            return storage == null ? "0" : String.valueOf(storage.getEnergyStored());
        }
    }

    public static class MaxEnergy extends IntegrationTag {
        public MaxEnergy() {
            super("energy");
        }

        @Override
        protected String value() {
            final IEnergyStorage storage = energy();
            return storage == null ? "0" : String.valueOf(storage.getMaxEnergyStored());
        }
    }

    public static class EnergyPercent extends IntegrationTag {
        public EnergyPercent() {
            super("energy");
        }

        @Override
        protected String value() {
            final IEnergyStorage storage = energy();
            if (storage == null || storage.getMaxEnergyStored() <= 0) {
                return "0.0";
            }
            return String.format(Locale.ENGLISH, "%.1f", storage.getEnergyStored() * 100.0D / storage.getMaxEnergyStored());
        }
    }

    public static void register() {
        TagRegistry.INSTANCE.register(new Energy().setName("energy").setAliases("fe"));
        TagRegistry.INSTANCE.register(new MaxEnergy().setName("maxenergy").setAliases("maxfe"));
        TagRegistry.INSTANCE.register(new EnergyPercent().setName("energypercent").setAliases("fepercent"));
    }
}
