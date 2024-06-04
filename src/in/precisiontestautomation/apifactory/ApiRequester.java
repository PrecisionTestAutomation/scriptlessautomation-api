package in.precisiontestautomation.apifactory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import in.precisiontestautomation.scriptlessautomation.core.utils.CoreFrameworkActions;
import in.precisiontestautomation.utils.*;
import in.precisiontestautomation.scriptlessautomation.core.exceptionhandling.PrecisionTestException;
import in.precisiontestautomation.scriptlessautomation.core.utils.AutomationAsserts;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import org.awaitility.Awaitility;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * ApiRequester manages the execution of API requests based on parameters defined in an ApiParameters instance.
 * It handles the preparation, execution, and validation of requests, leveraging the configuration details
 * stored in ApiParameters. This class uses a ThreadLocal pattern to ensure that API requests are isolated per thread,
 * making it suitable for use in multi-threaded testing environments.
 *
 * The class includes methods to execute tests, send HTTP requests, validate responses, and manage authentication.
 *
 * @author PTA-dev
 * @version 1.2
 * @since 2024-05-02
 */
public class ApiRequester {
    private final ApiParameters testParameters;
    private String jsonRepository = System.getProperty("user.dir") + "/test_data/api/JsonRepository";

    private ApiRequester(ApiParameters testParameters) {
        this.testParameters = testParameters;
    }

    /**
     * Provides a ThreadLocal instance of ApiRequester using the given ApiParameters. This ensures that each thread
     * has its own instance of ApiRequester, configured with its own set of API parameters.
     *
     * @param testParameters The parameters to be used for the API requests in this thread.
     * @return A ThreadLocal instance of ApiRequester.
     * @author PTA-dev
     */
    public static ThreadLocal<ApiRequester> getInstance(ApiParameters testParameters) {
        return ThreadLocal.withInitial(() -> new ApiRequester(testParameters));
    }

    /**
     * Executes an API tests based on the configured parameters. This method prepares the request, logs the request
     * details, and sends the request. It also waits for the expected conditions to be met and logs the response.
     *
     * @param testCaseName The name of the tests case, used for logging.
     * @param automationAsserts A helper object for performing assertions and logging in the context of automated testing.
     * @return The current instance of ApiRequester, allowing for method chaining.
     * @author PTA-dev
     */
    public ApiRequester executeTest(String testCaseName, AutomationAsserts automationAsserts) {
        automationAsserts.info("TestCase <b>" + testCaseName + "</b> -> <i>Endpoint</i> : " + testParameters.getEndpoint());
        Map<String, Object> requestParameters = testParameters.getRequestParameters();
        RequestSpecification request = setAuth(RestAssured.given(),requestParameters)
                .baseUri(testParameters.getEndpoint())
                .log()
                .all();



        if (!requestParameters.isEmpty()) {
            Map<String, Object> headers = (Map<String, Object>) requestParameters.get("headers");
            Map<String, Object> params = (Map<String, Object>) requestParameters.get("params");
            Map<String, Object> body = getBody((Map<String, Object>) requestParameters.get("body"));

            if (headers != null && !headers.isEmpty()) {
                automationAsserts.info("TestCase <b>" + testCaseName + "</b> -> <i>Header</i> : " + headers);
                request.headers(headers);
            }

            if (params != null && !params.isEmpty()) {
                automationAsserts.info("TestCase <b>" + testCaseName + "</b> -> <i>Params</i> : " + params);
                request.queryParams(params);
            }

            if (body != null && !body.isEmpty()) {
                automationAsserts.info("TestCase <b>" + testCaseName + "</b> -> <i>Body</i> : " + body);
                if (Objects.requireNonNull(headers).containsKey("Content-Type") && headers.get("Content-Type").toString().equalsIgnoreCase("application/x-www-form-urlencoded")) {
                    request.formParams(body);
                } else {
                    request.body(body);
                }
            }
        }

        testParameters.setExpectedValues(testParameters.setValue("RESPONSE:EXPECTED_VALUE", (ArrayList<Object>) testParameters.getExpectedValues()));
        String[] expectedValue = testParameters.getExpectedValues().get(0).toString().split(":");
        testParameters.getExpectedValues().set(0,expectedValue[0]);
        int timeOut;
        try {
            timeOut = Integer.parseInt(expectedValue[1]);
        } catch (ArrayIndexOutOfBoundsException e) {
            timeOut = 30;
        }
        Awaitility.await().atMost(timeOut, TimeUnit.SECONDS)
                .until(() -> {
                    if (!expectedValue[0].equals("NONE") || expectedValue[0].isEmpty()) {
                        return sendHttpRequest(request).getBody().asString()
                                .contains(expectedValue[0]);
                    }
                    return true;
                });

        ApiKeyInitializers.getResponse().set(sendHttpRequest(request));
        ApiKeyInitializers.getResponse().get().prettyPrint();

        automationAsserts.info("TestCase <b>" + testCaseName + "</b> -> <i>Response</i> : " + ApiKeyInitializers.getResponse().get().getBody().asString());
        return this;
    }

    /**
     * Sends an HTTP request based on the specified method. This method supports various HTTP methods including
     * GET, POST, PUT, DELETE, and their secure variants with relaxed HTTPS validation.
     *
     * @param request The prepared RequestSpecification object.
     * @return The response received after executing the HTTP request.
     * @author PTA-dev
     */
    private Response sendHttpRequest(RequestSpecification request) {
        String method = testParameters.getMethod().toUpperCase();

        return switch (method) {
            case "GET" -> request.get();
            case "POST" -> request.post();
            case "PUT" -> request.put();
            case "DELETE" -> request.delete();
            case "PATCH" -> request.patch();
            case "RELAX_GET" -> request.relaxedHTTPSValidation().redirects().follow(true).get();
            case "RELAX_POST" -> request.relaxedHTTPSValidation().redirects().follow(true).post();
            case "RELAX_PUT" -> request.relaxedHTTPSValidation().redirects().follow(true).put();
            case "RELAX_DELETE" -> request.relaxedHTTPSValidation().redirects().follow(true).delete();
            default -> throw new PrecisionTestException("Unsupported HTTP method: " + method);
        };
    }

    /**
     * Validates the HTTP response status code against the expected status code stored in ApiParameters.
     *
     * @param automationAsserts A helper object for assertions.
     * @param condition A boolean value that if true, triggers the validation of the response code.
     * @return The current instance of ApiRequester, allowing for method chaining.
     * @author PTA-dev
     */
    public ApiRequester validateResponseCode(AutomationAsserts automationAsserts, Boolean condition) {
        if (condition) {
            automationAsserts.assertEquals("Response Code", String.valueOf(ApiKeyInitializers.getResponse().get().statusCode()), testParameters.getResponseStatusCode(),false,null);
        }
        return this;
    }

    /**
     * Validates the response body against expected values defined in ApiParameters.
     *
     * @param automationAsserts A helper object for assertions.
     * @param condition A boolean value that if true, triggers the validation of the response body.
     * @return The current instance of ApiRequester, allowing for method chaining.
     * @author PTA-dev
     */
    public ApiRequester validateResponse(AutomationAsserts automationAsserts, Boolean condition) {
        if (condition) {
            Map<String, Object> validationPoints = testParameters.mergeListsToMap("ValidationPoints", testParameters.getJsonPath(), testParameters.getExpectedValues());
            validationPoints.entrySet().stream()
                    .filter(f -> !f.getValue().toString().equals("NONE"))
                    .forEach(f -> {
                        if(!f.getKey().toLowerCase().startsWith("custom")) {
                            automationAsserts.assertEquals(f.getKey(), ApiKeyInitializers.getResponse().get().getBody().jsonPath().getJsonObject(f.getKey()).toString(),
                                    f.getValue().toString(),false,null);
                        } else {
                            String[] custom = f.getKey().split(":");
                            String className = custom[1];
                            String methodName = custom[2];
                            ApiFrameworkActions.invokeCustomClassMethods(className,methodName);
                        }
                    });
        }
        return this;
    }

    /**
     * Saves response objects into global variables based on the configuration in ApiParameters.
     * This is useful for persisting data that needs to be reused in subsequent tests or operations.
     *
     * @return The current instance of ApiRequester, allowing for method chaining.
     * @author PTA-dev
     */
    public ApiRequester saveResponseObjects() {
        Map<String, String> apiResponseObjects = testParameters.mergeListsToMaps(testParameters.getStoreValue(), testParameters.getJsonPath());
        try {
            apiResponseObjects.entrySet()
                    .stream()
                    .filter(e -> !e.getKey().equalsIgnoreCase("none") || !e.getKey().isEmpty())
                    .filter(e -> !e.getValue().equalsIgnoreCase("none") || !e.getValue().isEmpty())
                    .forEach(e -> {
                        Object jsonValue = ApiKeyInitializers.getResponse().get().getBody().jsonPath().getJsonObject(e.getValue());
                        ApiKeyInitializers.getGlobalVariables().get().put(e.getKey(), jsonValue);
                    });
        } catch (Exception ex) {
            throw new PrecisionTestException("Error will setting value " + ex.getLocalizedMessage());
        }

        return this;
    }

    /**
     * Authenticates the request using authentication parameters specified in ApiParameters.
     * Currently, this method supports Bearer token authentication.
     *
     * @param request The request to which authentication needs to be added.
     * @return The request with authentication headers set.
     * @author PTA-dev
     */
    private RequestSpecification setAuth(RequestSpecification request,Map<String, Object> requestParameters) {
        if (requestParameters.containsKey("auth")) {
            Map<String, Object> auth = (Map<String, Object>) requestParameters.get("auth");
            if (!auth.isEmpty()) {
                auth.entrySet().stream()
                        .iterator().forEachRemaining(map -> {
                            if (map.getKey().equalsIgnoreCase("Bearer")) {
                                request.header("Authorization", "Bearer " + map.getValue());
                            }
                        });
            }
        }

        return request;
    }

    /**
     * Clears all entries in the global variables map. This method is typically called at the end of a tests
     * or tests suite to clean up and ensure no residual data affects subsequent tests.
     *
     * @return The current instance of ApiRequester, allowing for method chaining.
     * @author PTA-dev
     */
    public ApiRequester apiGlobalVariableClear() {
        try {
            if (!Objects.isNull(ApiKeyInitializers.getGlobalVariables().get().isEmpty())) {
                ApiKeyInitializers.getGlobalVariables().get().clear();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return this;
    }

    /**
     * Constructs the body of the request by potentially replacing placeholders in the JSON template with actual values.
     * This method also handles the retrieval of the JSON template from the repository.
     *
     * @param body A map representing the body of the request with possible placeholders.
     * @return A map representing the fully constructed body of the request.
     * @author PTA-dev
     */
    public Map<String, Object> getBody(Map<String, Object> body) {
        String json = null;
        if (!Objects.isNull(body)) {
            if (body.containsKey("JsonRepository")) {

                String jsonName = body.get("JsonRepository").toString();
                Path filePath = findFile(jsonName);
                if (!Objects.isNull(filePath)) {
                    try {
                        json = Files.readString(filePath);
                    } catch (IOException e) {
                        throw new PrecisionTestException(jsonName + " Json file not found,either create one or verify file name");
                    }
                }

                body.remove("JsonRepository");

                String finalJson = json;
                for (Map.Entry<String, Object> bodyMap : body.entrySet()) {
                    String key = "{{" + bodyMap.getKey() + "}}";
                    finalJson = finalJson.replace(key, bodyMap.getValue().toString());
                }

                ObjectMapper mapper = new ObjectMapper();

                body.clear();
                try {
                    body = mapper.readValue(finalJson, Map.class);
                } catch (JsonProcessingException e) {
                    throw new PrecisionTestException(jsonName + " Error while mapping json to map");
                }
            }
        }
        return body;
    }

    /**
     * Finds a file within the specified JSON repository. This method is used internally to retrieve JSON templates.
     *
     * @param fileName The name of the file to be found.
     * @return A Path object representing the found file, or null if no file is found.
     * @author PTA-dev
     */
    private Path findFile(String fileName) {
        try (var paths = Files.walk(Paths.get(jsonRepository))) {
            Optional<Path> result = paths
                    .filter(path -> path.getFileName().toString().startsWith(fileName))
                    .findFirst();

            return result.orElse(null);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public ApiRequester validateResponseSchema(boolean condition){
        if(condition) {
            try {
                String expectedSchemaString = JsonFileReader.getInstance().setJsonString(testParameters.getSchemaJson()).getJsonString();
                List<String> expectedSchema = JsonPathExtractor.getInstance().getListOfJsonPaths("", expectedSchemaString);
                List<String> actualSchema = JsonPathExtractor.getInstance().getListOfJsonPaths("", ApiKeyInitializers.getResponse().get().getBody().asString());
                validateSchema(actualSchema,expectedSchema);
                return validateSchemaValue(expectedSchemaString);
            } catch (Exception exception) {
                System.err.println("Schema validation is not mentioned in the template");
                return this;
            }
        }
        return this;
    }

    private void validateSchema(List<String> actual,List<String> expected){
        ApiKeyInitializers.getCustomSoftAssert().get()
                .assertEquals("Schema validation",actual,expected,false,null);
    }

    private ApiRequester validateSchemaValue(String expectedSchemaJson){
        JsonFileReader actualJsonFileReader = JsonFileReader.getInstance().setJsonString(ApiKeyInitializers.getResponse().get().getBody().asString());
        Map<String,Object> expectedSchemaMap = JsonPathExtractor.getInstance().getMapJsonPathValue("",expectedSchemaJson);
        Set<Map.Entry<String,Object>> entryMap = expectedSchemaMap.entrySet();
        for(Map.Entry<String,Object> map : entryMap){
            String jsonPath = map.getKey();
            Object actualValue = actualJsonFileReader.getJsonValueByPath(addQuotesIfContainsSpace(jsonPath));
            actualValue = Objects.isNull(actualValue) ? "null" : actualValue;
            Object expectedValue = expectedSchemaMap.get(jsonPath);
            if(String.valueOf(expectedValue).startsWith("@")){
                SchemaTypeValidations.schemaTypeValidation(jsonPath,String.valueOf(expectedValue).substring(1),actualValue);
            } else {
                if(expectedValue.toString().toLowerCase().startsWith("apiglobalvariables")){
                    expectedValue = ApiKeyInitializers.getGlobalVariables().get().get(expectedValue.toString().split(":")[1]);
                }
                ApiKeyInitializers.getCustomSoftAssert().get()
                        .assertEquals(jsonPath,actualValue,expectedValue,false,null);
            }
        }

        return this;
    }

    private String addQuotesIfContainsSpace(String input) {
        if (input.contains(" ")) {
            return "'" + input + "'";
        }
        return input;
    }
}
