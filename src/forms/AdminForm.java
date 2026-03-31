package forms;

import utils.Frame;

import java.awt.event.WindowEvent;

public class AdminForm extends Frame {
    public AdminForm() {
        init("관리자");

        showPackedPage();
    }

    @Override
    public void windowClosing(WindowEvent e) {
        new MainForm();
    }
}
