package in.precisiontestautomation.utils;

import io.restassured.path.json.JsonPath;
import lombok.Getter;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Objects;

/**
 * Facilitates reading JSON data from files and provides methods to extract specific values from the JSON structure.
 * This class supports reading JSON content from a file path and fetching data using JSON path expressions.
 */
public class JsonFileReader {

    @Getter
    private String jsonString;

    /**
     * Private constructor to prevent external instantiation. Use {@link #getInstance()} to obtain an instance.
     */
    private JsonFileReader() {
    }

    /**
     * Provides a new instance of JsonFileReader. This method supports the use case where separate readers
     * with different JSON contents are needed.
     * @return a new instance of JsonFileReader.
     */
    public static JsonFileReader getInstance() {
        return new JsonFileReader();
    }

    /**
     * Reads JSON content from a specified file path into a string.
     * @param jsonFilePath the path to the JSON file to be read.
     * @return the JsonFileReader instance with loaded JSON string.
     * @throws RuntimeException if the file cannot be found or read.
     */
    public JsonFileReader readJson(String jsonFilePath) {
        try {
            jsonString = new String(Files.readAllBytes(Paths.get(jsonFilePath)));
        } catch (IOException ioe) {
            throw new RuntimeException(jsonFilePath + "\n is not located");
        }
        return this;
    }

    /**
     * Retrieves a value from the loaded JSON string using a JSON path expression.
     * If the path matches a system environment variable or system property, that value is returned instead.
     * @param path the JSON path expression or system property/environment variable name.
     * @return the value fetched from JSON or system properties, cast to the expected type.
     */
    public <T> T getJsonValueByPath(String path) {
        return JsonPath.from(jsonString).get(path);
    }

    /**
     * Sets the JSON string manually to this reader.
     * @param jsonString the JSON string to set.
     * @return the JsonFileReader instance for fluent chaining.
     */
    public JsonFileReader setJsonString(String jsonString) {
        this.jsonString = jsonString;
        return this;
    }

}
