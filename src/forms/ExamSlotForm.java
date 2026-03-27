package forms;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.WindowEvent;
import java.sql.ResultSet;
import java.time.LocalDate;
import java.time.YearMonth;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JSlider;

import forms.MainForm;
import utils.Alert;
import utils.DB;
import utils.Frame;
import utils.Temp;

public class ExamSlotForm extends Frame {
	int cno;
	LocalDate base = LocalDate.now();
	YearMonth ym = YearMonth.now();
	JLabel date = new JLabel();
	DefaultListModel<String> m = new DefaultListModel<>();
	JList<String> list = new JList<>(m);

	public ExamSlotForm(int cno) {
		this.cno = cno;
		init("시험스케줄");
		setSize(760, 520);

		JPanel top = new JPanel(new BorderLayout());
		top.add(new JLabel("현재 날짜: " + base), WEST);
		JButton home = new JButton("메인");
		top.add(home, EAST);
		home.addActionListener(e -> {
			new MainForm().showPage();
			dispose();
		});
		northP.add(top);

		centerP.add(p0 = new JPanel(new BorderLayout(0, 12)));
		p0.add(date, NORTH);
		JSlider slider = new JSlider(1, ym.lengthOfMonth(), base.getDayOfMonth());
		p0.add(slider, CENTER);
		p0.add(list, SOUTH);
		date.setText("선택일: " + ym.atDay(slider.getValue()));
		load(ym.atDay(slider.getValue()));
		slider.addChangeListener(e -> {
			LocalDate d = ym.atDay(slider.getValue());
			date.setText("선택일: " + d);
			load(d);
		});
		list.addMouseListener(new java.awt.event.MouseAdapter() {
			public void mouseClicked(java.awt.event.MouseEvent e) {
				if (list.getSelectedValue() == null) return;
				if (list.getSelectedValue().endsWith("(마감)")) return;
				String t = list.getSelectedValue().split(" ")[0];
				Temp.selectedCno = cno;
				Temp.selectedExamDate = date.getText().replace("선택일: ", "");
				Temp.selectedExamTime = t;
				Temp.paymentExamMode = true;
				new PaymentForm().showPage();
				dispose();
			}
		});
	}

	void load(LocalDate d) {
		m.clear();
		try {
			ResultSet rs = DB.execute("select stime from schedule where cno=" + cno + " and exam_date='" + d + "' limit 1");
			if (!rs.next()) {
				m.addElement("해당 날짜 시험 스케줄이 없습니다.");
				return;
			}
			String[] time = rs.getString(1).replace(" ", "").split(",");
			for (String t : time) {
				ResultSet c = DB.execute("select count(*) from test where cno=" + cno + " and exam_date='" + d + "' and exam='" + t + "'");
				c.next();
				int cnt = c.getInt(1);
				m.addElement(t + (cnt >= 30 ? " (마감)" : " (" + cnt + "/30)"));
			}
		} catch (Exception e) {
			Alert.error("조회 실패");
		}
	}

	@Override
	public void windowClosing(WindowEvent e) {
		new MainForm().showPage();
	}
}
