package forms;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GridLayout;
import java.awt.event.WindowEvent;
import java.sql.ResultSet;
import java.time.LocalDate;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;

import forms.MainForm;
import utils.Alert;
import utils.DB;
import utils.Frame;
import utils.Temp;

public class CertiListForm extends Frame {
	JPanel list = new JPanel(new GridLayout(0, 1, 0, 8));
	int cgno;
	String keyword;

	public CertiListForm(int cgno, String keyword) {
		this.cgno = cgno;
		this.keyword = keyword;
		init("자격증 목록");
		setSize(1100, 760);
		northP.setBorder(new EmptyBorder(8, 12, 8, 12));
		northP.add(p0 = new JPanel(new BorderLayout(10, 0)));
		JLabel logo = new JLabel("Skills Qualification Association");
		logo.setForeground(Color.BLUE.darker());
		logo.addMouseListener(new java.awt.event.MouseAdapter() {
			public void mouseClicked(java.awt.event.MouseEvent e) {
				new MainForm().showPage();
				dispose();
			}
		});
		p0.add(logo, WEST);

		p1 = new JPanel(new GridLayout(1, 7, 6, 0));
		String[] c = { "추천과정", "IT", "요리", "봉사", "운동", "의학", "항공" };
		int[] map = { 0, 5, 2, 1, 4, 3, 6 };
		for (int i = 0; i < c.length; i++) {
			JButton b = new JButton(c[i]);
			int m = map[i];
			if (m == cgno && keyword == null) b.setBackground(Color.CYAN.brighter());
			b.addActionListener(e -> {
				new CertiListForm(m, null).showPage();
				dispose();
			});
			p1.add(b);
		}
		p0.add(p1, CENTER);

		JScrollPane jsp = new JScrollPane(list);
		list.setBorder(new EmptyBorder(10, 10, 10, 10));
		jsp.setBorder(new LineBorder(Color.LIGHT_GRAY));
		centerP.add(jsp);
		load();
	}

	void load() {
		list.removeAll();
		try {
			String sql;
			if (keyword != null) {
				sql = "select * from certi where cname like '%" + keyword + "%' order by cno";
			} else if (cgno == 0) {
				sql = "select c.*,count(r.crno) cnt from certi c left join course_registration r on c.cno=r.cno "
						+ "group by c.cno order by cnt desc,c.cno asc limit 6";
			} else {
				sql = "select * from certi where instr(cgno,'" + cgno + "')>0 order by cno";
			}
			rs = DB.execute(sql);
			while (rs.next()) {
				int cno = rs.getInt("cno");
				int origin = rs.getInt("caftprice");
				int price = rs.getInt("ccrprice");
				JPanel card = new JPanel(new BorderLayout(8, 8));
				card.setBorder(new LineBorder(Color.GRAY));
				card.setBackground(Color.WHITE);
				card.add(new JLabel("[" + rs.getString("cname") + "] " + rs.getString("ratring") + "급"), NORTH);
				JPanel body = new JPanel(new GridLayout(0, 1, 0, 4));
				body.add(new JLabel("교육방식: " + rs.getString("type") + " / " + rs.getString("type1")));
				body.add(new JLabel("강의기간: " + rs.getString("days")));
				body.add(new JLabel("수강혜택: 교안무료+시험예상기출문제제공"));
				body.add(new JLabel("원가: " + origin + "원 / 결제금액: " + price + "원"));
				card.add(body, CENTER);
				JPanel btns = new JPanel(new GridLayout(1, 2, 8, 0));
				JButton choose = new JButton("과목선택하기");
				JButton preview = new JButton("기출문제 맛보기");
				bl(choose);
				bl(preview);
				choose.addActionListener(e -> {
					Temp.selectedCno = cno;
					Temp.paymentExamMode = false;
					new PaymentForm().showPage();
					dispose();
				});
				preview.addActionListener(e -> openPdf(cno, 1));
				btns.add(choose);
				btns.add(preview);
				card.add(btns, SOUTH);
				list.add(card);
			}
			if (list.getComponentCount() == 0) {
				list.add(new JLabel("조회 결과가 없습니다."));
			}
		} catch (Exception e) {
			Alert.error("조회 실패");
		}
		list.revalidate();
		list.repaint();
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

	@Override
	public void windowClosing(WindowEvent e) {
		new MainForm().showPage();
	}
}
