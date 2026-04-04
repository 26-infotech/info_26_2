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
import java.util.ArrayList;
import java.util.List;

public class CertiListForm extends Frame {

    private JTextField searchField;
    private JPanel     listPanel;
    private JButton[]  catBtns;

    private static final String[][] CATEGORIES = {
            {"-1", "추천과정"},
            {"1",  "봉사"},
            {"2",  "요리"},
            {"3",  "의학"},
            {"4",  "운동"},
            {"5",  "IT"},
            {"6",  "항공"},
    };

    public CertiListForm() {
        init("자격증 목록");
        setSize(860, 620);

        // ── NORTH ────────────────────────────────────────────────────
        northP.setBorder(new EmptyBorder(6, 10, 0, 10));

        JLabel logoLabel = new JLabel();
        png(logoLabel, "icon/logo", 40, 40);
        logoLabel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        logoLabel.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) { dispose(); new MainForm(); }
        });

        JLabel siteTitle = new JLabel("  Skills Qualification Association");
        ft(siteTitle, Font.BOLD, 14);
        JPanel logoGroup = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        logoGroup.add(logoLabel);
        logoGroup.add(siteTitle);

        searchField = new JTextField();
        sz(searchField, 280, 30);
        line(searchField, Color.CYAN);
        ft(searchField, Font.PLAIN, 13);
        searchField.addActionListener(e -> search());

        JLabel searchIcon = new JLabel();
        png(searchIcon, "icon/search", 22, 22);
        searchIcon.setPreferredSize(new Dimension(40, 40));
        marginBorder(searchIcon, 0, 0, 0, 6);
        searchIcon.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) { search(); }
        });

        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 4, 0));
        searchPanel.add(searchField);
        searchPanel.add(searchIcon);

        JPanel logoRow = new JPanel(new BorderLayout());
        logoRow.add(logoGroup, WEST);
        logoRow.add(searchPanel, EAST);

        JLabel menuList = new JLabel("자격증 목록");
        marginBorder(menuList, 0, 150, 0, 150);
        JLabel menuSchedule = new JLabel("시험 일정");
        marginBorder(menuSchedule, 0, 150, 0, 150);

        JPanel menuRow = new JPanel(new FlowLayout(FlowLayout.LEFT));
        marginBorder(menuRow, 20, 0, 20, 0);
        menuRow.add(menuList);
        menuRow.add(menuSchedule);

        // 카테고리 버튼
        JPanel categoryPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 4, 4));
        catBtns = new JButton[CATEGORIES.length];

        for (int i = 0; i < CATEGORIES.length; i++) {
            final int idx  = i;
            final int cgno = Integer.parseInt(CATEGORIES[i][0]);

            catBtns[i] = new JButton(CATEGORIES[i][1]);
            ft(catBtns[i], Font.BOLD, 11);
            catBtns[i].setBorderPainted(false);
            catBtns[i].setFocusPainted(false);
            catBtns[i].setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            sz(catBtns[i], 85, 30);
            catBtns[i].addActionListener(e -> {
                Temp.selectedCgno  = cgno;
                Temp.searchKeyword = "";
                selectCategory(idx);
                loadList();
            });
            categoryPanel.add(catBtns[i]);
        }

        northP.add(logoRow,      NORTH);
        northP.add(menuRow,      CENTER);
        northP.add(categoryPanel, SOUTH);

        listPanel = new JPanel();
        listPanel.setLayout(new BoxLayout(listPanel, BoxLayout.Y_AXIS));
        listPanel.setBorder(new EmptyBorder(6, 10, 6, 10));

        JScrollPane scroll = new JScrollPane(listPanel);
        scroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        centerP.add(scroll, CENTER);

        int initCat = findCategoryIndex();
        selectCategory(initCat);
        loadList();

        showPage();
    }

    private int findCategoryIndex() {
        if (!Temp.searchKeyword.isEmpty()) return -1;
        for (int i = 0; i < CATEGORIES.length; i++) {
            if (Integer.parseInt(CATEGORIES[i][0]) == Temp.selectedCgno) return i;
        }
        return 0;
    }

    private void selectCategory(int idx) {
        for (int i = 0; i < catBtns.length; i++) {
            boolean sel = (i == idx);
            bk(catBtns[i], sel ? Color.BLUE : Color.WHITE);
            fk(catBtns[i], sel ? Color.WHITE : Color.BLACK);
        }
    }

    private void loadList() {
        listPanel.removeAll();
        try {
            String sql = buildSql();
            ResultSet rs = DB.execute(sql);
            boolean any = false;
            while (rs.next()) {
                any = true;
                listPanel.add(buildCard(rs));
                listPanel.add(Box.createRigidArea(new Dimension(0, 6)));
            }
            if (!any) {
                listPanel.add(new JLabel("해당하는 자격증이 없습니다."));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        listPanel.revalidate();
        listPanel.repaint();
    }

    private String buildSql() {
        String base =
                "SELECT c.cno, c.cname, c.ratring, c.type1, c.cciorgan, c.cmicorgan, " +
                        "       t.tname, c.cgno, " +
                        "       COUNT(cr.crno) AS cnt " +
                        "FROM certi c " +
                        "LEFT JOIN course_registration cr ON c.cno = cr.cno " +
                        "LEFT JOIN teacher t ON c.tno = t.tno ";

        String where = "";
        if (!Temp.searchKeyword.isEmpty()) {
            where = "WHERE c.cname LIKE '%" + Temp.searchKeyword + "%' ";
        } else if (Temp.selectedCgno == -1) {
            // 추천과정: 수강신청 Top6
            where = "";
        } else {
            where = "WHERE FIND_IN_SET('" + Temp.selectedCgno + "', c.cgno) > 0 ";
        }

        String group = "GROUP BY c.cno ";
        String order = "ORDER BY cnt DESC, c.cno ASC ";
        String limit = (Temp.selectedCgno == -1 && Temp.searchKeyword.isEmpty()) ? "LIMIT 6" : "";

        return base + where + group + order + limit;
    }

    private JPanel buildCard(ResultSet rs) throws Exception {
        int    cno      = rs.getInt("cno");
        String cname    = rs.getString("cname");
        int    ratring  = rs.getInt("ratring");
        String type1    = rs.getString("type1");
        String cciorgan = rs.getString("cciorgan");
        String cmicorgan= rs.getString("cmicorgan");
        String tname    = rs.getString("tname");

        JPanel card = new JPanel(new BorderLayout(10, 0));
        card.setBorder(new LineBorder(Color.LIGHT_GRAY));
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 160));
        marginBorder(card, 10, 10, 10, 10);

        // 왼쪽 이미지
        JLabel imgLabel = new JLabel();
        imgLabel.setPreferredSize(new Dimension(200, 130));
        try {
            ImageIcon icon = new ImageIcon("datafiles/certification/" + cno + ".png");
            imgLabel.setIcon(new ImageIcon(icon.getImage().getScaledInstance(200, 130, Image.SCALE_SMOOTH)));
        } catch (Exception ignored) {}
        card.add(imgLabel, WEST);

        // 중앙 정보
        JPanel infoP = new JPanel(new GridLayout(4, 1, 0, 2));
        infoP.setBorder(new EmptyBorder(4, 4, 4, 4));

        JLabel titleLbl = new JLabel(cname + " " + ratring + "급");
        ft(titleLbl, Font.BOLD, 15);
        infoP.add(titleLbl);

        JPanel row1 = new JPanel(new GridLayout(1, 2));
        row1.add(makeInfoLabel("담당교수", tname + " 교수"));
        row1.add(makeInfoLabel("응시조건", type1));
        infoP.add(row1);

        JPanel row2 = new JPanel(new GridLayout(1, 2));
        row2.add(makeInfoLabel("주무부처", cmicorgan));
        row2.add(makeInfoLabel("발급기관", cciorgan));
        infoP.add(row2);

        JPanel row3 = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 0));
        JLabel benefitKey = new JLabel("· 수강혜택");
        ft(benefitKey, Font.PLAIN, 11);
        JLabel benefitVal = new JLabel("교안무료+시험예상기출문제제공");
        ft(benefitVal, Font.BOLD, 11);
        fk(benefitVal, Color.RED);
        row3.add(benefitKey);
        row3.add(benefitVal);
        infoP.add(row3);

        card.add(infoP, CENTER);

        // 오른쪽 버튼
        JPanel btnP = new JPanel(new GridLayout(2, 1, 0, 6));
        btnP.setBorder(new EmptyBorder(20, 4, 20, 4));

        JButton selectBtn = new JButton("과목선택하기");
        bk(selectBtn, new Color(0, 60, 180));
        fk(selectBtn, Color.WHITE);
        selectBtn.setBorderPainted(false);
        selectBtn.setFocusPainted(false);
        selectBtn.addActionListener(e -> {
            Temp.selectedCno = cno;
            dispose();
            new PaymentForm();
        });

        JButton pdfBtn = new JButton("기출문제 맛보기");
        bk(pdfBtn, new Color(0, 60, 180));
        fk(pdfBtn, Color.WHITE);
        pdfBtn.setBorderPainted(false);
        pdfBtn.setFocusPainted(false);
        pdfBtn.addActionListener(e -> openFirstPdf(cname));

        btnP.add(selectBtn);
        btnP.add(pdfBtn);
        card.add(btnP, EAST);

        return card;
    }

    private JLabel makeInfoLabel(String key, String val) {
        JLabel lbl = new JLabel("· " + key + " " + val);
        ft(lbl, Font.PLAIN, 11);
        return lbl;
    }

    private void openFirstPdf(String cname) {
        try {
            java.io.File pdf = new java.io.File("datafiles/question/" + cname + "/1.pdf");
            if (pdf.exists()) {
                Desktop.getDesktop().open(pdf);
            } else {
                Alert.error("PDF 파일을 찾을 수 없습니다.");
            }
        } catch (Exception e) {
            e.printStackTrace();
            Alert.error("PDF 실행 중 오류가 발생했습니다.");
        }
    }

    private void search() {
        String keyword = searchField.getText().trim();
        if (keyword.isEmpty()) return;
        try {
            ResultSet rs = DB.execute(
                    "SELECT COUNT(*) AS cnt FROM certi WHERE cname LIKE '%" + keyword + "%'"
            );
            rs.next();
            if (rs.getInt("cnt") == 0) {
                Alert.error("해당하는 자격증이 존재하지 않습니다.");
                return;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }
        Temp.searchKeyword = keyword;
        Temp.selectedCgno  = -1;
        selectCategory(-1);
        loadList();
    }

    @Override
    public void windowClosing(java.awt.event.WindowEvent e) {
        dispose();
        new MainForm();
    }
}