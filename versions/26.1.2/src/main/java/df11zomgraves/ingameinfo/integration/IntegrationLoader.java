package df11zomgraves.ingameinfo.integration;

import df11zomgraves.ingameinfo.InGameInfoXML;
import net.neoforged.fml.ModList;

/** Registers the built-in optional-mod tags, but only when the mod is present. */
public final class IntegrationLoader {
    private IntegrationLoader() {
    }

    public static void load() {
        register("create", CreateIntegration::register);
        register("curios", CuriosIntegration::register);
        register("sereneseasons", SereneSeasonsIntegration::register);
        register("ars_nouveau", ArsNouveauIntegration::register);
        register("irons_spellbooks", IronsSpellbooksIntegration::register);
        register("toughasnails", ToughAsNailsIntegration::register);
        register("vampirism", VampirismIntegration::register);
        register("cold_sweat", ColdSweatIntegration::register);
        register("thirst", ThirstWasTakenIntegration::register);
        register("goety", GoetyIntegration::register);
        register("paraglider", ParaglidersIntegration::register);
        register("botania", BotaniaIntegration::register);
        register("ae2", Ae2Integration::register);
        register("mekanism", MekanismIntegration::register);
        register("cobblemon", CobblemonIntegration::register);
        register("tconstruct", TinkersIntegration::register);
        try {
            EnergyIntegration.register();
        } catch (final Throwable t) {
            InGameInfoXML.logger.error("Failed to register energy integration tags.", t);
        }

        InGameInfoXML.logger.info("Registered optional mod integration tags.");
    }

    /** Registers one integration in isolation, so a failure cannot abort the remaining ones. */
    private static void register(final String modId, final Runnable registration) {
        if (!loaded(modId)) {
            return;
        }
        try {
            registration.run();
        } catch (final Throwable t) {
            InGameInfoXML.logger.error("Failed to register {} integration tags.", modId, t);
        }
    }

    private static boolean loaded(final String modId) {
        return ModList.get() != null && ModList.get().isLoaded(modId);
    }
}