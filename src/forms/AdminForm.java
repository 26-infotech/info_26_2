package forms;

import utils.Alert;
import utils.DB;
import utils.Frame;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.Arc2D;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class AdminForm extends Frame {

    public AdminForm() {
        init("관리자");
        setSize(500, 480);

        northP.setLayout(new FlowLayout(FlowLayout.LEFT, 8, 8));
        JLabel logoLabel = new JLabel();
        png(logoLabel, "icon/logo", 40, 40);
        northP.add(logoLabel);
        northP.add(jl = new JLabel("Skills Qualification Association"));
        ft(jl, Font.PLAIN, 14);

        PieChartPanel chart = new PieChartPanel();
        centerP.add(chart, CENTER);

        showPage();
    }

    @Override
    public void windowClosing(WindowEvent e) {
        dispose();
        new MainForm();
    }

    class PieChartPanel extends JPanel {

        private List<String>  labels  = new ArrayList<>();
        private List<Integer> values  = new ArrayList<>();
        private int           total   = 0;

        private int     hoveredIdx     = -1;
        private int     separatedIdx   = -1;
        private boolean rotating       = false;
        private double  rotateAngle    = 0;
        private Timer   hoverTimer;
        private Timer   rotateTimer;

        private final Color[] COLORS = {
                Color.RED, Color.YELLOW, Color.GREEN, Color.BLUE, Color.MAGENTA
        };

        PieChartPanel() {
            setPreferredSize(new Dimension(460, 380));
            loadData();

            addMouseListener(new MouseAdapter() {
                @Override public void mouseEntered(MouseEvent e) { checkHover(e); }
                @Override public void mouseMoved(MouseEvent e)    { checkHover(e); }
                @Override public void mouseExited(MouseEvent e) {
                    if (hoverTimer != null) hoverTimer.stop();
                    hoveredIdx = -1; repaint();
                }
            });
            addMouseMotionListener(new MouseAdapter() {
                @Override public void mouseMoved(MouseEvent e) { checkHover(e); }
            });
        }

        private void loadData() {
            try {
                ResultSet rs = DB.execute(
                        "SELECT t.tname, COUNT(cr.crno) AS cnt " +
                                "FROM course_registration cr " +
                                "JOIN certi c ON cr.cno = c.cno " +
                                "JOIN teacher t ON c.tno = t.tno " +
                                "GROUP BY t.tno, t.tname " +
                                "ORDER BY cnt DESC " +
                                "LIMIT 5"
                );
                while (rs.next()) {
                    labels.add(rs.getString("tname"));
                    int cnt = rs.getInt("cnt");
                    values.add(cnt);
                    total += cnt;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        private void checkHover(MouseEvent e) {
            if (rotating || values.isEmpty()) return;

            int idx = getSliceAt(e.getX(), e.getY());
            if (idx == hoveredIdx) return;

            hoveredIdx = idx;
            if (hoverTimer != null) hoverTimer.stop();

            if (idx >= 0) {
                hoverTimer = new Timer(1000, ev -> {
                    hoverTimer.stop();
                    startRotate(idx);
                });
                hoverTimer.setRepeats(false);
                hoverTimer.start();
            }
            repaint();
        }

        private void startRotate(int targetIdx) {
            rotating = true;
            rotateAngle = 0;

            rotateTimer = new Timer(16, null);
            rotateTimer.addActionListener(e -> {
                rotateAngle += 6;
                if (rotateAngle >= 360) {
                    rotateAngle = 0;
                    rotating = false;
                    separatedIdx = targetIdx;
                    rotateTimer.stop();
                }
                repaint();
            });
            rotateTimer.start();
        }

        private int getSliceAt(int mx, int my) {
            if (values.isEmpty() || total == 0) return -1;

            int cx = getWidth() / 2;
            int cy = getHeight() / 2;
            int r  = Math.min(getWidth(), getHeight()) / 2 - 30;

            double dx = mx - cx;
            double dy = my - cy;
            if (dx * dx + dy * dy > (double) r * r) return -1;

            double angle = Math.toDegrees(Math.atan2(-dy, dx));
            if (angle < 0) angle += 360;

            double start = rotateAngle;
            for (int i = 0; i < values.size(); i++) {
                double sweep = (double) values.get(i) / total * 360;
                double end   = start + sweep;
                if (angle >= start % 360 && angle < end % 360) return i;
                start = end;
            }
            return -1;
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (values.isEmpty() || total == 0) return;

            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int cx    = getWidth() / 2;
            int cy    = getHeight() / 2;
            int r     = Math.min(getWidth(), getHeight()) / 2 - 50;
            int diam  = r * 2;

            double startAngle = rotateAngle;

            for (int i = 0; i < values.size(); i++) {
                double sweep = (double) values.get(i) / total * 360;
                g2.setColor(COLORS[i % COLORS.length]);

                int ox = 0, oy = 0;
                if (i == separatedIdx && !rotating) {
                    double mid = Math.toRadians(startAngle + sweep / 2);
                    ox = (int)(Math.cos(mid) * 14);
                    oy = (int)(-Math.sin(mid) * 14);
                }

                g2.fill(new Arc2D.Double(cx - r + ox, cy - r + oy, diam, diam,
                        startAngle, sweep, Arc2D.PIE));

                startAngle += sweep;
            }

            // 툴팁: separatedIdx 영역에 라벨 표시
            if (separatedIdx >= 0 && !rotating) {
                double pct = (double) values.get(separatedIdx) / total * 100;
                String tip = labels.get(separatedIdx) + ": " + String.format("%.1f", pct) + "%";

                double startA = rotateAngle;
                for (int i = 0; i < separatedIdx; i++) startA += (double) values.get(i) / total * 360;
                double sweep  = (double) values.get(separatedIdx) / total * 360;
                double mid    = Math.toRadians(startA + sweep / 2);

                int ox = (int)(Math.cos(mid) * 14);
                int oy = (int)(-Math.sin(mid) * 14);
                int tx = cx + (int)(Math.cos(mid) * (r * 0.65)) + ox;
                int ty = cy + (int)(-Math.sin(mid) * (r * 0.65)) + oy;

                FontMetrics fm = g2.getFontMetrics();
                int tw = fm.stringWidth(tip);
                int th = fm.getHeight();

                g2.setColor(new Color(255, 255, 200));
                g2.fillRect(tx - 4, ty - th + 2, tw + 8, th + 2);
                g2.setColor(Color.DARK_GRAY);
                g2.drawRect(tx - 4, ty - th + 2, tw + 8, th + 2);
                g2.drawString(tip, tx, ty);
            }
        }
    }
}