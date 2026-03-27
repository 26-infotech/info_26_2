package forms;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GridLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowEvent;
import java.sql.ResultSet;
import java.util.HashSet;
import java.util.Set;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.border.LineBorder;

import forms.MainForm;
import utils.Alert;
import utils.DB;
import utils.Frame;
import utils.Temp;

public class LectureForm extends Frame {
	int cno;
	JPanel list = new JPanel(new GridLayout(0, 1, 0, 8));
	Set<Integer> seen = new HashSet<>();

	public LectureForm(int cno) {
		this.cno = cno;
		init("강의");
		setSize(1000, 700);
		JScrollPane jsp = new JScrollPane(list);
		centerP.add(jsp);
		try {
			ResultSet rr = DB.execute("select rate from course_registration where uno=" + Temp.loginUno + " and cno=" + cno + " order by crno desc limit 1");
			if (rr.next() && rr.getString(1) != null && !rr.getString(1).isEmpty()) {
				for (String s : rr.getString(1).split(",")) seen.add(Integer.parseInt(s));
			}
		} catch (Exception e) {
		}
		load();
		final int[] py = { 0 }, sy = { 0 };
		jsp.getViewport().addMouseListener(new MouseAdapter() {
			public void mousePressed(MouseEvent e) {
				py[0] = e.getY();
				sy[0] = jsp.getVerticalScrollBar().getValue();
			}
		});
		jsp.getViewport().addMouseMotionListener(new MouseAdapter() {
			public void mouseDragged(MouseEvent e) {
				jsp.getVerticalScrollBar().setValue(sy[0] + py[0] - e.getY());
			}
		});
	}

	void load() {
		list.removeAll();
		try {
			ResultSet r = DB.execute("select lno,title,contents,pdfnum from lecture where cno=" + cno + " order by lno");
			while (r.next()) {
				int no = r.getInt("pdfnum");
				JPanel row = new JPanel(new BorderLayout(8, 4));
				row.setBorder(new LineBorder(Color.GRAY));
				JLabel t = new JLabel(r.getString("title"));
				JLabel c = new JLabel(r.getString("contents"));
				JLabel p = new JLabel("PDF 열기", 0);
				p.setOpaque(true);
				p.setBackground(Color.LIGHT_GRAY);
				if (seen.contains(no)) {
					t.setForeground(Color.GRAY);
					p.setForeground(Color.GRAY);
				}
				p.addMouseListener(new MouseAdapter() {
					public void mouseClicked(MouseEvent e) {
						openPdf(cno, no);
						seen.add(no);
						saveRate();
						t.setForeground(Color.GRAY);
						p.setForeground(Color.GRAY);
						checkAttend();
					}
				});
				row.add(t, NORTH);
				row.add(c, CENTER);
				row.add(p, EAST);
				list.add(row);
			}
		} catch (Exception e) {
		}
	}

	void openPdf(int cno, int pdfnum) {
		try {
			java.io.File f = new java.io.File("datafiles/pdf/" + cno + "/" + pdfnum + ".pdf");
			if (!f.exists()) {
				Alert.error("PDF 파일이 없습니다.");
				return;
			}
			java.awt.Desktop.getDesktop().open(f);
		} catch (Exception e) {
			Alert.error("PDF 실행 실패");
		}
	}

	void saveRate() {
		StringBuilder sb = new StringBuilder();
		seen.stream().sorted().forEach(i -> {
			if (sb.length() > 0) sb.append(",");
			sb.append(i);
		});
		try {
			DB.executeUpdate("update course_registration set rate='" + sb + "' where uno=" + Temp.loginUno + " and cno=" + cno + " order by crno desc limit 1");
		} catch (Exception e) {
		}
	}

	void checkAttend() {
		int total = 0;
		try {
			ResultSet r = DB.execute("select count(*) from lecture where cno=" + cno);
			if (r.next()) total = r.getInt(1);
		} catch (Exception e) {
		}
		if (total == 0) return;
		double per = seen.size() * 100.0 / total;
		if (per >= 60 && !Temp.attendNoti.contains(cno)) {
			Temp.attendNoti.add(cno);
			Alert.info("60% 이상입니다.");
		}
	}

	@Override
	public void windowClosing(WindowEvent e) {
		new MainForm().showPage();
	}
}
