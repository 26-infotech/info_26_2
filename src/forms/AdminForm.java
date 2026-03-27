package forms;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowEvent;
import java.sql.ResultSet;
import java.util.ArrayList;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.Timer;

import forms.MainForm;
import utils.DB;
import utils.Frame;
import utils.Temp;

public class AdminForm extends Frame {
	static class D {
		String name;
		int value;
	}

	ArrayList<D> list = new ArrayList<>();
	double startAngle;
	int hover = -1;
	long hoverAt;

	public AdminForm() {
		init("관리자");
		setSize(780, 620);
		load();
		centerP.setLayout(new BorderLayout());
		centerP.add(new JLabel("교수별 수강인원 TOP5", 0), NORTH);
		JPanel pie = new JPanel() {
			protected void paintComponent(Graphics g) {
				super.paintComponent(g);
				if (list.isEmpty()) return;
				Graphics2D g2 = (Graphics2D) g;
				g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
				int sum = list.stream().mapToInt(x -> x.value).sum();
				int x = 190, y = 100, w = 340, h = 340;
				double a = startAngle;
				Color[] col = { Color.RED, Color.BLUE, Color.GREEN, Color.ORANGE, Color.MAGENTA };
				for (int i = 0; i < list.size(); i++) {
					int ang = (int) Math.round(list.get(i).value * 360.0 / sum);
					int ex = i == hover ? 14 : 0;
					g2.setColor(col[i % col.length]);
					g2.fillArc(x + ex, y + ex, w, h, (int) a, ang);
					a += ang;
				}
				g2.setColor(Color.WHITE);
				g2.fillOval(x + 90, y + 90, 160, 160);
				g2.setColor(Color.DARK_GRAY);
				g2.setStroke(new BasicStroke(2f));
				g2.drawOval(x + 90, y + 90, 160, 160);
			}
		};
		pie.setPreferredSize(new Dimension(700, 520));
		pie.addMouseMotionListener(new MouseAdapter() {
			public void mouseMoved(MouseEvent e) {
				int idx = indexAt(e.getX(), e.getY());
				if (idx != hover) {
					hover = idx;
					hoverAt = System.currentTimeMillis();
					if (idx >= 0) pie.setToolTipText(list.get(idx).name + " : " + list.get(idx).value + "명");
					pie.repaint();
				}
			}
		});
		centerP.add(pie, CENTER);
		new Timer(40, e -> {
			if (hover >= 0 && System.currentTimeMillis() - hoverAt >= 1000) startAngle += 8;
			pie.repaint();
		}).start();
	}

	int indexAt(int mx, int my) {
		if (list.isEmpty()) return -1;
		double cx = 360, cy = 270;
		double dx = mx - cx, dy = my - cy;
		double dist = Math.sqrt(dx * dx + dy * dy);
		if (dist < 80 || dist > 180) return -1;
		double deg = Math.toDegrees(Math.atan2(dy, dx));
		if (deg < 0) deg += 360;
		double cur = (deg - startAngle) % 360;
		if (cur < 0) cur += 360;
		int sum = list.stream().mapToInt(x -> x.value).sum();
		double acc = 0;
		for (int i = 0; i < list.size(); i++) {
			acc += list.get(i).value * 360.0 / sum;
			if (cur <= acc) return i;
		}
		return -1;
	}

	void load() {
		try {
			ResultSet rs = DB.execute("select t.tname,count(r.crno) cnt from teacher t join certi c on c.tno=t.tno "
					+ "left join course_registration r on r.cno=c.cno group by t.tno order by cnt desc limit 5");
			while (rs.next()) {
				D d = new D();
				d.name = rs.getString(1);
				d.value = rs.getInt(2);
				list.add(d);
			}
		} catch (Exception e) {
		}
	}

	@Override
	public void windowClosing(WindowEvent e) {
		Temp.admin = false;
		new MainForm().showPage();
	}
}
