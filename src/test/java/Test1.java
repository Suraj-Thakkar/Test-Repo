import io.github.bonigarcia.wdm.WebDriverManager;
import org.apache.commons.io.FileUtils;
import org.monte.media.Format;
import org.monte.media.math.Rational;
import org.monte.screenrecorder.ScreenRecorder;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.annotations.Test;

import java.awt.*;
import java.awt.Rectangle;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.util.Date;

import static org.monte.media.FormatKeys.*;
import static org.monte.media.VideoFormatKeys.*;

public class Test1 {

    private ScreenRecorder screenRecorder;

    @Test
    public void Test() throws Exception {
        startRecording("Yogi_Login_Test");

        WebDriverManager.chromedriver().setup();
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--remote-allow-origins=*");


        WebDriver driver = new ChromeDriver(options);
        // Explicitly set the size for the Virtual Display
        driver.manage().window().maximize();

        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(30));

        try {
            driver.get("https://yogi.web.cashbook.in/login");
            wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//button[text()='Other ways to login']"))).click();

            // Step 1: Login
            WebElement phoneInput = wait.until(ExpectedConditions.elementToBeClickable(By.id("phoneNumber")));
            phoneInput.sendKeys("+911000112587");

            WebElement submitBtn = wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//button[@type='submit']")));
            submitBtn.click();

            // Step 2: OTP
            WebElement otpInput = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("input[type='text']")));
            otpInput.sendKeys("123456");
            driver.findElement(By.xpath("//button[text()='Verify']")).click();

            // Step 3: Handle the "Ok, Got it" pop-up safely
            try {
                WebElement okBtn = wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//button[text()='Ok, Got it']")));
                okBtn.click();
            } catch (TimeoutException e) {
                System.out.println("Pop-up 'Ok, Got it' did not appear, continuing...");
            }

            // Step 4: Final Actions
            wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//div[contains(@class,'box_position_relative')]"))).click();

            WebElement search = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("input[name='q']")));
            search.sendKeys("Rahul Mehta");

            wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("input[value='lj6wYpwHdtxTL1acFmln']"))).click();

        } catch (Exception e) {
            takeScreenshot(driver, "Failure_at_Step");
            throw e;
        } finally {
            stopRecording();
            driver.quit();
        }
    }

    // --- Utility Methods ---

    public void startRecording(String methodName) throws Exception {
        File file = new File(System.getProperty("user.dir") + File.separator + "recordings");
        if (!file.exists()) file.mkdirs();

        GraphicsConfiguration gc = GraphicsEnvironment
                .getLocalGraphicsEnvironment()
                .getDefaultScreenDevice()
                .getDefaultConfiguration();

        this.screenRecorder = new SpecializedScreenRecorder(gc, gc.getBounds(),
                new Format(MediaTypeKey, MediaType.FILE, MimeTypeKey, MIME_AVI),
                new Format(MediaTypeKey, MediaType.VIDEO, EncodingKey, ENCODING_AVI_TECHSMITH_SCREEN_CAPTURE,
                        CompressorNameKey, ENCODING_AVI_TECHSMITH_SCREEN_CAPTURE,
                        DepthKey, 24, FrameRateKey, Rational.valueOf(15),
                        QualityKey, 1.0f, KeyFrameIntervalKey, 15 * 60),
                new Format(MediaTypeKey, MediaType.VIDEO, EncodingKey, "black", FrameRateKey, Rational.valueOf(30)),
                null, file, methodName);

        this.screenRecorder.start();
        System.out.println("Recording started...");
    }

    public void stopRecording() throws Exception {
        this.screenRecorder.stop();
        System.out.println("Recording stopped.");
    }

    public void takeScreenshot(WebDriver driver, String fileName) throws IOException {
        File scrFile = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);
        String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        File destFile = new File("./screenshots/" + fileName + "_" + timestamp + ".png");
        FileUtils.copyFile(scrFile, destFile);
        System.out.println("Screenshot saved at: " + destFile.getAbsolutePath());
    }
}

// Custom Recorder Class to handle file naming
class SpecializedScreenRecorder extends ScreenRecorder {
    private String name;

    public SpecializedScreenRecorder(GraphicsConfiguration cfg, Rectangle captureArea, Format fileFormat,
                                     Format screenFormat, Format mouseFormat, Format audioFormat, File movieFolder, String name)
            throws IOException, AWTException {
        super(cfg, captureArea, fileFormat, screenFormat, mouseFormat, audioFormat, movieFolder);
        this.name = name;
    }

    @Override
    protected File createMovieFile(Format fileFormat) throws IOException {
        if (!movieFolder.exists()) movieFolder.mkdirs();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd_HHmmss");
        return new File(movieFolder, name + "_" + dateFormat.format(new Date()) + ".avi");
    }
}