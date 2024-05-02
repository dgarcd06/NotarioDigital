package vista;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;

import javax.swing.JButton;
import javax.swing.JLayeredPane;
import javax.swing.JPanel;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import com.formdev.flatlaf.FlatLightLaf;

import org.icepdf.ri.common.SwingController;
import org.icepdf.ri.common.SwingViewBuilder;
import org.icepdf.ri.common.utility.signatures.SignaturesHandlerPanel;
import org.icepdf.ri.common.views.*;
import org.icepdf.core.exceptions.PDFException;
import org.icepdf.core.exceptions.PDFSecurityException;
import org.icepdf.core.pobjects.Document;
import org.icepdf.core.util.GraphicsRenderingHints;

public class VisorPDF extends JLayeredPane{

	private File rutaPDF;
	private SwingController controller;
	private SwingViewBuilder factory;
	private JPanel panelPDF, panelFirma;
	private int startX, startY, endX, endY;
	private Rectangle selectionRect;
	
	public VisorPDF(SwingController controller, File documento){
		try {
			UIManager.setLookAndFeel(new FlatLightLaf());
		} catch (UnsupportedLookAndFeelException e) {
			e.printStackTrace();
		}
		this.controller = controller;
		this.rutaPDF = documento;
		this.factory = new SwingViewBuilder(this.controller);
		panelPDF = factory.buildViewerPanel();
		panelFirma = new JPanel();
		panelFirma.setBackground(new Color(143, 151, 43, 60));
		
        
		panelPDF.setBounds(0, 0, getWidth(), getHeight());
		setLayout(new BorderLayout());
		add(panelFirma,BorderLayout.CENTER);
	    addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
            	panelPDF.setBounds(0, 0, getWidth(), getHeight());
            	panelFirma.setBounds(0, 0, getWidth(), getHeight());
            }
        });
	    setComponentZOrder(panelPDF, 0);
        setComponentZOrder(panelFirma, 1);
		configurarVistaPredeterminada();
	}
	
	public SwingController getController() {
		return this.controller;
	}
	
	public SwingViewBuilder getFactory() {
		return this.factory;
	}
	public void cargarPDF() {
		this.controller.openDocument(this.rutaPDF.toString());
        this.controller.getDocumentViewController().setAnnotationCallback(
                new org.icepdf.ri.common.MyAnnotationCallback(
                        controller.getDocumentViewController()));
	}
	
	private void configurarVistaPredeterminada() {
        // Configurar el modo de vista predeterminado
        this.controller.setPageViewMode(ABORT, getVerifyInputWhenFocusTarget());
        this.controller.setToolBarVisible(true);
        this.controller.getDocumentViewController().setAnnotationCallback(null);
        panelFirma.setVisible(false);
        DocumentViewModel documentViewModel = controller.getDocumentViewController().getDocumentViewModel();

     
        
    }
	public void firmaPDF() {
		//Ocultar la barra de herramientas y cambiar el color a un grisácreo
		 this.controller.setToolBarVisible(false);
		panelFirma.setVisible(true);
		panelFirma.addMouseListener(new MouseAdapter() {
	            @Override
	            public void mouseClicked(MouseEvent e) {
	                // Obtener las coordenadas del clic del mouse
	                int mouseX = e.getX();
	                int mouseY = e.getY();
	                
	                // Imprimir las coordenadas del clic del mouse en el panel de firma
	                System.out.println("Coordenadas del clic del mouse: (" + mouseX + ", " + mouseY + ")");
	            }
	        });
	}
	/*protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g.create();
        g2d.setColor(Color.RED); // Color del rectángulo de selección
        g2d.setStroke(new BasicStroke(2)); // Grosor del borde del rectángulo
        g2d.draw(selectionRect); // Dibujar el rectángulo de selección
        g2d.dispose();
    }*/
	
	public void setCursorFirma() {
		
	}
	public void setCursorDefault() {
		setCursor(null);
	}
}
