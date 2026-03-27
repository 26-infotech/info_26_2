package utils;

import javax.swing.JOptionPane;

public class Alert {
	public static void info(String msg) {
		JOptionPane.showMessageDialog(null, msg, "정보", JOptionPane.INFORMATION_MESSAGE);
	}
	
	public static void error(String msg) {
		JOptionPane.showMessageDialog(null, msg, "경고", JOptionPane.ERROR_MESSAGE);
	}
	
	public static boolean confirm(String msg) {
		return JOptionPane.showConfirmDialog(null, msg, "확인", JOptionPane.YES_NO_OPTION) == JOptionPane.OK_OPTION;
	}
}
