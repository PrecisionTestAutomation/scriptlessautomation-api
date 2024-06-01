# Api Automation

![Java CI with Maven](https://img.shields.io/badge/Java%20CI%20with%20Maven-blue.svg)
![Maven Central](https://img.shields.io/maven-central/v/in.precisiontestautomation.scriptlessautomation/scriptlessautomation-api.svg)
![Version](https://img.shields.io/badge/version-4.4.0-blue.svg)
![Javadoc](https://img.shields.io/badge/javadoc-4.4.0-brightgreen.svg)
![PRs welcome](https://img.shields.io/badge/PRs-welcome-brightgreen.svg)

## Table of Contents
- [Introduction](#Introduction)
- [Prerequisites](#Prerequisites)
- [ProjectSetup](#ProjectSetup)
- [Configuration](#Configuration)
- [IntelliJIdeaSetup](#IntelliJIdeaSetup)
- [Conclusion](#Conclusion)
- [NextSteps](#NextSteps)

## Introduction
This guide provides a step-by-step process to set up and run a Maven project with Scriptless Automation for API testing. It includes adding dependencies, configuring plugins, and setting up the necessary configurations for API automation.

## Prerequisites
Before you begin, ensure you have the following installed on your system:
* Java Development Kit (JDK)
* Apache Maven
* IntelliJ IDEA (or any preferred IDE)

## ProjectSetup
### 1. **Create a Maven Project**
   Create a new Maven project in your IDE or via the command line.
### 2. **Add Dependencies**
   Add the Scriptless Automation dependency for API testing to your pom.xml file:
    
```
    <dependencies>
      <dependency>
        <groupId>in.precisiontestautomation.scriptlessautomation</groupId>
        <artifactId>scriptlessautomation-api</artifactId>
        <version>LATEST</version>
      </dependency>
    </dependencies>
```
### 3. **Add Plugins**
   Add the following plugins to your pom.xml file to configure the execution of the main class and testing framework:

```
    <plugins>
      <plugin>
        <groupId>org.codehaus.mojo</groupId>
        <artifactId>exec-maven-plugin</artifactId>
        <version>3.1.0</version>
        <executions>
          <execution>
            <goals>
              <goal>java</goal>
            </goals>
          </execution>
        </executions>
        <configuration>
          <mainClass>in.precisiontestautomation.runner.ScriptlessApplication</mainClass>
          <arguments>
            <argument>true</argument>
          </arguments>
        </configuration>
      </plugin>
      <plugin>
        <groupId>org.apache.maven.plugins</groupId>
        <artifactId>maven-surefire-plugin</artifactId>
        <version>3.0.0-M4</version>
        <configuration>
          <argLine>-XX:+ExplicitGInvokesConcurrent</argLine>
          <argLine>--add-opens java.base/java.lang.reflect=ALL-UNNAMED</argLine>
          <testFailureIgnore>false</testFailureIgnore>
          <suiteXmlFiles>
            <suiteXmlFile>target/testngenerator.xml</suiteXmlFile>
          </suiteXmlFiles>
        </configuration>
      </plugin>
    </plugins>
```

## Configuration
### 4. **Add Scriptless Configuration**
   To execute Scriptless Automation for API testing, add the following configuration files in your project:
    
```
    project
         └── config
             ├── reportConfiguration
             │   └── extentReportConfiguration.properties
             └── testNgConfiguration.properties
```

**4.1 reportConfiguration/extentReportConfiguration.properties**
   This file contains the configuration settings for the Extent Reports.

        # Extent Report Configuration
        ReportName = AutomationReport
        
        #DARK , STANDARD
        Report_Theme = DARK
        Report_JS = for (let e of document.querySelectorAll (".details-col")) { e.innerHTML ='Steps' };
        Report_CSS = .header .vheader .nav-logo>a .logo {min-height: 52px;min-width: 52px;}
        Report_logo = "/config/logo.jpg"
        Report_captureScreenshotOnPass = true

   **4.2 testNgConfiguration.properties**
    This file contains the TestNG configuration settings.
    
```
    # TestNG Configuration
    ReportName = AutomationReport

    Env = sandbox
    ThreadCount= 1
    
    FAILED_RETRY_COUNT=0
    SET_TEST_SUITE_NAME = Scriptless
    SET_TEST_NAME = Regression Testing
    
    TEST_DATA_SECTIONS=All
    TEST_IDS=LoadUrl
    DISABLE_TEST_IDS=
    
    GROUPS=All
```

### 5. Add Test Data
Add the necessary test data to your project. This involves creating a directory structure to store test data files and ensuring they are accessible to your test scripts. Use the following directory structure:
```
    project
       └── test_data
           └── API
               ├── JsonRepository
               │   └── file.json
               ├── test_case_flows
               │   └── TestDirectory
               │       └── TestID_GroupName.csv
               └── schemaJson
                   └── schema.json  
```   
       
   **5.1 test_data/API/JsonRepository/**    
   This file contains the JSON payloads for your API tests.
```
    {
        "exampleKey": "exampleValue"
    }
```
   
   **5.2 5.2 test_data/API/test_case_flows/TestDirectory/TestID_GroupName.csv**  
   This file contains test case flows and steps for each test. Each directory under test_case_flows represents a feature.
```
    # Example TestID_GroupName.csv
        DEPENDANT_TEST_CASE,NONE,
        END_POINT,https://api-generator.retool.com/vEQvcT/data,
        METHOD,POST,
        PARAMS:KEY,NONE,
        PARAMS:VALUE,NONE,
        AUTH:KEY,NONE,
        AUTH:VALUE,NONE,
        HEADERS:KEY,Content-Type,
        HEADERS:VALUE,application/json,
        BODY:KEY,JsonRepository,name
        BODY:VALUE,test,Custom:CustomClass:body
        RESPONSE:CODE,201,
        RESPONSE:SCHEMA,putSchema.json
        RESPONSE:JSON_PATH,'Column 1',id
        RESPONSE:EXPECTED_VALUE,ApiGlobalVariables:name,NONE
        RESPONSE:STORE_VALUE,Name,ID
```  

   **5.3 test_data/API/test_case_flows/schemaJson/**
   This file performs schema validation against the response generated by the API request
```
    {
          "Column 1": "ApiGlobalVariables:name",
          "id": "@Integer"
    }
```

## IntelliJIdeaSetup
### 6. Add IntelliJ Run Configuration
   Create a run configuration in IntelliJ IDEA to run your Maven project. Follow these steps:
1.    Go to Run > Edit Configurations. ![](https://files.gitbook.com/v0/b/gitbook-x-prod.appspot.com/o/spaces%2FiGaaGnfeM1ej74weWKLs%2Fuploads%2Fov27CJI8g209RJEE2kzj%2FScreenshot%202024-06-01%20at%201.32.26%20AM.png?alt=media&token=d2a9f970-7be7-4590-8027-95d7e4602ce1)
2.    Click on the + icon and select Application.![](https://files.gitbook.com/v0/b/gitbook-x-prod.appspot.com/o/spaces%2FiGaaGnfeM1ej74weWKLs%2Fuploads%2F9I0zNOwOgjx8myBigtYb%2FScreenshot%202024-06-01%20at%201.33.27%20AM.png?alt=media&token=0c1267c0-25e1-46e0-8b23-9af2678d04ef)
3.    Set the name for your configuration (e.g., runner). ![](https://files.gitbook.com/v0/b/gitbook-x-prod.appspot.com/o/spaces%2FiGaaGnfeM1ej74weWKLs%2Fuploads%2FU2SL6vg25OZtPlztqmTr%2FScreenshot%202024-06-01%20at%201.35.44%20AM.png?alt=media&token=baf687fd-df81-4f93-9f88-5e56a19b801d) 
4.    Click on Apply
### 7. Run CLI Command
   To execute the project, use the following CLI command:
```
    mvn clean exec:java test
```
   
   This command will run the main class defined in the `exec-maven-plugin` configuration.

## Conclusion
   By following this guide, you have set up a Maven project with Scriptless Automation for API testing. You configured the necessary dependencies, plugins, and run configurations to execute your API automation tests.

## NextSteps
*    Customize your test scripts according to your API requirements.
*    Explore more features of Scriptless Automation to enhance your test coverage.
*    Integrate additional tools and frameworks as needed for comprehensive test automation.
