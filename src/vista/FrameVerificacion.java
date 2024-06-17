package vista;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.SwingConstants;

import org.apache.pdfbox.pdmodel.interactive.digitalsignature.PDSignature;

@SuppressWarnings("serial")
/**
 * Este frame se muestra cuando el documento PDF abierto contiene una firma.
 * En ese caso, muestra si la firma ha podido ser verificada a partir de las comprobaciones
 * en la clase Firma Digital. Además, muestra la clave pública, la firma digital y el certificado.
 * @author David García Diez
 */
public class FrameVerificacion extends JFrame {

    private static Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
    /**
     * Construye el Frame de verificación y se muestra.
     * @param signatures La lista con las firmas digitales del documento
     * @param verificados La lista con la respuesta a la verificación de las diferentes firmas del documento
     * @param firmas La lista con los diferentes codigos de la firma digital del documento
     * @param clavesPublicas La lista con las diferentes claves públicas del documento
     * @param certificados La lista con los diferentes certificados del documento
     */
    public FrameVerificacion(List<PDSignature> signatures, List<Boolean> verificados, List<byte[]> firmas, List<byte[]> clavesPublicas, List<X509Certificate> certificados) {
        this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        this.setUndecorated(false);
        this.setSize(500, 450);
        this.setLocation(dim.width / 2 - this.getSize().width / 3, dim.height / 4);
        this.setTitle("Verificación");
        
        this.setVisible(true);
        JPanel leftPanel = new JPanel();
        JPanel rightPanel = new JPanel();
        leftPanel.setLayout(new GridLayout(signatures.size(), 1, 10, 10));
        rightPanel.setLayout(new GridBagLayout());
        rightPanel.setBackground(Color.WHITE);
        
        for (int i = 0; i < signatures.size();i++) {
            JLabel label = new JLabel("Firma " + (i + 1), SwingConstants.CENTER);
            label.setFont(new Font("Segoe UI", Font.PLAIN, 16));
            label.setOpaque(true);
            label.setBackground(new Color(240, 240, 240)); 
            label.setForeground(Color.DARK_GRAY);
            label.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200), 1), 
                BorderFactory.createEmptyBorder(10, 20, 10, 20) 
            ));
            
            Boolean verificadoI = verificados.get(i);
            byte[] firmaI = firmas.get(i);
            byte[] claveI = clavesPublicas.get(i);
            X509Certificate certI = certificados.get(i);
            PDSignature sig = signatures.get(i);
            // Add mouse listener to handle clicks
            label.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                	mostrarPanelDerecha(sig, rightPanel,verificadoI,firmaI,claveI,certI);
                }

                @Override
                public void mouseEntered(MouseEvent e) {
                    label.setBackground(new Color(220, 220, 220));
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    label.setBackground(new Color(240, 240, 240));
                }
            });
            label.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createMatteBorder(1, 1, 1, 1, new Color(200, 200, 200)),
                    BorderFactory.createEmptyBorder(10, 20, 10, 20)
                ));
            leftPanel.add(label);
        }
        leftPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, leftPanel, rightPanel);
        splitPane.setDividerLocation(150);
        splitPane.setDividerSize(5);
        splitPane.setOneTouchExpandable(true);

        this.add(splitPane);
        this.revalidate();
        this.repaint();

    }
	/**
     * Configura el frame, su apariencia y añade la información deseada
     * @param signature la firma digital, con datos sobre el firmante
     * @param panel El panel sobre el que se imprimirán los datos
     * @param verificado Mostrará en verde el texto si es verificado y en rojo si no
     * @param firma	Mostrará un JDialog con la firma digital
     * @param clavePublica Mostrará un JDialog con la clave pública
     * @param certificado Mostrará un JDialog con el certificado digital
     */
    public static void mostrarPanelDerecha(PDSignature signature, JPanel panel, Boolean verificado, byte[] firma, byte[] clavePublica, X509Certificate certificado) {
    	
    	panel.removeAll();
    	
        panel.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();

        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 0;
        gbc.gridy = GridBagConstraints.RELATIVE;
        gbc.weightx = 1.0;
        gbc.insets = new Insets(5,5,5,5);

        JLabel titulo = new JLabel("Verificación de Firma", SwingConstants.CENTER);
        titulo.setFont(new Font("Arial", Font.BOLD, 16));
        titulo.setBorder(BorderFactory.createEmptyBorder(5,5,5,5));
        panel.add(titulo, gbc);
        
        JLabel alg = new JLabel("Algoritmo: " + certificado.getSigAlgName(), SwingConstants.CENTER);
        alg.setFont(new Font("Arial", Font.BOLD, 14));
        alg.setBorder(BorderFactory.createEmptyBorder(5,5,5,5));
        panel.add(alg, gbc);
        
        JLabel fecha = new JLabel(signature.getSignDate().getTime().toString(), SwingConstants.CENTER);
        fecha.setFont(new Font("Arial", Font.BOLD, 14));
        fecha.setBorder(BorderFactory.createEmptyBorder(5,5,5,5));
        panel.add(fecha, gbc);
        
        String estadoVerificacion = verificado ? "Verificado" : "No Verificado";
        JLabel estadoLabel = new JLabel(estadoVerificacion, SwingConstants.CENTER);
        estadoLabel.setFont(new Font("Arial", Font.BOLD, 16));
        estadoLabel.setForeground(verificado ? Color.GREEN : Color.RED);
        panel.add(estadoLabel, gbc);

        JButton infoFirmaButtn = new JButton("Ver Info Firma");
        JButton firmaButton = new JButton("Ver Firma");
        JButton clavePublicaButton = new JButton("Ver Clave");
        JButton certificadoButton = new JButton("Ver Certificado");

        JPanel infoPanel = createPanelWithButton("Info Firma: ", infoFirmaButtn);
        JPanel firmaPanel = createPanelWithButton("Firma: ", firmaButton);
        JPanel clavePublicaPanel = createPanelWithButton("Clave pública: ", clavePublicaButton);
        JPanel certificadoPanel = createPanelWithButton("Certificado: ", certificadoButton);

        panel.add(infoPanel, gbc);
        panel.add(firmaPanel, gbc);
        panel.add(clavePublicaPanel, gbc);
        panel.add(certificadoPanel, gbc);

        JButton btnAceptar = new JButton("Aceptar");
        btnAceptar.setFont(new Font("Arial", Font.PLAIN, 14));
        btnAceptar.setBackground(Color.LIGHT_GRAY);
        btnAceptar.setFocusPainted(false);
        panel.add(btnAceptar, gbc);

        btnAceptar.addActionListener(e -> ((JFrame) panel.getTopLevelAncestor()).dispose());
        
        String infoString = "Fecha de firma: " + signature.getSignDate().getTime().toString() + 
        					"\nAutor: " + signature.getName() + 
        					"\nUbicación: " + signature.getLocation() + 
        					"\nRazón: " + signature.getReason() +
        					"\nFecha de revocación: " + certificado.getNotAfter().toString();

        infoFirmaButtn.addActionListener(e -> mostrarContenido("Información de la Firma", infoString));
        firmaButton.addActionListener(e -> mostrarContenido("Firma", Arrays.toString(firma)));
        clavePublicaButton.addActionListener(e -> mostrarContenido("Clave Pública", Arrays.toString(clavePublica)));
        certificadoButton.addActionListener(e -> mostrarContenido("Certificado", certificado != null ? certificado.toString() : "Certificado no disponible"));

        panel.revalidate();
        panel.repaint();
    }
    /**
     * Método que introduce y configura la apariencia del botón que abre los JDialog
     * @param labelText El texto que llevará el botón
     * @param button El botón
     * @return panel el panel configurando la apariencia
     */
    private static JPanel createPanelWithButton(String labelText, JButton button) {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.NONE;
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.CENTER;

        JLabel label = new JLabel(labelText);
        label.setFont(new Font("Arial", Font.PLAIN, 14));
        panel.add(label, gbc);

        gbc.gridx = 1; // Move to the next cell
        panel.add(button, gbc);

        button.setFont(new Font("Arial", Font.PLAIN, 14));
        button.setBackground(Color.LIGHT_GRAY);
        button.setFocusPainted(false);

        return panel;
    }

    /**
     * Método que muestra el JDialog con sus componentes
     * @param titulo El titulo del JDialog
     * @param contenido El contenido del JDialog
     */
    public static void mostrarContenido(String titulo, String contenido) {
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
