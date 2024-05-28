package vista;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.security.cert.X509Certificate;
import java.util.Arrays;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingConstants;

@SuppressWarnings("serial")
public class FrameVerificacion extends JFrame {

    private static Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
    private JPanel panel = new JPanel();

    public FrameVerificacion(Boolean verificado, byte[] firma, byte[] clavePublica, X509Certificate certificado) {
        this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        this.setUndecorated(false);
        this.setSize(500, 300);
        this.setLocation(dim.width / 2 - this.getSize().width / 3, dim.height / 4);
        this.add(panel);
        this.setTitle("Verificación");
        editarPanel(panel, verificado, firma, clavePublica, certificado);
        this.setVisible(true);
    }

    public static void editarPanel(JPanel panel, Boolean verificado, byte[] firma, byte[] clavePublica, X509Certificate certificado) {
        panel.setLayout(new GridBagLayout());
        panel.setBackground(Color.WHITE);
        GridBagConstraints gbc = new GridBagConstraints();

        JLabel titulo = new JLabel("Verificación de Firma con Dilithium", SwingConstants.CENTER);
        titulo.setFont(new Font("Arial", Font.BOLD, 16));
        titulo.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        gbc.insets = new Insets(10, 0, 10, 0);
        panel.add(titulo, gbc);

        String estadoVerificacion = verificado ? "Verificado" : "No Verificado";
        JLabel estadoLabel = new JLabel(estadoVerificacion, SwingConstants.CENTER);
        estadoLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        estadoLabel.setForeground(verificado ? Color.GREEN : Color.RED);
        gbc.gridy = 1;
        gbc.insets = new Insets(0, 0, 10, 0);
        panel.add(estadoLabel, gbc);

        JButton firmaButton = new JButton("Ver Firma");
        JButton clavePublicaButton = new JButton("Ver Clave");
        JButton certificadoButton = new JButton("Ver Certificado");

        JPanel firmaPanel = createPanelWithButton("Firma: ", firmaButton);
        JPanel clavePublicaPanel = createPanelWithButton("Clave pública: ", clavePublicaButton);
        JPanel certificadoPanel = createPanelWithButton("Certificado: ", certificadoButton);

        gbc.gridy = 2;
        gbc.gridwidth = 1;
        gbc.insets = new Insets(5, 0, 5, 0);
        panel.add(firmaPanel, gbc);
        
        gbc.gridy = 3;
        panel.add(clavePublicaPanel, gbc);

        gbc.gridy = 4;
        panel.add(certificadoPanel, gbc);

        JButton btnAceptar = new JButton("Aceptar");
        btnAceptar.setFont(new Font("Arial", Font.PLAIN, 14));
        btnAceptar.setBackground(Color.LIGHT_GRAY);
        btnAceptar.setFocusPainted(false);
        gbc.gridy = 5;
        gbc.insets = new Insets(10, 0, 10, 0);
        panel.add(btnAceptar, gbc);

        // Acción del botón aceptar (cerrar el frame)
        btnAceptar.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                ((JFrame) panel.getTopLevelAncestor()).dispose(); // Cierra el JFrame
            }
        });

        // Acciones para mostrar el contenido de la clave y la firma
        firmaButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                mostrarContenido("Firma", Arrays.toString(firma));
            }
        });

        clavePublicaButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                mostrarContenido("Clave Pública", Arrays.toString(clavePublica));
            }
        });

        certificadoButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                mostrarContenido("Certificado", certificado.toString());
            }
        });
    }

    private static JPanel createPanelWithButton(String labelText, JButton button) {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        
        JLabel label = new JLabel(labelText);
        label.setFont(new Font("Arial", Font.PLAIN, 14));
        panel.add(label);
        
        button.setFont(new Font("Arial", Font.PLAIN, 14));
        button.setBackground(Color.LIGHT_GRAY);
        button.setFocusPainted(false);
        panel.add(button);
        
        return panel;
    }

    private static void mostrarContenido(String titulo, String contenido) {
        JDialog dialogo = new JDialog();
        dialogo.setTitle(titulo);
        
        JTextArea textArea = new JTextArea(contenido);
        textArea.setEditable(false);
        textArea.setLineWrap(true);
        textArea.setWrapStyleWord(true);
        textArea.setColumns(20);
        
        JScrollPane scrollPane = new JScrollPane(textArea);
        dialogo.add(scrollPane);
        
        dialogo.setSize(500, 300);
        dialogo.setLocationRelativeTo(null);
        dialogo.setVisible(true);
    }
}
