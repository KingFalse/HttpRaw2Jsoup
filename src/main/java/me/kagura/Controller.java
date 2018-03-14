package me.kagura;

import javafx.fxml.FXML;
import javafx.scene.control.TextArea;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.KeyEvent;

public class Controller {

    @FXML
    TextArea input;

    @FXML
    TextArea output;

    Service service = new Service();

    public void inputOnKeyReleased(KeyEvent keyEvent) {
        if ((keyEvent.isControlDown() || keyEvent.isMetaDown()) && keyEvent.getText().equalsIgnoreCase("v")) {
            try {
                input.selectPositionCaret(0);
                output.clear();

                String generateResult = service.doGenerate(input.getText());
                output.setText(generateResult);
                //将生成的代码放入剪切板
                try {
                    Clipboard clipboard = Clipboard.getSystemClipboard();
                    ClipboardContent cc = new ClipboardContent();
                    cc.putString(generateResult);
                    clipboard.setContent(cc);
                } catch (Exception exx) {
                    exx.printStackTrace();
                }
            } catch (Exception e) {
                e.printStackTrace();
                output.setText("发生异常：" + e.getLocalizedMessage());
            }
        }
    }

}
