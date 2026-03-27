package forms;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GridLayout;
import java.awt.event.WindowEvent;
import java.sql.ResultSet;
import java.time.LocalDate;
import java.util.ArrayList;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.LineBorder;

import forms.MainForm;
import utils.DB;
import utils.Frame;
import utils.Temp;

public class MyCourseForm extends Frame {
	static class Item {
		boolean exam;
		int cno;
		String cname;
		String date;
		String detail;
	}

	ArrayList<Item> items = new ArrayList<>();
	int idx;
	JLabel title = new JLabel("", 0);
	JLabel date = new JLabel("", 0);
	JLabel detail = new JLabel("", 0);
	JLabel image = new JLabel("강의", 0);
	JButton prev = new JButton("<");
	JButton next = new JButton(">");

	public MyCourseForm() {
		init("나의 과정");
		setSize(700, 480);
		centerP.add(p0 = new JPanel(new BorderLayout(0, 12)));
		p0.setBorder(new javax.swing.border.EmptyBorder(14, 14, 14, 14));
		p0.add(p1 = new JPanel(new BorderLayout()), NORTH);
		p1.add(prev, WEST);
		p1.add(next, EAST);
		p1.add(new JLabel("나의 과정", 0), CENTER);
		prev.addActionListener(e -> {
			if (idx > 0) idx--;
			draw();
		});
		next.addActionListener(e -> {
			if (idx < items.size() - 1) idx++;
			draw();
		});

		JPanel card = new JPanel(new GridLayout(0, 1, 0, 8));
		card.setBorder(new LineBorder(Color.GRAY));
		card.add(title);
		card.add(date);
		card.add(detail);
		image.setOpaque(true);
		image.setBackground(Color.LIGHT_GRAY);
		card.add(image);
		image.addMouseListener(new java.awt.event.MouseAdapter() {
			public void mouseClicked(java.awt.event.MouseEvent e) {
				if (e.getClickCount() != 2 || items.isEmpty()) return;
				Item it = items.get(idx);
				if (it.exam) return;
				new LectureForm(it.cno).showPage();
				dispose();
			}
		});
		p0.add(card, CENTER);
		load();
		draw();
	}

	void load() {
		try {
			ResultSet r = DB.execute("select r.cno,c.cname,r.start_date from course_registration r join certi c on c.cno=r.cno where r.uno=" + Temp.loginUno);
			while (r.next()) {
				Item i = new Item();
				i.exam = false;
				i.cno = r.getInt(1);
				i.cname = r.getString(2);
				i.date = r.getString(3);
				i.detail = "수강기간: " + i.date + " ~ " + LocalDate.parse(i.date).plusDays(29);
				items.add(i);
			}
			r = DB.execute("select t.cno,c.cname,t.exam_date,t.exam,t.passed from test t join certi c on c.cno=t.cno where t.uno=" + Temp.loginUno);
			while (r.next()) {
				Item i = new Item();
				i.exam = true;
				i.cno = r.getInt(1);
				i.cname = r.getString(2);
				i.date = r.getString(3);
				i.detail = "시험시간: " + r.getString(4) + " / " + (r.getInt(5) == 1 ? "합격" : "불합격");
				items.add(i);
			}
			items.sort((a, b) -> a.date.compareTo(b.date));
		} catch (Exception e) {
		}
	}

	void draw() {
		if (items.isEmpty()) {
			title.setText("신청 내역이 없습니다.");
			date.setText("");
			detail.setText("");
			prev.setEnabled(false);
			next.setEnabled(false);
			return;
		}
		Item i = items.get(idx);
		title.setText((i.exam ? "[시험신청] " : "[수강신청] ") + i.cname);
		date.setText("신청일: " + i.date);
		detail.setText(i.detail);
		image.setText(i.exam ? "시험" : "강의");
		image.setForeground(i.exam ? Color.DARK_GRAY : Color.BLUE.darker());
		prev.setEnabled(idx > 0);
		next.setEnabled(idx < items.size() - 1);
	}

	@Override
	public void windowClosing(WindowEvent e) {
		new MainForm().showPage();
	}
}
