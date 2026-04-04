package forms;

import utils.Alert;
import utils.DB;
import utils.Frame;
import utils.Temp;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.*;
import java.sql.ResultSet;
import java.time.LocalDate;

public class ExamScheduleForm extends Frame {

    private JComboBox<String> categoryCombo;
    private JTextField        searchField;
    private JPanel            listPanel;

    public ExamScheduleForm() {
        init("시험일정");
        setSize(700, 560);

        // ── NORTH ────────────────────────────────────────────────────
        northP.setLayout(new FlowLayout(FlowLayout.LEFT, 6, 8));
        marginBorder(northP, 4, 8, 4, 8);

        categoryCombo = new JComboBox<>();
        categoryCombo.addItem("전체");
        try {
            ResultSet rs = DB.execute("SELECT cgname FROM category ORDER BY cgno");
            while (rs.next()) categoryCombo.addItem(rs.getString("cgname"));
        } catch (Exception e) { e.printStackTrace(); }
        sz(categoryCombo, 120, 32);
        northP.add(categoryCombo);

        searchField = new JTextField();
        sz(searchField, 340, 32);
        line(searchField, Color.LIGHT_GRAY);
        searchField.addActionListener(e -> search());
        northP.add(searchField);

        JButton searchBtn = new JButton("조회하기");
        sz(searchBtn, 100, 32);
        bk(searchBtn, new Color(0, 60, 180));
        fk(searchBtn, Color.WHITE);
        searchBtn.setBorderPainted(false);
        searchBtn.setFocusPainted(false);
        searchBtn.addActionListener(e -> search());
        northP.add(searchBtn);

        // ── CENTER: 스크롤 목록 ───────────────────────────────────────
        listPanel = new JPanel();
        listPanel.setLayout(new BoxLayout(listPanel, BoxLayout.Y_AXIS));
        listPanel.setBorder(new EmptyBorder(6, 8, 6, 8));

        JScrollPane scroll = new JScrollPane(listPanel);
        scroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        centerP.add(scroll, CENTER);

        loadList("", "전체");
        showPage();
    }

    private void search() {
        String keyword  = searchField.getText().trim();
        String category = (String) categoryCombo.getSelectedItem();

        if (keyword.isEmpty() && "전체".equals(category)) {
            loadList("", "전체");
            return;
        }

        // 빈칸: 검색어 없이 카테고리만 선택한 경우는 허용, 검색어만 빈칸인 경우 처리
        // 조건: combobox가 선택됐거나 keyword가 있으면 검색 진행
        try {
            String sql = buildCountSql(keyword, category);
            ResultSet rs = DB.execute(sql);
            rs.next();
            if (rs.getInt("cnt") == 0) {
                Alert.error("해당 자격증이 존재하지 않습니다.");
                loadList("", "전체");
                categoryCombo.setSelectedItem("전체");
                searchField.setText("");
                return;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }

        loadList(keyword, category);
    }

    private String buildCountSql(String keyword, String category) {
        String sql = "SELECT COUNT(*) AS cnt FROM schedule sc JOIN certi c ON sc.cno = c.cno ";
        sql += buildWhere(keyword, category);
        return sql;
    }

    private String buildWhere(String keyword, String category) {
        StringBuilder where = new StringBuilder("WHERE 1=1 ");
        if (!keyword.isEmpty()) {
            where.append("AND c.cname LIKE '%").append(keyword).append("%' ");
        }
        if (!"전체".equals(category)) {
            where.append("AND FIND_IN_SET((SELECT cgno FROM category WHERE cgname='")
                    .append(category).append("'), c.cgno) > 0 ");
        }
        return where.toString();
    }

    private void loadList(String keyword, String category) {
        listPanel.removeAll();
        try {
            String sql =
                    "SELECT sc.scno, sc.cno, sc.start_date, sc.exam_date, " +
                            "       c.cname, c.ratring, c.cgno, " +
                            "       (SELECT cgname FROM category WHERE cgno = SUBSTRING_INDEX(c.cgno,',',1)) AS cgname " +
                            "FROM schedule sc JOIN certi c ON sc.cno = c.cno " +
                            buildWhere(keyword, category) +
                            "ORDER BY sc.exam_date ASC";

            ResultSet rs = DB.execute(sql);
            boolean any = false;
            while (rs.next()) {
                any = true;
                listPanel.add(buildCard(rs));
                listPanel.add(Box.createRigidArea(new Dimension(0, 6)));
            }
            if (!any) listPanel.add(new JLabel("일정이 없습니다."));
        } catch (Exception e) {
            e.printStackTrace();
        }
        listPanel.revalidate();
        listPanel.repaint();
    }

    private JPanel buildCard(ResultSet rs) throws Exception {
        int    cno       = rs.getInt("cno");
        String cname     = rs.getString("cname");
        int    ratring   = rs.getInt("ratring");
        String startDate = rs.getString("start_date"); // 시험접수일
        String examDate  = rs.getString("exam_date");  // 시험일
        String cgname    = rs.getString("cgname");

        // 신청일: start_date ~ start_date + 5일
        LocalDate applyStart = LocalDate.parse(startDate);
        LocalDate applyEnd   = applyStart.plusDays(5);
        // 시험기간: exam_date ~ exam_date + 7일
        LocalDate examStart  = LocalDate.parse(examDate);
        LocalDate examEnd    = examStart.plusDays(7);

        JPanel card = new JPanel(new BorderLayout(10, 0));
        card.setBorder(new LineBorder(Color.LIGHT_GRAY));
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 180));
        marginBorder(card, 8, 8, 8, 8);

        // 이미지
        JLabel imgLabel = new JLabel();
        imgLabel.setPreferredSize(new Dimension(220, 150));
        try {
            ImageIcon icon = new ImageIcon("datafiles/certification/" + cno + ".png");
            imgLabel.setIcon(new ImageIcon(icon.getImage().getScaledInstance(220, 150, Image.SCALE_SMOOTH)));
        } catch (Exception ignored) {}
        card.add(imgLabel, WEST);

        // 정보
        JPanel infoP = new JPanel(new GridLayout(3, 1, 0, 4));
        infoP.setBorder(new EmptyBorder(10, 8, 10, 8));

        JLabel line1 = new JLabel(cname + " " + ratring + "급  " + examStart + "~" + examEnd);
        ft(line1, Font.PLAIN, 13);
        JLabel line2 = new JLabel("신청 일자: " + applyStart + "~" + applyEnd);
        ft(line2, Font.PLAIN, 13);
        JLabel line3 = new JLabel("분류:" + (cgname != null ? cgname : ""));
        ft(line3, Font.PLAIN, 13);

        infoP.add(line1);
        infoP.add(line2);
        infoP.add(line3);
        card.add(infoP, CENTER);

        // 신청하러 가기 버튼
        JPanel btnP = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        btnP.setBorder(new EmptyBorder(0, 0, 4, 4));

        JButton applyBtn = new JButton("신청하러 가기");
        sz(applyBtn, 120, 32);
        bk(applyBtn, new Color(0, 60, 180));
        fk(applyBtn, Color.WHITE);
        applyBtn.setBorderPainted(false);
        applyBtn.setFocusPainted(false);

        // 수강신청 여부 확인 → 버튼 활성/비활성
        boolean enrolled = false;
        if (Temp.isLoggedIn()) {
            try {
                ResultSet crRs = DB.execute(
                        "SELECT crno FROM course_registration WHERE cno = " + cno +
                                " AND uno = " + Temp.uno
                );
                enrolled = crRs.next();
            } catch (Exception ignored) {}
        }
        applyBtn.setEnabled(enrolled);
        if (!enrolled) bk(applyBtn, Color.LIGHT_GRAY);

        final int finalCno = cno;
        applyBtn.addActionListener(e -> checkAttendanceAndGo(finalCno));

        JPanel south = new JPanel(new BorderLayout());
        south.add(btnP, SOUTH);
        btnP.add(applyBtn);
        card.add(south, SOUTH);

        return card;
    }

    private void checkAttendanceAndGo(int cno) {
        try {
            ResultSet crRs = DB.execute(
                    "SELECT rate FROM course_registration WHERE cno = " + cno +
                            " AND uno = " + Temp.uno
            );
            if (!crRs.next()) { Alert.error("수강신청 정보를 찾을 수 없습니다."); return; }

            String rate = crRs.getString("rate");

            ResultSet totalRs = DB.execute(
                    "SELECT COUNT(*) AS cnt FROM lecture WHERE cno = " + cno
            );
            totalRs.next();
            int totalLecture = totalRs.getInt("cnt");

            int attended = 0;
            if (rate != null && !rate.isEmpty()) {
                attended = rate.split(",").length;
            }

            double attendRate = totalLecture > 0 ? (double) attended / totalLecture * 100 : 0;

            if (attendRate >= 60) {
                Temp.selectedCno = cno;
                dispose();
                new ScheduleForm();
            } else {
                Alert.error("현재 신청하려는 과정의 출석률이 60%미만입니다.\n출석을 완료한 뒤 다시 돌아와주세요.");
                Temp.selectedCno = cno;
                dispose();
                new LectureForm();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void windowClosing(WindowEvent e) {
        dispose();
        new MainForm();
    }
}