package df11zomgraves.ingameinfo.integration;

import df11zomgraves.ingameinfo.tag.TagRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.InteractionHand;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.transfer.access.ItemAccess;
import net.neoforged.neoforge.transfer.energy.EnergyHandler;

import java.util.Locale;

/** Forge Energy / NeoForge Energy tags for the held item (works with Mekanism, Thermal, IE, ...). */
public final class EnergyIntegration {
    private EnergyIntegration() {
    }

    private static EnergyHandler energy() {
        final Player player = Minecraft.getInstance().player;
        if (player == null) {
            return null;
        }
        return player.getMainHandItem().getCapability(Capabilities.Energy.ITEM,
                ItemAccess.forPlayerInteraction(player, InteractionHand.MAIN_HAND));
    }

    public static class Energy extends IntegrationTag {
        public Energy() {
            super("energy");
        }

        @Override
        protected String value() {
            final EnergyHandler storage = energy();
            return storage == null ? "0" : String.valueOf(storage.getAmountAsInt());
        }
    }

    public static class MaxEnergy extends IntegrationTag {
        public MaxEnergy() {
            super("energy");
        }

        @Override
        protected String value() {
            final EnergyHandler storage = energy();
            return storage == null ? "0" : String.valueOf(storage.getCapacityAsInt());
        }
    }

    public static class EnergyPercent extends IntegrationTag {
        public EnergyPercent() {
            super("energy");
        }

        @Override
        protected String value() {
            final EnergyHandler storage = energy();
            if (storage == null || storage.getCapacityAsInt() <= 0) {
                return "0.0";
            }
            return String.format(Locale.ENGLISH, "%.1f", storage.getAmountAsInt() * 100.0D / storage.getCapacityAsInt());
        }
    }

    public static void register() {
        TagRegistry.INSTANCE.register(new Energy().setName("energy").setAliases("fe"));
        TagRegistry.INSTANCE.register(new MaxEnergy().setName("maxenergy").setAliases("maxfe"));
        TagRegistry.INSTANCE.register(new EnergyPercent().setName("energypercent").setAliases("fepercent"));
    }
}
