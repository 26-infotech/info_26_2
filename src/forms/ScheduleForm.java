package forms;

import utils.Alert;
import utils.DB;
import utils.Frame;
import utils.Temp;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.ResultSet;
import java.text.SimpleDateFormat;
import java.time.YearMonth;
import java.util.Date;

public class ScheduleForm extends Frame {

    private Date selectDate = new Date();
    private SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

    public ScheduleForm() {
        init("시험스케줄");

        String cname;
        int ratring;
        Date nowDate = new Date();
        SimpleDateFormat monthFmt = new SimpleDateFormat("yyyy-MM");

        try {
            rs = DB.execute("SELECT * FROM certi WHERE cno = " + Temp.selectedCno);
            rs.next();
            cname  = rs.getString("cname");
            ratring = rs.getInt("ratring");
        } catch (Exception e) {
            e.printStackTrace();
            Alert.error(e.getMessage());
            dispose(); new MainForm();
            return;
        }

        JPanel titleRow = new JPanel();
        titleRow.setLayout(new BoxLayout(titleRow, BoxLayout.Y_AXIS));

        p0 = new JPanel(new FlowLayout(FlowLayout.LEFT));
        p0.add(jl = new JLabel(cname + " " + ratring + "급"));
        ft(jl, Font.BOLD, 20);
        marginBorder(jl, 10, 0, 0, 10);
        titleRow.add(p0);

        p1 = new JPanel(new FlowLayout(FlowLayout.CENTER));
        p1.add(jl = new JLabel(monthFmt.format(nowDate)));
        fk(jl, Color.GRAY);
        marginBorder(jl, 4, 0, 0, 0);
        titleRow.add(p1);

        int startDay = 1;
        int nowDay   = nowDate.getDate();
        int endDay   = YearMonth.now().lengthOfMonth();

        p2 = new JPanel(new FlowLayout(FlowLayout.CENTER));
        p2.add(jl = new JLabel("" + startDay));
        marginBorder(jl, 0, 200, 0, 20);
        p2.add(jl = new JLabel("" + endDay));
        marginBorder(jl, 0, 20, 0, 200);
        titleRow.add(p2);

        northP.add(titleRow);

        selectDate = (Date) nowDate.clone();

        JSlider daySlider = new JSlider(startDay, endDay, nowDay);
        daySlider.setPaintTicks(false);
        daySlider.setPaintLabels(false);
        marginBorder(daySlider, 10, 10, 0, 10);

        daySlider.setToolTipText(String.valueOf(nowDay));
        ToolTipManager.sharedInstance().registerComponent(daySlider);
        ToolTipManager.sharedInstance().setInitialDelay(0);
        ToolTipManager.sharedInstance().setReshowDelay(0);

        daySlider.addMouseMotionListener(new MouseAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {
                daySlider.setToolTipText(String.valueOf(daySlider.getValue()));
                ToolTipManager.sharedInstance().mouseMoved(
                        new MouseEvent(daySlider, MouseEvent.MOUSE_MOVED,
                                e.getWhen(), 0, e.getX(), e.getY(), 0, false)
                );
            }
            @Override
            public void mouseMoved(MouseEvent e) {
                daySlider.setToolTipText(String.valueOf(daySlider.getValue()));
            }
        });

        daySlider.addChangeListener(e -> {
            selectDate.setDate(daySlider.getValue());
            updateSchedule();
        });

        centerP.add(daySlider, CENTER);

        updateSchedule();

        showPackedPage();
    }

    private void updateSchedule() {
        southP.removeAll();
        southP.setLayout(new FlowLayout(FlowLayout.CENTER));

        try {
            String dateStr = sdf.format(selectDate);
            rs = DB.execute("SELECT * FROM schedule WHERE start_date = '" + dateStr + "' AND cno = '" + Temp.selectedCno + "'");

            if (!rs.next()) {
                southP.add(jl = new JLabel("시험이 존재하지 않습니다."));
                marginBorder(jl, 20, 0, 20, 0);
            } else {
                String stimeRaw = rs.getString("stime");
                String[] times  = stimeRaw.split(", ");

                p5 = new JPanel(new FlowLayout(FlowLayout.CENTER, 50, 11));
                southP.add(p5);

                for (String time : times) {
                    jb = new JButton(time);
                    sz(jb, 70, 30);
                    bk(jb, Color.WHITE);
                    ft(jb, Font.BOLD, 12);

                    try {
                        ResultSet rs2 = DB.execute("SELECT COUNT(*) AS cnt FROM test WHERE cno = " + Temp.selectedCno + " AND exam_date = '" + dateStr + "' AND exam = '" + time + ":00'");
                        rs2.next();
                        int cnt = rs2.getInt("cnt");

                        if (cnt >= 30) {
                            jb.setText("마감");
                            jb.setEnabled(false);
                            bk(jb, Color.LIGHT_GRAY);
                            fk(jb, Color.GRAY);
                            p5.add(jb);
                            continue;
                        }
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }

                    final String selectedTime = time;
                    jb.addActionListener(e -> {
                        Temp.selectedExamDate = dateStr;
                        Temp.selectedExamTime = time;
                        Temp.selectedStime = selectedTime;
                        dispose();
                        new PaymentForm();
                    });

                    p5.add(jb);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            Alert.error(e.getMessage());
        }

        revalidate();
        repaint();
    }

    @Override
    public void windowClosing(java.awt.event.WindowEvent e) {
        dispose();
        new MainForm();
    }
}