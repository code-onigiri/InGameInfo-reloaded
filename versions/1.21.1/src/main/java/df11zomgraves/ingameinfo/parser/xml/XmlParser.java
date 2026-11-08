package df11zomgraves.ingameinfo.parser.xml;

import df11zomgraves.ingameinfo.InGameInfoXML;
import df11zomgraves.ingameinfo.parser.IParser;
import df11zomgraves.ingameinfo.reference.Alignment;
import df11zomgraves.ingameinfo.value.Value;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class XmlParser implements IParser {
    private static final int MAX_VALUE_DEPTH = 100;

    private Document document;

    @Override
    public boolean load(final InputStream inputStream) throws ParserConfigurationException, SAXException, IOException {
		final DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
		// The file is user-supplied: never expand DOCTYPE or external entities.
		try {
			dbFactory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
		} catch (final ParserConfigurationException e) {
			dbFactory.setFeature("http://xml.org/sax/features/external-general-entities", false);
			dbFactory.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
			dbFactory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
		}
		dbFactory.setXIncludeAware(false);
		dbFactory.setExpandEntityReferences(false);
		final DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
		this.document = dBuilder.parse(inputStream);
		this.document.getDocumentElement().normalize();
		return true;
    }

    @Override
    public boolean parse(final Map<Alignment, List<List<Value>>> format) {
        if (this.document == null) {
            return false;
        }

        final Element documentElement = this.document.getDocumentElement();
        final NodeList nodeListLines = documentElement.getChildNodes();
        for (int i = 0; i < nodeListLines.getLength(); i++) {
            final Element elementLines = getElement(nodeListLines.item(i), "lines");
            if (elementLines != null) {
                final Alignment alignment = Alignment.parse(elementLines.getAttribute("at"));
                if (alignment != null) {
                    format.put(alignment, getLines(elementLines));
                } else {
                    InGameInfoXML.logger.error("Skipping <lines> block with unknown alignment at=\"{}\" in xml configuration file.", elementLines.getAttribute("at"));
                }
            }
        }

        return true;
    }

    private List<List<Value>> getLines(final Element element) {
        final List<List<Value>> listLines = new ArrayList<List<Value>>();

        final NodeList nodeListLine = element.getChildNodes();
        for (int i = 0; i < nodeListLine.getLength(); i++) {
            final Element elementLine = getElement(nodeListLine.item(i), "line");
            if (elementLine != null) {
                listLines.add(getValues(elementLine));
            }
        }

        return listLines;
    }

    private List<Value> getValues(final Element element) {
        return getValues(element, 0);
    }

    private List<Value> getValues(final Element element, final int depth) {
        final List<Value> values = new ArrayList<Value>();

        if (depth > MAX_VALUE_DEPTH) {
            InGameInfoXML.logger.error("Skipping xml values nested deeper than {} levels in xml configuration file.", MAX_VALUE_DEPTH);
            return values;
        }

        final NodeList nodeListValues = element.getChildNodes();
        for (int i = 0; i < nodeListValues.getLength(); i++) {
            final Element elementValue = getElement(nodeListValues.item(i));
            if (elementValue != null) {
                final String type = elementValue.getNodeName().equalsIgnoreCase("value") ? elementValue.getAttribute("type") : elementValue.getNodeName();
                final Value value = Value.fromString(type);

                if (!value.isValid()) {
                    InGameInfoXML.logger.error("Skipping unknown xml value type \"{}\" in xml configuration file.", type);
                    continue;
                }

                if (value.isSimple()) {
                    value.setRawValue(elementValue.getTextContent(), false);
                } else {
                    value.values.addAll(getValues(elementValue, depth + 1));
                }
                values.add(value);
            }
        }

        return values;
    }

    private Element getElement(final Node node) {
        if (node.getNodeType() == Node.ELEMENT_NODE) {
            return (Element) node;
        }

        return null;
    }

    private Element getElement(final Node node, final String name) {
        final Element element = getElement(node);
        if (element != null && element.getNodeName().equalsIgnoreCase(name)) {
            return element;
        }

        return null;
    }
}
