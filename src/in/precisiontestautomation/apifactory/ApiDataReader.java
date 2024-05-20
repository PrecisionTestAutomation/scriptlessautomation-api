package in.precisiontestautomation.apifactory;

import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvException;

import java.io.FileReader;
import java.io.IOException;
/**
 * The ApiDataReader class provides a singleton, thread-safe API data reader that utilizes ThreadLocal
 * to maintain separate instances per thread. It is designed for reading API parameters from CSV files,
 * ensuring that multiple threads can safely process different data sources concurrently.
 * This class is particularly useful in multi-threaded testing environments where isolation of
 * tests data per thread is crucial.
 *
 * @author PTA-dev
 * @version 1.2
 * @since 2024-05-02
 */
public class ApiDataReader {

    // ThreadLocal storage for ApiDataReader instances, ensuring thread-specific instances.
    private static final ThreadLocal<ApiDataReader> THREAD_LOCAL_INSTANCE =
            ThreadLocal.withInitial(ApiDataReader::new);

    // Private constructor to enforce Singleton pattern via ThreadLocal.
    private ApiDataReader() {
    }

    /**
     * Provides access to the singleton instance of ApiDataReader for the current thread,
     * utilizing ThreadLocal to ensure a separate instance per thread.
     *
     * @return ApiDataReader instance specific to the current thread.
     */
    public static ApiDataReader getInstance() {
        return THREAD_LOCAL_INSTANCE.get();
    }

    /**
     * Reads and parses API tests data from a CSV file into an ApiParameters object.
     * It handles CSV parsing with error management, throwing a runtime exception
     * if parsing fails due to file read errors or CSV format issues.
     *
     * @param csvFile The path to the CSV file containing the API tests data.
     * @return ApiParameters object containing the parsed data.
     * @throws java.lang.RuntimeException If an error occurs during file reading or CSV parsing.
     */
    public ApiParameters readTestData(String csvFile) {
        try (CSVReader reader = new CSVReader(new FileReader(csvFile))) {
            return ApiParameters.getInstance(reader.readAll()).get();
        } catch (IOException | CsvException e) {
            throw new RuntimeException("Error reading tests data from CSV file", e);
        }
    }
}
