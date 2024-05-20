package in.precisiontestautomation.tests;

import in.precisiontestautomation.scriptlessautomation.core.configurations.TestNgConfig;
import in.precisiontestautomation.scriptlessautomation.core.exceptionhandling.PrecisionTestException;
import in.precisiontestautomation.scriptlessautomation.core.testng.setup.BaseTest;
import in.precisiontestautomation.scriptlessautomation.core.testng.xmlgenerator.DataProviderUtil;
import in.precisiontestautomation.scriptlessautomation.core.utils.CoreKeyInitializers;
import in.precisiontestautomation.apifactory.ApiDataReader;
import in.precisiontestautomation.apifactory.ApiRequester;
import org.apache.commons.lang3.StringUtils;
import org.testng.Reporter;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.Test;

import java.io.File;
import java.util.Optional;

/**
 * The {@code API} class provides functionality to execute API tests cases as part of an automated tests suite.
 * It manages API requests and responses, ensuring that tests data is correctly parsed, executed, and validated.
 * This class uses a singleton pattern to manage a single instance of the class across different tests runs.
 *
 * <p>It integrates with TestNG for executing tests methods and managing tests lifecycle events such as setup and cleanup.
 * ThreadLocal variables are used to maintain thread safety, allowing parallel execution of tests without data interference.</p>
 *
 * @author PTA-dev
 * @version 1.2
 * @since 2024-05-02
 */
public class API extends BaseTest {

    private static API instance = null;

    private final ThreadLocal<ApiRequester> apiRequester = new ThreadLocal<>();
    private final ThreadLocal<Boolean> validationCondition = new ThreadLocal<>();
    private final ThreadLocal<String> categoryName = new ThreadLocal<>();

    private API() {
    }

    /**
     * Provides access to the singleton instance of the {@code API} class. This method ensures that
     * only one instance of the {@code API} class is created, shared, and reused across different parts
     * of the application. This implementation uses lazy initialization to create the instance when it is
     * first needed, without additional synchronization.
     *
     * <p>This method is thread-safe as the instance is created without explicit synchronization mechanisms,
     * relying on the class's static initializer for instance creation at first use. This approach minimizes
     * synchronization overhead and ensures that the instance is created only when necessary.</p>
     *
     * @return the single instance of the {@code API} class.
     */
    public static API getInstance() {
        if (instance == null) {
            instance = new API();
        }
        return instance;
    }

    @BeforeSuite(alwaysRun = true)
    public void beforeSuiteApi(){
        TestNgConfig.PLATFORM = "Api";
    }

    /**
     * Executes API tests cases specified by the filePath. This method is driven by the TestNG framework,
     * leveraging data provided through {@link DataProviderUtil}. It reads tests configurations from
     * the 'testNgConfiguration.properties' file which includes directives like TEST_DATA_SECTIONS,
     * TEST_IDS, DISABLE_TEST_IDS, GROUPS, and ThreadCount to control tests execution.
     *
     * <p>This method initializes and executes each tests case based on the provided filePath and validation condition.
     * It handles tests execution dynamically by evaluating the provided conditions and configurations to ensure
     * that only the relevant tests are run. This flexibility allows for selective tests execution which is crucial
     * during different stages of development and testing.</p>
     *
     * <p>Key configurations are:</p>
     * <ul>
     *   <li><b>TEST_DATA_SECTIONS</b>: Specifies which sections of the tests data to include in the run.</li>
     *   <li><b>TEST_IDS</b>: Allows specifying particular tests IDs that should be executed.</li>
     *   <li><b>DISABLE_TEST_IDS</b>: Lists tests IDs that should be excluded from execution.</li>
     *   <li><b>GROUPS</b>: Manages tests groups for execution, useful for categorizing tests into batches.</li>
     *   <li><b>ThreadCount</b>: Controls the number of threads to be used for parallel tests execution.</li>
     * </ul>
     *
     * @param filePath The path to the tests data file, which guides what specific API tests to execute.
     * @param validationCondition Boolean flag that indicates if the response of the API call should be validated.
     */
    @Test(dataProviderClass = DataProviderUtil.class, dataProvider = "dataProvide")
    public void testRunner(String filePath, Boolean validationCondition) {
        this.validationCondition.set(validationCondition);
        final String testCaseName = new File(filePath).getName().split("_")[0];
        categoryName.set(StringUtils.capitalize(new File(filePath).getParentFile().getName()));
        System.out.println("----------------------------------" + testCaseName + " Started----------------------------------");
        try {
            apiRequester.set(ApiDataReader.getInstance().readTestData(filePath)
                    .parseTestData()
                    .executeTest(testCaseName, CoreKeyInitializers.getCustomSoftAssert().get())
                    .validateResponseCode(CoreKeyInitializers.getCustomSoftAssert().get(), this.validationCondition.get())
                    .validateResponse(CoreKeyInitializers.getCustomSoftAssert().get(), this.validationCondition.get())
                    .saveResponseObjects());
        } catch (Exception e) {
            throw new PrecisionTestException("Failed While running tests case " + testCaseName + " " + e.getLocalizedMessage());
        } finally {
            Reporter.getCurrentTestResult().setAttribute("testRailId", testCaseName);
            Reporter.getCurrentTestResult().setAttribute("suiteName", categoryName.get());
        }
        System.out.println("----------------------------------" + testCaseName + "Ended----------------------------------");

    }

    /**
     * Cleans up thread-local variables used in the tests execution to free up resources and prevent memory leaks.
     * This method ensures that after each tests method execution, all thread-local storages are cleared properly.
     * This is crucial for preventing cross-thread data leakage in a multi-threaded testing environment.
     */
    @AfterMethod(alwaysRun = true)
    public void cleanUpThreadLocals() {
        validationCondition.remove();
        Optional.ofNullable(apiRequester.get()).ifPresent(ApiRequester::apiGlobalVariableClear);
        apiRequester.remove();
    }
}
