package forms;

import utils.Frame;

import java.awt.event.WindowEvent;

public class MyPageForm extends Frame {
    public MyPageForm() {
        init("나의 과정");

        showPackedPage();
    }

    @Override
    public void windowClosing(WindowEvent e) {
        new MainForm();
    }
}
