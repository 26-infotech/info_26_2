package utils;

public class Temp {
	// 로그인한 사용자 정보 (창 간 데이터 공유)
	public static int uno = -1;         // 사용자 번호 (-1: 미로그인)
	public static String uname = "";    // 사용자 이름
	public static String uid = "";      // 사용자 아이디

	// 자격증 목록 폼 이동 시 공유 데이터
	public static String searchKeyword = ""; // 검색 키워드 (메인 검색 시 사용)
	public static int selectedCgno = -1;     // 선택한 카테고리 번호 (-1: 추천과정)

	// 결제/시험 관련 공유 데이터
	public static int selectedCno = -1;      // 선택한 자격증 번호
	public static String selectedStime = ""; // 선택한 시험 시간

	public static boolean isLoggedIn() {
		return uno != -1;
	}

	public static void logout() {
		uno = -1;
		uname = "";
		uid = "";
		searchKeyword = "";
		selectedCgno = -1;
		selectedCno = -1;
		selectedStime = "";
	}
}