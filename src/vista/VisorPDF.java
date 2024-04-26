package vista;

import java.awt.BorderLayout;
import java.io.File;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import com.formdev.flatlaf.FlatLightLaf;

import org.icepdf.ri.common.SwingController;
import org.icepdf.ri.common.SwingViewBuilder;
import org.icepdf.ri.common.views.*;
import org.icepdf.core.exceptions.PDFException;
import org.icepdf.core.exceptions.PDFSecurityException;
import org.icepdf.core.pobjects.Document;
import org.icepdf.core.util.GraphicsRenderingHints;

public class VisorPDF extends JPanel{

	private File rutaPDF;
	private SwingController controller;
	private SwingViewBuilder factory;
	private JPanel viewerComponentPanel;
	
	
	public VisorPDF(SwingController controller, File documento){
		try {
			UIManager.setLookAndFeel(new FlatLightLaf());
		} catch (UnsupportedLookAndFeelException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		this.controller = controller;
		this.rutaPDF = documento;
		this.factory = new SwingViewBuilder(this.controller);
		viewerComponentPanel = factory.buildViewerPanel();
		setLayout(new BorderLayout());
		add(viewerComponentPanel, BorderLayout.CENTER);
		configurarVistaPredeterminada();
	}
	
	public void cargarPDF() {
		this.controller.openDocument(this.rutaPDF.toString());
        this.controller.getDocumentViewController().setAnnotationCallback(
                new org.icepdf.ri.common.MyAnnotationCallback(
                        controller.getDocumentViewController()));
	}
	
	private void configurarVistaPredeterminada() {
        // Configurar el modo de vista predeterminado
        //controller.setPageViewMode(PageViewCo, false);
    }
}
