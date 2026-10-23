package df11zomgraves.ingameinfo.integration;

import df11zomgraves.ingameinfo.tag.TagRegistry;

import java.util.Collections;
import java.util.List;
import java.util.Locale;

/** Cobblemon party tags read from the client-side party storage. */
public final class CobblemonIntegration {
    private static final String CLIENT = "com.cobblemon.mod.common.client.CobblemonClient";

    private CobblemonIntegration() {
    }

    private static Object party() {
        final Object client = Reflect.staticField(CLIENT, "INSTANCE");
        final Object storage = Reflect.call(client, "getStorage");
        return Reflect.call(storage, "getParty");
    }

    private static List<?> slots() {
        final Object slots = Reflect.call(party(), "getSlots");
        return slots instanceof List<?> ? (List<?>) slots : Collections.emptyList();
    }

    private static Object pokemon(final int slot) {
        final List<?> slots = slots();
        final int index = slot - 1;
        return index >= 0 && index < slots.size() ? slots.get(index) : null;
    }

    private static int size() {
        int count = 0;
        for (final Object pokemon : slots()) {
            if (pokemon != null) {
                count++;
            }
        }
        return count;
    }

    private static String text(final Object component) {
        if (component == null) {
            return "";
        }
        final Object value = Reflect.call(component, "getString");
        return value != null ? value.toString() : component.toString();
    }

    private static String species(final int slot) {
        final Object pokemon = pokemon(slot);
        final Object species = pokemon == null ? null : Reflect.call(pokemon, "getSpecies");
        final Object name = species == null ? null : Reflect.call(species, "getName");
        return name == null ? "" : name.toString();
    }

    private static String displayName(final int slot) {
        final Object pokemon = pokemon(slot);
        if (pokemon == null) {
            return "";
        }
        final String name = text(Reflect.call(pokemon, "getDisplayName", true));
        return name.isEmpty() ? species(slot) : name;
    }

    private static int integer(final Object value) {
        return value instanceof Number ? ((Number) value).intValue() : 0;
    }

    private static String status(final int slot) {
        final Object pokemon = pokemon(slot);
        if (pokemon == null) {
            return "";
        }
        if (Boolean.TRUE.equals(Reflect.call(pokemon, "isFainted"))) {
            return "fainted";
        }
        final Object container = Reflect.call(pokemon, "getStatus");
        final Object status = container == null ? null : Reflect.call(container, "getStatus");
        if (status == null) {
            return "none";
        }
        final Object name = Reflect.call(status, "getName");
        final Object path = name == null ? null : Reflect.call(name, "getPath");
        if (path != null) {
            return path.toString();
        }
        final Object showdown = Reflect.call(status, "getShowdownName");
        return showdown == null ? "none" : showdown.toString().toLowerCase(Locale.ENGLISH);
    }

    private static String types(final int slot) {
        final Object pokemon = pokemon(slot);
        if (pokemon == null) {
            return "";
        }
        final String primary = typeName(Reflect.call(pokemon, "getPrimaryType"));
        final String secondary = typeName(Reflect.call(pokemon, "getSecondaryType"));
        if (primary.isEmpty()) {
            return secondary;
        }
        return secondary.isEmpty() ? primary : primary + "/" + secondary;
    }

    private static String typeName(final Object type) {
        final Object name = type == null ? null : Reflect.call(type, "getName");
        return name == null ? "" : name.toString().toLowerCase(Locale.ENGLISH);
    }

    private static String value(final int slot, final String field) {
        final Object pokemon = pokemon(slot);
        if (pokemon == null) {
            return "";
        }
        switch (field) {
        case "species":
            return species(slot);
        case "nickname":
            return displayName(slot);
        case "level":
            return Integer.toString(integer(Reflect.call(pokemon, "getLevel")));
        case "hp":
            return Integer.toString(integer(Reflect.call(pokemon, "getCurrentHealth")));
        case "maxhp":
            return Integer.toString(integer(Reflect.call(pokemon, "getMaxHealth")));
        case "status":
            return status(slot);
        case "shiny":
            return Boolean.TRUE.equals(Reflect.call(pokemon, "getShiny")) ? "true" : "false";
        case "type":
            return types(slot);
        default:
            return "";
        }
    }

    private static String compact(final int slot) {
        final Object pokemon = pokemon(slot);
        if (pokemon == null) {
            return "";
        }
        return String.format(Locale.ENGLISH, "%s Lv.%d %d/%d", species(slot),
                integer(Reflect.call(pokemon, "getLevel")),
                integer(Reflect.call(pokemon, "getCurrentHealth")),
                integer(Reflect.call(pokemon, "getMaxHealth")));
    }

    public static class PartySize extends IntegrationTag {
        public PartySize() {
            super("cobblemon");
        }

        @Override
        protected String value() {
            return Integer.toString(size());
        }
    }

    public static class PartyField extends IntegrationTag {
        private final int slot;
        private final String field;

        public PartyField(final int slot, final String field) {
            super("cobblemon");
            this.slot = slot;
            this.field = field;
        }

        @Override
        protected String value() {
            return CobblemonIntegration.value(this.slot, this.field);
        }
    }

    public static class PartyCompact extends IntegrationTag {
        private final int slot;

        public PartyCompact(final int slot) {
            super("cobblemon");
            this.slot = slot;
        }

        @Override
        protected String value() {
            return compact(this.slot);
        }
    }

    public static void register() {
        TagRegistry.INSTANCE.register(new PartySize().setName("cobblemonpartysize"));
        final String[] fields = { "species", "nickname", "level", "hp", "maxhp", "status", "shiny", "type" };
        for (int slot = 1; slot <= 6; slot++) {
            for (final String field : fields) {
                final PartyField tag = new PartyField(slot, field);
                tag.setName("cobblemonparty" + slot + field);
                if (field.equals("species")) {
                    tag.setAliases("cobblemonparty" + slot + "name");
                } else if (field.equals("hp")) {
                    tag.setAliases("cobblemonparty" + slot + "health");
                } else if (field.equals("maxhp")) {
                    tag.setAliases("cobblemonparty" + slot + "maxhealth");
                }
                TagRegistry.INSTANCE.register(tag);
            }
            TagRegistry.INSTANCE.register(new PartyCompact(slot).setName("cobblemonparty" + slot)
                    .setAliases("cobblemonparty" + slot + "summary"));
        }
    }
}
