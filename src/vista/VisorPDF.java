package vista;

import java.awt.BorderLayout;
import java.io.File;

import javax.swing.JPanel;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import com.formdev.flatlaf.FlatLightLaf;

import org.icepdf.ri.common.ComponentKeyBinding;
import org.icepdf.ri.common.SwingController;
import vista.SwingViewBuilder;

@SuppressWarnings("serial")
public class VisorPDF extends JPanel {

	private File rutaPDF;
	private SwingController controller;
	private SwingViewBuilder factory;
	private JPanel panelPDF;

	public VisorPDF(SwingController controller, File documento) {
		try {
			UIManager.setLookAndFeel(new FlatLightLaf());
		} catch (UnsupportedLookAndFeelException e) {
			e.printStackTrace();
		}

		this.controller = controller;
		this.rutaPDF = documento;
		this.factory = new SwingViewBuilder(this.controller);
		this.controller.setToolBarVisible(false);
		configurarVistaPredeterminada();
		panelPDF = factory.buildViewerPanel();

		panelPDF.setBounds(0, 0, getWidth(), getHeight());
		setLayout(new BorderLayout());
		this.add(panelPDF);
	}

	public SwingController getController() {
		return this.controller;
	}

	public SwingViewBuilder getFactory() {
		return this.factory;
	}

	public void setDocumento(File rutaPDF) {
		this.rutaPDF = rutaPDF;
		this.controller.closeDocument();
		this.controller.openDocument(this.rutaPDF.toString());
	}

	public void cargarPDF() {
		this.controller.openDocument(this.rutaPDF.toString());
		this.controller.getDocumentViewController().setAnnotationCallback(
				new org.icepdf.ri.common.MyAnnotationCallback(controller.getDocumentViewController()));
	}

	private void configurarVistaPredeterminada() {
		// Configurar el modo de vista predeterminado
		this.controller.setPageViewMode(ABORT, getVerifyInputWhenFocusTarget());
		ComponentKeyBinding.install(this.controller, this);
		this.controller.setToolBarVisible(false);
		this.controller.getDocumentViewController().setAnnotationCallback(null);
	}
}
