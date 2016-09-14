/*
 * Copyright 2016 Tuntuni.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.tuntuni.controllers;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;
import javafx.application.Platform;
import javafx.collections.ListChangeListener;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.ScrollBar;
import javafx.scene.control.TextArea;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.Region;
import org.tuntuni.Core;
import org.tuntuni.connection.Client;
import org.tuntuni.models.Message;

/**
 * The controller for text messaging and file sharing.
 * <p>
 * It gives a history based text conversation window. Below is a text box and
 * send text button. Above is the conversation history. </p>
 */
public class MessagingController implements Initializable {

    private Client mClient;

    @FXML
    private TextArea messageText;
    @FXML
    private ListView messageList;
    @FXML
    private Label errorLabel;
    @FXML
    private Label userName;
    @FXML
    private ImageView userPhoto;

    private final double MINIMUM_HEIGHT = 70D;
    private final double MAXIMUM_HEIGHT = 150D;

    public MessagingController() {
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        Core.instance().messaging(this);
    }

    public void setClient(Client client) {
        mClient = client;
        Platform.runLater(() -> loadAll());
    }

    private void loadAll() {
        messageText.clear();
        errorLabel.setText("");
        messageList.getItems().clear();
        // load user name and avatar 
        if (mClient == null) {
            userName.setVisible(false);
            return;
        }
        // show user info
        if (mClient.getUserData() != null) {
            userName.setVisible(true);
            userName.setText(mClient.getUserData().getUserName());
            userPhoto.setImage(mClient.getUserData().getAvatar(
                    userPhoto.getFitWidth(), userPhoto.getFitHeight()));
        }
        // load list of past messages
        int size = mClient.messageProperty().size();
        addHistory(mClient.messageProperty().subList(0, size));
        // add listener
        mClient.messageProperty().addListener((ListChangeListener.Change<? extends Message> c) -> {
            addHistory(c.getAddedSubList());
        });
    }

    private void addHistory(List<? extends Message> list) {
        // add each elements
        list.forEach((message) -> {
            Platform.runLater(() -> {
                messageList.getItems().add(MessageBox.createInstance(message));
            });
        });
        // show last
        Platform.runLater(() -> {
            int last = messageList.getItems().size() - 1;
            if (last > 0) {
                messageList.scrollTo(last);
            }
        });
    }

    @FXML
    private void messageKeyPressed(KeyEvent evt) {
        //setTextAreaHeight();
        if (evt.getCode() == KeyCode.ENTER) {
            evt.consume();
            if (evt.isShiftDown()) {
                messageText.appendText("\n");
            } else {
                handleSendMessage(null);
            }
        }
    }

    @FXML
    private void handleSendMessage(ActionEvent event) {
        errorLabel.setText("");
        String text = messageText.getText().trim();

        if (text.isEmpty()) {
            errorLabel.setText("Message is too short");
            return;
        }

        if (mClient == null) {
            errorLabel.setText("The receiver is unknown");
            return;
        }

        String res = sendMessage(text);
        errorLabel.setText(res);
    }

    private String sendMessage(String text) {
        try {
            Message message = new Message();
            message.setText(text);
            mClient.sendMessage(message);
            messageText.clear();
            return "Message sent!";
        } catch (Exception e) {
            return "Failed to deliver";
        }
    }

    @Deprecated
    private void setTextAreaHeight() {
        // elements
        Region content = (Region) messageText.lookup(".content");
        ScrollBar scrollBarv = (ScrollBar) messageText.lookup(".scroll-bar:vertical");
        // get the height of content
        double height = MINIMUM_HEIGHT;
        if (messageText.getText().length() > 10) {
            Insets padding = messageText.getPadding();
            height = content.getHeight() + padding.getTop() + padding.getBottom();
        }
        // set height
        height = Math.max(MINIMUM_HEIGHT, height);
        height = Math.min(MAXIMUM_HEIGHT, height);
        messageText.setMinHeight(height);
        messageText.setPrefHeight(height);
        // enable or the scroll bar
        scrollBarv.setVisibleAmount(MAXIMUM_HEIGHT);
    }
}
