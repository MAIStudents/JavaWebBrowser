package ru.mai.lessons.rpks.Controllers;

import javafx.fxml.FXML;
import javafx.scene.control.TextArea;
import lombok.Setter;
import org.tinylog.Logger;

public class CodeEditorController {
    @FXML
    private TextArea codeTextArea;

    @Setter
    private CodeEditorListener listener;

    public void initialize() {
    }

    public void setCode(String code) {
        codeTextArea.setText(code);
    }

    @FXML
    private void saveCode() {
        String updatedCode = codeTextArea.getText();
        if (listener != null) {
            listener.onCodeSaved(updatedCode);
        } else {
            Logger.warn("Listener is null. Cannot save code");
        }
        codeTextArea.getScene().getWindow().hide();
    }

    public interface CodeEditorListener {
        void onCodeSaved(String updatedCode);
    }
}
