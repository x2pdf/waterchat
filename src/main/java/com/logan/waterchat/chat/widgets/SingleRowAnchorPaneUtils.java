package com.logan.waterchat.chat.widgets;

import javafx.collections.ObservableList;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;

import java.util.ArrayList;

/**
 * @author Logan Qin
 * @date 2021/12/27 9:27
 */
public class SingleRowAnchorPaneUtils {


    public static AnchorPane getTextTextFieldButton(String text, String textField, String buttonText) {
        Text label = new Text(text);
        label.setWrappingWidth(130);
        label.setTextAlignment(TextAlignment.RIGHT);


        HBox labelBox = new HBox(label);
        labelBox.setAlignment(Pos.CENTER_RIGHT);
        labelBox.setStyle("-fx-background-color: #d3d7d4");

        TextField field = new TextField(textField);
        field.setMinWidth(400);
        field.setMaxWidth(400);


        HBox box = new HBox(labelBox, field);
        box.setSpacing(5);

        AnchorPane anchorPane = null;
        if (buttonText == null) {
            anchorPane = new AnchorPane(box);
        } else {
            Button button = new Button(buttonText);
            button.setMinWidth(100);
            button.setMaxWidth(100);

            anchorPane = new AnchorPane(box, button);
            // 距离右边 2 offset
            AnchorPane.setRightAnchor(button, 2.0);
        }

        return anchorPane;
    }

    public static AnchorPane getTextButton(String text, String buttonText) {
        Text label = new Text(text);
        label.setWrappingWidth(130);
        label.setTextAlignment(TextAlignment.RIGHT);


        HBox labelBox = new HBox(label);
        labelBox.setAlignment(Pos.CENTER_RIGHT);
        labelBox.setStyle("-fx-background-color: #d3d7d4");

        HBox box = new HBox(labelBox);
        box.setSpacing(5);

        AnchorPane anchorPane = null;
        if (buttonText == null) {
            anchorPane = new AnchorPane(box);
        } else {
            Button button = new Button(buttonText);
            button.setMinWidth(100);
            button.setMaxWidth(100);

            anchorPane = new AnchorPane(box, button);
            // 距离右边 2 offset
            AnchorPane.setRightAnchor(button, 2.0);
        }

        return anchorPane;
    }

    public static AnchorPane getTextTextAreaButton(String text, String textArea, String buttonText) {
        Text label = new Text(text);
        label.setWrappingWidth(130);
        label.setTextAlignment(TextAlignment.RIGHT);
        HBox labelBox = new HBox(label);
        labelBox.setStyle("-fx-background-color: #d3d7d4");

        TextArea field = new TextArea(textArea);
        field.setMinWidth(400);
        field.setMaxWidth(400);

        Button button = new Button(buttonText);
        button.setMinWidth(100);
        button.setMaxWidth(100);

        HBox box = new HBox(labelBox, field);
        box.setSpacing(5);
        AnchorPane anchorPane = new AnchorPane(box, button);
        // 距离右边 2 offset
        AnchorPane.setRightAnchor(button, 2.0);

        return anchorPane;
    }


    public static AnchorPane getTextTextChoiceBox(String text, String text2, String choiceBoxDefaultText, ArrayList<String> choiceBoxTexts) {
        Text previewLabel = new Text(text);
        previewLabel.setWrappingWidth(130);
        previewLabel.setTextAlignment(TextAlignment.RIGHT);
        HBox labelBox = new HBox(previewLabel);
        labelBox.setStyle("-fx-background-color: #d3d7d4");

        Text previewText = new Text(" " + text2);

        ChoiceBox previewChoiceBox = new ChoiceBox();
        previewChoiceBox.getItems().addAll(choiceBoxTexts);
        previewChoiceBox.setValue(choiceBoxDefaultText);


        previewChoiceBox.setMinWidth(100);
        previewChoiceBox.setMaxWidth(100);

        HBox previewBox = new HBox(labelBox, previewText);
        AnchorPane previewAnchorPane = new AnchorPane(previewBox, previewChoiceBox);
        AnchorPane.setRightAnchor(previewChoiceBox, 2.0);

        return previewAnchorPane;
    }

    public static AnchorPane getTextTextChoiceBoxButton(String text,
                                                        String choiceBoxDefaultText, ArrayList<String> choiceBoxTexts,
                                                        String buttonText) {
        Text previewLabel = new Text(text);
        previewLabel.setWrappingWidth(200);
        previewLabel.setTextAlignment(TextAlignment.RIGHT);
        HBox labelBox = new HBox(previewLabel);
//        labelBox.setStyle("-fx-background-color: #d3d7d4");
        labelBox.setAlignment(Pos.BOTTOM_RIGHT);


        ChoiceBox previewChoiceBox = new ChoiceBox();
        previewChoiceBox.getItems().addAll(choiceBoxTexts);
        previewChoiceBox.setValue(choiceBoxDefaultText);


        previewChoiceBox.setMinWidth(100);
        previewChoiceBox.setMaxWidth(100);

        Button button = new Button(buttonText);
        button.setMinWidth(100);
        button.setMaxWidth(100);

        HBox previewBox = new HBox(labelBox);
        AnchorPane previewAnchorPane = new AnchorPane(previewBox, previewChoiceBox, button);
        AnchorPane.setRightAnchor(previewChoiceBox, 102.0);
        AnchorPane.setRightAnchor(button, 2.0);

        return previewAnchorPane;
    }


    public static AnchorPane getTextLabelTextFieldLabelTextFieldButton(String text, String label1, String label2,
                                                                       String buttonText) {
        Text previewLabel = new Text(text);
        previewLabel.setWrappingWidth(160);
        previewLabel.setTextAlignment(TextAlignment.RIGHT);
        HBox labelBox = new HBox(previewLabel);
//        labelBox.setStyle("-fx-background-color: #d3d7d4");
        labelBox.setAlignment(Pos.CENTER_RIGHT);

        Label labelF1 = new Label("  " + label1);
        TextField field1 = new TextField("");
        field1.setMinWidth(50);
        field1.setMaxWidth(80);

        Label labelF2 = new Label("  " + label2);
        TextField field2 = new TextField("");
        field2.setMinWidth(50);
        field2.setMaxWidth(80);


        Button button = new Button(buttonText);
        button.setMinWidth(100);
        button.setMaxWidth(100);

        HBox previewBox = new HBox(labelBox, labelF1, field1, labelF2, field2);
        previewBox.setAlignment(Pos.CENTER_RIGHT);

        AnchorPane previewAnchorPane = new AnchorPane(previewBox, button);
        AnchorPane.setRightAnchor(button, 2.0);

        return previewAnchorPane;
    }


    /**
     * 入参的 pane 只可以是 本工具类返回的 pane 对象，其他自定义 pane 不该调用被方法
     */
    public static Button getButton(AnchorPane anchorPane) {
        if (anchorPane == null) {
            return null;
        }
        ObservableList<Node> children = anchorPane.getChildren();
        for (Node child : children) {
            if (child instanceof Button) {
                Button button = (Button) child;
                return button;
            }
        }

        return null;
    }


    public static TextArea getTextArea(AnchorPane anchorPane) {
        if (anchorPane == null) {
            return null;
        }
        ObservableList<Node> children = anchorPane.getChildren();
        for (Node child : children) {
            if (child instanceof HBox) {
                HBox hBox = (HBox) child;
                for (Node hBoxChild : hBox.getChildren()) {
                    if (hBoxChild instanceof TextArea) {
                        TextArea textField = (TextArea) hBoxChild;
                        return textField;
                    }
                }
            }
        }

        return null;
    }


    /**
     * 入参的 pane 只可以是 本工具类返回的 pane 对象，其他自定义 pane 不该调用被方法
     */
    public static void getTextFieldAndUpdate(AnchorPane anchorPane, String newTextValue) {
        if (anchorPane == null) {
            return;
        }
        ObservableList<Node> children = anchorPane.getChildren();
        for (Node child : children) {
            if (child instanceof HBox) {
                HBox hBox = (HBox) child;
                for (Node hBoxChild : hBox.getChildren()) {
                    if (hBoxChild instanceof TextField) {
                        TextField textField = (TextField) hBoxChild;
                        textField.setText(" " + newTextValue);
                    }
                }
            }
        }
    }


}
