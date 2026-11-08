package df11zomgraves.ingameinfo.printer.text;

import df11zomgraves.ingameinfo.InGameInfoXML;
import df11zomgraves.ingameinfo.printer.IPrinter;
import df11zomgraves.ingameinfo.reference.Alignment;
import df11zomgraves.ingameinfo.value.Value;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class TextPrinter implements IPrinter {
    @Override
    public boolean print(final File file, final Map<Alignment, List<List<Value>>> format) {
        File tempFile = null;
        try {
            final File directory = file.getAbsoluteFile().getParentFile();
            tempFile = File.createTempFile("ingameinfo", ".tmp", directory);

            try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(tempFile), StandardCharsets.UTF_8))) {
                writeLines(writer, format);
            }

            try {
                Files.move(tempFile.toPath(), file.toPath(), StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
            } catch (final AtomicMoveNotSupportedException e) {
                Files.move(tempFile.toPath(), file.toPath(), StandardCopyOption.REPLACE_EXISTING);
            }
            tempFile = null;
            return true;
        } catch (final Exception e) {
            InGameInfoXML.logger.fatal("Could not save text configuration file!", e);
        } finally {
            if (tempFile != null) {
                tempFile.delete();
            }
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
