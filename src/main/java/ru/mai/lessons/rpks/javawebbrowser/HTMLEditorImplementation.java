package ru.mai.lessons.rpks.javawebbrowser;

import javafx.scene.control.TextArea;

public class HTMLEditorImplementation {

    private TextArea textArea;
    private boolean enabled;

    public HTMLEditorImplementation() {
        enabled = false;
    }

    public boolean isEnabled() {
        return enabled;
    }
    public void setEnabled() {
        enabled = true;
    }
    public void setDisabled() {
        enabled = false;
    }

    public void setTextArea(TextArea textArea) {
        this.textArea = textArea;
    }


    public void setText(String text) {
        textArea.setText(text);
    }
    public String getText() {
        return textArea.getText();
    }
}
