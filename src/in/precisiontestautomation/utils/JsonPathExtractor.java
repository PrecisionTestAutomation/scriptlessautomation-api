package in.precisiontestautomation.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.*;

/**
 * Provides functionality to extract values from a JSON string based on JSON paths.
 * This class utilizes a ThreadLocal singleton pattern to ensure thread-safe operations,
 * allowing it to be used concurrently across multiple threads without interference.
 */
public class JsonPathExtractor {

    /**
     * Private constructor to prevent direct instantiation.
     * Instances should be obtained through the {@link #getInstance()} method to ensure
     * that each thread receives its own instance.
     */
    private JsonPathExtractor(){
    }

    /**
     * ThreadLocal storage for JsonPathExtractor instances, ensuring each thread has its own instance.
     */
    private static final ThreadLocal<JsonPathExtractor> THREAD_LOCAL_INSTANCE =
            ThreadLocal.withInitial(JsonPathExtractor::new);

    /**
     * Provides access to the singleton instance of the JsonPathExtractor for the current thread.
     * @return the singleton instance specific to the current thread.
     */
    public static JsonPathExtractor getInstance(){
        return THREAD_LOCAL_INSTANCE.get();
    }

    /**
     * Extracts all paths and their corresponding values from a JSON string starting from a specified root path.
     * @param root the root path from which to start the search in the JSON structure.
     * @param jsonString the JSON string from which paths are to be extracted.
     * @return a HashMap where each key is a JSON path and each value is the content found at that path in the JSON structure.
     * @throws RuntimeException if there is an error processing the JSON string.
     */
    public HashMap<String,Object> getMapJsonPathValue(String root,String jsonString){
        ObjectMapper mapper = new ObjectMapper();
        JsonNode rootNode;
        try {
            rootNode = mapper.readTree(jsonString);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

        HashMap<String,Object> paths = new HashMap<>();
        generateJsonPaths(new StringBuilder(root), rootNode, paths);
        return paths;
    }

    /**
     * Recursively generates JSON paths for all elements within a given JSON node.
     * @param currentPath the current path being constructed.
     * @param currentNode the current node being evaluated.
     * @param paths the map where generated paths and their corresponding values are stored.
     */
    private void generateJsonPaths(StringBuilder currentPath, JsonNode currentNode, HashMap<String, Object> paths) {
        if (currentNode.isObject()) {
            Iterator<Map.Entry<String, JsonNode>> fields = currentNode.fields();
            int originalLength = currentPath.length();
            while (fields.hasNext()) {
                Map.Entry<String, JsonNode> field = fields.next();
                if (originalLength != 0) {
                    currentPath.append(".");
                }
                currentPath.append(field.getKey());
                generateJsonPaths(currentPath, field.getValue(), paths);
                currentPath.setLength(originalLength); // Reset the StringBuilder to its original state after recursion
            }
        } else if (currentNode.isArray()) {
            int originalLength = currentPath.length();
            for (int i = 0; i < currentNode.size(); i++) {
                currentPath.append("[").append(i).append("]");
                generateJsonPaths(currentPath, currentNode.get(i), paths);
                currentPath.setLength(originalLength);
            }
        } else {
            paths.put(currentPath.toString(), convertJsonNode(currentNode)); // Assuming convertJsonNode method exists
        }
    }

    /**
     * Converts a JsonNode to an appropriate Java object based on its type.
     * @param node the JsonNode to convert.
     * @return the Java object corresponding to the JsonNode's type (String, Integer, Long, Double, Boolean, etc.).
     */
    private Object convertJsonNode(JsonNode node) {
        if (node.isTextual()) {
            return node.asText();
        } else if (node.isInt()) {
            return node.asInt();
        } else if (node.isLong()) {
            return node.asLong();
        } else if (node.isDouble()) {
            return node.asDouble();
        } else if (node.isBoolean()) {
            return node.asBoolean();
        } else {
            return node.toString(); // Fallback for complex types (arrays, objects)
        }
    }

    /**
     * Retrieves a list of all JSON paths from a given JSON string starting from a specified root path.
     * This method is useful for obtaining a quick overview of all paths available in a JSON structure.
     * @param root the root path from which to start the extraction in the JSON structure.
     * @param jsonString the JSON string to analyze.
     * @return a list of strings, each representing a unique path within the JSON structure.
     */
    public List<String> getListOfJsonPaths(String root,String jsonString){
        HashMap<String,Object> jsonMaps = getMapJsonPathValue(root,jsonString);
        List<String> jsonPathLists = new ArrayList<>();
        jsonMaps.entrySet().stream().iterator().forEachRemaining(maps -> {
            jsonPathLists.add(maps.getKey());
        });

        return jsonPathLists;
    }

}
