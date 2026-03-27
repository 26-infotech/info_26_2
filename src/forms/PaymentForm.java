package forms;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GridLayout;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.WindowEvent;
import java.sql.ResultSet;
import java.time.LocalDate;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;

import forms.MainForm;
import utils.Alert;
import utils.DB;
import utils.Frame;
import utils.Temp;

public class PaymentForm extends Frame {
	JTextField[] card = { new JTextField(), new JTextField(), new JTextField(), new JTextField() };
	JTextField birth = new JTextField();
	JTextField gender = new JTextField();
	JTextField pass = new JTextField();
	JLabel price = new JLabel();
	JLabel discount = new JLabel();
	int finalPrice, originPrice;
	int cno;

	public PaymentForm() {
		init("결제");
		setSize(760, 520);
		cno = Temp.selectedCno;
		centerP.setBorder(new EmptyBorder(12, 12, 12, 12));
		centerP.add(p0 = new JPanel(new GridLayout(0, 1, 0, 8)));

		JPanel cp = new JPanel(new GridLayout(1, 4, 6, 0));
		for (int i = 0; i < card.length; i++) {
			JTextField t = card[i];
			int idx = i;
			t.addKeyListener(new KeyAdapter() {
				public void keyTyped(KeyEvent e) {
					if (!Character.isDigit(e.getKeyChar()) || t.getText().length() >= 4) e.consume();
				}
				public void keyReleased(KeyEvent e) {
					if (t.getText().length() == 4 && idx < 3) card[idx + 1].requestFocus();
				}
			});
			cp.add(t);
		}
		p0.add(new JLabel("카드번호"));
		p0.add(cp);

		p0.add(new JLabel("주민번호 앞 6자리"));
		p0.add(birth);
		birth.addKeyListener(new KeyAdapter() {
			public void keyTyped(KeyEvent e) {
				if (!Character.isDigit(e.getKeyChar()) || birth.getText().length() >= 6) e.consume();
			}
			public void keyReleased(KeyEvent e) {
				if (birth.getText().length() == 6) gender.requestFocus();
			}
		});
		p0.add(new JLabel("성별번호(1자리)"));
		p0.add(gender);
		gender.addKeyListener(new KeyAdapter() {
			public void keyTyped(KeyEvent e) {
				if (!Character.isDigit(e.getKeyChar()) || gender.getText().length() >= 1) e.consume();
			}
			public void keyReleased(KeyEvent e) {
				if (gender.getText().isEmpty()) return;
				char g = gender.getText().charAt(0);
				if ("1234".indexOf(g) < 0) {
					Alert.error("성별을 확인하세요");
					gender.setText("");
				}
			}
		});
		p0.add(new JLabel("비밀번호(2자리)"));
		p0.add(pass);
		pass.addKeyListener(new KeyAdapter() {
			public void keyTyped(KeyEvent e) {
				if (!Character.isDigit(e.getKeyChar()) || pass.getText().length() >= 2) e.consume();
			}
		});

		p0.add(discount);
		p0.add(price);
		JButton pay = new JButton("결제하기");
		bl(pay);
		p0.add(pay);
		pay.addActionListener(e -> pay());
		loadPrice();
	}

	void loadPrice() {
		try {
			ResultSet r = DB.execute("select caftprice,ccrprice from certi where cno=" + cno);
			if (!r.next()) return;
			originPrice = r.getInt(1);
			finalPrice = r.getInt(2);
			discount.setText("할인금액: " + (originPrice - finalPrice) + "원");
			price.setText("최종 결제금액: " + finalPrice + "원");
			if (originPrice > finalPrice) {
				discount.setForeground(Color.RED);
				price.setForeground(Color.BLUE);
			}
		} catch (Exception e) {
		}
	}

	void pay() {
		String c = card[0].getText() + "-" + card[1].getText() + "-" + card[2].getText() + "-" + card[3].getText();
		if (card[0].getText().isEmpty() || card[1].getText().isEmpty() || card[2].getText().isEmpty() || card[3].getText().isEmpty()
				|| birth.getText().isEmpty() || gender.getText().isEmpty() || pass.getText().isEmpty()) {
			Alert.error("빈칸이 존재합니다.");
			return;
		}
		try {
			ResultSet u = DB.execute("select card,birth,gender from user where uno=" + Temp.loginUno);
			u.next();
			if (!c.equals(u.getString("card"))) {
				Alert.error("카드번호가 올바르지 않습니다.");
				return;
			}
			String b = u.getString("birth").replace("-", "").substring(2);
			String g = u.getString("gender").equals("M") ? "1" : "2";
			if (!birth.getText().equals(b) || !gender.getText().equals(g)) {
				Alert.error("주민번호가 올바르지 않습니다.");
				return;
			}
			String p = "" + birth.getText().charAt(2) + birth.getText().charAt(5);
			if (!p.equals(pass.getText())) {
				Alert.error("비밀번호를 확인해 주세요.");
				return;
			}
			if (Temp.paymentExamMode) {
				DB.executeUpdate("insert into test(cno,uno,exam_date,exam,passed) values(" + cno + "," + Temp.loginUno + ",'" + Temp.selectedExamDate + "','" + Temp.selectedExamTime + "',0)");
			} else {
				DB.executeUpdate("insert into course_registration(cno,uno,start_date,rate) values(" + cno + "," + Temp.loginUno + ",'" + LocalDate.now() + "','')");
			}
			Alert.info("결제가 완료되었습니다.");
			new MyCourseForm().showPage();
			dispose();
		} catch (Exception e) {
			Alert.error("결제 실패");
		}
	}

	@Override
	public void windowClosing(WindowEvent e) {
		new MainForm().showPage();
	}
}
