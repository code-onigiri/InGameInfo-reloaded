package df11zomgraves.ingameinfo.printer.json;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import df11zomgraves.ingameinfo.InGameInfoXML;
import df11zomgraves.ingameinfo.printer.IPrinter;
import df11zomgraves.ingameinfo.reference.Alignment;
import df11zomgraves.ingameinfo.value.Value;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class JsonPrinter implements IPrinter {
    @Override
    public boolean print(final File file, final Map<Alignment, List<List<Value>>> format) {
        File tempFile = null;
        try {
            final File directory = file.getAbsoluteFile().getParentFile();
            tempFile = File.createTempFile("ingameinfo", ".tmp", directory);

            final JsonObject jsonConfig = new JsonObject();

            appendLines(jsonConfig, format);

            final Gson gson = new GsonBuilder().setPrettyPrinting().create();
            try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(tempFile), StandardCharsets.UTF_8))) {
                writer.write(gson.toJson(jsonConfig));
            }

            try {
                Files.move(tempFile.toPath(), file.toPath(), StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
            } catch (final AtomicMoveNotSupportedException e) {
                Files.move(tempFile.toPath(), file.toPath(), StandardCopyOption.REPLACE_EXISTING);
            }
            tempFile = null;
            return true;
        } catch (final Exception e) {
            InGameInfoXML.logger.fatal("Could not save json configuration file!", e);
        } finally {
            if (tempFile != null) {
                tempFile.delete();
            }
        }

        return false;
    }

    private void appendLines(final JsonObject jsonConfig, final Map<Alignment, List<List<Value>>> format) {
        for (final Alignment alignment : Alignment.values()) {
            final List<List<Value>> lists = format.get(alignment);
            if (lists != null) {
                final JsonArray arrayLines = new JsonArray();

                appendLine(arrayLines, lists);

                jsonConfig.add(alignment.toString().toLowerCase(Locale.ENGLISH), arrayLines);
            }
        }
    }

    private void appendLine(final JsonArray jsonLines, final List<List<Value>> lines) {
        for (final List<Value> line : lines) {
            final JsonArray arrayLine = new JsonArray();

            appendValues(arrayLine, line);

            jsonLines.add(arrayLine);
        }
    }

    private void appendValues(final JsonArray jsonValues, final List<Value> values) {
        for (final Value value : values) {
            final JsonObject obj = new JsonObject();

            final String type = value.getType();
            if (value.values.size() > 0) {
                final JsonArray array = new JsonArray();
                appendValues(array, value.values);
                obj.add(type, array);
            } else {
                // Always write strings as strings: the parser reads them back
                // with getAsString(), so a numeric-looking value such as
                // "0012" must not be re-typed into a JSON number.
                obj.addProperty(type, value.getRawValue(false));
            }

            jsonValues.add(obj);
        }
    }
}
