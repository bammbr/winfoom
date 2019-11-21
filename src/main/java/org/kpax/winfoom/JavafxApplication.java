package org.kpax.winfoom;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.WindowEvent;
import org.apache.commons.configuration2.builder.FileBasedConfigurationBuilder;
import org.apache.commons.configuration2.builder.fluent.Configurations;
import org.kpax.winfoom.config.UserConfig;
import org.kpax.winfoom.proxy.LocalProxyServer;
import org.kpax.winfoom.util.LocalIOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.core.io.Resource;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.util.Optional;

public class JavafxApplication extends Application {

    private final Logger logger = LoggerFactory.getLogger(JavafxApplication.class);

    private ConfigurableApplicationContext applicationContext;

    private Stage primaryStage;

    @Override
    public void init() throws Exception {
        ApplicationContextInitializer<GenericApplicationContext> initializer = new ApplicationContextInitializer<GenericApplicationContext>() {
            @Override
            public void initialize(GenericApplicationContext genericApplicationContext) {
                genericApplicationContext.registerBean(JavafxApplication.class, () -> JavafxApplication.this);
                genericApplicationContext.registerBean(FileBasedConfigurationBuilder.class, () -> new Configurations()
                        .propertiesBuilder(LocalIOUtils.toPath(System.getProperty("user.dir"), "config",
                                "user.properties")));
            }
        };

        SpringApplication springApplication = new SpringApplication(FoomApplication.class);
        springApplication.addInitializers(initializer);
        this.applicationContext = springApplication.run(getParameters().getRaw().toArray(new String[0]));

    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        this.primaryStage = primaryStage;

        Platform.setImplicitExit(false);

        Resource fxml = this.applicationContext.getResource("classpath:/view/main.fxml");
        FXMLLoader fxmlLoader = new FXMLLoader(fxml.getURL());
        fxmlLoader.setControllerFactory(this.applicationContext::getBean);
        Parent root = fxmlLoader.load();

        Scene scene = new Scene(root);
        this.primaryStage.setScene(scene);
        this.primaryStage.setTitle("WinFoom");
        this.primaryStage.getIcons().add(
                new javafx.scene.image.Image(new File("./config/img/icon.png").toURI().toURL().toExternalForm()));

        if (SystemTray.isSupported()) {
            Image iconImage = Toolkit.getDefaultToolkit().getImage("config/img/icon.png");
            final TrayIcon trayIcon = new TrayIcon(iconImage, "Basic Proxy Facade");
            trayIcon.setImageAutoSize(true);
            trayIcon.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    Platform.runLater(() -> {
                        primaryStage.setIconified(false);
                        primaryStage.show();
                    });
                }
            });
            final SystemTray tray = SystemTray.getSystemTray();
            this.primaryStage.iconifiedProperty().addListener((observableValue, oldVal, newVal) -> {
                if (newVal != null) {
                    if (newVal) {
                        try {
                            tray.add(trayIcon);
                            Platform.runLater(() -> {
                                primaryStage.hide();
                            });
                        } catch (AWTException ex) {
                            logger.error("Cannot add icon to tray", ex);
                        }
                    } else {
                        tray.remove(trayIcon);
                    }
                }
            });
        } else {
            logger.warn("Icon tray not supported!");
        }

        this.primaryStage.show();

        scene.getWindow().addEventFilter(WindowEvent.WINDOW_CLOSE_REQUEST, event -> {
            if (this.applicationContext.getBean(LocalProxyServer.class).isStarted()) {
                Alert alert =
                        new Alert(Alert.AlertType.NONE,
                                "The local proxy facade is started. \nDo you like to stop the proxy facade and leave the application?",
                                ButtonType.OK,
                                ButtonType.CANCEL);
                alert.initStyle(StageStyle.UTILITY);
                alert.setTitle("Warning");
                Optional<ButtonType> result = alert.showAndWait();
                if (result.get() == ButtonType.OK) {
                    try {
                        applicationContext.getBean(UserConfig.class).save();
                    } catch (Exception e) {
                        logger.error("Error on saving user configuration", e);
                    }
                    Platform.exit();
                } else {
                    event.consume();
                }
            } else {
                Platform.exit();
            }
        });
    }

    @Override
    public void stop() throws Exception {
        logger.info("Close the Spring context");
        this.applicationContext.close();
    }


    public Stage getPrimaryStage() {
        return primaryStage;
    }

    public void sizeToScene() {
        primaryStage.sizeToScene();
    }

    public BorderPane getMainContainer() {
        return (BorderPane) primaryStage.getScene().getRoot();
    }


}
