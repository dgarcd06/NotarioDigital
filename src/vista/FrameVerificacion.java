package vista;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Toolkit;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JLayeredPane;
import javax.swing.JPanel;

@SuppressWarnings("serial")
public class FrameVerificacion extends JFrame{
	
	private Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
	private JPanel panel = new JPanel();
	public FrameVerificacion() {
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		this.setUndecorated(false);
		this.setSize(500, 500);
		this.setLocation(dim.width / 2 - this.getSize().width / 3, dim.height - this.getSize().height);
		this.setVisible(true);
		this.add(panel);
		this.setTitle("Verificación");
		editarPanel(panel);
	}
	public static void editarPanel(JPanel panel) {
		panel.add(new JLabel("Verificación de Firma con Dilithium"));
	}
}
