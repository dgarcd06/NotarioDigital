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
/**
 * La clase que carga la visualización del PDF.
 * @author David García Diez
 */
public class VisorPDF extends JPanel {

	private File rutaPDF;
	private SwingController controller;
	private SwingViewBuilder factory;
	private JPanel panelPDF;
	/**
	 * Prepara la configuración del visor con el archivo que se le pasa
	 * @param controller este objeto se encarga de configurar el comportamiento del visor PDF
	 * @param documento El archivo que se va a abrir
	 */
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
	/**
	 * Devuelve el controller
	 * @return el SwingController
	 */
	public SwingController getController() {
		return this.controller;
	}
	/**
	 * Devuelve el SwingViewBuilder
	 * @return SwingViewBuilder
	 */
	public SwingViewBuilder getFactory() {
		return this.factory;
	}
	/**
	 * Para modificar el archivo que carga el visor
	 * @param rutaPDF el archivo que se va a cargar
	 */
	public void setDocumento(File rutaPDF) {
		this.rutaPDF = rutaPDF;
		this.controller.closeDocument();
		this.controller.openDocument(this.rutaPDF.toString());
	}
	/**
	 * Método que carga el archivo PDF en el visor.
	 */
	public void cargarPDF() {
		this.controller.openDocument(this.rutaPDF.toString());
		this.controller.getDocumentViewController().setAnnotationCallback(
				new org.icepdf.ri.common.MyAnnotationCallback(controller.getDocumentViewController()));
	}
	/**
	 * Método que configura ciertos valores del visor.
	 */
	private void configurarVistaPredeterminada() {
		// Configurar el modo de vista predeterminado
		this.controller.setPageViewMode(ABORT, getVerifyInputWhenFocusTarget());
		ComponentKeyBinding.install(this.controller, this);
		this.controller.setToolBarVisible(false);
		this.controller.getDocumentViewController().setAnnotationCallback(null);
	}
}
