package forms;

import utils.Alert;
import utils.DB;
import utils.Frame;
import utils.Temp;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.sql.ResultSet;

public class PaymentForm extends Frame {

    private JTextField[] cardFields = new JTextField[4];
    private JTextField rrFront;
    private JTextField rrGender;
    private JTextField pwField;

    public PaymentForm() {
        init("결제하기");

        int ccrprice = 0;
        double sale  = 0;

        try {
            rs = DB.execute("SELECT ccrprice FROM certi WHERE cno = " + Temp.selectedCno);
            if (rs.next()) ccrprice = rs.getInt("ccrprice");

            ResultSet rs2 = DB.execute(
                    "SELECT sale FROM sale WHERE cno = " + Temp.selectedCno + " AND sale_end_date >= CURDATE()"
            );
            if (rs2.next()) sale = rs2.getDouble("sale");
        } catch (Exception e) {
            e.printStackTrace();
        }

        int discountAmt       = (int)(ccrprice * (sale/100));
        int payAmt            = ccrprice - discountAmt;
        final int finalPrice  = ccrprice;
        final int finalPay    = payAmt;
        final int finalDisc   = discountAmt;
        final double finalSale = sale;

        mainP.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 14, 6, 10);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill   = GridBagConstraints.NONE;

        // 카드번호
        gbc.gridx = 0; gbc.gridy = 0;
        mainP.add(new JLabel("카드번호"), gbc);

        JPanel cardPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        for (int i = 0; i < 4; i++) {
            cardFields[i] = new JTextField();
            sz(cardFields[i], 80, 28);
            line(cardFields[i], Color.GRAY);
            cardPanel.add(cardFields[i]);
        }
        gbc.gridx = 1; gbc.gridy = 0;
        mainP.add(cardPanel, gbc);

        for (int i = 0; i < 4; i++) {
            final int idx = i;
            cardFields[i].addKeyListener(new KeyAdapter() {
                @Override public void keyTyped(KeyEvent e) {
                    if (!Character.isDigit(e.getKeyChar())) { e.consume(); return; }
                    if (cardFields[idx].getText().length() >= 4) e.consume();
                }
                @Override public void keyReleased(KeyEvent e) {
                    if (e.getKeyCode() == KeyEvent.VK_BACK_SPACE) return;
                    if (cardFields[idx].getText().length() == 4 && idx < 3)
                        cardFields[idx + 1].requestFocus();
                }
            });
        }

        // 주민등록번호
        gbc.gridx = 0; gbc.gridy = 1;
        mainP.add(new JLabel("주민등록번호"), gbc);

        rrFront = new JTextField();
        sz(rrFront, 130, 28);
        line(rrFront, Color.GRAY);

        rrGender = new JTextField();
        sz(rrGender, 36, 28);
        line(rrGender, Color.GRAY);

        JLabel maskLabel = new JLabel("●●●●●●");
        ft(maskLabel, Font.BOLD, 13);

        JPanel rrPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        rrPanel.add(rrFront);
        rrPanel.add(new JLabel("-"));
        rrPanel.add(rrGender);
        rrPanel.add(maskLabel);

        gbc.gridx = 1; gbc.gridy = 1;
        mainP.add(rrPanel, gbc);

        rrFront.addKeyListener(new KeyAdapter() {
            @Override public void keyTyped(KeyEvent e) {
                if (!Character.isDigit(e.getKeyChar())) { e.consume(); return; }
                if (rrFront.getText().length() >= 6) e.consume();
            }
            @Override public void keyReleased(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_BACK_SPACE) return;
                if (rrFront.getText().length() == 6) rrGender.requestFocus();
            }
        });

        rrGender.addKeyListener(new KeyAdapter() {
            @Override public void keyTyped(KeyEvent e) {
                if (!Character.isDigit(e.getKeyChar())) { e.consume(); return; }
                if (rrGender.getText().length() >= 1) e.consume();
            }
            @Override public void keyReleased(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_BACK_SPACE) return;
                String val = rrGender.getText();
                if (val.length() == 1) {
                    char c = val.charAt(0);
                    if (c != '1' && c != '2' && c != '3' && c != '4') {
                        Alert.error("성별을 확인하세요");
                        rrGender.setText("");
                    }
                }
            }
        });

        // 비밀번호
        gbc.gridx = 0; gbc.gridy = 2;
        mainP.add(new JLabel("비밀번호"), gbc);

        pwField = new JTextField();
        sz(pwField, 100, 28);
        line(pwField, Color.GRAY);

        JLabel pwMask = new JLabel("●●");
        ft(pwMask, Font.BOLD, 13);

        JPanel pwPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        pwPanel.add(pwField);
        pwPanel.add(pwMask);

        gbc.gridx = 1; gbc.gridy = 2;
        mainP.add(pwPanel, gbc);

        pwField.addKeyListener(new KeyAdapter() {
            @Override public void keyTyped(KeyEvent e) {
                if (!Character.isDigit(e.getKeyChar())) { e.consume(); return; }
                if (pwField.getText().length() >= 2) e.consume();
            }
        });

        // line
        gbc.gridx = 0; gbc.gridy = 3;
        gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(6, 10, 4, 10);
        mainP.add(new JSeparator(), gbc);
        gbc.gridwidth = 1;
        gbc.fill = GridBagConstraints.NONE;
        gbc.insets = new Insets(10, 14, 6, 10);

        // 가격
        JPanel pricePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 2, 0));
        if (finalSale > 0) {
            pricePanel.add(jl = new JLabel("원가 " + String.format("%,d", finalPrice) + "원 - "));
            pricePanel.add(jl = new JLabel("할인 가격 " + String.format("%,d", finalDisc) + "원"));
            fk(jl, Color.RED);
            pricePanel.add(jl = new JLabel(" = "));
            pricePanel.add(jl = new JLabel(String.format("%,d", finalPay) + "원"));
            fk(jl, Color.BLUE);
        } else {
            pricePanel.add(new JLabel("결제금액 : "));
            pricePanel.add(jl = new JLabel(String.format("%,d", finalPrice) + "원"));
            fk(jl, Color.BLUE);
        }

        gbc.gridx = 0; gbc.gridy = 4;
        gbc.gridwidth = 2;
        gbc.insets = new Insets(0, 14, 4, 10);
        mainP.add(pricePanel, gbc);
        gbc.gridwidth = 1;

        // 결제하기 버튼
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 4));
        jb = new JButton("결제하기");
        sz(jb, 90, 28);
        jb.addActionListener(e -> pay(finalPrice, finalPay));
        btnPanel.add(jb);

        gbc.gridx = 0; gbc.gridy = 5;
        gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(0, 0, 6, 4);
        mainP.add(btnPanel, gbc);

        showPackedPage();
    }

    private void pay(int ccrprice, int payAmt) {
        String card   = cardFields[0].getText() + cardFields[1].getText()
                + cardFields[2].getText() + cardFields[3].getText();
        String front  = rrFront.getText();
        String gender = rrGender.getText();
        String pw     = pwField.getText();

        if (card.isEmpty() || front.isEmpty() || gender.isEmpty() || pw.isEmpty()) {
            Alert.error("빈칸이 존재합니다.");
            return;
        }

        try {
            rs = DB.execute("SELECT card, birth, gender FROM user WHERE uno = " + Temp.uno);
            if (!rs.next()) {
                Alert.error("사용자 정보를 찾을 수 없습니다.");
                return;
            }

            String dbCard   = rs.getString("card").replaceAll("-", "").strip();
            String dbBirth  = rs.getString("birth");
            String dbGender = rs.getString("gender");

            if (!dbCard.equals(card)) {
                Alert.error("카드번호가 올바르지 않습니다.");
                return;
            }

            String expectedFront = dbBirth.substring(2, 4) + dbBirth.substring(5, 7) + dbBirth.substring(8, 10);
            if (!front.equals(expectedFront)) {
                Alert.error("주민번호가 올바르지 않습니다.");
                return;
            }

            boolean isMale = "M".equalsIgnoreCase(dbGender);
            boolean genderOk = isMale
                    ? (gender.equals("1") || gender.equals("3"))
                    : (gender.equals("2") || gender.equals("4"));
            if (!genderOk) {
                Alert.error("주민번호가 올바르지 않습니다.");
                return;
            }

            String expectedPw = String.valueOf(front.charAt(2)) + String.valueOf(front.charAt(5));
            if (!pw.equals(expectedPw)) {
                Alert.error("비밀번호를 확인해 주세요.");
                return;
            }

            // 중복 검증
            ResultSet dupRs = DB.execute(
                    "SELECT COUNT(*) AS cnt FROM test WHERE cno = " + Temp.selectedCno
                            + " AND uno = " + Temp.uno
                            + " AND exam_date = '" + Temp.selectedExamDate + "'"
                            + " AND exam = '" + Temp.selectedExamTime + "'"
            );
            if (dupRs.next() && dupRs.getInt("cnt") > 0) {
                Alert.error("이미 신청된 시험입니다.");
                return;
            }

            // 결제
            String sql = "INSERT INTO test (cno, uno, exam_date, exam, passed) VALUES ("
                    + Temp.selectedCno + ","
                    + Temp.uno + ","
                    + "'" + Temp.selectedExamDate + "',"
                    + "'" + Temp.selectedExamTime + "',"
                    + 0 + ")";

            DB.executeUpdate(sql);

            Alert.info("결제가 완료되었습니다.");
            dispose();
            new MyPageForm();

        } catch (Exception e) {
            e.printStackTrace();
            Alert.error("결제 처리 중 오류가 발생했습니다.");
        }
    }

    @Override
    public void windowClosing(java.awt.event.WindowEvent e) {
        dispose();
        new MainForm();
    }
}