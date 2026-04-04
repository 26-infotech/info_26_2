package forms;

import utils.DB;
import utils.Frame;
import utils.Temp;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class MyPageForm extends Frame {

    private static final int TYPE_COURSE = 0;
    private static final int TYPE_TEST   = 1;

    private List<int[]> typeAndKey = new ArrayList<>();
    private int         cursor     = 0;

    private JLabel  headerLabel;
    private JPanel  titleBar;
    private JLabel  imageLabel;
    private JLabel  infoLine1;
    private JLabel  infoLine2;
    private JLabel  infoLine3;
    private JButton prevBtn;
    private JButton nextBtn;

    public MyPageForm() {
        init("나의 과정");

        northP.setLayout(new FlowLayout(FlowLayout.LEFT, 6, 8));
        JLabel logoLabel = new JLabel();
        png(logoLabel, "icon/logo", 36, 36);
        northP.add(logoLabel);
        headerLabel = new JLabel();
        ft(headerLabel, Font.PLAIN, 13);
        northP.add(headerLabel);

        centerP.setLayout(new BorderLayout(0, 6));
        marginBorder(centerP, 0, 10, 10, 10);

        titleBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        jl = new JLabel("나의 강의실");
        ft(jl, Font.BOLD, 13);
        fk(jl, Color.WHITE);
        titleBar.add(jl);
        centerP.add(titleBar, NORTH);

        prevBtn = new JButton("<");
        sz(prevBtn, 50, 200);
        prevBtn.setFocusPainted(false);
        prevBtn.addActionListener(e -> move(-1));

        nextBtn = new JButton(">");
        sz(nextBtn, 50, 200);
        nextBtn.setFocusPainted(false);
        nextBtn.addActionListener(e -> move(1));

        imageLabel = new JLabel();
        imageLabel.setHorizontalAlignment(SwingConstants.CENTER);
        imageLabel.setPreferredSize(new Dimension(300, 220));
        imageLabel.setBorder(new EmptyBorder(4, 4, 4, 4));

        JPanel slideRow = new JPanel(new BorderLayout());
        slideRow.add(prevBtn,    WEST);
        slideRow.add(imageLabel, CENTER);
        slideRow.add(nextBtn,    EAST);
        centerP.add(slideRow, CENTER);

        JPanel infoPanel = new JPanel();
        infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS));
        marginBorder(infoPanel, 20, 50, 0, 50);

        infoLine1 = new JLabel();
        infoLine2 = new JLabel();
        infoLine3 = new JLabel();

        infoPanel.add(infoLine1);
        infoPanel.add(Box.createVerticalStrut(15));
        infoPanel.add(infoLine2);
        infoPanel.add(Box.createVerticalStrut(15));
        infoPanel.add(infoLine3);
        infoPanel.add(Box.createVerticalStrut(15));
        centerP.add(infoPanel, SOUTH);

        loadData();
        showPackedPage();
    }

    private void loadData() {
        typeAndKey.clear();
        try {
            List<String[]> merged = new ArrayList<>();

            ResultSet crAll = DB.execute(
                    "SELECT crno, start_date FROM course_registration WHERE uno = " + Temp.uno
            );
            while (crAll.next()) {
                merged.add(new String[]{
                        crAll.getString("start_date"),
                        "C",
                        String.valueOf(crAll.getInt("crno"))
                });
            }

            ResultSet trAll = DB.execute(
                    "SELECT ano, exam_date FROM test WHERE uno = " + Temp.uno
            );
            while (trAll.next()) {
                merged.add(new String[]{
                        trAll.getString("exam_date"),
                        "T",
                        String.valueOf(trAll.getInt("ano"))
                });
            }

            merged.sort((a, b) -> b[0].compareTo(a[0]));

            for (String[] row : merged) {
                typeAndKey.add(new int[]{
                        "C".equals(row[1]) ? TYPE_COURSE : TYPE_TEST,
                        Integer.parseInt(row[2])
                });
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        cursor = 0;
        render();
    }

    private void render() {
        if (typeAndKey.isEmpty()) {
            headerLabel.setText("신청 내역이 없습니다.");
            imageLabel.setIcon(null);
            infoLine1.setText(""); infoLine2.setText(""); infoLine3.setText("");
            prevBtn.setEnabled(false); nextBtn.setEnabled(false);
            return;
        }

        prevBtn.setEnabled(cursor > 0);
        nextBtn.setEnabled(cursor < typeAndKey.size() - 1);

        int[] cur  = typeAndKey.get(cursor);
        int   type = cur[0];
        int   key  = cur[1];

        try {
            if (type == TYPE_COURSE) renderCourse(key);
            else                     renderTest(key);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void renderCourse(int crno) throws Exception {
        ResultSet rs = DB.execute(
                "SELECT cr.cno, cr.start_date, c.cname, c.ratring " +
                        "FROM course_registration cr JOIN certi c ON cr.cno = c.cno " +
                        "WHERE cr.crno = " + crno
        );
        if (!rs.next()) return;

        int    cno       = rs.getInt("cno");
        String cname     = rs.getString("cname");
        int    ratring   = rs.getInt("ratring");
        String startDate = rs.getString("start_date");

        java.time.LocalDate start = java.time.LocalDate.parse(startDate);
        java.time.LocalDate end   = start.plusDays(29);

        headerLabel.setText("수강신청이 완료되었습니다.");
        bk(titleBar, Color.BLUE);

        imageLabel.setIcon(loadCertImage(cno));
        imageLabel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        for (MouseListener ml : imageLabel.getMouseListeners()) imageLabel.removeMouseListener(ml);
        imageLabel.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    Temp.selectedCno = cno;
                    dispose();
                    new LectureForm();
                }
            }
        });

        infoLine1.setText(cname + " " + ratring + "급");
        infoLine2.setText(startDate + "~" + end.toString());
        infoLine3.setText("시청률은 60% 이상이어야 합니다.");
    }

    private void renderTest(int ano) throws Exception {
        ResultSet rs = DB.execute(
                "SELECT t.cno, t.exam_date, c.cname, c.ratring " +
                        "FROM test t JOIN certi c ON t.cno = c.cno " +
                        "WHERE t.ano = " + ano
        );
        if (!rs.next()) return;

        int    cno      = rs.getInt("cno");
        String cname    = rs.getString("cname");
        int    ratring  = rs.getInt("ratring");
        String examDate = rs.getString("exam_date");

        headerLabel.setText("시험신청이 완료되었습니다.");
        bk(titleBar, Color.RED);

        for (MouseListener ml : imageLabel.getMouseListeners()) imageLabel.removeMouseListener(ml);
        imageLabel.setCursor(Cursor.getDefaultCursor());
        imageLabel.setIcon(loadCertImage(cno));

        infoLine1.setText(cname + " " + ratring + "급");
        infoLine2.setText(examDate);
        infoLine3.setText("점수는 60점이상이어야 합니다.");
    }

    private ImageIcon loadCertImage(int cno) {
        String path = "datafiles/certification/" + cno + ".png";
        ImageIcon raw = new ImageIcon(path);
        if (raw.getIconWidth() > 0) {
            return new ImageIcon(raw.getImage().getScaledInstance(300, 220, Image.SCALE_SMOOTH));
        }
        return new ImageIcon();
    }

    private void move(int dir) {
        cursor += dir;
        render();
    }

    @Override
    public void windowClosing(WindowEvent e) {
        dispose();
        new MainForm();
    }
}