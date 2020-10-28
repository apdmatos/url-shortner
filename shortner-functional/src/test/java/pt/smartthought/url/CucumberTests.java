package pt.smartthought.url;

import io.cucumber.junit.Cucumber;
import io.cucumber.junit.CucumberOptions;
import io.cucumber.spring.CucumberContextConfiguration;
import org.junit.runner.RunWith;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.ConfigFileApplicationContextInitializer;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.test.context.ContextConfiguration;
import pt.smartthought.url.config.ServiceConfig;


@CucumberOptions(
        features = {"src/test/resources"},
        tags = "not @IGNORE",
        plugin = {
                "pretty",
                "html:build/cucumber-functional-html-report",
                "junit:build/junit-functional-test-report.xml",
                "json:build/cucumber-reports/cucumber-functional-json-report.json"
        })
@RunWith(Cucumber.class)
@EnableAutoConfiguration
@ComponentScan
@EnableConfigurationProperties
@CucumberContextConfiguration
@ContextConfiguration(
        classes = { ServiceConfig.class },
        initializers = {ConfigFileApplicationContextInitializer.class} )
public class CucumberTests {
}
