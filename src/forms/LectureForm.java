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
import java.util.Arrays;
import java.util.List;

public class LectureForm extends Frame {

    private boolean shownAttendAlert = false;

    public LectureForm() {
        init("강의");
        setSize(480, 600);

        String cname   = "";
        int    ratring = 0;
        int    crno    = 0;
        String rate    = "";
        String startDate = "";

        try {
            ResultSet crRs = DB.execute(
                    "SELECT cr.crno, cr.rate, cr.start_date, c.cname, c.ratring " +
                            "FROM course_registration cr JOIN certi c ON cr.cno = c.cno " +
                            "WHERE cr.cno = " + Temp.selectedCno + " AND cr.uno = " + Temp.uno
            );
            if (crRs.next()) {
                crno      = crRs.getInt("crno");
                rate      = crRs.getString("rate");
                startDate = crRs.getString("start_date");
                cname     = crRs.getString("cname");
                ratring   = crRs.getInt("ratring");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }


        List<int[]>    lnoList    = new ArrayList<>();
        List<String>   titleList  = new ArrayList<>();
        List<String>   contentsList = new ArrayList<>();
        List<Integer>  pdfnumList = new ArrayList<>();

        try {
            ResultSet lRs = DB.execute(
                    "SELECT lno, title, contents, pdfnum FROM lecture WHERE cno = " + Temp.selectedCno +
                            " ORDER BY lno"
            );
            while (lRs.next()) {
                lnoList.add(new int[]{ lRs.getInt("lno") });
                titleList.add(lRs.getString("title"));
                contentsList.add(lRs.getString("contents"));
                pdfnumList.add(lRs.getInt("pdfnum"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        int totalLecture = lnoList.size();

        if (totalLecture > 0) {
            try {
                ResultSet initRs = DB.execute(
                        "SELECT COUNT(*) AS cnt FROM lecture WHERE cno = " + Temp.selectedCno +
                                " AND FIND_IN_SET('" + Temp.uno + "', student) > 0"
                );
                initRs.next();
                double initRate = (double) initRs.getInt("cnt") / totalLecture * 100;
                if (initRate >= 60) shownAttendAlert = true;
            } catch (Exception ignored) {}
        }

        northP.setLayout(new BorderLayout());
        marginBorder(northP, 8, 12, 4, 12);

        JPanel topLeft = new JPanel(new GridLayout(2, 1));
        JLabel nameLabel = new JLabel(cname + " " + ratring + "급");
        ft(nameLabel, Font.PLAIN, 13);
        JLabel dayLabel = new JLabel();
        ft(dayLabel, Font.BOLD, 13);
        fk(dayLabel, new Color(0, 90, 200));

        if (!startDate.isEmpty()) {
            try {
                java.time.LocalDate start = java.time.LocalDate.parse(startDate);
                long diff = java.time.temporal.ChronoUnit.DAYS.between(start, java.time.LocalDate.now()) + 1;
                dayLabel.setText(diff + "일차");
            } catch (Exception e) {
                dayLabel.setText("1일차");
            }
        }

        topLeft.add(nameLabel);
        topLeft.add(dayLabel);

        JPanel topRight = new JPanel(new GridLayout(2, 1));
        JLabel memberLabel = new JLabel(Temp.uname + " 회원님", SwingConstants.RIGHT);
        ft(memberLabel, Font.PLAIN, 12);

        int totalQ = totalLecture * 20;
        JLabel totalQLabel = new JLabel("문제 갯수는 총 " + totalQ + "개", SwingConstants.RIGHT);
        ft(totalQLabel, Font.PLAIN, 12);
        topRight.add(memberLabel);
        topRight.add(totalQLabel);

        northP.add(topLeft,  WEST);
        northP.add(topRight, EAST);

        JPanel listPanel = new JPanel();
        listPanel.setLayout(new BoxLayout(listPanel, BoxLayout.Y_AXIS));
        listPanel.setBorder(new EmptyBorder(6, 8, 6, 8));

        for (int i = 0; i < lnoList.size(); i++) {
            int    lno     = lnoList.get(i)[0];
            String title   = titleList.get(i);
            String content = contentsList.get(i);
            int    pdfnum  = pdfnumList.get(i);

            String studentField = "";
            try {
                ResultSet stRs = DB.execute("SELECT student FROM lecture WHERE lno = " + lno);
                if (stRs.next()) studentField = stRs.getString("student");
                if (studentField == null) studentField = "";
            } catch (Exception ignored) {}
            boolean viewed = studentField.contains(String.valueOf(Temp.uno));

            JPanel card = new JPanel(new BorderLayout(8, 0));
            card.setBorder(new LineBorder(Color.LIGHT_GRAY));
            card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 80));
            marginBorder(card, 6, 8, 6, 8);

            JLabel pdfIcon = new JLabel();
            png(pdfIcon, "icon/pdf", 48, 48);
            if (viewed) {
                pdfIcon.setIcon(new ImageIcon(
                        new ImageIcon("datafiles/icon/pdf.png")
                                .getImage().getScaledInstance(48, 48, Image.SCALE_SMOOTH)
                ));
                pdfIcon.setEnabled(false);
            }
            sz(pdfIcon, 56, 56);
            pdfIcon.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

            JPanel textP = new JPanel(new GridLayout(3, 1));
            JLabel titleLabel = new JLabel(title);
            ft(titleLabel, Font.BOLD, 12);
            JLabel qLabel   = new JLabel("문제갯수: 20개");
            ft(qLabel, Font.PLAIN, 11);
            JLabel contLabel = new JLabel(content.length() > 18 ? content.substring(0, 18) : content);
            ft(contLabel, Font.PLAIN, 11);

            if (viewed) {
                fk(titleLabel, Color.GRAY);
                fk(qLabel,     Color.GRAY);
                fk(contLabel,  Color.GRAY);
            }

            textP.add(titleLabel);
            textP.add(qLabel);
            textP.add(contLabel);

            card.add(pdfIcon, WEST);
            card.add(textP,   CENTER);

            pdfIcon.addMouseListener(new MouseAdapter() {
                @Override public void mouseClicked(MouseEvent e) {
                    openPdf(pdfnum);
                    if (!viewed) {
                        updateAttendance(lno);
                        titleLabel.setForeground(Color.GRAY);
                        contLabel.setForeground(Color.GRAY);
                        qLabel.setForeground(Color.GRAY);
                        pdfIcon.setEnabled(false);
                        card.repaint();
                        if (!shownAttendAlert && totalLecture > 0) {
                            try {
                                ResultSet cntRs = DB.execute(
                                        "SELECT COUNT(*) AS cnt FROM lecture " +
                                                "WHERE cno = " + Temp.selectedCno +
                                                " AND FIND_IN_SET('" + Temp.uno + "', student) > 0"
                                );
                                cntRs.next();
                                double r = (double) cntRs.getInt("cnt") / totalLecture * 100;
                                if (r >= 60) {
                                    shownAttendAlert = true;
                                    Alert.info("60% 이상입니다.");
                                }
                            } catch (Exception ex) { ex.printStackTrace(); }
                        }
                    }
                }
            });

            listPanel.add(card);
            listPanel.add(Box.createVerticalStrut(4));
        }

        JScrollPane scroll = new JScrollPane(listPanel);
        scroll.setBorder(null);
        scroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scroll.getVerticalScrollBar().setUnitIncrement(16);

        final int[] dragY = {0};
        listPanel.addMouseListener(new MouseAdapter() {
            @Override public void mousePressed(MouseEvent e) { dragY[0] = e.getY(); }
        });
        listPanel.addMouseMotionListener(new MouseAdapter() {
            @Override public void mouseDragged(MouseEvent e) {
                int dy = e.getY() - dragY[0];
                JScrollBar bar = scroll.getVerticalScrollBar();
                bar.setValue(bar.getValue() - dy);
                dragY[0] = e.getY();
            }
        });

        centerP.add(scroll, CENTER);

        showPage();
    }

    private void openPdf(int pdfnum) {
        try {
            String cnoFolder = String.valueOf(Temp.selectedCno);
            ResultSet folderRs = DB.execute(
                    "SELECT cname FROM certi WHERE cno = " + Temp.selectedCno
            );
            String folderName = "";
            if (folderRs.next()) folderName = folderRs.getString("cname");

            java.io.File pdf = new java.io.File(
                    "datafiles/question/" + folderName + "/" + pdfnum + ".pdf"
            );
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

    private void updateAttendance(int lno) {
        try {
            ResultSet rs = DB.execute("SELECT student FROM lecture WHERE lno = " + lno);
            if (!rs.next()) return;
            String student = rs.getString("student");
            if (student == null) student = "";

            String unoStr = String.valueOf(Temp.uno);

            List<String> list = new java.util.ArrayList<>();
            if (!student.isEmpty()) {
                list.addAll(java.util.Arrays.asList(student.split(",")));
            }
            if (!list.contains(unoStr)) {
                list.add(unoStr);
                String newStudent = String.join(",", list);
                DB.executeUpdate(
                        "UPDATE lecture SET student = '" + newStudent +
                                "' WHERE lno = " + lno
                );
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