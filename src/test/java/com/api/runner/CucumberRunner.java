package com.api.runner;

import com.automation.platform.config.Configvariable;
import com.automation.platform.config.TapBeansLoad;
import com.automation.platform.filehandling.FileReaderUtil;
import com.automation.platform.reporting.TapReporting;
import com.automation.platform.selenium.SeleniumBase;
import com.github.mkolisnyk.cucumber.runner.ExtendedCucumberOptions;
import cucumber.api.CucumberOptions;
import cucumber.api.testng.AbstractTestNGCucumberTests;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.BeforeSuite;

import java.util.TimeZone;

@ComponentScan(basePackages = {"com.automation.platform"})
@Configuration
@ExtendedCucumberOptions(
        jsonReport = "reports/cucumber/cucumber.json"
        , retryCount = 3
        , detailedReport = true
        , detailedAggregatedReport = true
        , overviewReport = true
        , jsonUsageReport = "reports/cucumber-usage.json"
        , usageReport = true
        , toPDF = true
        , outputFolder = "reports")
@CucumberOptions(
        monochrome = true,
        features = "classpath:features",
        glue = {"com/automation/platform/tapsteps"},
        tags = {"@api_test", "~@ignore"},
        plugin = {"pretty",
                "html:reports/cucumber/cucumber-html",
                "json:reports/cucumber/cucumber.json",
                "usage:reports/cucumber-usage.json",
                "junit:reports/newrelic-report/cucumber-junit.xml"}
)

public class CucumberRunner extends AbstractTestNGCucumberTests {

    private static final Logger LOGGER = LoggerFactory.getLogger(CucumberRunner.class);

    public static ConfigurableApplicationContext context;
    private Configvariable configvariable;


    @BeforeSuite(alwaysRun = true)
    public void setUpEnvironmentToTest() {
        // write if anything needs to be set up once before tests run. e.g. connection to database
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
        TapBeansLoad.setConfigClass(CucumberRunner.class);
        TapBeansLoad.init();
        configvariable = (Configvariable) TapBeansLoad.getBean(Configvariable.class);
        LOGGER.info("Setting environment file....");
        //configvariable.setupEnvironmentProperties(System.getProperty("api.env"), System.getProperty("api.lbu"));

    }

    @AfterSuite(alwaysRun = true)
    public void cleanUp() {
        // close if something enabled in @before suite. e.g. closing connection to DB
        LOGGER.info("Copying and generating reports....");
        String deviceFarmLogDir = System.getenv("DEVICEFARM_LOG_DIR");
        TapReporting.generateReportForJsonFiles(deviceFarmLogDir);
        LOGGER.info("Quiting driver if needed....");
        if (SeleniumBase.driver != null) {
            SeleniumBase.driver.quit();
        }
        FileReaderUtil.deleteFile("reports/api-test-results.pdf");
        TapReporting.detailedReport("reports/cucumber/cucumber.json", "api");


    }

}

