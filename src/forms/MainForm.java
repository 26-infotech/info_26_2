package forms;

import utils.Alert;
import utils.DB;
import utils.Frame;
import utils.Temp;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
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
	private JPanel cPanel;
	private CardLayout cCard;
	private JPanel loginPanel;
	private JPanel loggedPanel;
	private JTextArea infoArea;
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
		setSize(900, 620);
		buildUI();
		loadSlideImages();
		startSlideShow();
		loadCategoryIcons();
		refreshCPanel();
		showPackedPage();
	}

	private void buildUI() {
		buildNorthArea();
		buildCenterArea();
		buildSouthArea();
	}

	private void buildNorthArea() {
		northP.setBorder(new EmptyBorder(6, 10, 0, 10));

		// 로고 행
		JPanel logoRow = new JPanel(new BorderLayout());
		logoRow.setBackground(Color.WHITE);

		// 로고 (로고 클릭 시 메인으로 - 이미 메인이지만 규격 맞춤)
		JLabel logoLabel = new JLabel();
		png(logoLabel, "icon/logo", 40, 40);
		JLabel siteTitle = new JLabel("  Skills Qualification Association");
		ft(siteTitle, Font.PLAIN, 14);
		JPanel logoGroup = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
		logoGroup.setBackground(Color.WHITE);
		logoGroup.add(logoLabel);
		logoGroup.add(siteTitle);

		// B영역: 검색 (NORTH에서 우측)
		JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 4, 0));
		searchPanel.setBackground(Color.WHITE);
		searchField = new JTextField();
		sz(searchField, 280, 30);
		line(searchField, Color.GRAY);
		ft(searchField, Font.PLAIN, 13);

		JLabel searchIcon = new JLabel();
		png(searchIcon, "icon/search", 22, 22);
		searchIcon.setPreferredSize(new Dimension(40, 40));
		marginBorder(searchIcon, 0, 0, 0, 6);
		searchIcon.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				search();
			}
		});
		searchField.addActionListener(e -> search());

		searchPanel.add(searchField);
		searchPanel.add(searchIcon);

		logoRow.add(logoGroup, WEST);
		logoRow.add(searchPanel, EAST);

		JPanel menuRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 220, 0));
		menuRow.setBackground(Color.WHITE);
		marginBorder(menuRow, 20, 0, 20, 0);

		JLabel menuList = new JLabel("자격증 목록");
		JLabel menuSchedule = new JLabel("시험 일정");

		menuRow.add(menuList);
		menuRow.add(menuSchedule);

		northP.add(logoRow, NORTH);
		northP.add(menuRow, CENTER);
	}

	// ──────────────────────────────────────────────────────────────
	// CENTER 영역 (A + C)
	// ──────────────────────────────────────────────────────────────
	private void buildCenterArea() {
		// A영역: 슬라이드
		JPanel aPanel = new JPanel(new BorderLayout());
		sz(aPanel, 500, 260);
		slideLabel = new JLabel();
		slideLabel.setHorizontalAlignment(SwingConstants.CENTER);
		aPanel.add(slideLabel, CENTER);

		// C영역 ─────────────────────────────────────────
		cCard = new CardLayout();
		cPanel = new JPanel(cCard);
		cPanel.setBackground(Color.WHITE);
		sz(cPanel, 280, 260);

		// ── 미로그인 패널 ──
		buildLoginPanel();
		// ── 로그인 패널 ──
		buildLoggedPanel();

		cPanel.add(loginPanel, "login");
		cPanel.add(loggedPanel, "logged");

		JPanel leftSection = new JPanel(new BorderLayout());
		leftSection.add(aPanel, NORTH);

		centerP.add(leftSection, CENTER);
		centerP.add(cPanel, EAST);
	}

	private void buildLoginPanel() {
		loginPanel = new JPanel();
		loginPanel.setLayout(new BoxLayout(loginPanel, BoxLayout.Y_AXIS));
		loginPanel.setBackground(Color.WHITE);

		JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 6));
		top.setBackground(Color.WHITE);
		JLabel checkIcon = new JLabel();
		png(checkIcon, "icon/check", 34, 34);
		fk(checkIcon, new Color(0, 150, 100));
		ft(checkIcon, Font.BOLD, 16);
		JLabel needLogin = new JLabel("로그인이 필요합니다.");
		ft(needLogin, Font.PLAIN, 12);
		top.add(checkIcon);
		top.add(needLogin);

		JButton loginBtn = new JButton("로그인");
		loginBtn.setMaximumSize(new Dimension(100, 38));
		loginBtn.setAlignmentX(Component.LEFT_ALIGNMENT);
		fk(loginBtn, Color.WHITE);
		bk(loginBtn, Color.BLUE);
		ft(loginBtn, Font.BOLD, 14);
		loginBtn.addActionListener(e -> {
			dispose();
			new LoginForm();
		});

		JPanel infoArea = new JPanel();
		infoArea.setLayout(new BoxLayout(infoArea, BoxLayout.Y_AXIS));

		infoArea.add(jl = new JLabel("로그인이 필요합니다."));
		bk(jl, Color.RED);
		infoArea.add(jl = new JLabel("1. 유효한 사용자 정보를 입력하세요."));
		infoArea.add(jl = new JLabel("2. 인증 절차를 확인하세요."));
		infoArea.add(jl = new JLabel("3. 로그인 후 이용 가능합니다."));
		infoArea.add(jl = new JLabel("4. 오류가 지속되면 관리자에게 문의하세요."));
		infoArea.setBorder(new LineBorder(Color.BLACK, 1, true));

		loginPanel.add(top);
		loginPanel.add(loginBtn);
		loginPanel.add(infoArea);
	}

	private void buildLoggedPanel() {
		loggedPanel = new JPanel(new BorderLayout());
		loggedPanel.setBorder(new LineBorder(Color.BLACK, 1, true));

		// 상단: 인사 + 버튼
		JPanel topBar = new JPanel(new BorderLayout());
		topBar.setBackground(Color.WHITE);
		topBar.setBorder(new EmptyBorder(6, 8, 4, 8));

		JPanel greetRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 0));
		greetRow.setBackground(Color.WHITE);
		JLabel checkGreen = new JLabel("✔");
		fk(checkGreen, new Color(0, 150, 100));
		ft(checkGreen, Font.BOLD, 14);
		JLabel greetLabel = new JLabel(); // 이름 나중에 세팅
		greetLabel.setName("greetLabel");
		ft(greetLabel, Font.PLAIN, 12);
		greetRow.add(checkGreen);
		greetRow.add(greetLabel);

		JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 4));
		btnRow.setBackground(Color.WHITE);

		JButton logoutBtn = new JButton("로그아웃");
		bk(logoutBtn, Color.BLUE);
		fk(logoutBtn, Color.WHITE);
		logoutBtn.setBorderPainted(false);
		logoutBtn.setFocusPainted(false);
		ft(logoutBtn, Font.BOLD, 12);
		logoutBtn.addActionListener(e -> logout());

		JButton myInfoBtn = new JButton("내 정보");
		bk(myInfoBtn, Color.LIGHT_GRAY);
		myInfoBtn.setBorderPainted(false);
		myInfoBtn.setFocusPainted(false);
		myInfoBtn.addActionListener(e -> {
			dispose();
			new forms.MyPageForm();
		});

		btnRow.add(logoutBtn);
		btnRow.add(myInfoBtn);

		topBar.add(greetRow, NORTH);
		topBar.add(btnRow, CENTER);

		// 수강 목록 스크롤
		courseListPanel = new JPanel();
		courseListPanel.setLayout(new BoxLayout(courseListPanel, BoxLayout.Y_AXIS));
		courseListPanel.setBackground(Color.WHITE);
		JScrollPane scroll = new JScrollPane(courseListPanel);
		scroll.setBorder(null);
		scroll.getVerticalScrollBar().setUnitIncrement(10);

		loggedPanel.add(topBar, NORTH);
		loggedPanel.add(scroll, CENTER);
	}

	private void buildSouthArea() {
		JPanel medalRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 4));
		medalRow.setBackground(Color.WHITE);
		JLabel medalIcon = new JLabel();
		png(medalIcon, "icon/medel", 44, 44);
		marginBorder(medalIcon, 20, 0, 20, 20);
		JLabel medalText = new JLabel("자격증을 선택해주세요.");
		medalRow.add(medalIcon);
		medalRow.add(medalText);

		iconPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 30, 10));
		iconPanel.setBorder(new LineBorder(Color.BLACK, 1, true));

		southP.add(medalRow, NORTH);
		southP.add(iconPanel, CENTER);
	}

	private void loadSlideImages() {
		for (int i=1; i<=5; i++) {
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
			addIconButton(entry[0], entry[1]);
		}
		iconPanel.revalidate();
		iconPanel.repaint();
	}

	private void addIconButton(String cgname, String iconFile) {
		ImageIcon normalIcon = loadIconScaled("datafiles/icon/" + iconFile + ".png", 50, 50);
		ImageIcon hoverIcon  = loadIconScaled("datafiles/icon/" + iconFile + ".png", 60, 60);

		JLabel iconLabel = new JLabel(normalIcon);
		iconLabel.setToolTipText(cgname);
		iconLabel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		iconLabel.setPreferredSize(new Dimension(60, 60));
		iconLabel.setMinimumSize(new Dimension(60, 60));
		iconLabel.setMaximumSize(new Dimension(60, 60));
		iconLabel.setHorizontalAlignment(SwingConstants.CENTER);
		iconLabel.setVerticalAlignment(SwingConstants.CENTER);

		iconLabel.addMouseListener(new MouseAdapter() {
			@Override public void mouseEntered(MouseEvent e) { iconLabel.setIcon(hoverIcon); }
			@Override public void mouseExited(MouseEvent e)  { iconLabel.setIcon(normalIcon); }
			@Override public void mouseClicked(MouseEvent e) {
				if (!Temp.isLoggedIn()) {
					Alert.error("로그인하세요");
					dispose();
					new forms.LoginForm();
					return;
				}

				try {
					DB.init();
					ResultSet rs = DB.execute(
							"SELECT cgno FROM category WHERE cgname='" + cgname + "' LIMIT 1"
					);
					if (rs.next()) Temp.selectedCgno = rs.getInt("cgno");
				} catch (Exception ex) { ex.printStackTrace(); }
				Temp.searchKeyword = "";
				dispose();
				new forms.CertiListForm();
			}
		});

		iconPanel.add(iconLabel);
	}

	private ImageIcon loadIconScaled(String path, int w, int h) {
		try {
			return new ImageIcon(new ImageIcon(path).getImage().getScaledInstance(w, h, Image.SCALE_SMOOTH));
		} catch (Exception e) {
			return new ImageIcon();
		}
	}

	public void refreshCPanel() {
		if (!Temp.isLoggedIn()) {
			cCard.show(cPanel, "login");
		} else {
			for (Component c : ((JPanel) ((BorderLayout) loggedPanel.getLayout())
					.getLayoutComponent(NORTH)).getComponents()) {
			}

			updateGreetLabel();
			loadCourseList();
			cCard.show(cPanel, "logged");
		}
	}

	private void updateGreetLabel() {
		traverseAndUpdate(loggedPanel);
	}

	private void traverseAndUpdate(Container container) {
		for (Component c : container.getComponents()) {
			if (c instanceof JLabel && "greetLabel".equals(c.getName())) {
				((JLabel) c).setText(Temp.uname + "님, 환영합니다.");
				return;
			}
			if (c instanceof Container) traverseAndUpdate((Container) c);
		}
	}

	private void loadCourseList() {
		courseListPanel.removeAll();
		try {
			DB.init();
			String sql =
					"SELECT cr.crno, cr.cno, cr.start_date, cr.rate, c.cname, c.days " +
							"FROM course_registration cr " +
							"JOIN certi c ON cr.cno = c.cno " +
							"WHERE cr.uno = " + Temp.uno + " " +
							"ORDER BY cr.start_date DESC";
			ResultSet rs = DB.execute(sql);
			while (rs.next()) {
				String cname     = rs.getString("cname");
				String startDate = rs.getString("start_date");
				String days      = rs.getString("days");
				int    cno       = rs.getInt("cno");
				String rate      = rs.getString("rate");

				boolean ended = isEnded(startDate, days);
				String endDate = calcEndDate(startDate, days);

				JPanel item = buildCourseItem(cname, startDate, endDate, ended, cno, rate);
				courseListPanel.add(item);
				courseListPanel.add(Box.createRigidArea(new Dimension(0, 4)));
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		courseListPanel.revalidate();
		courseListPanel.repaint();
	}

	private JPanel buildCourseItem(String cname, String startDate, String endDate,
	                               boolean ended, int cno, String rate) {
		JPanel item = new JPanel(new BorderLayout(4, 2));
		item.setBackground(Color.WHITE);
		item.setBorder(new EmptyBorder(4, 6, 4, 6));

		JPanel infoP = new JPanel(new GridLayout(3, 1));
		infoP.setBackground(Color.WHITE);

		// 상태 뱃지
		JLabel statusLabel = new JLabel(ended ? "종료" : "학습중");
		statusLabel.setOpaque(true);
		bk(statusLabel, ended ? Color.RED : new Color(0, 120, 0));
		fk(statusLabel, Color.WHITE);
		ft(statusLabel, Font.BOLD, 10);
		statusLabel.setHorizontalAlignment(SwingConstants.CENTER);
		sz(statusLabel, 50, 18);

		JLabel nameLabel = new JLabel(cname);
		ft(nameLabel, Font.BOLD, 12);

		JLabel dateLabel = new JLabel("수강 신청 : " + startDate + "~" + endDate);
		ft(dateLabel, Font.PLAIN, 10);
		fk(dateLabel, Color.GRAY);

		infoP.add(nameLabel);
		infoP.add(dateLabel);

		JPanel rightP = new JPanel(new BorderLayout(0, 4));
		rightP.setBackground(Color.WHITE);
		rightP.add(statusLabel, NORTH);

		if (!ended) {
			JLabel goBtn = new JLabel("강의실 가기");
			goBtn.setOpaque(true);
			bk(goBtn, new Color(0, 90, 200));
			fk(goBtn, Color.WHITE);
			ft(goBtn, Font.BOLD, 10);
			goBtn.setHorizontalAlignment(SwingConstants.CENTER);
			sz(goBtn, 70, 20);
			goBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
			goBtn.addMouseListener(new MouseAdapter() {
				@Override public void mouseClicked(MouseEvent e) {
					Temp.selectedCno = cno;
					dispose();
					new forms.LectureForm();
				}
			});
			rightP.add(goBtn, SOUTH);
		} else {
			JLabel goBtn = new JLabel("강의실 가기");
			goBtn.setOpaque(true);
			bk(goBtn, Color.LIGHT_GRAY);
			fk(goBtn, Color.WHITE);
			ft(goBtn, Font.PLAIN, 10);
			goBtn.setHorizontalAlignment(SwingConstants.CENTER);
			sz(goBtn, 70, 20);
			goBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
			goBtn.addMouseListener(new MouseAdapter() {
				@Override public void mouseClicked(MouseEvent e) {
					Alert.info("학습기간이 종료되었습니다.");
				}
			});
			rightP.add(goBtn, SOUTH);
		}

		item.add(infoP, CENTER);
		item.add(rightP, EAST);
		item.setBorder(new LineBorder(Color.LIGHT_GRAY));
		return item;
	}

	private boolean isEnded(String startDate, String days) {
		try {
			int d = Integer.parseInt(days.replaceAll("[^0-9]", ""));
			java.time.LocalDate start = java.time.LocalDate.parse(startDate);
			java.time.LocalDate end   = start.plusDays(d);
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
			DB.init();
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
		dispose();
		new forms.CertiListForm();
	}

	private void openCertiList(int cgno) {
		if (!Temp.isLoggedIn()) {
			Alert.error("로그인이 되어있지 않습니다.");
			dispose();
			new forms.LoginForm();
			return;
		}
		Temp.searchKeyword = "";
		Temp.selectedCgno  = cgno;
		dispose();
		new forms.CertiListForm();
	}

	private void openSchedule() {
		if (!Temp.isLoggedIn()) {
			Alert.error("로그인이 되어있지 않습니다.");
			dispose();
			new forms.LoginForm();
			return;
		}
		dispose();
		new forms.ScheduleForm();
	}

	private void logout() {
		Temp.logout();
		dispose();
		new forms.MainForm();
	}

	@Override
	public void windowClosing(WindowEvent e) {
		if (slideTimer != null) slideTimer.stop();
	}
}