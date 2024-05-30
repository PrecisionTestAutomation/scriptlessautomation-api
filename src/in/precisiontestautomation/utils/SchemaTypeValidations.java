package in.precisiontestautomation.utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.UUID;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Utility class for validating schema types.
 * This class provides static methods to validate specific schema data types, ensuring they conform
 * to expected formats. It is used primarily to validate data against certain predefined standards
 * or formats, such as UUIDs, enhancing data integrity and consistency.
 */
public class SchemaTypeValidations {


    public static void schemaTypeValidation(String jsonPath,String schema, Object actualSchemaValue){
        String schemaType = schema.toUpperCase().split("->")[0];
        String schemaFormat = schema.contains("->") ? schema.split("->")[1] : "";
        SchemaValueTypes schemaValueTypes = SchemaValueTypes.valueOf(schemaType.toUpperCase());
        switch (schemaValueTypes){
            case UUID -> {
                String actual = (String) actualSchemaValue;
                validateSchemaType(jsonPath,isValidUUID(actual),schemaType,actual);
            }
            case DATE -> {
                String actual = String.valueOf(actualSchemaValue);
                validateSchemaType(jsonPath,isValidDate(actual,schemaFormat),schemaType,actual);
            }
            case ARRAY -> {
                String actual = String.valueOf(actualSchemaValue);
                validateSchemaType(jsonPath,isValidArray(actual),schemaType,actual);
            }

            case FLOAT -> {
                String actual = String.valueOf(actualSchemaValue);
                validateSchemaType(jsonPath,isValidFloat(actual),schemaType,actual);
            }
            case STRING -> {
                String actual = String.valueOf(actualSchemaValue);
                validateSchemaType(jsonPath,isValidString(actual),schemaType,actual);
            }
            case BOOLEAN -> {
                String actual = String.valueOf(actualSchemaValue);
                validateSchemaType(jsonPath,isValidBoolean(actual),schemaType,actual);
            }
            case INTEGER -> {
                String actual = String.valueOf(actualSchemaValue) ;
                validateSchemaType(jsonPath,isValidInteger(actual),schemaType,actual);
            }
            case JSON_OBJECT -> {
                String actual = (String) actualSchemaValue;
                validateSchemaType(jsonPath,isValidJSONObject(actual),schemaType,actual);
            }
        }
    }


    /**
     * Validates whether the provided string is a valid universally unique identifier (UUID).
     *
     * This method attempts to parse the string as a UUID and returns true if the string is a valid UUID,
     * otherwise, it returns false. This method is typically used in validating data fields that are
     * expected to contain UUIDs to ensure that they conform to the UUID standard format.
     *
     * @param uuidString The string to be tested for validity as a UUID.
     * @return true if the provided string is a valid UUID, false otherwise.
     */
    private static boolean isValidUUID(String uuidString) {
        if (uuidString == null) {
            return false;
        }
        try {
            // This line will throw IllegalArgumentException if the uuidString is not a valid UUID
            UUID.fromString(uuidString);
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    private static boolean isValidDate(String dateStr,String format) {
        SimpleDateFormat sdf = new SimpleDateFormat(format);
        sdf.setLenient(false);
        try {
            sdf.parse(dateStr);
            return true;
        } catch (ParseException e) {
            return false;
        }
    }

    private static boolean isValidInteger(String value) {
        try {
            Integer.parseInt(value);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    private static boolean isValidString(String value) {
        return value != null;
    }

    private static boolean isValidFloat(String value) {
        try {
            Float.parseFloat(value);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    private static boolean isValidBoolean(String value) {
        return "true".equalsIgnoreCase(value) || "false".equalsIgnoreCase(value);
    }

    private static boolean isValidArray(String value) {
        try {
            new JSONArray(value);
            return true;
        } catch (JSONException e) {
            return false;
        }
    }

    private static boolean isValidJSONObject(String value) {
        try {
            new JSONObject(value);
            return true;
        } catch (JSONException e) {
            return false;
        }
    }

    private static void validateSchemaType(String jsonPath, boolean schemaCheck,String schemaType,String actual){
        ApiKeyInitializers.getCustomSoftAssert().get().assertTrue(jsonPath,schemaCheck,"Schema Type("+actual+") is as expected "+schemaType, "Schema Type("+actual+") is not as expected "+schemaType);
    }

}
