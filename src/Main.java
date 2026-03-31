import java.awt.Color;
import java.sql.SQLException;

import javax.swing.UIManager;
import javax.swing.plaf.ColorUIResource;

import forms.MainForm;
import forms.*;
import utils.Alert;
import utils.DB;


public class Main {
	public static void main(String[] args) {
		
		try {
			DB.init();
		} catch (SQLException e) {
			e.printStackTrace();
			Alert.error("DB 접속 실패: " + e);
			System.exit(1);
		}

		for (Object f : UIManager.getLookAndFeelDefaults().keySet())
			if (f.toString().contains("background"))
				UIManager.getLookAndFeelDefaults().put(f, new ColorUIResource(Color.white));
		
		new MainForm();
	}
}
