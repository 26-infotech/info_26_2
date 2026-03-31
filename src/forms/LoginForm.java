package forms;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowEvent;
import java.sql.SQLException;

import utils.Alert;
import utils.DB;
import utils.Frame;
import utils.Temp;

public class LoginForm extends Frame {
	JTextField idField;
	JPasswordField pwField;
	JButton loginBtn;

	public LoginForm() {
		init("로그인");

		northP.setLayout(new FlowLayout(FlowLayout.CENTER));
		marginBorder(northP, 10, 10, 0, 10);

		JLabel img = new JLabel();
		png(img, "icon/logo", 50, 50);
		img.setAlignmentX(Component.CENTER_ALIGNMENT);
		northP.add(img);

		northP.add(jl = new JLabel("Skills Qualification Association"));
		marginBorder(jl, 0, 20, 0, 5);

		// ---------


		centerP.setLayout(new BoxLayout(centerP, BoxLayout.Y_AXIS));
		marginBorder(centerP, 10, 10, 10, 10);

		idField = hint("아이디를 입력하세요!");
		idField.setAlignmentX(Component.CENTER_ALIGNMENT);
		idField.setMaximumSize(new Dimension(250, 40));
		sz(idField, 250, 40);

		pwField = hintPw("비밀번호를 입력하세요!");
		pwField.setAlignmentX(Component.CENTER_ALIGNMENT);
		pwField.setMaximumSize(new Dimension(250, 40));
		sz(pwField, 250, 40);

		loginBtn = new JButton("로그인");
		loginBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
		loginBtn.setMaximumSize(new Dimension(250, 40));
		sz(loginBtn, 250, 40);
		fk(loginBtn, Color.WHITE);
		bk(loginBtn, Color.BLUE);
		loginBtn.addActionListener(e -> login());

		centerP.add(idField);
		centerP.add(Box.createVerticalStrut(8));
		centerP.add(pwField);
		centerP.add(Box.createVerticalStrut(8));
		centerP.add(loginBtn);

		showPackedPage();
	}

	private void login() {
		String id = idField.getText();
		String pw = pwField.getText();

		if (id.isEmpty() || pw.isEmpty()) {
			Alert.error("빈칸이 있습니다.");
			return;
		}

		if (id.equals("admin") && pw.equals("1234")) {
			dispose();
			new AdminForm();
			return;
		}

        try {
            rs = DB.execute("select * from user where id = \""+id+"\" and pw = \""+pw+"\"");

			if (!rs.next()) {
				Alert.error("아이디또는 비밀번호가 올바르지 않습니다.");
				idField.setText("");
				pwField.setText("");
				return;
			}

			Temp.uno = rs.getInt("uno");
			Temp.uname = rs.getString("uname");
			Temp.uid = rs.getString("id");

			Alert.info(Temp.uname + "님 환영합니다.");
			dispose();
			new MainForm();
        } catch (SQLException e) {
            Alert.error("조회 실패: " + e.getMessage());
			return;
        }
    }

	@Override
	public void windowClosing(WindowEvent e) {
		new MainForm();
	}
}