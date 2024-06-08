package vista;

import java.awt.BorderLayout;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.security.cert.X509Certificate;

import javax.swing.JPanel;
import javax.swing.JTextField;
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
	private Boolean firmaVerificada;
	private byte[] firma, clavePublica;
	X509Certificate certificado;
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
		cargarPDF();
	}
	/**
	 * Método que carga el archivo PDF en el visor.
	 * El CustomAnnotationCallback personaliza el comportamiento al clickar en una firma
	 */
	public void cargarPDF() {
		this.controller.openDocument(this.rutaPDF.toString());
		this.controller.setCurrentPageNumberTextField(new JTextField("Pagina:"+ controller.getCurrentPageNumber() + "/" + controller.getPageTree().getNumberOfPages()));
		if(this.certificado == null || this.firma == null || this.clavePublica == null) {
			this.controller.getDocumentViewController().setAnnotationCallback(
					new org.icepdf.ri.common.MyAnnotationCallback(controller.getDocumentViewController()));
		}else {
			this.controller.getDocumentViewController().setAnnotationCallback(
					new CustomAnnotationCallback(controller.getDocumentViewController(),this.firmaVerificada,this.firma,this.clavePublica,this.certificado));
		}
		
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
	public void getPropiedadesFirma(Boolean firmaVerificada, byte[] firma, byte[] clavePublica,
			X509Certificate certificado) {
		this.firmaVerificada = firmaVerificada;
		this.firma = firma;
		this.clavePublica = clavePublica;
		this.certificado = certificado;
	}
}
