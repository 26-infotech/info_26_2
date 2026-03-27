package forms;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GridLayout;
import java.awt.event.WindowEvent;
import java.sql.ResultSet;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;

import forms.MainForm;
import utils.Alert;
import utils.DB;
import utils.Frame;
import utils.Temp;

public class LoginForm extends Frame {
	JTextField id = hint("아이디");
	JPasswordField pw = new JPasswordField();

	public LoginForm() {
		init("로그인");
		setSize(520, 360);
		centerP.setBorder(new EmptyBorder(25, 25, 25, 25));
		centerP.add(p0 = new JPanel(new BorderLayout(0, 14)));

		JLabel logo = new JLabel("Skills Qualification Association", 0);
		logo.setForeground(Color.BLUE.darker());
		logo.addMouseListener(new java.awt.event.MouseAdapter() {
			public void mouseClicked(java.awt.event.MouseEvent e) {
				new MainForm().showPage();
				dispose();
			}
		});
		p0.add(logo, NORTH);

		p1 = new JPanel(new GridLayout(0, 1, 0, 10));
		pw.setEchoChar((char) 0);
		pw.setText("비밀번호");
		pw.setForeground(Color.LIGHT_GRAY);
		pw.addFocusListener(new java.awt.event.FocusAdapter() {
			public void focusGained(java.awt.event.FocusEvent e) {
				if ("비밀번호".equals(new String(pw.getPassword()))) {
					pw.setText("");
					pw.setEchoChar('*');
					pw.setForeground(Color.BLACK);
				}
			}
		});
		p1.add(id);
		p1.add(pw);
		p0.add(p1, CENTER);

		JButton login = new JButton("로그인");
		bl(login);
		login.addActionListener(e -> login());
		p0.add(login, SOUTH);
	}

	void login() {
		String uid = id.getText().trim();
		String upw = new String(pw.getPassword()).trim();
		if (uid.isEmpty() || upw.isEmpty() || "비밀번호".equals(upw)) {
			Alert.error("빈칸이 있습니다.");
			return;
		}
		if ("admin".equals(uid) && "1234".equals(upw)) {
			Temp.admin = true;
			new AdminForm().showPage();
			dispose();
			return;
		}
		try {
			ResultSet rs = DB.execute("select uno,uname,pw from user where id='" + uid + "'");
			if (!rs.next() || !upw.equals(rs.getString("pw"))) {
				Alert.error("아이디또는 비밀번호가 올바르지 않습니다.");
				id.setText("");
				pw.setText("비밀번호");
				pw.setEchoChar((char) 0);
				pw.setForeground(Color.LIGHT_GRAY);
				return;
			}
			Temp.loginUno = rs.getInt("uno");
			Temp.loginName = rs.getString("uname");
			Alert.info(Temp.loginName + "님 환영합니다.");
			new MainForm().showPage();
			dispose();
		} catch (Exception e) {
			Alert.error("로그인 실패");
		}
	}

	@Override
	public void windowClosing(WindowEvent e) {
		new MainForm().showPage();
	}
}
