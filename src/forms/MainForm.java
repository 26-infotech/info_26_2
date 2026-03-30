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
import java.io.File;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class MainForm extends Frame {

	// ── 슬라이드쇼 관련 ──────────────────────────────────────────
	private List<ImageIcon> slideImages = new ArrayList<>();
	private int slideIndex = 0;
	private JLabel slideLabel;
	private Timer slideTimer;

	// ── 검색 ─────────────────────────────────────────────────────
	private JTextField searchField;

	// ── C 영역 (로그인/로그아웃 패널) ────────────────────────────
	private JPanel cPanel;          // CardLayout 부모
	private CardLayout cCard;
	private JPanel loginPanel;      // 미로그인 상태
	private JPanel loggedPanel;     // 로그인 상태
	private JTextArea infoArea;

	// 로그인 후 수강 목록 패널 (스크롤)
	private JPanel courseListPanel;

	// ── 카테고리 아이콘 영역 ──────────────────────────────────────
	private JPanel iconPanel;

	// 카테고리 데이터 (cgno, cgname, iconfile)
	private static final String[][] ICON_MAP = {
			// cgname 은 DB에서 읽지만, 아이콘 파일명은 datafiles/icon/ 하위
			// 실제 카테고리 순서는 DB category 테이블 기준
	};

	// ──────────────────────────────────────────────────────────────
	public MainForm() {
		init("자격증 메인 화면");
		setSize(900, 620);
		buildUI();
		loadSlideImages();
		startSlideShow();
		loadCategoryIcons();
		refreshCPanel();
		showPage();
	}

	// =========================================================
	// UI 구성
	// =========================================================
	private void buildUI() {
		mainP.setBackground(Color.WHITE);

		// ── NORTH: 로고 + 검색바 + 메뉴 ──────────────────────────
		buildNorthArea();

		// ── CENTER: 슬라이드(A) + 로그인C 패널 ───────────────────
		buildCenterArea();

		// ── SOUTH: 아이콘 카테고리 ────────────────────────────────
		buildSouthArea();
	}

	// ──────────────────────────────────────────────────────────────
	// NORTH 영역
	// ──────────────────────────────────────────────────────────────
	private void buildNorthArea() {
		northP.setBackground(Color.WHITE);
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
		searchField = hint("검색어를 입력하세요");
		sz(searchField, 280, 30);
		line(searchField, Color.GRAY);
		ft(searchField, Font.PLAIN, 13);

		JButton searchBtn = new JButton();
		png(new JLabel(), "icon/search", 22, 22);
		searchBtn.setText("🔍");
		searchBtn.setPreferredSize(new Dimension(34, 30));
		searchBtn.setBorderPainted(false);
		searchBtn.setFocusPainted(false);
		searchBtn.setBackground(Color.WHITE);
		searchBtn.addActionListener(e -> doSearch());
		searchField.addActionListener(e -> doSearch());

		searchPanel.add(searchField);
		searchPanel.add(searchBtn);

		logoRow.add(logoGroup, WEST);
		logoRow.add(searchPanel, EAST);

		// 메뉴 행 (자격증 목록 | 시험 일정)
		JPanel menuRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 2));
		menuRow.setBackground(Color.WHITE);

		JLabel menuList = new JLabel("자격증 목록");
		JLabel menuSchedule = new JLabel("시험 일정");
		ft(menuList, Font.PLAIN, 13);
		ft(menuSchedule, Font.PLAIN, 13);
		menuList.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		menuSchedule.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

		menuList.addMouseListener(new MouseAdapter() {
			@Override public void mouseClicked(MouseEvent e) { openCertiList(-1); }
		});
		menuSchedule.addMouseListener(new MouseAdapter() {
			@Override public void mouseClicked(MouseEvent e) { openSchedule(); }
		});

		menuRow.add(menuList);
		menuRow.add(menuSchedule);

		northP.add(logoRow, NORTH);
		northP.add(menuRow, CENTER);
	}

	// ──────────────────────────────────────────────────────────────
	// CENTER 영역 (A + C)
	// ──────────────────────────────────────────────────────────────
	private void buildCenterArea() {
		centerP.setBackground(Color.WHITE);

		// A영역: 슬라이드
		JPanel aPanel = new JPanel(new BorderLayout());
		aPanel.setBackground(Color.BLACK);
		sz(aPanel, 580, 260);
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

		// 메달 라벨 (아이콘 + "자격증을 선택해주세요.")
		JPanel medalRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 4));
		medalRow.setBackground(Color.WHITE);
		JLabel medalIcon = new JLabel();
		png(medalIcon, "icon/medel", 42, 42);
		marginBorder(medalIcon, 20, 0, 20, 20);
		JLabel medalText = new JLabel("자격증을 선택해주세요.");
		ft(medalText, Font.PLAIN, 13);
		medalRow.add(medalIcon);
		medalRow.add(medalText);

		// 슬라이드 아래 메달 행
		JPanel leftSection = new JPanel(new BorderLayout());
		leftSection.setBackground(Color.WHITE);
		leftSection.add(aPanel, NORTH);
		leftSection.add(medalRow, CENTER);

		centerP.add(leftSection, CENTER);
		centerP.add(cPanel, EAST);
	}

	// ── 미로그인 C패널 ────────────────────────────────────────────
	private void buildLoginPanel() {
		loginPanel = new JPanel();
		loginPanel.setLayout(new BoxLayout(loginPanel, BoxLayout.Y_AXIS));
		loginPanel.setBackground(Color.WHITE);
		loginPanel.setBorder(new LineBorder(Color.LIGHT_GRAY));

		JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 6));
		top.setBackground(Color.WHITE);
		JLabel checkIcon = new JLabel();
		png(checkIcon, "icon/check", 30, 30);
		fk(checkIcon, new Color(0, 150, 100));
		ft(checkIcon, Font.BOLD, 16);
		JLabel needLogin = new JLabel("로그인이 필요합니다.");
		ft(needLogin, Font.PLAIN, 12);
		top.add(checkIcon);
		top.add(needLogin);

		JButton loginBtn = new JButton("로그인");
		loginBtn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
		loginBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
		bk(loginBtn, new Color(0, 90, 200));
		fk(loginBtn, Color.WHITE);
		ft(loginBtn, Font.BOLD, 14);
		loginBtn.setBorderPainted(false);
		loginBtn.setFocusPainted(false);
		loginBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		loginBtn.addActionListener(e -> {
			dispose();
			new forms.LoginForm();
		});

		infoArea = new JTextArea(
				"로그인이 필요합니다.\n1. 유효한 사용자 정보를 입력하세요.\n2. 인증 절차를 확인하세요.\n3. 로그인 후 이용 가능합니다.\n4. 오류가 지속되면 관리자에게 문의하세요."
		);
		infoArea.setEditable(false);
		infoArea.setLineWrap(true);
		infoArea.setWrapStyleWord(true);
		infoArea.setBackground(Color.WHITE);
		ft(infoArea, Font.PLAIN, 11);
		infoArea.setBorder(new EmptyBorder(6, 8, 6, 8));

		loginPanel.add(top);
		loginPanel.add(Box.createRigidArea(new Dimension(0, 4)));
		loginPanel.add(loginBtn);
		loginPanel.add(Box.createRigidArea(new Dimension(0, 4)));
		loginPanel.add(infoArea);
	}

	// ── 로그인 후 C패널 ───────────────────────────────────────────
	private void buildLoggedPanel() {
		loggedPanel = new JPanel(new BorderLayout());
		loggedPanel.setBackground(Color.WHITE);
		loggedPanel.setBorder(new LineBorder(Color.LIGHT_GRAY));

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
		bk(logoutBtn, new Color(0, 90, 200));
		fk(logoutBtn, Color.WHITE);
		logoutBtn.setBorderPainted(false);
		logoutBtn.setFocusPainted(false);
		ft(logoutBtn, Font.BOLD, 12);
		logoutBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		logoutBtn.addActionListener(e -> doLogout());

		JButton myInfoBtn = new JButton("내 정보");
		bk(myInfoBtn, new Color(0, 90, 200));
		fk(myInfoBtn, Color.WHITE);
		myInfoBtn.setBorderPainted(false);
		myInfoBtn.setFocusPainted(false);
		ft(myInfoBtn, Font.BOLD, 12);
		myInfoBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
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

	// ──────────────────────────────────────────────────────────────
	// SOUTH 영역 (카테고리 아이콘)
	// ──────────────────────────────────────────────────────────────
	private void buildSouthArea() {
		southP.setBackground(new Color(245, 245, 245));
		southP.setBorder(new LineBorder(Color.LIGHT_GRAY));

		iconPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
		iconPanel.setBackground(new Color(245, 245, 245));

		southP.add(iconPanel, CENTER);
	}

	// =========================================================
	// 슬라이드쇼
	// =========================================================
	private void loadSlideImages() {
		File dir = new File("datafiles/main");
		if (!dir.exists()) return;
		File[] files = dir.listFiles((d, name) -> name.toLowerCase().endsWith(".png") || name.toLowerCase().endsWith(".jpg"));
		if (files == null) return;
		for (File f : files) {
			ImageIcon icon = new ImageIcon(f.getAbsolutePath());
			// 슬라이드 크기에 맞게 스케일
			Image scaled = icon.getImage().getScaledInstance(580, 260, Image.SCALE_SMOOTH);
			slideImages.add(new ImageIcon(scaled));
		}
	}

	private void startSlideShow() {
		if (slideImages.isEmpty()) return;
		slideLabel.setIcon(slideImages.get(0));

		// 오른쪽 → 왼쪽 슬라이딩: 타이머로 단순 전환 (3초)
		slideTimer = new Timer(3000, e -> {
			slideIndex = (slideIndex + 1) % slideImages.size();
			slideLabel.setIcon(slideImages.get(slideIndex));
		});
		slideTimer.start();
	}

	// =========================================================
	// 카테고리 아이콘 로드 (DB category 테이블)
	// =========================================================
	// 아이콘 파일명 매핑 (cgname → icon 파일명)
	private String iconFileFor(String cgname) {
    if (cgname == null) return "check";

    switch (cgname.trim()) {
        case "IT":   return "it";
        case "요리":  return "cooking";
        case "봉사":  return "volunteer";
        case "운동":  return "health";
        case "의학":  return "hospital";
        case "항공":  return "aviation";
        default:     return "check";
    }
}

	private void loadCategoryIcons() {
		iconPanel.removeAll();
		try {
			DB.init();
			ResultSet rs = DB.execute("SELECT cgno, cgname FROM category ORDER BY cgno");
			while (rs.next()) {
				int cgno = rs.getInt("cgno");
				String cgname = rs.getString("cgname");
				addIconButton(cgno, cgname);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		iconPanel.revalidate();
		iconPanel.repaint();
	}

	private void addIconButton(int cgno, String cgname) {
		String iconFile = iconFileFor(cgname);
		ImageIcon normalIcon = loadIconScaled("datafiles/icon/" + iconFile + ".png", 50, 50);
		ImageIcon hoverIcon  = loadIconScaled("datafiles/icon/" + iconFile + ".png", 60, 60);

		JLabel iconLabel = new JLabel(normalIcon);
		iconLabel.setToolTipText(cgname);
		iconLabel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

		iconLabel.addMouseListener(new MouseAdapter() {
			@Override public void mouseEntered(MouseEvent e) {
				iconLabel.setIcon(hoverIcon);
			}
			@Override public void mouseExited(MouseEvent e) {
				iconLabel.setIcon(normalIcon);
			}
			@Override public void mouseClicked(MouseEvent e) {
				// 로그인 체크
				if (!Temp.isLoggedIn()) {
					Alert.error("로그인하세요");
					dispose();
					new forms.LoginForm();
					return;
				}
				Temp.searchKeyword = "";
				Temp.selectedCgno = cgno;
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

	// =========================================================
	// C영역 갱신 (로그인 여부에 따라 카드 전환)
	// =========================================================
	public void refreshCPanel() {
		if (!Temp.isLoggedIn()) {
			cCard.show(cPanel, "login");
		} else {
			// 인사 라벨 세팅
			for (Component c : ((JPanel) ((BorderLayout) loggedPanel.getLayout())
					.getLayoutComponent(NORTH)).getComponents()) {
				// greetRow에서 greetLabel 찾기
			}
			// 간단하게 loggedPanel 재빌드 방식 대신 직접 접근
			updateGreetLabel();
			loadCourseList();
			cCard.show(cPanel, "logged");
		}
	}

	private void updateGreetLabel() {
		// loggedPanel > topBar > greetRow 에서 이름 라벨 찾아 업데이트
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

	// 수강 목록 로드 (수강신청 시작 날짜 내림차순)
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
				String days      = rs.getString("days");   // 강의기간(일수)
				int    cno       = rs.getInt("cno");
				String rate      = rs.getString("rate");   // 출석 회차

				// 종료 여부: start_date + days일 < 오늘
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

	// =========================================================
	// 검색
	// =========================================================
	private void doSearch() {
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

	// =========================================================
	// 메뉴 클릭
	// =========================================================
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

	// =========================================================
	// 로그아웃
	// =========================================================
	private void doLogout() {
		Temp.logout();
		dispose();
		new forms.MainForm();
	}

	// =========================================================
	// 윈도우 닫기
	// =========================================================
	@Override
	public void windowClosing(WindowEvent e) {
		if (slideTimer != null) slideTimer.stop();
	}

	// =========================================================
	// main (진입점)
	// =========================================================
	public static void main(String[] args) {
		try { DB.init(); } catch (Exception ignored) {}
		SwingUtilities.invokeLater(MainForm::new);
	}
}