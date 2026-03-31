package forms;

import utils.Alert;
import utils.DB;
import utils.Frame;
import utils.Temp;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.border.MatteBorder;
import javax.swing.border.StrokeBorder;
import java.awt.*;
import java.awt.event.*;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class MainForm extends Frame {

	private List<ImageIcon> slideImages = new ArrayList<>();
	private int slideIndex = 0;
	private JLabel slideLabel;
	private Timer slideTimer;
	private JTextField searchField;
	private JLabel greetLabel;
	private JPanel cPanel;
	private CardLayout cCard;
	private JPanel loginPanel;
	private JPanel loggedPanel;
	private JPanel courseListPanel;
	private JPanel iconPanel;

	private static final String[][] FIXED_ICONS = {
			{"IT",   "it"},
			{"요리",  "cooking"},
			{"봉사",  "volunteer"},
			{"운동",  "health"},
			{"항공",  "aviation"},
			{"의학",  "hospital"},
	};

	public MainForm() {
		init("자격증 메인 화면");

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

		northP.add(logoRow, NORTH);
		northP.add(menuRow, CENTER);

		// ============================================================
		// WEST: 캐러셀 이미지
		// ============================================================
		slideLabel = new JLabel();
		slideLabel.setHorizontalAlignment(SwingConstants.CENTER);
		sz(slideLabel, 500, 300);
		westP.add(slideLabel, CENTER);

		// ============================================================
		// CENTER: 비로그인 / 로그인 카드레이아웃
		// ============================================================
		cCard  = new CardLayout();
		cPanel = new JPanel(cCard);
		sz(cPanel, 280, 300);

		// 비로그인 패널
		loginPanel = new JPanel();
		loginPanel.setLayout(new BoxLayout(loginPanel, BoxLayout.Y_AXIS));

		JPanel loginTop = new JPanel(new FlowLayout(FlowLayout.LEFT));
		JLabel loginCheck = new JLabel();
		png(loginCheck, "icon/check", 40, 40);
		marginBorder(loginCheck, 0, 10, 0, 10);
		loginTop.add(loginCheck);
		loginTop.add(jl = new JLabel("로그인이 필요합니다."));

		JButton loginBtn = new JButton("로그인");
		loginBtn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
		fk(loginBtn, Color.WHITE);
		bk(loginBtn, Color.BLUE);
		ft(loginBtn, Font.BOLD, 14);
		loginBtn.addActionListener(e -> { dispose(); new LoginForm(); });

		JPanel infoArea = new JPanel();
		infoArea.setLayout(new BoxLayout(infoArea, BoxLayout.Y_AXIS));
		infoArea.add(jl = new JLabel("로그인이 필요합니다.")); fk(jl, Color.RED);
		infoArea.add(jl = new JLabel("1. 유효한 사용자 정보를 입력하세요."));
		infoArea.add(jl = new JLabel("2. 인증 절차를 확인하세요."));
		infoArea.add(jl = new JLabel("3. 로그인 후 이용 가능합니다."));
		infoArea.add(jl = new JLabel("4. 오류가 지속되면 관리자에게 문의하세요."));
		infoArea.setBorder(new LineBorder(Color.BLACK, 1, true));

		loginPanel.add(loginTop);
		loginPanel.add(loginBtn);
		loginPanel.add(infoArea);

		// 로그인 후 패널
		loggedPanel = new JPanel(new BorderLayout());
		loggedPanel.setBorder(new LineBorder(Color.BLACK, 1, true));

		JLabel greetCheck = new JLabel();
		png(greetCheck, "icon/check", 40, 40);
		greetLabel = new JLabel();
		JPanel greetRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 0));
		greetRow.add(greetCheck);
		greetRow.add(greetLabel);

		JButton logoutBtn = new JButton("로그아웃");
		bk(logoutBtn, Color.BLUE); fk(logoutBtn, Color.WHITE);
		logoutBtn.setBorderPainted(false); logoutBtn.setFocusPainted(false);
		ft(logoutBtn, Font.BOLD, 12);
		logoutBtn.addActionListener(e -> logout());

		JButton myInfoBtn = new JButton("내 정보");
		bk(myInfoBtn, Color.LIGHT_GRAY);
		myInfoBtn.setBorderPainted(false); myInfoBtn.setFocusPainted(false);
		myInfoBtn.addActionListener(e -> { dispose(); new MyPageForm(); });

		JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 4));
		btnRow.add(logoutBtn);
		btnRow.add(myInfoBtn);

		JPanel loggedTop = new JPanel(new BorderLayout());
		loggedTop.setBorder(new EmptyBorder(6, 8, 4, 8));
		loggedTop.add(greetRow, NORTH);
		loggedTop.add(btnRow, CENTER);

		courseListPanel = new JPanel();
		courseListPanel.setLayout(new BoxLayout(courseListPanel, BoxLayout.Y_AXIS));
		courseListPanel.setBackground(Color.WHITE);
		JScrollPane scroll = new JScrollPane(courseListPanel);
		scroll.setBorder(null);
		scroll.getVerticalScrollBar().setUnitIncrement(10);

		loggedPanel.add(loggedTop, NORTH);
		loggedPanel.add(scroll, CENTER);

		cPanel.add(loginPanel, "login");
		cPanel.add(loggedPanel, "logged");
		centerP.add(cPanel, CENTER);

		// SOUTH
		JLabel medalIcon = new JLabel();
		png(medalIcon, "icon/medel", 44, 44);
		marginBorder(medalIcon, 10, 0, 10, 15);
		JLabel medalText = new JLabel("자격증을 선택해주세요.");

		JPanel medalRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 4));
		medalRow.add(medalIcon);
		medalRow.add(medalText);

		iconPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 30, 10));
		iconPanel.setBorder(new LineBorder(Color.BLACK, 1, true));

		southP.add(medalRow, NORTH);
		southP.add(iconPanel, CENTER);

		loadSlideImages();
		startSlideShow();
		loadCategoryIcons();
		refreshCPanel();

		showPackedPage();
	}

	// ============================================================
	// 기능 메서드
	// ============================================================

	private void loadSlideImages() {
		for (int i = 1; i <= 5; i++) {
			ImageIcon icon = new ImageIcon("datafiles/main/" + i + ".png");
			Image scaled = icon.getImage().getScaledInstance(500, 300, Image.SCALE_SMOOTH);
			slideImages.add(new ImageIcon(scaled));
		}
	}

	private void startSlideShow() {
		if (slideImages.isEmpty()) return;
		slideLabel.setIcon(slideImages.get(0));
		slideTimer = new Timer(3000, e -> {
			slideIndex = (slideIndex + 1) % slideImages.size();
			slideLabel.setIcon(slideImages.get(slideIndex));
		});
		slideTimer.start();
	}

	private void loadCategoryIcons() {
		iconPanel.removeAll();
		for (String[] entry : FIXED_ICONS) {
			String cgname   = entry[0];
			String iconFile = entry[1];

			ImageIcon normalIcon = loadIconScaled("datafiles/icon/" + iconFile + ".png", 50, 50);
			ImageIcon hoverIcon  = loadIconScaled("datafiles/icon/" + iconFile + ".png", 60, 60);

			JLabel iconLabel = new JLabel(normalIcon);
			iconLabel.setToolTipText(cgname);
			iconLabel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
			iconLabel.setPreferredSize(new Dimension(60, 60));
			iconLabel.setHorizontalAlignment(SwingConstants.CENTER);
			iconLabel.setVerticalAlignment(SwingConstants.CENTER);

			iconLabel.addMouseListener(new MouseAdapter() {
				@Override public void mouseEntered(MouseEvent e) { iconLabel.setIcon(hoverIcon); }
				@Override public void mouseExited(MouseEvent e)  { iconLabel.setIcon(normalIcon); }
				@Override public void mouseClicked(MouseEvent e) {
					if (!Temp.isLoggedIn()) {
						Alert.error("로그인하세요");
						dispose(); new LoginForm(); return;
					}
					System.out.println("Selected category: [" + cgname + "]");
					try {
						rs = DB.execute("SELECT cgno FROM category WHERE cgname like '%" + cgname + "%'");
						if (rs.next()) Temp.selectedCgno = rs.getInt("cgno");
						System.out.println("Selected cgno: " + Temp.selectedCgno);
					} catch (Exception ex) {
						ex.printStackTrace();
					}
					Temp.searchKeyword = "";
					dispose();
					new CertiListForm();
				}
			});

			iconPanel.add(iconLabel);
		}
		iconPanel.revalidate();
		iconPanel.repaint();
	}

	private ImageIcon loadIconScaled(String path, int w, int h) {
		try {
			return new ImageIcon(new ImageIcon(path).getImage().getScaledInstance(w, h, Image.SCALE_SMOOTH));
		} catch (Exception e) { return new ImageIcon(); }
	}

	public void refreshCPanel() {
		if (!Temp.isLoggedIn()) {
			greetLabel.setText("로그인이 필요합니다.");
			cCard.show(cPanel, "login");
			return;
		}

		loadCourseList();
		greetLabel.setText(Temp.uname + "님, 환영합니다.");
		cCard.show(cPanel, "logged");
	}

	private void loadCourseList() {
		courseListPanel.removeAll();
		try {
			ResultSet rs = DB.execute(
					"SELECT cr.cno, cr.start_date, cr.rate, c.cname, c.days, c.ratring " +
							"FROM course_registration cr JOIN certi c ON cr.cno = c.cno " +
							"WHERE cr.uno = " + Temp.uno + " ORDER BY cr.start_date DESC"
			);
			while (rs.next()) {
				String  cname     = rs.getString("cname");
				String  startDate = rs.getString("start_date");
				String  days      = rs.getString("days");
				int     ratring   = rs.getInt("ratring");
				int     cno       = rs.getInt("cno");
				boolean ended     = isEnded(startDate, days);
				String  endDate   = calcEndDate(startDate, days);

				JPanel item = new JPanel(new BorderLayout(4, 2));
				item.setBorder(new LineBorder(Color.LIGHT_GRAY));

				JPanel infoP = new JPanel();
				infoP.setLayout(new BoxLayout(infoP, BoxLayout.Y_AXIS));
				marginBorder(infoP, 4, 6, 4, 6);
				JLabel nameLabel = new JLabel(cname + " " + ratring + "급");
				ft(nameLabel, Font.BOLD, 12);
				JLabel dateLabel = new JLabel("수강 신청 : " + startDate + "~" + endDate);
				ft(dateLabel, Font.PLAIN, 10); fk(dateLabel, Color.GRAY);
				infoP.add(nameLabel);
				infoP.add(dateLabel);

				JPanel rightP = new JPanel(new BorderLayout(0, 4));
				rightP.setBackground(Color.WHITE);
				rightP.setBorder(new EmptyBorder(4, 0, 4, 6));

				JLabel statusLabel = new JLabel(ended ? "종료" : "학습중");
				statusLabel.setOpaque(true);
				bk(statusLabel, Color.ORANGE);
				fk(statusLabel, Color.WHITE);
				statusLabel.setHorizontalAlignment(SwingConstants.CENTER);
				sz(statusLabel, 50, 18);

				JLabel goLectureLabel = new JLabel("강의실 가기");
				goLectureLabel.setOpaque(true);
				fk(goLectureLabel, Color.BLUE);
				goLectureLabel.setBorder(new MatteBorder(0, 0, 1, 0, Color.BLUE));
				goLectureLabel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
				goLectureLabel.addMouseListener(new MouseAdapter() {
					@Override public void mouseClicked(MouseEvent e) {
						if (ended) { Alert.info("학습기간이 종료되었습니다."); return; }
						Temp.selectedCno = cno;
						dispose(); new LectureForm();
					}
				});

				rightP.add(statusLabel, NORTH);
				rightP.add(goLectureLabel, SOUTH);

				item.add(infoP, CENTER);
				item.add(rightP, EAST);

				courseListPanel.add(item);
				courseListPanel.add(Box.createRigidArea(new Dimension(0, 4)));
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		courseListPanel.revalidate();
		courseListPanel.repaint();
		SwingUtilities.invokeLater(() ->
				((JScrollPane) courseListPanel.getParent().getParent())
						.getVerticalScrollBar().setValue(0)
		);
	}

	private boolean isEnded(String startDate, String days) {
		try {
			int d = Integer.parseInt(days.replaceAll("[^0-9]", ""));
			java.time.LocalDate end = java.time.LocalDate.parse(startDate).plusDays(d);
			return java.time.LocalDate.now().isAfter(end);
		} catch (Exception e) { return false; }
	}

	private String calcEndDate(String startDate, String days) {
		try {
			int d = Integer.parseInt(days.replaceAll("[^0-9]", ""));
			return java.time.LocalDate.parse(startDate).plusDays(d).toString();
		} catch (Exception e) { return startDate; }
	}

	private void search() {
		String keyword = searchField.getText().trim();
		if (keyword.isEmpty()) return;
		try {
			rs = DB.execute(
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
		dispose();
		new CertiListForm();
	}

	private void openCertiList() {
		if (!Temp.isLoggedIn()) {
			Alert.error("로그인이 되어있지 않습니다.");
			dispose(); new LoginForm(); return;
		}
		Temp.searchKeyword = "";
		Temp.selectedCgno  = -1;
		dispose();
		new CertiListForm();
	}

	private void openSchedule() {
		if (!Temp.isLoggedIn()) {
			Alert.error("로그인이 되어있지 않습니다.");
			dispose(); new LoginForm(); return;
		}
		dispose();
		new ScheduleForm();
	}

	private void logout() {
		Temp.logout();
		refreshCPanel();
		revalidate();
		repaint();
	}

	@Override
	public void windowClosing(WindowEvent e) {
		if (slideTimer != null) slideTimer.stop();
	}
}