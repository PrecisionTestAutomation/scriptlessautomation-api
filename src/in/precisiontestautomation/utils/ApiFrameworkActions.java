package in.precisiontestautomation.utils;

import in.precisiontestautomation.scriptlessautomation.core.configurations.TestNgConfig;
import in.precisiontestautomation.scriptlessautomation.core.exceptionhandling.PrecisionTestException;

import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.ToolProvider;
import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collections;
import java.util.Objects;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

/**
 * <p>FrameworkActions class.</p>
 *
 * @author PTA-dev
 * @version 1.2
 * @since 2024-05-02
 */
public class ApiFrameworkActions {
    /**
     * <p>getInstance.</p>
     *
     * @param tClass a {@link java.lang.Class} object
     * @param <T> a T class
     * @return a T object
     */
    @SuppressWarnings("unchecked")
    public static <T> T getInstance(Class<?> tClass) {
        try {
            return (T) tClass.getConstructor().newInstance();
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException |
                 NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }


    /**
     * <p>fetchPreFlow.</p>
     *
     * @param sendKeys a {@link java.lang.String} object
     * @return a {@link java.lang.String} object
     */
    public static String fetchPreFlow(String sendKeys) {
        if (sendKeys.toLowerCase().contains("mock")) {
            String[] invokeMethodArr = sendKeys.split(":");
            String className = invokeMethodArr[0];
            String methodName = invokeMethodArr[1];
            String param = invokeMethodArr.length == 3 ? invokeMethodArr[2] : null;
            return Objects.isNull(param) ? invokeClassMethods(className, methodName) : invokeClassMethods(className, methodName, param);
        } else if (sendKeys.toLowerCase().contains("globalvariables")) {
            String variableName = sendKeys.split(":")[1];
            return ApiKeyInitializers.getGlobalVariables().get().get(variableName).toString();
        }
        return getDynamicString(sendKeys);
    }

    /**
     * <p>invokeClassMethods.</p>
     *
     * @param className a {@link java.lang.String} object
     * @param methodName a {@link java.lang.String} object
     * @param <T> a T class
     * @return a T object
     */
    @SuppressWarnings("unchecked")
    public static <T> T invokeClassMethods(String className, String methodName) {
        try {
            Class<?> cls = Class.forName(ApiFrameworkActions.class.getPackageName() + "." + className);
            Method method = cls.getMethod(methodName);
            Object obj = cls.getDeclaredConstructor().newInstance();
            return (T) method.invoke(obj);
        } catch (ClassNotFoundException | NoSuchMethodException | InvocationTargetException | IllegalAccessException |
                 InstantiationException e) {
            throw new RuntimeException(e);
        }
    }


    /**
     * <p>invokeClassMethods.</p>
     *
     * @param className a {@link java.lang.String} object
     * @param methodName a {@link java.lang.String} object
     * @param param a {@link java.lang.String} object
     * @param <T> a T class
     * @return a T object
     */
    @SuppressWarnings("unchecked")
    public static <T> T invokeClassMethods(String className, String methodName, String param) {

        try {
            Class<?> cls = Class.forName(ApiFrameworkActions.class.getPackageName() + "." + className);
            Method method = cls.getMethod(methodName, String.class);
            Object obj = cls.getDeclaredConstructor().newInstance();
            return (T) method.invoke(obj, param);
        } catch (ClassNotFoundException | NoSuchMethodException | InvocationTargetException | InstantiationException |
                 IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * <p>getFileWithStartName.</p>
     *
     * @param fileStartWithName a {@link java.lang.String} object
     * @return a {@link java.lang.String} object
     */
    public static String getFileWithStartName(String fileStartWithName) {
        return searchFiles(fileStartWithName, System.getProperty("user.dir") + "/test_data/");
    }

    /**
     * <p>getProperty.</p>
     *
     * @param variable a {@link java.lang.String} object
     * @return a {@link java.lang.String} object
     */
    public static String getProperty(String variable) {
        return Objects.isNull(System.getProperty(variable)) ?
                null :
                System.getProperty(variable).replaceAll("%20", " ");
    }

    /**
     * <p>searchFiles.</p>
     *
     * @param fileName a {@link java.lang.String} object
     * @param searchDirectory a {@link java.lang.String} object
     * @return a {@link java.lang.String} object
     */
    public static String searchFiles(String fileName, String searchDirectory) {
        Path directory = Path.of(searchDirectory);

        try (Stream<Path> pathStream = Files.walk(directory)) {
            return pathStream
                    .filter(Files::isRegularFile)
                    .filter(path -> path.getFileName().toString().startsWith(fileName))
                    .findFirst()
                    .map(Path::toString)
                    .orElse(null);
        } catch (IOException e) {
            throw new PrecisionTestException(fileName + " not found in the " + TestNgConfig.PLATFORM + " directory");
        }
    }

    /**
     * <p>invokeCustomClassMethods.</p>
     *
     * @param className a {@link java.lang.String} object
     * @param methodName a {@link java.lang.String} object
     * @param <T> a T class
     * @return a T object
     */
    @SuppressWarnings("unchecked")
    public static <T> T invokeCustomClassMethods(String className, String methodName) {
        try {
            String javaFilePath = searchFiles(className, System.getProperty("user.dir") + File.separator + "src");
            File javaFile = new File(javaFilePath);

            // Directory for compiled .class files
            String outputDir = System.getProperty("user.dir") + File.separator + "target";
            File outputDirFile = new File(outputDir);
            if (!outputDirFile.exists()) {
                outputDirFile.mkdirs();
            }

            JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
            try (StandardJavaFileManager fileManager = compiler.getStandardFileManager(null, null, null)) {
                Iterable<? extends JavaFileObject> compilationUnits = fileManager.getJavaFileObjectsFromFiles(Collections.singletonList(javaFile));
                // Correctly specify the compiler options
                compiler.getTask(null, fileManager, null, Arrays.asList("-d", outputDir), null, compilationUnits).call();
            }

            // Create URL to the output directory
            URL[] classUrls = {outputDirFile.toURI().toURL()};

            // Use URLClassLoader to load the class
            try (URLClassLoader classLoader = new URLClassLoader(classUrls)) {
                String packageName = getPackageName(javaFilePath);
                Class<?> cls = classLoader.loadClass(packageName + className);
                Method method = getMethod(className, methodName, cls);
                Object obj = cls.getDeclaredConstructor().newInstance();
                Object[] params = new Object[method.getParameterCount()];

                return (T) method.invoke(obj, params);

            }
        } catch (IOException | ClassNotFoundException | NoSuchMethodException | InvocationTargetException |
                 InstantiationException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    private static Method getMethod(String className, String methodName, Class<?> cls) throws NoSuchMethodException {
        Method method = null;

        // Iterate over methods to find the matching one with the right name
        for (Method m : cls.getDeclaredMethods()) {
            if (m.getName().equals(methodName)) {
                method = m;
                break;
            }
        }

        if (method == null) {
            throw new NoSuchMethodException("Method " + methodName + " not found in class " + className);
        }
        return method;
    }

    private static String getPackageName(String javaFilePath) throws IOException {
        try (BufferedReader br = new BufferedReader(new FileReader(javaFilePath))) {
            String firstLine = br.readLine();
            if (firstLine != null && firstLine.startsWith("package")) {
                return firstLine.split("package")[1].trim().replace(";", "") + ".";
            } else {
                return ""; // Default package
            }
        }
    }

    /**
     * <p>getStringBetweenTwoStrings.</p>
     *
     * @param text a {@link java.lang.String} object
     * @param startString a {@link java.lang.String} object
     * @param endString a {@link java.lang.String} object
     * @return a {@link java.lang.String} object
     */
    public static String getStringBetweenTwoStrings(String text, String startString, String endString) {
        // Check if both startString and endString are empty, if so, return the entire text
        if (startString.isEmpty() && endString.isEmpty()) {
            return text;
        }

        // Check if either startString or endString is empty but not both
        if (startString.isEmpty() || endString.isEmpty()) {
            throw new IllegalArgumentException("Both startString and endString should be provided or neither should be provided.");
        }

        String regexPattern = Pattern.quote(startString) + "(.*?)" + Pattern.quote(endString);
        Pattern pattern = Pattern.compile(regexPattern);
        Matcher matcher = pattern.matcher(text);
        if (matcher.find()) {
            return matcher.group(1).trim(); // The captured content between the start and end strings
        }
        return null;
    }

    static String integrateString(String expected) {
        String globalVariableValue = null;

        String regex = "\\{\\{GlobalVariable:([^}]+)\\}\\}";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(expected);

        if (matcher.find()) {
            String variableName = matcher.group(1);
            globalVariableValue = ApiKeyInitializers.getGlobalVariables().get().get(variableName).toString();
        }

        return matcher.replaceAll(globalVariableValue);
    }

    /**
     * <p>getDynamicString.</p>
     *
     * @param sendKeys a {@link java.lang.String} object
     * @return a {@link java.lang.String} object
     */
    public static String getDynamicString(String sendKeys){
        Pattern pattern = Pattern.compile("\\$(\\w+)\\.(\\w+)");
        Matcher matcher = pattern.matcher(sendKeys);
        if (matcher.find()) {
            String propertyFileName = matcher.group(1);
            String key = matcher.group(2);
            return getPropertyValue(propertyFileName, key);
        }
        return sendKeys;
    }

    private static String getPropertyValue(String propertyFileName, String key) {
        String basePath = "test_data/api/dynamic_strings/";
        String propertyFilePath = Paths.get(basePath, TestNgConfig.ENV, propertyFileName + ".properties").toString();

        Properties prop = new Properties();
        try (FileInputStream fis = new FileInputStream(propertyFilePath)) {
            prop.load(fis);
            return prop.getProperty(key);
        } catch (IOException e) {
            e.printStackTrace();
            return null; // Or handle this case as per your requirement
        }
    }
}
