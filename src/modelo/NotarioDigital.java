package modelo;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.geom.Rectangle2D;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.GeneralSecurityException;
import java.security.PrivateKey;
import java.security.cert.Certificate;
import java.util.Calendar;
import java.awt.event.ActionEvent;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenuBar;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.WindowConstants;
import javax.swing.filechooser.FileNameExtensionFilter;

import org.apache.pdfbox.io.IOUtils;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDDocumentCatalog;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.PDPageTree;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.apache.pdfbox.pdmodel.interactive.action.PDAction;
import org.apache.pdfbox.pdmodel.interactive.action.PDActionFactory;
import org.apache.pdfbox.pdmodel.interactive.action.PDAnnotationAdditionalActions;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAnnotationWidget;
import org.apache.pdfbox.pdmodel.interactive.digitalsignature.PDSignature;
import org.apache.pdfbox.pdmodel.interactive.digitalsignature.SignatureInterface;
import org.apache.pdfbox.pdmodel.interactive.digitalsignature.SignatureOptions;
import org.apache.pdfbox.pdmodel.interactive.digitalsignature.visible.PDVisibleSigProperties;
import org.apache.pdfbox.pdmodel.interactive.digitalsignature.visible.PDVisibleSignDesigner;
import org.apache.pdfbox.pdmodel.interactive.form.PDAcroForm;
import org.apache.pdfbox.pdmodel.interactive.form.PDSignatureField;
import org.icepdf.core.exceptions.PDFException;
import org.icepdf.core.exceptions.PDFSecurityException;
import org.icepdf.core.pobjects.Document;
import org.icepdf.core.pobjects.Page;
import org.icepdf.core.pobjects.acroform.SignatureDictionary;
import org.icepdf.core.pobjects.acroform.SignatureFieldDictionary;
import org.icepdf.core.pobjects.acroform.SignatureHandler;
import org.icepdf.core.pobjects.acroform.signature.DigitalSignatureFactory;
import org.icepdf.core.pobjects.acroform.signature.SignatureSigner;
import org.icepdf.core.util.GraphicsRenderingHints;
import org.icepdf.ri.common.SwingController;
import org.icepdf.ri.common.SwingViewBuilder;
import org.icepdf.ri.common.views.AnnotationComponent;
import org.icepdf.ri.common.views.DocumentViewController;
import org.icepdf.ri.common.views.DocumentViewControllerImpl;
import org.icepdf.ri.common.views.DocumentViewModel;
import org.icepdf.core.pobjects.annotations.AbstractWidgetAnnotation;
import org.icepdf.core.pobjects.annotations.Annotation;
import org.icepdf.core.pobjects.annotations.SignatureWidgetAnnotation;
import org.icepdf.core.pobjects.annotations.SquareAnnotation;
import org.icepdf.core.pobjects.annotations.WidgetAnnotation;
import org.icepdf.core.pobjects.AbstractStringObject;

import com.formdev.flatlaf.FlatLightLaf;
import com.itextpdf.kernel.geom.Rectangle;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfName;
import com.itextpdf.kernel.pdf.PdfPage;
import com.itextpdf.kernel.pdf.PdfReader;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.kernel.pdf.StampingProperties;
import com.itextpdf.signatures.PdfSignatureAppearance;
import com.itextpdf.signatures.PdfSigner;
import com.itextpdf.signatures.PrivateKeySignature;
import com.itextpdf.signatures.IExternalSignature;
import com.itextpdf.signatures.IExternalSignatureContainer;
import com.itextpdf.signatures.BouncyCastleDigest;
import com.itextpdf.signatures.ExternalBlankSignatureContainer;
import com.itextpdf.signatures.IExternalDigest;

import controlador.FirmaDigital;
import vista.ImagenFirma;
import vista.VisorPDF;

@SuppressWarnings("serial")
public class NotarioDigital extends JFrame {
	private Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
	private JMenuBar menu; // Menú de opciones para la pantalla
	private JMenu archivo, editar, ayuda, firma_visual, firma_rapida;
	private JMenuItem abrir, guardar, salir, verificar, visual2, visual3, visual5, rapida2, rapida3, rapida5,
			como_firmar, acerca_de;
	private static int pdf_cargado = 0; // 0 = No cargado | 1 = Cargado | 2 = Modificado
	private static File rutaPDF; // Objeto que usaremos para cargar despues el pdf
	private static PDDocument doc;
	private static FirmaDigital firmaDigital;
	private SwingController controller;
	private static VisorPDF visor;
	private final static String dir = System.getProperty("user.dir");

	public NotarioDigital() {
		try {
			UIManager.setLookAndFeel(new FlatLightLaf());
			this.setTitle("Notario Digital");
			this.setSize(650, 500);
			this.setLocation(dim.width / 2 - this.getSize().width / 2, dim.height / 2 - this.getSize().height / 2);
			this.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);

			/* CODIGO SOBRE EL MENÚ Y SUS OPCIONES */
			menu = new JMenuBar();

			archivo = new JMenu("Archivo");
			abrir = new JMenuItem("Abrir");
			guardar = new JMenuItem("Guardar");
			salir = new JMenuItem("Salir");
			archivo.add(abrir);
			archivo.add(guardar);
			archivo.add(salir);

			editar = new JMenu("Editar");
			verificar = new JMenuItem("Verificar");
			firma_visual = new JMenu("Firma Visual");
			firma_rapida = new JMenu("Firma Automática");
			verificar.setEnabled(false);
			firma_visual.setEnabled(false);
			firma_rapida.setEnabled(false);
			editar.add(firma_visual);
			editar.add(firma_rapida);
			editar.add(verificar);
			visual2 = new JMenuItem("Dilithium 2");
			firma_visual.add(visual2);
			visual3 = new JMenuItem("Dilithium 3");
			firma_visual.add(visual3);
			visual5 = new JMenuItem("Dilithium 5");
			firma_visual.add(visual5);
			rapida2 = new JMenuItem("Dilithium 2");
			firma_rapida.add(rapida2);
			rapida3 = new JMenuItem("Dilithium 3");
			firma_rapida.add(rapida3);
			rapida5 = new JMenuItem("Dilithium 5");
			firma_rapida.add(rapida5);

			ayuda = new JMenu("Ayuda");
			como_firmar = new JMenuItem("Cómo Firmar");
			acerca_de = new JMenuItem("Acerca De...");
			ayuda.add(como_firmar);
			ayuda.add(acerca_de);

			menu.add(archivo);
			menu.add(editar);
			menu.add(ayuda);
		} catch (Exception e) {
			e.printStackTrace();
		}

		/* ACCIONES DE LOS BOTONES */

		// ARCHIVO
		abrir.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				// Si ya hay un pdf cargado SIN GUARDAR (pdf_cargado == 2 es que está
				// modificado) hay que cerrarlo para abrir otro
				if (pdf_cargado == 2) {
					int option = JOptionPane.showConfirmDialog(null,
							"Un PDF ha sido modificado sin guardar cambios. ¿Desea guardar antes de cerrarlo?");
					if (option == 0) {
						try {
							guardar();
							pdf_cargado = 0;
						} catch (Exception ex) {
							JOptionPane.showMessageDialog(null,
									"No se ha podido guardar el PDF. Comprueba que tiene permisos para escribir en la ruta indicada.\n"
											+ ex.getMessage(),
									"Error", JOptionPane.ERROR_MESSAGE);
						}
					}
				}

				JFileChooser selector = new JFileChooser();
				FileNameExtensionFilter filter = new FileNameExtensionFilter("Archivos PDF", "pdf");
				selector.setFileFilter(filter);
				int result = selector.showOpenDialog(null);
				if (result == JFileChooser.APPROVE_OPTION) {
					rutaPDF = selector.getSelectedFile();
					try {
						// Cerrar el documento PDF anterior si está cargado
						if (doc != null) {
							doc.close();
							visor.removeAll();
						}

						doc = PDDocument.load(rutaPDF);
						if (doc != null) {
							pdf_cargado = 1;
						}

						// Cargar el visor web
						verificar.setEnabled(true);
						firma_visual.setEnabled(true);
						firma_rapida.setEnabled(true);
						// Inicializar el controlador de Swing
						controller = new SwingController();
						visor = new VisorPDF(controller, rutaPDF);

						// Agregar el componente de visualización al marco

						getContentPane().add(visor, BorderLayout.CENTER);
						visor.cargarPDF();
					} catch (IOException ex) {
						ex.printStackTrace();
					}
				}
			}
		});

		guardar.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (pdf_cargado == 0) {
					JOptionPane.showMessageDialog(null, "No se ha cargado ningún PDF", "Guardar",
							JOptionPane.INFORMATION_MESSAGE);
				} else {
					try {
						guardar();
					} catch (Exception ex) {
						JOptionPane.showMessageDialog(null,
								"No se ha podido guardar el PDF. Comprueba que tiene permisos para escribir en la ruta indicada.\n"
										+ ex.getMessage(),
								"Error", JOptionPane.ERROR_MESSAGE);
					}
				}
			}
		});

		salir.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				salir();
			}
		});

		// ACCIONES DE EDITAR
		firma_visual.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (pdf_cargado == 1) {
					/**
					 * TODO Aqui se puede usar el objeto scene para pintar la firma Se usa el objeto
					 * "controlador" que debe estar inicializado en "abrir" No debería haber error
					 * porque sólo se ejecuta este código cuando se abre un pdf-> COMPROBAR!
					 */

					/*
					 * try { BufferedImage image = ImageIO.read(new
					 * File("C:\\Users\\David\\Pictures\\GBKuCm5b0AAgHOA.jpg")); String keystorePath
					 * = "ruta/al/almacen_de_claves.p12"; String keystorePassword =
					 * "contraseña_del_almacén"; String keyAlias = "alias_de_la_clave"; String
					 * keyPassword = "contraseña_de_la_clave"; PDVisibleSignDesigner
					 * visibleSignDesigner; visibleSignDesigner = new
					 * PDVisibleSignDesigner(rutaPDF.toString(),image,1); PDVisibleSigProperties
					 * visibleSigProperties = new PDVisibleSigProperties();
					 * visibleSigProperties.signerName("Nombre del firmante").
					 * signerLocation("Ubicación del firmante")
					 * .signatureReason("Razón de la firma").preferredSize(0).page(1).
					 * visualSignEnabled(true)
					 * .setPdVisibleSignature(visibleSignDesigner).buildSignature(); } catch
					 * (IOException e1) { // TODO Auto-generated catch block e1.printStackTrace(); }
					 */

					visor.firmaPDF();
					// Lógica APACHE PDFBox

					PDPage paginaPDF = doc.getPage(visor.getController().getCurrentPageNumber());

				} else {
					JOptionPane.showMessageDialog(null, "No se ha cargado ningún PDF.", "Error",
							JOptionPane.ERROR_MESSAGE);
				}
			}

		});

		rapida2.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				llamadaFirma(2);
			}
		});
		rapida3.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				llamadaFirma(3);
			}
		});
		rapida5.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				llamadaFirma(5);
			}
		});

		verificar.addActionListener(new ActionListener() {
			// TODO
			public void actionPerformed(ActionEvent e) {
				if (pdf_cargado != 0) {
					if (firmaDigital.verificar()) {
						System.out.println("Firma verificada!!!");
					} else {
						System.out.println("Algo está mal");
					}

				} else {
					JOptionPane.showMessageDialog(null, "No se ha cargado ningún PDF.", "Error",
							JOptionPane.ERROR_MESSAGE);
				}
			}
		});

		// ACCIONES DE AYUDA
		como_firmar.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				// TODO Escribir texto Como firmar
				JOptionPane.showMessageDialog(null,
						"Para utilizar las funcionalidades de firma y verificación deberá haberse cargado un PDF.",
						"Como firmar", JOptionPane.INFORMATION_MESSAGE);

			}
		});

		acerca_de.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				// TODO Escribir texto Acerca De....
				JOptionPane.showMessageDialog(null,
						"Notario Digital es una aplicación de escritorio destinada a la firma y verificación de documentos digitales\ncon el uso del algoritmo Dilithium, finalista del proceso del NIST \"Post-Quantum Cryptography\".\nEsta aplicación ha sido desarrollada por David García Diez, como parte de su Trabajo de Fin\nde Grado en la Universidad de León.",
						"Acerca de...", JOptionPane.INFORMATION_MESSAGE);
			}

		});

		this.setJMenuBar(menu);
		// TODO REVISAR
		addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				salir();
			}
		});

	}

	/**
	 * Guarda cambios en un PDF. Escribe el PDF en la ruta indicada
	 * 
	 * @return 0 si se ha podido guardar correctamente, 1 si ha habido errores
	 * @throws IOException
	 */
	public static int guardar() throws IOException {
		if (pdf_cargado != 0) {
			JFileChooser select_guardar = new JFileChooser();
			select_guardar.setDialogTitle("Guardar");
			FileNameExtensionFilter filter = new FileNameExtensionFilter("Archivos PDF", "pdf");
			select_guardar.setFileFilter(filter);
			select_guardar.setSelectedFile(rutaPDF);
			int result = select_guardar.showOpenDialog(null);
			if (result == JFileChooser.APPROVE_OPTION) {
				File ruta = select_guardar.getSelectedFile();
				doc.save(ruta);
				pdf_cargado = 0;
				return 0;
			} else {
				return 1;
			}
		} else {
			JOptionPane.showMessageDialog(null, "No se ha cargado ningún PDF");
			return 1;
		}

	}

	public static void salir() {
		// Si hay un PDF modificado sin guardar, se preguntará antes de salir
		if (pdf_cargado == 2) {
			int option = JOptionPane.showOptionDialog(null,
					"Un PDF ha sido modificado sin guardar cambios. ¿Desea guardar antes de salir?",
					"Archivo Modificado", JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE, null,
					new String[] { "Guardar", "No Guardar", "Cancelar" }, "Guardar");
			switch (option) {
			case JOptionPane.YES_OPTION:
				try {
					guardar();
					System.exit(0);
				} catch (IOException e) {
					e.printStackTrace();
				}
				break;
			case JOptionPane.NO_OPTION:
				System.exit(0);
				break;
			case JOptionPane.CANCEL_OPTION:
				break;
			case JOptionPane.CLOSED_OPTION:
				break;
			}
		} else {
			System.exit(0);
		}
	}

	public static void llamadaFirma(int nivelSeguridad) {
		firmaDigital = new FirmaDigital(doc, nivelSeguridad);
		PrivateKey privateKey = firmaDigital.getPrivateKey();
		Certificate certificado = firmaDigital.getCertificado();
		doc = firmaDigital.getPDDocument();
		visor.repaint();
		visor.validate();
		
		pdf_cargado = 2; // Modificado(para que pregunte por guardar)
	}
}