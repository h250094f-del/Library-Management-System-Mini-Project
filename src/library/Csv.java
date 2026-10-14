package library;

import java.util.ArrayList;
import java.util.List;

final class Csv {
    private Csv() {
    }

    static String row(String... fields) {
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < fields.length; i++) {
            if (i > 0) {
                result.append(',');
            }
            String field = fields[i] == null ? "" : fields[i];
            result.append('"').append(field.replace("\"", "\"\"")).append('"');
        }
        return result.toString();
    }

    static List<String> parse(String line) {
        List<String> fields = new ArrayList<>();
        StringBuilder field = new StringBuilder();
        boolean quoted = false;
        for (int i = 0; i < line.length(); i++) {
            char character = line.charAt(i);
            if (quoted) {
                if (character == '"') {
                    if (i + 1 < line.length() && line.charAt(i + 1) == '"') {
                        field.append('"');
                        i++;
                    } else {
                        quoted = false;
                    }
                } else {
                    field.append(character);
                }
            } else if (character == ',' && !quoted) {
                fields.add(field.toString());
                field.setLength(0);
            } else if (character == '"' && field.length() == 0) {
                quoted = true;
            } else {
                field.append(character);
            }
        }
        if (quoted) {
            throw new IllegalArgumentException("Unclosed quoted CSV field.");
        }
        fields.add(field.toString());
        return fields;
    }
}
