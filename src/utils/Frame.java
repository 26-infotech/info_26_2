package utils;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.InputStream;
import java.sql.ResultSet;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Locale;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

public class Frame extends JFrame implements ActionListener, WindowListener, MouseListener, Runnable {

	public static JPanel mainP, northP, westP, centerP, southP, eastP, p0, p1, p2, p3, p4, p5, p6, p7, p8, p9, jp, mp;
	public static JLabel img, jl;
	public static JTextField jt;
	public static JTextArea jte;
	public static JButton jb;
	public static JComboBox jc;
	public static ResultSet rs;
	public static DefaultTableCellRenderer cell = new DefaultTableCellRenderer();
	public static DefaultTableModel dtm;
	public static String EAST = BorderLayout.EAST;
	public static String WEST = BorderLayout.WEST;
	public static String SOUTH = BorderLayout.SOUTH;
	public static String NORTH = BorderLayout.NORTH;
	public static String CENTER = BorderLayout.CENTER;
	public static JTable jta;
	public static JScrollPane jsp;
	public static JCheckBox jck;
	public static Thread th;
	public static JSpinner spin;
	public static SimpleDateFormat daf = new SimpleDateFormat("yyyy-MM-dd");
	public static Locale locale = new Locale("ko", "KR");
			
	public void init(String t) {
		setTitle(t);
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		
		setIconImage(Toolkit.getDefaultToolkit().createImage("datafiles/icon/logo.png"));
		
		add(mainP = new JPanel(new BorderLayout()));
		mainP.add(northP = new JPanel(new BorderLayout()), NORTH);
		mainP.add(westP = new JPanel(new BorderLayout()), WEST);
		mainP.add(centerP = new JPanel(new BorderLayout()), CENTER);
		mainP.add(southP = new JPanel(new BorderLayout()), SOUTH);
		mainP.add(eastP = new JPanel(new BorderLayout()), EAST);
		
		addWindowListener(this);
		cell.setHorizontalAlignment(0);
		th = new Thread(this);
	}

	public JTextField hint(String t) {
		return new JTextField() {
			@Override
			public void paint(Graphics g) {
				super.paint(g);
				if (getText().isEmpty()) {
					g.setColor(Color.LIGHT_GRAY);
					g.drawString(t, getInsets().left, getHeight() / 2 + 5);
				}
			}
		};
	}

	public JTextArea ahint(String t) {
		return new JTextArea() {
			@Override
			public void paint(Graphics g) {
				super.paint(g);
				if (getText().isEmpty()) {
					g.setColor(Color.black);
					g.drawString(t, getInsets().left, 25);
				}
			}
		};
	}

	public void bl(JComponent c) {
		bk(c, Color.green);
		fk(c, Color.white);
	}

	public static void gifup(JLabel c, String data, int w, int h) {
		try {
			c.setIcon(new ImageIcon(
					new ImageIcon("datafiles/image/icon/" + data + ".gif").getImage().getScaledInstance(w, h, 0)));
		} catch (Exception e) {
			c.setIcon(new ImageIcon("datatfiles/image/icon/img-logo.gif"));
		}
	}

	public void showPage() {
		setLocationRelativeTo(null);
		getContentPane().setBackground(Color.WHITE);
		setVisible(true);
	}
	
	public void showPackedPage() {
		pack();
		setLocationRelativeTo(null);
		getContentPane().setBackground(Color.WHITE);
		setVisible(true);
	}

	public void marginBorder(JComponent c, int top, int right, int bottom, int left) {
		c.setBorder(new EmptyBorder(top, left, bottom, right));
	}

	public void sz(JComponent c, int w, int h) {
		c.setPreferredSize(new Dimension(w, h));
	}

	public void jpg(JLabel jl, String data, int w, int h) {
		jl.setIcon(new ImageIcon(new ImageIcon("datafiles/" + data + ".jpg").getImage().getScaledInstance(w, h, 4)));
	}

	public void png(JLabel jl, String data, int w, int h) {
		jl.setIcon(new ImageIcon(new ImageIcon("datafiles/" + data + ".png").getImage().getScaledInstance(w, h, 4)));
	}

	public void ft(JComponent c, int a, int b) {
		c.setFont(new Font("맑은 고딕", a, b));
	}

	public void ft2(JComponent c, int a, int b) {
		c.setFont(new Font("Calibri", a, b));
	}

	public void line(JComponent c, Color col) {
		c.setBorder(new LineBorder(col));
	}

	public void bk(JComponent c, Color col) {
		c.setBackground(col);
	}

	public void fk(JComponent c, Color col) {
		c.setForeground(col);
	}

	public int rei(String t) {
		try {
			return Integer.parseInt(t);
		} catch (Exception e) {
			return 0;
		}
	}

	// public boolean cknum(String t) {
	// try {
	// Integer.parseInt(t);
	// return true;
	// } catch (Exception e) {
	// return false;
	// }
	// }

	// public boolean cdate(String t) {
	// try {
	// if (daf.format(daf.parse(t)).equals(t))
	// return true;
	// } catch (Exception e) {
	// }
	// return false;
	// }

	// public ImageIcon blob(InputStream is, int w, int h) {
	// try {
	// Image img = ImageIO.read(is);
	// img = img.getScaledInstance(w, h, 0);
	// return new ImageIcon(img);
	// } catch (Exception e) {
	// }
	// return null;
	// }

	@Override
	public void run() {
		// TODO Auto-generated method stub

	}

	@Override
	public void mouseClicked(MouseEvent e) {
		// TODO Auto-generated method stub

	}

	@Override
	public void mousePressed(MouseEvent e) {
		// TODO Auto-generated method stub

	}

	@Override
	public void mouseReleased(MouseEvent e) {
		// TODO Auto-generated method stub

	}

	@Override
	public void mouseEntered(MouseEvent e) {
		// TODO Auto-generated method stub

	}

	@Override
	public void mouseExited(MouseEvent e) {
		// TODO Auto-generated method stub

	}

	@Override
	public void windowOpened(WindowEvent e) {
		// TODO Auto-generated method stub

	}

	@Override
	public void windowClosing(WindowEvent e) {
		// TODO Auto-generated method stub

	}

	@Override
	public void windowClosed(WindowEvent e) {
		// TODO Auto-generated method stub

	}

	@Override
	public void windowIconified(WindowEvent e) {
		// TODO Auto-generated method stub

	}

	@Override
	public void windowDeiconified(WindowEvent e) {
		// TODO Auto-generated method stub

	}

	@Override
	public void windowActivated(WindowEvent e) {
		// TODO Auto-generated method stub

	}

	@Override
	public void windowDeactivated(WindowEvent e) {
		// TODO Auto-generated method stub

	}

	@Override
	public void actionPerformed(ActionEvent e) {
		// TODO Auto-generated method stub

	}
}
