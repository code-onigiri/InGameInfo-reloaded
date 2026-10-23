package df11zomgraves.ingameinfo.printer.text;

import df11zomgraves.ingameinfo.InGameInfoXML;
import df11zomgraves.ingameinfo.printer.IPrinter;
import df11zomgraves.ingameinfo.reference.Alignment;
import df11zomgraves.ingameinfo.value.Value;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class TextPrinter implements IPrinter {
    @Override
    public boolean print(final File file, final Map<Alignment, List<List<Value>>> format) {
        try {
            final FileWriter fileWriter = new FileWriter(file);
            final BufferedWriter writer = new BufferedWriter(fileWriter);

            writeLines(writer, format);

            writer.close();
            fileWriter.close();
            return true;
        } catch (final Exception e) {
            InGameInfoXML.logger.fatal("Could not save text configuration file!", e);
        }

        return false;
    }

    private void writeLines(final BufferedWriter writer, final Map<Alignment, List<List<Value>>> format) throws IOException {
        for (final Alignment alignment : Alignment.values()) {
            final List<List<Value>> lists = format.get(alignment);
            if (lists != null) {
                writer.write(String.format("<%s>", alignment.toString().toLowerCase(Locale.ENGLISH)));

                for (final List<Value> line : lists) {
                    writer.write(TextSerializer.serialize(line));
                    writer.write("\n");
                }
            }
        }
    }
}
