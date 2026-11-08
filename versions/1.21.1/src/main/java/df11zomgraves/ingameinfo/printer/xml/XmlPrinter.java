package df11zomgraves.ingameinfo.printer.xml;

import df11zomgraves.ingameinfo.InGameInfoXML;
import df11zomgraves.ingameinfo.printer.IPrinter;
import df11zomgraves.ingameinfo.reference.Alignment;
import df11zomgraves.ingameinfo.value.Value;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.File;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class XmlPrinter implements IPrinter {
    @Override
    public boolean print(final File file, final Map<Alignment, List<List<Value>>> format) {
        final DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        final DocumentBuilder dBuilder;
        File tempFile = null;
        try {
            dBuilder = dbFactory.newDocumentBuilder();
            final Document doc = dBuilder.newDocument();

            final Element config = doc.createElement("config");
            appendLines(doc, config, format);
            doc.appendChild(config);

            final TransformerFactory transformerFactory = TransformerFactory.newInstance();
            final Transformer transformer = transformerFactory.newTransformer();
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");

            // Write to a temp file first so a failed transform cannot
            // truncate the player's existing configuration.
            final File directory = file.getAbsoluteFile().getParentFile();
            tempFile = File.createTempFile("ingameinfo", ".tmp", directory);

            final DOMSource source = new DOMSource(doc);
            final StreamResult streamResult = new StreamResult(tempFile);
            transformer.transform(source, streamResult);

            try {
                Files.move(tempFile.toPath(), file.toPath(), StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
            } catch (final AtomicMoveNotSupportedException e) {
                Files.move(tempFile.toPath(), file.toPath(), StandardCopyOption.REPLACE_EXISTING);
            }
            tempFile = null;

            return true;
        } catch (final Exception e) {
            InGameInfoXML.logger.fatal("Could not save xml configuration file!", e);
        } finally {
            if (tempFile != null) {
                tempFile.delete();
            }
        }

        return false;
    }

    private void appendLines(final Document doc, final Element config, final Map<Alignment, List<List<Value>>> format) {
        for (final Alignment alignment : Alignment.values()) {
            final List<List<Value>> lists = format.get(alignment);
            if (lists != null) {
                final Element elementLines = doc.createElement("lines");
                elementLines.setAttribute("at", alignment.toString().toLowerCase(Locale.ENGLISH));

                appendLine(doc, elementLines, lists);

                config.appendChild(elementLines);
            }
        }
    }

    private void appendLine(final Document doc, final Element elementLines, final List<List<Value>> lines) {
        for (final List<Value> line : lines) {
            final Element elementLine = doc.createElement("line");

            appendValues(doc, elementLine, line);

            elementLines.appendChild(elementLine);
        }
    }

    private void appendValues(final Document doc, final Element elementValues, final List<Value> values) {
        for (final Value value : values) {
            final Element elementValue = doc.createElement(value.getType());

            elementValue.setTextContent(value.getRawValue(false));
            if (value.values.size() > 0) {
                appendValues(doc, elementValue, value.values);
            }

            elementValues.appendChild(elementValue);
        }
    }
}
