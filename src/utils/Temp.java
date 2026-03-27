package utils;

import java.util.HashSet;
import java.util.Set;

public class Temp {
	public static String searchInput;
	public static int loginUno;
	public static String loginName;
	public static boolean admin;
	public static int selectedCgno;
	public static int selectedCno;
	public static String selectedExamDate;
	public static String selectedExamTime;
	public static boolean paymentExamMode;
	public static Set<Integer> attendNoti = new HashSet<>();

	public static boolean login() {
		return loginUno > 0;
	}

	public static void logout() {
		loginUno = 0;
		loginName = null;
		admin = false;
		selectedCgno = 0;
		selectedCno = 0;
		selectedExamDate = null;
		selectedExamTime = null;
		paymentExamMode = false;
	}
}
