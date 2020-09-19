package converter;

import converter.intermediate.Intermediate;
import converter.intermediate.IntermediateConverter;
import converter.json.Json;
import converter.json.JsonConverter;
import converter.json.JsonParser;
import converter.xml.Xml;
import converter.xml.XmlConverter;
import converter.xml.XmlParser;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class Main {
    public static void main(String[] args) throws IOException {
        final String input = Files.readString(Path.of("test.txt"));
        if (JsonParser.isJson(input)) {
            final Json json = new JsonParser().parse(input);
            final Intermediate intermediate = IntermediateConverter.convert(json);
            final Xml xml = XmlConverter.convert(intermediate);
            System.out.println(xml);
        }
        if (XmlParser.isXml(input)) {
            final Xml xml = new XmlParser().parse(input);
            final Intermediate intermediate = IntermediateConverter.convert(xml);
            final Json json = JsonConverter.convert(intermediate);
            System.out.println(json);
        }
    }
}
