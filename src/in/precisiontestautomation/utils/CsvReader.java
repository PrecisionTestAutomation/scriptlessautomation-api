package in.precisiontestautomation.utils;

import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvValidationException;
import in.precisiontestautomation.scriptlessautomation.core.exceptionhandling.PrecisionTestException;

import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * <p>CsvReader class.</p>
 *
 * @author PTA-dev
 * @version 1.2
 * @since 2024-05-02
 */
public class
CsvReader {

    /**
     * Read csv file
     *
     * @param path : csv file path
     * @return {@code List&lt;String[]&gt;}
     */
    public static List<Map<String, String>> readCSVFile(String path) {
        List<Map<String, String>> table = new ArrayList<>();
        try (CSVReader reader = new CSVReader(new FileReader(path))) {
            String[] header = reader.readNext(); // read the first line as header
            String[] line;
            while ((line = reader.readNext()) != null) { // read each line as value
                Map<String, String> row = new HashMap<>();
                for (int i = 0; i < header.length; i++) {
                    if(line.length>1)
                        row.put(header[i], line[i]);
                    else
                        break;
                }
                if(!row.isEmpty()) {
                    table.add(row); // assuming the first column is the key
                }
            }
        } catch (CsvValidationException e) {
            throw new PrecisionTestException("Csv validation issue");
        } catch (IOException e) {
            throw new PrecisionTestException("File Not found "+path);
        }

        return table;
    }
}
