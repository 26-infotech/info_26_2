package forms;

import utils.Frame;
import utils.Temp;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class CertiListForm extends Frame {
    private JTextField searchField;
    
    private static final String[][] FIXED_ICONS = {
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
        System.out.println("Keyword: [" + Temp.searchKeyword + "]\nCgno: " + Temp.selectedCgno);

        northP.setBorder(new EmptyBorder(6, 10, 0, 10));

        JLabel logoLabel = new JLabel();
        png(logoLabel, "icon/logo", 40, 40);
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

        JPanel categoryPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 4, 0));
        for (String[] icon : FIXED_ICONS) {
            boolean selected = Temp.searchKeyword.isEmpty() && Integer.valueOf(Temp.selectedCgno) == Integer.valueOf(icon[0]);
            System.out.println(icon[0] + " " + icon[1] + " " + selected);
            categoryPanel.add(jb = new JButton());
            bk(jb, selected ? Color.BLUE : Color.WHITE);
            fk(jb, selected ? Color.WHITE : Color.BLACK);
            ft(jb, Font.BOLD, 10);
            jb.setText(icon[1]);
            jb.setBorderPainted(false);
            sz(jb, 80, 30);
        }

        northP.add(logoRow, NORTH);
        northP.add(menuRow, CENTER);
        northP.add(categoryPanel, SOUTH);


        showPackedPage();
	}

    private void search() {
    }

    @Override
    public void windowClosing(java.awt.event.WindowEvent e) {
        new MainForm();
    }
}
