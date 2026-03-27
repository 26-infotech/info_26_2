package forms;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.WindowEvent;
import java.sql.ResultSet;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;

import forms.MainForm;
import utils.Alert;
import utils.DB;
import utils.Frame;
import utils.Temp;

public class ExamScheduleForm extends Frame {
	DefaultTableModel model = new DefaultTableModel(new String[] { "cno", "자격증", "카테고리", "신청일자", "시험기간", "출석률", "" }, 0) {
		public boolean isCellEditable(int r, int c) { return false; }
	};
	JComboBox<String> combo = new JComboBox<>();
	JTextField search = hint("자격증 검색");
	Map<String, Integer> cgMap = new HashMap<>();

	public ExamScheduleForm() {
		init("시험일정");
		setSize(1050, 700);
		JPanel top = new JPanel(new GridLayout(1, 6, 8, 0));
		top.add(new JLabel("카테고리"));
		combo.addItem("전체");
		top.add(combo);
		top.add(search);
		JButton find = new JButton("조회하기");
		JButton home = new JButton("메인");
		bl(find);
		bl(home);
		top.add(find);
		top.add(home);
		northP.add(top);

		jta = new javax.swing.JTable(model);
		jta.removeColumn(jta.getColumnModel().getColumn(0));
		jta.getColumnModel().getColumn(5).setCellRenderer((t, v, s, f, r, c) -> {
			JButton b = new JButton("신청하러 가기");
			b.setEnabled("가능".equals(jta.getValueAt(r, 4)));
			return b;
		});
		jta.addMouseListener(new java.awt.event.MouseAdapter() {
			public void mouseClicked(java.awt.event.MouseEvent e) {
				int r = jta.getSelectedRow(), c = jta.getSelectedColumn();
				if (r < 0 || c != 5 || !"가능".equals(jta.getValueAt(r, 4))) return;
				int cno = Integer.parseInt(model.getValueAt(r, 0).toString());
				double rate = attendance(cno);
				if (rate >= 60) {
					new ExamSlotForm(cno).showPage();
					dispose();
				} else {
					Alert.error("현재 신청하려는 과정의 출석률이 60%미만입니다.");
					Alert.error("출석을 완료한 뒤 다시 돌아와주세요.");
					new LectureForm(cno).showPage();
					dispose();
				}
			}
		});
		centerP.add(new JScrollPane(jta));

		find.addActionListener(e -> load(search.getText().trim(), combo.getSelectedItem().toString()));
		home.addActionListener(e -> {
			new MainForm().showPage();
			dispose();
		});
		loadCategory();
		load("", "전체");
	}

	void loadCategory() {
		try {
			rs = DB.execute("select * from category order by cgno");
			while (rs.next()) {
				combo.addItem(rs.getString("cgname"));
				cgMap.put(rs.getString("cgname"), rs.getInt("cgno"));
			}
		} catch (Exception e) {
		}
	}

	void load(String kw, String cat) {
		model.setRowCount(0);
		try {
			String where = " where 1=1 ";
			if (!kw.isEmpty()) where += " and c.cname like '%" + kw + "%' ";
			if (!"전체".equals(cat)) where += " and instr(c.cgno,'" + cgMap.get(cat) + "')>0 ";
			String sql = "select s.cno,c.cname,c.cgno,s.start_date,s.exam_date from schedule s join certi c on c.cno=s.cno "
					+ where + " order by s.start_date";
			rs = DB.execute(sql);
			while (rs.next()) {
				int cno = rs.getInt(1);
				String cg = cgName(rs.getString(3));
				LocalDate st = LocalDate.parse(rs.getString(4));
				LocalDate ex = LocalDate.parse(rs.getString(5));
				String apply = st + " ~ " + st.plusDays(5);
				String exam = ex + " ~ " + ex.plusDays(7);
				boolean can = false;
				ResultSet t = DB.execute("select crno from course_registration where uno=" + Temp.loginUno + " and cno=" + cno + " limit 1");
				if (t.next()) can = true;
				model.addRow(new Object[] { cno, rs.getString(2), cg, apply, exam, can ? "가능" : "불가", "신청하러 가기" });
			}
			if (model.getRowCount() == 0) {
				if (!kw.isEmpty()) Alert.error("해당 자격증이 존재하지 않습니다.");
				if (!kw.isEmpty() || !"전체".equals(cat)) {
					search.setText("");
					combo.setSelectedItem("전체");
					load("", "전체");
				}
			}
		} catch (Exception e) {
			Alert.error("조회 실패");
		}
	}

	double attendance(int cno) {
		try {
			ResultSet r = DB.execute("select rate from course_registration where uno=" + Temp.loginUno + " and cno=" + cno + " order by crno desc limit 1");
			if (!r.next()) return 0;
			String x = r.getString(1);
			if (x == null || x.isEmpty()) return 0;
			int cnt = x.split(",").length;
			return cnt * 100.0 / 16.0;
		} catch (Exception e) {
			return 0;
		}
	}

	String cgName(String list) {
		try {
			String[] a = list.split(",");
			String out = "";
			for (String s : a) {
				ResultSet r = DB.execute("select cgname from category where cgno=" + s);
				if (r.next()) out += (out.isEmpty() ? "" : ",") + r.getString(1);
			}
			return out;
		} catch (Exception e) {
			return "";
		}
	}

	@Override
	public void windowClosing(WindowEvent e) {
		new MainForm().showPage();
	}
}
