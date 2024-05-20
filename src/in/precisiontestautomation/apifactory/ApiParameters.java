package in.precisiontestautomation.apifactory;

import in.precisiontestautomation.utils.ApiFrameworkActions;
import in.precisiontestautomation.utils.ApiKeyInitializers;
import in.precisiontestautomation.scriptlessautomation.core.exceptionhandling.PrecisionTestException;
import lombok.Getter;
import lombok.Setter;
import in.precisiontestautomation.tests.API;

import java.util.*;

/**
 * This class is designed to handle the parameters required for setting up and executing API requests.
 * It stores and manages API request details such as endpoint, method, headers, and parameters.
 * Utilizing ThreadLocal, this class ensures that each thread can have its own set of API parameters,
 * which is particularly useful in multi-threaded testing environments to prevent cross-thread data interference.
 *
 * @author PTA-dev
 * @version 1.2
 * @since 2024-05-02
 */
public class ApiParameters {
    @Getter @Setter private String endpoint;
    @Getter @Setter private String method;
    @Getter @Setter private List<String> headerKeys;
    @Getter @Setter private List<Object> headerValues;
    @Getter @Setter private List<String> paramsKeys;
    @Getter @Setter private List<Object> paramsValues;
    @Getter @Setter private List<String> jsonPath;
    @Getter @Setter private List<Object> expectedValues;
    @Getter @Setter private List<Object> storeValue;
    @Getter @Setter private List<String> bodyKey;
    @Getter @Setter private List<Object> bodyValue;
    @Getter @Setter private String responseStatusCode;
    @Getter @Setter private List<String> authKeys;
    @Getter @Setter private List<Object> authValues;

    private List<String[]> rows;

    private ApiParameters(List<String[]> rows) {
        this.rows = rows;
    }

    /**
     * Returns a ThreadLocal instance of ApiParameters, which encapsulates API request data
     * specific to the current thread.
     *
     * @param rows Data extracted from a CSV file or similar data source, parsed into a list of string arrays.
     * @return ThreadLocal instance of ApiParameters
     */
    public static ThreadLocal<ApiParameters> getInstance(List<String[]> rows) {
        return ThreadLocal.withInitial(() -> new ApiParameters(rows));
    }

    /**
     * Parses the tests data from CSV rows into structured API request parameters.
     * Sets various attributes such as headers, body, authentication keys/values based on the row data.
     *
     * @return ApiRequester instance prepared with the parsed and structured API request data.
     */
    public ApiRequester parseTestData() {
        for (String[] row : rows) {
            String fieldType = row[0].trim();
            switch (fieldType) {
                case "END_POINT":
                    setEndpoint(setValue("END_POINT", row[1].trim()));
                    break;
                case "METHOD":
                    setMethod(row[1].trim());
                    break;
                case "PARAMS:KEY":
                    ArrayList<String> paramKey = new ArrayList<>(Arrays.asList(row));
                    paramKey.remove(0);
                    setParamsKeys(paramKey);
                    break;
                case "PARAMS:VALUE":
                    ArrayList<Object> paramsValue = new ArrayList<>(Arrays.asList(row));
                    paramsValue.remove(0);
                    setParamsValues(setValue("PARAMS:VALUE", paramsValue));
                    break;
                case "AUTH:KEY":
                    ArrayList<String> authKeys = new ArrayList<>(Arrays.asList(row));
                    authKeys.remove(0);
                    setAuthKeys(authKeys);
                    break;
                case "AUTH:VALUE":
                    ArrayList<Object> authValue = new ArrayList<>(Arrays.asList(row));
                    authValue.remove(0);
                    setAuthValues(setValue("AUTH:VALUE", authValue));
                    break;
                case "HEADERS:KEY":
                    ArrayList<String> headerKey = new ArrayList<>(Arrays.asList(row));
                    headerKey.remove(0);
                    setHeaderKeys(headerKey);
                    break;
                case "HEADERS:VALUE":
                    ArrayList<Object> headerValue = new ArrayList<>(Arrays.asList(row));
                    headerValue.remove(0);
                    setHeaderValues(setValue("HEADERS:VALUE", headerValue));
                    break;
                case "BODY:KEY":
                    ArrayList<String> bodyKey = new ArrayList<>(Arrays.asList(row));
                    bodyKey.remove(0);
                    setBodyKey(bodyKey);
                    break;
                case "BODY:VALUE":
                    ArrayList<Object> bodyValue = new ArrayList<>(Arrays.asList(row));
                    bodyValue.remove(0);
                    setBodyValue(setValue("BODY:VALUE", bodyValue));
                    break;

                case "RESPONSE:JSON_PATH":
                    ArrayList<String> jsonPath = new ArrayList<>(Arrays.asList(row));
                    jsonPath.remove(0);
                    setJsonPath(jsonPath);
                    break;
                case "RESPONSE:EXPECTED_VALUE":
                    ArrayList<Object> expectedValues = new ArrayList<>(Arrays.asList(row));
                    expectedValues.remove(0);
                    setExpectedValues(setValue("RESPONSE:EXPECTED_VALUE", expectedValues));
                    break;
                case "RESPONSE:STORE_VALUE":
                    ArrayList<Object> storeValues = new ArrayList<>(Arrays.asList(row));
                    storeValues.remove(0);
                    setStoreValue(setValue("RESPONSE:STORE_VALUE", storeValues));
                    break;
                case "DEPENDANT_TEST_CASE":
                    if (row[1].trim().equals("NONE"))
                        break;
                    API.getInstance().testRunner(ApiFrameworkActions.getFileWithStartName(row[1].trim()),false);
                    break;
                case "RESPONSE:CODE":
                    setResponseStatusCode(row[1].trim());
            }
        }
        return ApiRequester.getInstance(this).get();
    }

    /**
     * Consolidates various parameter lists (headers, parameters, body, authentication) into a single map.
     *
     * @return Map containing the complete set of API request parameters.
     */
    public Map<String, Object> getRequestParameters() {
        Map<String, Object> params = mergeListsToMap("params", getParamsKeys(), getParamsValues());
        Map<String, Object> headers = mergeListsToMap("headers", getHeaderKeys(), getHeaderValues());
        Map<String, Object> body = mergeListsToMap("body", getBodyKey(), getBodyValue());

        Map<String, Object> auth = mergeListsToMap("auth", getAuthKeys(), getAuthValues());

        Map<String, Object> requestParameters = new HashMap<>();

        if (params != null && !params.isEmpty()) {
            requestParameters.put("params", params);
        }

        if (headers != null && !headers.isEmpty()) {
            requestParameters.put("headers", headers);
        }

        if (!Objects.isNull(body) && !body.isEmpty()) {
            requestParameters.put("body", body);
        }

        if (!Objects.isNull(auth) && !auth.isEmpty()) {
            requestParameters.put("auth", auth);
        }

        requestParameters.put("endpoint", getEndpoint());
        requestParameters.put("method", getMethod());

        return requestParameters;
    }


    /**
     * Merges two lists into a map, ensuring keys and values align. This method is specifically designed
     * to handle different data types appropriately by converting booleans or invoking custom class methods
     * based on the values' content. This flexibility allows for dynamic API testing scenarios.
     *
     * Example Usage:
     * Map&lt;String, Object&gt; params = mergeListsToMap("params", getParamsKeys(), getParamsValues());
     * This example merges parameter keys with their corresponding values to create a map that can be used
     * in setting up API request parameters.
     *
     * @param step A string that indicates the part of the API request this map represents (e.g., "params", "auth").
     * @param keys A list of strings representing the keys in the resulting map.
     * @param values A list of objects representing the values in the resulting map. The handling of these values
     *               can be specialized based on their content, including boolean conversion and custom method invocation.
     * @return A map where each key is associated with its corresponding value, properly processed.
     */
    public Map<String, Object> mergeListsToMap(String step, List<String> keys, List<Object> values) {
        Map<String, Object> mergedMap = new HashMap<>();
        Object value;
        try {
            if(Objects.nonNull(keys)) {
                for (int i = 0; i < keys.size(); i++) {
                    if (!keys.get(i).equals("NONE") && !keys.get(i).isEmpty()) {
                        if (BooleanVerifier.isValidBoolean(values.get(i).toString())) {
                            value = Boolean.parseBoolean(values.get(i).toString());
                        } else if(values.get(i).toString().split(":")[0].equalsIgnoreCase("Custom") && values.get(i).toString().split(":").length>1) {
                            String[] valueArr = values.get(i).toString().split(":");
                            value= ApiFrameworkActions.invokeCustomClassMethods(valueArr[1].trim(),valueArr[2].trim());

                        } else{
                                value = values.get(i);
                        }
                        mergedMap.put(keys.get(i), value);
                    }
                }
            }
        } catch (Exception e) {
            throw new PrecisionTestException(step.toUpperCase() + ":Error while merging the list into map keys:"+keys+" values:"+values);
        }

        return mergedMap;
    }

    /**
     * Merges two lists into a map where the keys are of type Object and values are of type String. This method is typically
     * used to link response objects with their corresponding JSON paths as part of handling API responses.
     *
     * Example Usage:
     * Map&lt;String, Object&gt; apiResponseObjects = testParameters.mergeListsToMaps(testParameters.getStoreValue(), testParameters.getJsonPath());
     * This usage example demonstrates merging stored values with their corresponding JSON paths to create a map
     * that can be used to easily access API response data based on the structure defined in JSON paths.
     *
     * @param keys A list of Objects that will serve as the keys in the resulting map. Typically, these are identifiers or unique labels.
     * @param values A list of Strings that will serve as the values in the resulting map, often corresponding to JSON paths in API testing.
     * @return A map where each key, converted to a String, is associated with its corresponding String value.
     */
    public Map<String, String> mergeListsToMaps(List<Object> keys, List<String> values) {
        Map<String, String> mergedMap = new HashMap<>();
        for (int i = 0; i < keys.size(); i++) {
            if (!keys.get(i).equals("NONE") && !keys.get(i).toString().isEmpty()) {
                mergedMap.put(keys.get(i).toString(), values.get(i));
            }
        }

        return mergedMap;
    }

    /**
     * Validates if a given string can be interpreted as a boolean value.
     * This is used for parsing string values that need to be converted to boolean.
     */
    public class BooleanVerifier {
        /**
         * Checks if the provided string can be interpreted as a boolean value.
         * This method accepts a string input and determines whether it matches
         * "true" or "false" regardless of the case sensitivity, which are the only valid
         * boolean string representations in Java.
         *
         * @param input the string to check for boolean validity
         * @return true if the input is "true" or "false" (case insensitive), false otherwise
         */
        public static boolean isValidBoolean(String input) {
            return input.equalsIgnoreCase("true") || input.equalsIgnoreCase("false");
        }
    }

    /**
     * Transforms a list of object values based on specific rules defined as "PreFlow" or "ApiGlobalVariables".
     * This method is crucial for preprocessing values before they are used in an API request, ensuring they meet
     * the required format or derive necessary data dynamically.
     *
     * @param step The context or category of values being processed, aiding in error identification.
     * @param valueList A list of objects containing the raw values to be processed. Each value may contain
     *                  special instructions on how it should be transformed.
     * @return A new list of objects with all values transformed according to the specified rules.
     */
    public List<Object> setValue(String step, ArrayList<Object> valueList) {
        List<Object> newValueList = new ArrayList<>();
        try {
            valueList.stream()
                    .forEach(value -> {

                        String[] values = value.toString().split(":");

                        switch (values[0]) {
                            case "PreFlow":
                                newValueList.add(ApiFrameworkActions.fetchPreFlow(value.toString()).trim());
                                break;
                            case "ApiGlobalVariables":
                                newValueList.add(ApiKeyInitializers.getGlobalVariables().get().get(values[1].trim()));
                                break;
                            default:
                                newValueList.add(value);
                                break;
                        }
                    });
        } catch (Exception e) {
            throw new PrecisionTestException(step.toUpperCase() + ":Error while reading the value");
        }

        return newValueList;
    }

    /**
     * Modifies an endpoint string by inserting global variables where specified. This method is particularly useful
     * for dynamically configuring API endpoints with values that may change or need to be injected at runtime.
     * The endpoint string can include a placeholder that indicates a variable from the global settings should be
     * inserted at that place.
     *
     * Example Usage:
     * String processedEndpoint = setValue("API Endpoint", "http://api.example.com/data?user=ApiGlobalVariables:user_id");
     * If "user_id" is a key in the global variables map with a value of "12345", the processedEndpoint would become:
     * "http://api.example.com/data?user=12345"
     *
     * @param step Describes the context in which this method is called, e.g., "API Endpoint".
     *             This is used primarily for error reporting.
     * @param endpoint The original endpoint string that may contain placeholders for global variables.
     * @return A string where any placeholders are replaced with actual values from the global variables map.
     */
    public String setValue(String step, String endpoint) {
        try {
            String apiGlobalVariable = "ApiGlobalVariables:";
            if (endpoint.contains(apiGlobalVariable)) {
                String endPointTemp = endpoint.split(apiGlobalVariable)[0].trim();
                String variable = endpoint.split(apiGlobalVariable)[1].trim();
                return endPointTemp + ApiKeyInitializers.getGlobalVariables().get().get(variable.trim());
            }
            return endpoint.trim();
        } catch (Exception e) {
            throw new PrecisionTestException(step.toUpperCase() + ":Error while reading the value");
        }
    }
}

