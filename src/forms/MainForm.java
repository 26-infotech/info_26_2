package forms;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.event.WindowEvent;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableModel;

import forms.CertiListForm;
import forms.ExamScheduleForm;
import forms.LectureForm;
import forms.LoginForm;
import forms.MyCourseForm;
import utils.Alert;
import utils.DB;
import utils.Temp;

public class MainForm extends utils.Frame {
	JLabel slide = new JLabel(" ", 0);
	String[] slides = { "main/1", "main/2", "main/3", "main/4", "main/5" };
	int slideIdx;
	JPanel loginBox = new JPanel(new BorderLayout(0, 8));
	JPanel nav;

	void searchGo(String input) {
		if (input.isEmpty()) {
			Alert.error("해당하는 자격증이 존재하지 않습니다.");
			return;
		}
		try {
			ResultSet rs = DB.execute("select cno from certi where cname like '%" + input + "%' limit 1");
			if (!rs.next()) {
				Alert.error("해당하는 자격증이 존재하지 않습니다.");
				return;
			}
		} catch (SQLException e) {
			Alert.error("조회 실패");
			return;
		}
		Temp.searchInput = input;
		new CertiListForm(0, input).showPage();
		dispose();
	}

	void changeSlide() {
		ImageIcon icon = new ImageIcon("datafiles/" + slides[slideIdx] + ".png");
		if (icon.getIconWidth() <= 0) icon = new ImageIcon("datafiles/" + slides[slideIdx] + ".jpg");
		if (icon.getIconWidth() > 0) {
			Image m = icon.getImage().getScaledInstance(760, 300, Image.SCALE_SMOOTH);
			slide.setIcon(new ImageIcon(m));
			slide.setText("");
		} else {
			slide.setText("메인 이미지");
		}
	}

	void loadLoginBox() {
		loginBox.removeAll();
		if (!Temp.login()) {
			JPanel p = new JPanel(new GridLayout(0, 1, 0, 6));
			p.add(new JLabel("로그인이 필요합니다."));
			JButton b = new JButton("로그인");
			bl(b);
			b.addActionListener(e -> {
				new LoginForm().showPage();
				dispose();
			});
			p.add(b);
			loginBox.add(p, NORTH);
		} else {
			JPanel top = new JPanel(new GridLayout(0, 1, 0, 4));
			top.add(new JLabel(Temp.loginName + "님"));
			JButton info = new JButton("내정보");
			JButton out = new JButton("로그아웃");
			bl(info);
			bl(out);
			info.addActionListener(e -> {
				new MyCourseForm().showPage();
				dispose();
			});
			out.addActionListener(e -> {
				Temp.logout();
				new MainForm().showPage();
				dispose();
			});
			top.add(info);
			top.add(out);
			loginBox.add(top, NORTH);

			String[] cols = { "과정", "시작일", "상태", "" };
			DefaultTableModel d = new DefaultTableModel(cols, 0) {
				public boolean isCellEditable(int r, int c) { return c == 3; }
			};
			try {
				rs = DB.execute("select c.cno,c.cname,r.start_date,c.days from course_registration r"
						+ " join certi c on c.cno=r.cno "
						+ " where r.uno=" + Temp.loginUno + " order by r.start_date desc");
				while (rs.next()) {
					LocalDate st = LocalDate.parse(rs.getString(3));
					LocalDate end = st.plusDays(29);
					boolean on = !LocalDate.now().isAfter(end);
					d.addRow(new Object[] { rs.getString(2), rs.getString(3), on ? "학습중" : "종료", "강의실 가기" });
				}
			} catch (Exception e) {
			}
			jta = new javax.swing.JTable(d);
			jta.getColumnModel().getColumn(3).setCellRenderer((t, v, s, f, r, c) -> {
				JButton b = new JButton(v == null ? "" : v.toString());
				b.setEnabled("학습중".equals(jta.getValueAt(r, 2)));
				return b;
			});
			jta.addMouseListener(new java.awt.event.MouseAdapter() {
				public void mouseClicked(java.awt.event.MouseEvent e) {
					int r = jta.getSelectedRow(), c = jta.getSelectedColumn();
					if (r < 0 || c != 3) return;
					if (!"학습중".equals(jta.getValueAt(r, 2))) {
						Alert.info("학습기간이 종료되었습니다.");
						return;
					}
					try {
						ResultSet x = DB.execute("select cno from certi where cname='" + jta.getValueAt(r, 0) + "' limit 1");
						if (x.next()) Temp.selectedCno = x.getInt(1);
					} catch (Exception ex) {
					}
					new LectureForm(Temp.selectedCno).showPage();
					dispose();
				}
			});
			loginBox.add(new JScrollPane(jta), CENTER);
		}
		loginBox.revalidate();
	}

	void needLogin(Runnable ok) {
		if (Temp.login()) {
			ok.run();
			return;
		}
		Alert.error("로그인이 되어있지 않습니다.");
		new LoginForm().showPage();
		dispose();
	}

	public MainForm() {
		init("메인");
		setSize(1200, 760);
		northP.setBorder(new LineBorder(Color.LIGHT_GRAY));
		northP.add(p0 = new JPanel(new BorderLayout(10, 0)));
		p0.setBorder(new javax.swing.border.EmptyBorder(10, 10, 10, 10));
		p0.add(img = new JLabel("Skills Qualification Association"), WEST);
		p0.add(p1 = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0)), CENTER);
		p1.add(jt = new JTextField());
		sz(jt, 260, 34);
		jt.setBorder(new LineBorder(Color.CYAN, 1, true));
		p1.add(jb = new JButton("검색"));
		bl(jb);
		jb.addActionListener(e -> searchGo(jt.getText().trim()));

		p0.add(nav = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0)), EAST);
		JButton b1 = new JButton("자격증 목록");
		JButton b2 = new JButton("시험일정");
		JButton b3 = new JButton("로그인");
		bl(b1); bl(b2); bl(b3);
		nav.add(b1); nav.add(b2); nav.add(b3);
		b1.addActionListener(e -> needLogin(() -> {
			new CertiListForm(0, null).showPage();
			dispose();
		}));
		b2.addActionListener(e -> needLogin(() -> {
			new ExamScheduleForm().showPage();
			dispose();
		}));
		b3.addActionListener(e -> {
			new LoginForm().showPage();
			dispose();
		});

		centerP.add(p2 = new JPanel(new BorderLayout(10, 10)));
		p2.setBorder(new javax.swing.border.EmptyBorder(12, 12, 12, 12));
		p2.add(slide, CENTER);
		changeSlide();
		th = new Thread(this);
		th.start();

		p2.add(p3 = new JPanel(new GridLayout(1, 6, 8, 0)), SOUTH);
		String[] cat = { "봉사", "요리", "의학", "운동", "IT", "항공" };
		for (int i = 0; i < cat.length; i++) {
			int cg = i + 1;
			JButton b = new JButton(cat[i]);
			b.setToolTipText(cat[i]);
			b.addMouseListener(new java.awt.event.MouseAdapter() {
				public void mouseEntered(java.awt.event.MouseEvent e) {
					b.setFont(b.getFont().deriveFont((float) (b.getFont().getSize() * 1.2)));
				}
				public void mouseExited(java.awt.event.MouseEvent e) {
					b.setFont(b.getFont().deriveFont(12f));
				}
			});
			b.addActionListener(e -> {
				if (!Temp.login()) {
					Alert.error("로그인하세요");
					return;
				}
				Temp.selectedCgno = cg;
				new CertiListForm(cg, null).showPage();
				dispose();
			});
			p3.add(b);
		}

		eastP.add(loginBox);
		sz(loginBox, 360, 0);
		loadLoginBox();
	}

	@Override
	public void run() {
		try {
			while (!Thread.currentThread().isInterrupted()) {
				Thread.sleep(2500);
				slideIdx = (slideIdx + 1) % slides.length;
				javax.swing.SwingUtilities.invokeLater(this::changeSlide);
			}
		} catch (InterruptedException e) {
		}
	}

	@Override
	public void windowClosing(WindowEvent e) {
		if (th != null) th.interrupt();
	}
}
