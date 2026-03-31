package utils;

public class Temp {
	public static int uno = 1;
	public static String uname = "김하늘";
	public static String uid = "user01";

	public static String searchKeyword = "";
	public static int selectedCgno = -1;

	public static int selectedCno = -1;
	public static String selectedStime = "";

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