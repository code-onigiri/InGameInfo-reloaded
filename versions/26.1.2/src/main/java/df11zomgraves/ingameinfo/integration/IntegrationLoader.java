package df11zomgraves.ingameinfo.integration;

import df11zomgraves.ingameinfo.InGameInfoXML;
import net.neoforged.fml.ModList;

/** Registers the built-in optional-mod tags, but only when the mod is present. */
public final class IntegrationLoader {
    private IntegrationLoader() {
    }

    public static void load() {
        if (loaded("create")) {
            CreateIntegration.register();
        }
        if (loaded("curios")) {
            CuriosIntegration.register();
        }
        if (loaded("sereneseasons")) {
            SereneSeasonsIntegration.register();
        }
        if (loaded("ars_nouveau")) {
            ArsNouveauIntegration.register();
        }
        if (loaded("irons_spellbooks")) {
            IronsSpellbooksIntegration.register();
        }
        if (loaded("toughasnails")) {
            ToughAsNailsIntegration.register();
        }
        if (loaded("vampirism")) {
            VampirismIntegration.register();
        }
        if (loaded("cold_sweat")) {
            ColdSweatIntegration.register();
        }
        if (loaded("thirst")) {
            ThirstWasTakenIntegration.register();
        }
        if (loaded("goety")) {
            GoetyIntegration.register();
        }
        if (loaded("paraglider")) {
            ParaglidersIntegration.register();
        }
        if (loaded("botania")) {
            BotaniaIntegration.register();
        }
        if (loaded("ae2")) {
            Ae2Integration.register();
        }
        EnergyIntegration.register();

        InGameInfoXML.logger.info("Registered optional mod integration tags.");
    }

    private static boolean loaded(final String modId) {
        return ModList.get() != null && ModList.get().isLoaded(modId);
    }
}