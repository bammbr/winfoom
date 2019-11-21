package org.kpax.winfoom.view;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.MenuBar;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.WindowEvent;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.conn.HttpHostConnectException;
import org.kpax.winfoom.JavafxApplication;
import org.kpax.winfoom.config.SystemConfig;
import org.kpax.winfoom.config.UserConfig;
import org.kpax.winfoom.proxy.LocalProxyServer;
import org.kpax.winfoom.util.FxUtils;
import org.kpax.winfoom.util.HttpUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.security.auth.login.CredentialException;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.io.IOException;
import java.net.UnknownHostException;

/**
 * @author Eugen Covaci {@literal eugen.covaci.q@gmail.com}
 * Created on 9/10/2019
 */
@Component
public class MainController {
    Logger logger = LoggerFactory.getLogger(MainController.class);

    @Autowired
    private LocalProxyServer localProxyServer;

    @Autowired
    private UserConfig userConfig;

    @Autowired
    private SystemConfig systemConfig;

    @Autowired
    private JavafxApplication javafxApplication;

    @FXML
    private BorderPane borderPane;

    @FXML
    private TextField proxyHost;

    @FXML
    private TextField proxyPort;

    @FXML
    private TextField localProxyPort;

    @FXML
    private TextField testUrl;

    @FXML
    private Button startBtn;

    @FXML
    private MenuBar menuBar;

    @FXML
    private HBox buttonsBox;

    @FXML
    private VBox centerBox;

    @FXML
    public void initialize() throws Exception {
        proxyHost.setText(userConfig.getProxyHost());
        proxyHost.textProperty().addListener((obs, oldValue, newValue) -> {
            userConfig.setProxyHost(newValue);
        });

        proxyPort.setTextFormatter(FxUtils.createDecimalOnlyTextFormatter());
        proxyPort.setText("" + userConfig.getProxyPort());

        proxyPort.textProperty().addListener((obs, oldValue, newValue) -> {
            userConfig.setProxyPort(Integer.parseInt(newValue));
        });

        localProxyPort.setTextFormatter(FxUtils.createDecimalOnlyTextFormatter());
        localProxyPort.setText("" + userConfig.getLocalPort());
        localProxyPort.textProperty().addListener((obs, oldValue, newValue) -> {
            userConfig.setLocalPort(Integer.parseInt(newValue));
        });

        testUrl.setText(userConfig.getProxyTestUrl());
        testUrl.textProperty().addListener((obs, oldValue, newValue) -> {
            userConfig.setProxyTestUrl(newValue);
        });
    }

    public void start(ActionEvent actionEvent) {
        if (isValidInput()) {
            try {
                localProxyServer.start();
                centerBox.setDisable(true);
                buttonsBox.setDisable(true);
            } catch (Exception e) {
                logger.error("Error on starting proxy server", e);
                FxUtils.showMessage(FxUtils.MessageType.DLG_ERR_TITLE,
                        "Error on starting proxy server.\nSee the application's log for details.");
            }
        }
    }

    private boolean isValidInput() {
        if (StringUtils.isBlank(userConfig.getProxyHost())) {
            FxUtils.showMessage("Validation Error", "Fill in the proxy address!");
            return false;
        }
        if (userConfig.getProxyPort() < 1) {
            FxUtils.showMessage("Validation Error", "Fill in a valid proxy port!");
            return false;
        }
        if (userConfig.getLocalPort() < 1024) {
            FxUtils.showMessage("Validation Error", "Fill in a valid proxy port!");
            return false;
        }
        if (StringUtils.isBlank(userConfig.getProxyTestUrl())) {
            FxUtils.showMessage("Validation Error", "Fill in the test URL!");
            return false;
        }

        // Test the proxy configuration
        try {
            HttpUtils.testProxyConfig(userConfig);
        } catch (CredentialException e) {
            FxUtils.showMessage("Test Connection Error", "Wrong user/password!");
            return false;
        } catch (UnknownHostException e) {
            FxUtils.showMessage("Test Connection Error", "Wrong proxy host!");
            return false;
        } catch (HttpHostConnectException e) {
            FxUtils.showMessage("Test Connection Error", "Wrong proxy port!");
            return false;
        } catch (IOException e) {
            FxUtils.showMessage("Test Connection Error", e.getMessage());
            return false;
        }
        return true;
    }

    public void about(ActionEvent actionEvent) {
        FxUtils.showMessage("About", "Winfoom - Basic Proxy Facade" +
                "\nVersion: " + systemConfig.getReleaseVersion()
                + "\nProject home page: https://github.com/ecovaci/winfoom"
                + "\nLicense: Apache 2.0");
    }

    public void close(ActionEvent actionEvent) {
        menuBar.fireEvent(
                new WindowEvent(javafxApplication.getPrimaryStage().getScene().getWindow(),
                        WindowEvent.WINDOW_CLOSE_REQUEST));
    }
}
