package controlador;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.Security;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.SwingWorker;
import javax.swing.TransferHandler;
import javax.swing.UIManager;
import javax.swing.filechooser.FileNameExtensionFilter;

import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.examples.signature.SigUtils;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.interactive.digitalsignature.ExternalSigningSupport;
import org.apache.pdfbox.pdmodel.interactive.digitalsignature.PDSignature;
import org.apache.pdfbox.pdmodel.interactive.digitalsignature.SignatureInterface;
import org.apache.pdfbox.pdmodel.interactive.digitalsignature.SignatureOptions;
import org.apache.pdfbox.pdmodel.interactive.digitalsignature.visible.PDVisibleSigProperties;
import org.apache.pdfbox.pdmodel.interactive.digitalsignature.visible.PDVisibleSignDesigner;
import org.apache.pdfbox.pdmodel.interactive.form.PDAcroForm;
import org.apache.pdfbox.pdmodel.interactive.form.PDField;
import org.apache.pdfbox.pdmodel.interactive.form.PDSignatureField;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.pqc.jcajce.provider.BouncyCastlePQCProvider;
import org.icepdf.ri.common.SwingController;

import com.formdev.flatlaf.FlatLightLaf;

import modelo.FirmaDigital;
import vista.FrameVerificacion;
import vista.FrameVisual;
import vista.ImagenFirma;
import vista.VisorPDF;

/**
 * Frame principal de la aplicación. Aquí se coordinan las funciones lógicas de
 * Dilithium con el visor PDF.
 * 
 * @author David García Diez
 */
@SuppressWarnings("serial")
public class NotarioDigital extends JFrame {
	private Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
	// Menú de opciones para la pantalla con sus items
	private JMenuBar menu;
	private JMenu archivo, editar, ayuda, firmaVisual, firmaRapida;
	private JMenuItem abrir, guardar, salir, verificar, visual2, visual3, visual5, rapida2, rapida3, rapida5, comoUsar,
			acercaDe;
	private int pdfCargado = 0; // 0 = No cargado | 1 = Cargado | 2 = Modificado
	private static File rutaPDF; // Objeto que usaremos para cargar despues el pdf
	private static PDDocument doc; // Objeto de ApachePDFBox para todas las interacciones con el PDF
	private static FirmaDigital firmaDigital; // Para la parte de Modelo de la aplicación
	private static VisorPDF visor; // Para la parte de Vista de la aplicación
	private final static String dir = System.getProperty("user.dir"); // Variable para acceder a los recursos deld
	private JPanel panel;
	private static String archivo_output;
	static {
		Security.addProvider(new BouncyCastleProvider());
		Security.addProvider(new BouncyCastlePQCProvider());
	}

	public NotarioDigital() {
		try {
			UIManager.setLookAndFeel(new FlatLightLaf()); // Para mejorar el aspecto de Java Swing
			this.setTitle("Notario Digital");
			this.setSize(650, 500);
			this.setLocation(dim.width / 2 - this.getSize().width / 2, dim.height / 2 - this.getSize().height / 2);
			this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			this.setIconImage(Toolkit.getDefaultToolkit().getImage(dir + "\\recursos\\icono_jframe.png"));

			JLabel label = new JLabel("Arrastra un archivo aquí", SwingConstants.CENTER);
			label.setPreferredSize(new Dimension(300, 200));

			panel = new JPanel(new BorderLayout());
			panel.setTransferHandler(new FileTransferHandler());
			panel.setBorder(BorderFactory.createLineBorder(Color.BLACK));
			panel.add(label, BorderLayout.CENTER);
			setContentPane(panel);

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
			firmaVisual = new JMenu("Firma Visual");
			firmaRapida = new JMenu("Firma No Visual");
			verificar.setEnabled(false);
			firmaVisual.setEnabled(false);
			firmaRapida.setEnabled(false);
			editar.add(firmaVisual);
			editar.add(firmaRapida);
			editar.add(verificar);
			visual2 = new JMenuItem("Dilithium 2");
			firmaVisual.add(visual2);
			visual3 = new JMenuItem("Dilithium 3");
			firmaVisual.add(visual3);
			visual5 = new JMenuItem("Dilithium 5");
			firmaVisual.add(visual5);
			rapida2 = new JMenuItem("Dilithium 2");
			firmaRapida.add(rapida2);
			rapida3 = new JMenuItem("Dilithium 3");
			firmaRapida.add(rapida3);
			rapida5 = new JMenuItem("Dilithium 5");
			firmaRapida.add(rapida5);

			ayuda = new JMenu("Ayuda");
			comoUsar = new JMenuItem("Cómo Usar");
			acercaDe = new JMenuItem("Acerca De...");
			ayuda.add(comoUsar);
			ayuda.add(acercaDe);

			menu.add(archivo);
			menu.add(editar);
			menu.add(ayuda);
		} catch (Exception e) {
			e.printStackTrace();
		}

		/* ACCIONES DE LOS BOTONES */

		// Se abre un menú para seleccionar un archivo PDF y cargarlo
		abrir.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				JFileChooser selector = new JFileChooser();
				FileNameExtensionFilter filter = new FileNameExtensionFilter("Archivos PDF", "pdf");
				selector.setFileFilter(filter);
				int result = selector.showOpenDialog(null);
				if (result == JFileChooser.APPROVE_OPTION) {
					panel.removeAll(); // El panel de arrastrar archivos
					panel.setBorder(null);
					rutaPDF = selector.getSelectedFile();
					try {
						// Cerrar el documento PDF anterior si está cargado
						if (doc != null) {
							doc.close();
							visor.removeAll();
						}
						doc = PDDocument.load(rutaPDF);
						if (doc != null) {
							pdfCargado = 1;
							// Cargar el visor web
							verificar.setEnabled(true);
							firmaVisual.setEnabled(true);
							firmaRapida.setEnabled(true);
							// Inicializar el controlador de Swing
							visor = new VisorPDF(new SwingController(), rutaPDF);
							verificar(0);
							// Agregar el visor al Frame principal
							getContentPane().add(visor, BorderLayout.CENTER);
							visor.cargarPDF();
							revalidate();
							repaint();
						}
					} catch (IOException ex) {
						ex.printStackTrace();
						JOptionPane.showMessageDialog(null,
								"No se ha podido cargar el PDF, el formato es incorrecto o está corrupto.",
								"Error", JOptionPane.ERROR_MESSAGE);
					}
				}
			}
		});
		// Si está cargado, se puede guardar una copia del PDF
		guardar.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (pdfCargado == 0) {
					JOptionPane.showMessageDialog(null, "No se ha cargado ningún PDF", "Guardar",
							JOptionPane.INFORMATION_MESSAGE);
				} else {
					try {
						guardar(pdfCargado);
					} catch (Exception ex) {
						JOptionPane.showMessageDialog(null,
								"No se ha podido guardar el PDF. Comprueba que tiene permisos para escribir en la ruta indicada.\n"
										+ ex.getMessage(),
								"Error", JOptionPane.ERROR_MESSAGE);
					}
				}
			}
		});
		/**
		 * Termina el programa. Se pensó una comprobación si el PDF fue modificado, pero
		 * al firmar se crea un nuevo pdf con el sufijo "_firmado", por lo que no tiene
		 * sentido preguntar por guardar antes de cerrar.
		 */
		salir.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				System.exit(0);
			}
		});

		/**
		 * Visual corresponde a la posibilidad de seleccionar el área de la firma y
		 * rapida a una firma automática en el documento. 2, 3 y 5 se corresponde con
		 * los diferentes niveles de seguridad que incluye la implementación de
		 * Dilithium.
		 */
		visual2.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {

					FrameVisual panelFirma = new FrameVisual(NotarioDigital.this, visor.getWidth(), visor.getHeight(),
							getX() + 7, getY() + 55);
					// SwingWorker para esperar a la asincronía de la selección del área para firmar
					SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {
						@Override
						protected Void doInBackground() throws Exception {
							synchronized (panelFirma) {
								while (panelFirma.isVisible()) {
									panelFirma.wait();
								}
							}
							return null;
						}

						@Override
						protected void done() {
							if (panelFirma.getFirmaDeseada()) {
								try {
									firmaDocumento(2, panelFirma.getX(), panelFirma.getY(), panelFirma.getAncho(),
											panelFirma.getAlto());
									setPDFCargado(2); // Modificado(para que pregunte por guardar)
								} catch (IOException e1) {
									e1.printStackTrace();
								}
							} else {
								System.out.println("El usuario no confirmó el área seleccionada.");
							}
						}
					};

					worker.execute();

				
			}

		});
		visual3.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {

					FrameVisual panelFirma = new FrameVisual(NotarioDigital.this, visor.getWidth(), visor.getHeight(),
							getX() + 7, getY() + 55);
					// SwingWorker para esperar a la asincronía de la selección del área para firmar
					SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {
						@Override
						protected Void doInBackground() throws Exception {
							synchronized (panelFirma) {
								while (panelFirma.isVisible()) {
									panelFirma.wait();
								}
							}
							return null;
						}

						@Override
						protected void done() {
							if (panelFirma.getFirmaDeseada()) {
								try {
									firmaDocumento(3, panelFirma.getX(), panelFirma.getY(), panelFirma.getAncho(),
											panelFirma.getAlto());
									setPDFCargado(2); // Modificado(para que pregunte por guardar)
								} catch (IOException e1) {
									e1.printStackTrace();
								}
							} else {
								System.out.println("El usuario no confirmó el área seleccionada.");
							}
						}
					};

					worker.execute();

			}

		});
		visual5.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {

					FrameVisual panelFirma = new FrameVisual(NotarioDigital.this, visor.getWidth(), visor.getHeight(),
							getX() + 7, getY() + 55);
					// SwingWorker para esperar a la asincronía de la selección del área para firmar
					SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {
						@Override
						protected Void doInBackground() throws Exception {
							synchronized (panelFirma) {
								while (panelFirma.isVisible()) {
									panelFirma.wait();
								}
							}
							return null;
						}

						@Override
						protected void done() {
							if (panelFirma.getFirmaDeseada()) {
								try {
									firmaDocumento(5, panelFirma.getX(), panelFirma.getY(), panelFirma.getAncho(),
											panelFirma.getAlto());
									setPDFCargado(2); // Modificado(para que pregunte por guardar)
								} catch (IOException e1) {
									e1.printStackTrace();
								}
							} else {
								System.out.println("El usuario no confirmó el área seleccionada.");
							}
						}
					};

					worker.execute();

			}

		});

		rapida2.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {
						firmaAutomatica(2);
						setPDFCargado(2);
				} catch (IOException e1) {
					e1.printStackTrace();
				}
				revalidate();
			}
		});
		rapida3.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {
						firmaAutomatica(3);
						setPDFCargado(2);
				} catch (IOException e1) {
					e1.printStackTrace();
				}
				revalidate();
			}
		});
		rapida5.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {
						firmaAutomatica(5);
						setPDFCargado(2);
				} catch (IOException e1) {
					e1.printStackTrace();
				}
				revalidate();
			}
		});
		/**
		 * Si hay un PDF cargado se busca una firma en el documento. Si se encuentra, se
		 * abre un Frame (clase FrameVerificacion) que muestra la información de claves
		 * y afirma o niega la autoridad de la firma según la clave pública y la firma.
		 */
		verificar.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				verificar(1);
			}
		});

		// Estos dos últimos Items aportan información sobre el programa y su uso
		comoUsar.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				JOptionPane.showMessageDialog(null,
						"Para utilizar las funcionalidades de firma y verificación deberá haberse cargado un PDF.\n"
								+ "Puede elegir entre Firma No Visual, de manera que la firma se incluirá automáticamente en el documento, o bien seleccionar el área de firma con Firma Visual.\n"
								+ "Si selecciona Firma Visual, confirme el área seleccionado para añadir la firma en el documento. Puede pulsar la tecla ESC para cancelar la operación de firma.\n"
								+ "Dentro de las dos opciones, se puede seleccionar el nivel de seguridad de Dilithium, entre las opciones 2, 3 y 5.\n"
								+ "Al pulsar Verificación, se buscarán firmas en el documento. En el caso de existir una firma, se comprobará su veracidad a partir de la recogida de sus datos del documento.",
						"Como usar la aplicación", JOptionPane.INFORMATION_MESSAGE);

			}
		});

		acercaDe.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				JOptionPane.showMessageDialog(null,
						"Notario Digital es una aplicación de escritorio destinada a la firma y verificación de documentos digitales con el uso del algoritmo Dilithium, finalista del proceso del NIST \"Post-Quantum Cryptography\"."
								+ "\nEsta aplicación ha sido desarrollada por David García Diez, como parte de su Trabajo de Fin de Grado en la Universidad de León."
								+ "\nEl desarrollo de la aplicación tiene fines de investigación. Los certificados son generados durante el proceso de firma sin contar con una Autoridad de Certificación europea.",
						"Acerca de...", JOptionPane.INFORMATION_MESSAGE);
			}
		});
		this.setJMenuBar(menu);
	}

	/**
	 * Modifica el estado del PDF cargado
	 * 
	 * @param estadoPDF el estado en el que se quiere indicar al PDF 0 = No hay
	 *                  ningún PDF cargado 1 = Hay un PDF cargado 2 = Hay un PDF
	 *                  cargado que ha sido firmado (modificado)
	 */
	public void setPDFCargado(int estadoPDF) {
		this.pdfCargado = estadoPDF;
	}

	/**
	 * Guarda cambios en un PDF. Escribe el PDF en la ruta indicada
	 * 
	 * @param pdfCargado Se pasa por parametro el estado de carga del pdf
	 * @return 0 si se ha podido guardar correctamente, 1 si ha habido errores
	 * @throws IOException En el método save de ApachePDFBox
	 */
	public static int guardar(int pdfCargado) throws IOException {
		if (pdfCargado != 0 && doc != null) {
			JFileChooser select_guardar = new JFileChooser();
			select_guardar.setDialogTitle("Guardar");
			FileNameExtensionFilter filter = new FileNameExtensionFilter("Archivos PDF", "pdf");
			select_guardar.setFileFilter(filter);
			select_guardar.setSelectedFile(rutaPDF);
			int result = select_guardar.showOpenDialog(null);
			if (result == JFileChooser.APPROVE_OPTION) {
				File ruta = select_guardar.getSelectedFile();
				doc.save(ruta);
				pdfCargado = 0;
				return 0;
			} else {
				return 1;
			}
		} else {
			JOptionPane.showMessageDialog(null, "No se ha cargado ningún PDF");
			return 1;
		}

	}

	/**
	 * Método que se encarga de generar las claves, firma y certificado digital con
	 * la llamada a la clase FirmaDigital. Posteriormente crea la interfaz de la
	 * firma con la librería Apache PDFBox, y añade la firma en el PDF con los datos
	 * generados. El PDF original no es modificado, sino que se genera uno nuevo con
	 * la firma. Posteriormente se muestra el nuevo PDF generado.
	 * 
	 * @param nivelSeguridad El nivel de seguridad de Dilithium. Puede ser 2,3 o 5,
	 *                       y será necesario para la generación de las claves y de
	 *                       la firma al llamar a la clase FirmaDigital.
	 * @param x              La posición en x de la imagen de firma que se inserta
	 *                       en el documento.
	 * @param y              La posición en y de la imagen de firma que se inserta
	 *                       en el documento.
	 * @param width          El tamaño en anchura que tendrá la imagen de la firma.
	 *                       Se usa para su creación en la llamada a ImagenFirma.
	 * @param height         El tamaño en altura que tendrá la imamgen de la firma.
	 * @throws IOException Cubre posibles excepciones en varias llamadas de métodos
	 *                     de ApachePDBox, de la clase FirmaDigital y de la clase
	 *                     FileOutputStream al generar el archivo firmado.
	 */
	public void firmaDocumento(int nivelSeguridad, int x, int y, int width, int height) throws IOException {
		firmaDigital = new FirmaDigital(nivelSeguridad);
		new ImagenFirma("David García Diez", nivelSeguridad, width, height);
		archivo_output = rutaPDF.getAbsolutePath().substring(0, rutaPDF.getAbsolutePath().lastIndexOf("."));
		archivo_output = archivo_output + "_firmado.pdf";
		try (FileOutputStream archivoOutput = new FileOutputStream(archivo_output)) {
			byte[] codigoFirma = firmaDigital.codigoFirma(nivelSeguridad); // El código CMS de la firma digital

			// Creamos el elemento visual de firma
			PDVisibleSignDesigner visibleSignDesigner = new PDVisibleSignDesigner(doc,
					new FileInputStream(dir + "\\recursos\\firma.png"),
					visor.getController().getCurrentPageNumber() + 1);
			visibleSignDesigner.xAxis(x).yAxis(y).zoom(0).adjustForRotation();

			// Propiedades de la firma
			PDVisibleSigProperties visibleSignatureProperties = new PDVisibleSigProperties();
			visibleSignatureProperties.signerName("David García Diez").signerLocation("Universidad de León")
					.signatureReason("Firma con Dilithium").preferredSize(50)
					.page(visor.getController().getCurrentPageNumber() + 1).visualSignEnabled(true)
					.setPdVisibleSignature(visibleSignDesigner);

			int accessPermissions = SigUtils.getMDPPermission(doc);
			if (accessPermissions == 1) {
				throw new IllegalStateException(
						"No changes to the document are permitted due to DocMDP transform parameters dictionary");
			}
			PDSignature signature = new PDSignature();
			if (doc.getVersion() >= 1.5f && accessPermissions == 0) {
				SigUtils.setMDPPermission(doc, signature, 2);
			}
			PDAcroForm acroForm = doc.getDocumentCatalog().getAcroForm(null);
			if (acroForm != null && acroForm.getNeedAppearances()) {
				if (acroForm.getFields().isEmpty()) {
					acroForm.getCOSObject().removeItem(COSName.NEED_APPEARANCES);
				} else {
					System.out.println("/NeedAppearances is set, signature may be ignored by Adobe Reader");
				}
			}

			signature.setFilter(PDSignature.FILTER_ADOBE_PPKLITE);
			signature.setSubFilter(PDSignature.SUBFILTER_ADBE_PKCS7_DETACHED);
			visibleSignatureProperties.buildSignature();
			signature.setName("David García Diez");
			signature.setLocation("Universidad de León");
			signature.setReason("Análisis de Algoritmos Post-Cuánticos");
			signature.setSignDate(Calendar.getInstance());
			signature.setContents(firmaDigital.getClavePublica().getEncoded());
			SignatureInterface signatureInterface = new SignatureInterface() {

				public byte[] sign(InputStream arg0) throws IOException {
					return firmaDigital.getFirma();
				}
			};

			SignatureOptions signatureOptions = new SignatureOptions();
			if (visibleSignatureProperties.isVisualSignEnabled()) {

				signatureOptions.setVisualSignature(visibleSignatureProperties.getVisibleSignature());
				signatureOptions.setPage(visibleSignatureProperties.getPage() - 1);
				signatureOptions.setPreferredSignatureSize(13000);
				doc.addSignature(signature, signatureInterface, signatureOptions);
			} else {
				signatureOptions.setPreferredSignatureSize(13000);
				doc.addSignature(signature, signatureInterface);
			}
			ExternalSigningSupport externalSigning = doc.saveIncrementalForExternalSigning(archivoOutput);
			externalSigning.setSignature(codigoFirma);
			doc.close();
			File docFirmado = new File(archivo_output);
			doc = PDDocument.load(docFirmado);
			verificar(0);
			visor.setDocumento(docFirmado);
			pdfCargado = 2;
		}
	}

	/**
	 * Igualmente al metodo firmaDocumento, se encarga de generar las claves, firma
	 * y certificado digital con la llamada a la clase FirmaDigital. Posteriormente
	 * añade la firma en el PDF con los datos generados, de manera NO visible.
	 * 
	 * @param nivelSeguridad La preferencia de nivel de seguridad de Dilithium
	 * @throws IOException Cubre posibles excepciones en varias llamadas de métodos
	 *                     de ApachePDBox, de la clase FirmaDigital y de la clase
	 *                     FileOutputStream al generar el archivo firmado.
	 */
	public void firmaAutomatica(int nivelSeguridad) throws IOException {
		firmaDigital = new FirmaDigital(nivelSeguridad);
		archivo_output = rutaPDF.getAbsolutePath().substring(0, rutaPDF.getAbsolutePath().lastIndexOf("."));
		archivo_output = archivo_output + "_firmado.pdf";
		try (FileOutputStream archivoOutput = new FileOutputStream(archivo_output)) {
			byte[] codigoFirma = firmaDigital.codigoFirma(nivelSeguridad); // El código CMS de la firma digital

			int accessPermissions = SigUtils.getMDPPermission(doc);
			if (accessPermissions == 1) {
				throw new IllegalStateException(
						"No changes to the document are permitted due to DocMDP transform parameters dictionary");
			}
			PDSignature signature = new PDSignature();
			if (doc.getVersion() >= 1.5f && accessPermissions == 0) {
				SigUtils.setMDPPermission(doc, signature, 2);
			}
			PDAcroForm acroForm = doc.getDocumentCatalog().getAcroForm(null);
			if (acroForm != null && acroForm.getNeedAppearances()) {
				if (acroForm.getFields().isEmpty()) {
					acroForm.getCOSObject().removeItem(COSName.NEED_APPEARANCES);
				} else {
					System.out.println("/NeedAppearances is set, signature may be ignored by Adobe Reader");
				}
			}

			signature.setFilter(PDSignature.FILTER_ADOBE_PPKLITE);
			signature.setSubFilter(PDSignature.SUBFILTER_ADBE_PKCS7_DETACHED);
			signature.setName("David García Diez");
			signature.setLocation("Universidad de León");
			signature.setReason("Análisis de Algoritmos Post-Cuánticos");
			signature.setSignDate(Calendar.getInstance());
			signature.setContents(firmaDigital.getClavePublica().getEncoded());
			SignatureInterface signatureInterface = new SignatureInterface() {

				public byte[] sign(InputStream arg0) throws IOException {
					return firmaDigital.getFirma();
				}
			};

			SignatureOptions signatureOptions = new SignatureOptions();
			signatureOptions.setPreferredSignatureSize(13000);
			doc.addSignature(signature, signatureInterface);

			ExternalSigningSupport externalSigning = doc.saveIncrementalForExternalSigning(archivoOutput);
			externalSigning.setSignature(codigoFirma);
			doc.close();
			File docFirmado = new File(archivo_output);
			doc = PDDocument.load(docFirmado);
			verificar(0);
			visor.setDocumento(docFirmado);
			pdfCargado = 2;
		}
	}

	/**
	 * Función para cargar un archivo arrastrado hacia la pantalla. Similar a la
	 * funcionalidad del JMenuItem Abrir
	 * 
	 * @param ruta La ruta del archivo que se arrastre hacia la pantalla
	 */
	public void abrirArchivoArrastrado(File ruta) {
		panel.removeAll();
		panel.setBorder(null);
		rutaPDF = ruta;
		try {
			// Cerrar el documento PDF anterior si está cargado
			if (doc != null) {
				doc.close();
				visor.removeAll();
			}

			doc = PDDocument.load(rutaPDF);
			if (doc != null) {
				pdfCargado = 1;
			}

			// Cargar el visor web
			verificar.setEnabled(true);
			firmaVisual.setEnabled(true);
			firmaRapida.setEnabled(true);
			// Inicializar el controlador de Swing
			visor = new VisorPDF(new SwingController(), rutaPDF);
			verificar(0);
			// Agregar el componente de visualización al marco

			getContentPane().add(visor, BorderLayout.CENTER);
			visor.cargarPDF();
		} catch (IOException ex) {
			ex.printStackTrace();
		}
	}
	
	/**
	 * Método que prepara la verificación de dos formas:
	 * -El tipo 1, que se ejecuta al hacer click en u "Verificar" en el menú de opciones. Esta forma abre 
	 * un Frame con las firmas encontradas en el documento y sus datos.
	 * -El tipo 0, que se ejecuta al cargar un PDF y consigue una pre-carga de los datos para cuando se haga click sobre una firma.
	 * Recoge los datos de firma de un PDF y carga el frame de verificación.
	 * @param tipo Se usa para separar la funcionalidad de abrir la verificación pulsando sobre una firma y la del menú verificación
	 */
	public void verificar(int tipo) {
			PDAcroForm firmasDocumento;
			List<Boolean> firmasVerificadas = new ArrayList<Boolean>();
			List<byte[]> firmas = new ArrayList<byte[]>(),claves = new ArrayList<byte[]>();
			List<X509Certificate> certs = new ArrayList<X509Certificate>();
			List<PDSignature> signatures = new ArrayList<PDSignature>();
			if(tipo == 1) {
				if (pdfCargado != 0) {
					firmasDocumento = buscarFirmaDocumento(doc);
					if (firmasDocumento != null) {
						FirmaDigital verificador = new FirmaDigital();
						for(PDField firma : firmasDocumento.getFields()) {
							if (firma instanceof PDSignatureField) {
								PDSignatureField signatureField = (PDSignatureField) firma;
								signatures.add(signatureField.getSignature());
								firmasVerificadas.add(verificador.verificarFirmaDocumento(signatureField.getSignature()));
								firmas.add(verificador.getFirma());
								claves.add(verificador.getClavePublica().getEncoded());
								certs.add(verificador.getCertificado());
							}
						}
						new FrameVerificacion(signatures,firmasVerificadas, firmas, claves, certs);
					}else {
						JOptionPane.showMessageDialog(null, "No se ha encontrado una firma en el documento", "Error",
								JOptionPane.ERROR_MESSAGE);
					}
				}else {
					JOptionPane.showMessageDialog(null, "No se ha cargado ningún PDF.", "Error",
							JOptionPane.ERROR_MESSAGE);
				}
			}else {
				firmasDocumento = buscarFirmaDocumento(doc);
				if (firmasDocumento != null) {
					FirmaDigital verificador = new FirmaDigital();
					for(PDField firma : firmasDocumento.getFields()) {
						if (firma instanceof PDSignatureField) {
							PDSignatureField signatureField = (PDSignatureField) firma;
							signatures.add(signatureField.getSignature());
							firmasVerificadas.add(verificador.verificarFirmaDocumento(signatureField.getSignature()));
							firmas.add(verificador.getFirma());
							claves.add(verificador.getClavePublica().getEncoded());
							certs.add(verificador.getCertificado());
						}
					} 
					visor.getPropiedadesFirma(signatures,firmasVerificadas, firmas, claves, certs);
				}
			}
	}

	/**
	 * La clase interna FileTransferHandler se encarga de recoger el archivo PDF
	 * arrastrado, y si es correcto recoge su ruta. Después llama al método
	 * abrirArchivoArrastrado, creado justo para procesar el resultado recogido de
	 * este código.
	 */
	private class FileTransferHandler extends TransferHandler {
		@Override
		public boolean canImport(TransferSupport support) {
			if (!support.isDataFlavorSupported(DataFlavor.javaFileListFlavor)) {
				return false;
			}
			return true;
		}

		@Override
		public boolean importData(TransferSupport support) {
			if (!canImport(support)) {
				return false;
			}

			Transferable transferable = support.getTransferable();
			try {
				@SuppressWarnings("unchecked")
				java.util.List<File> fileList = (java.util.List<File>) transferable
						.getTransferData(DataFlavor.javaFileListFlavor);
				for (File file : fileList) {
					if (file.getName().toLowerCase().endsWith(".pdf")) {
						// Procesar el archivo PDF
						abrirArchivoArrastrado(file);
						break;
					} else {
						// Mostrar un JOptionPane de error
						JOptionPane.showMessageDialog(null, "Error: Sólo pueden cargarse archivos PDF.", "Error",
								JOptionPane.ERROR_MESSAGE);
					}
				}
			} catch (UnsupportedFlavorException | IOException e) {
				e.printStackTrace();
				return false;
			}
			return true;
		}
	}

	/**
	 * Método para evaluar la modificación de la variable global pdfCargado, que
	 * sirve como comprobación antes de realizar acciones de carga, guardado o
	 * incluso de firma y verificación
	 * 
	 * @return El estado de carga de un PDF en la aplicación.
	 */
	public int getPDFCargado() {
		return pdfCargado;
	}

	/**
	 * Método para recoger el PDDocument, y poder interactuar con él. Utilizado para
	 * tests.
	 * 
	 * @return El documento cargado o null si no se ha cargado ninguno
	 */
	public PDDocument getDoc() {
		return doc;
	}

	/**
	 * Método encargado de buscar un componente de firma en el documento. Para ello,
	 * en primer lugar busca un Acroform (formulario interactivo en un PDF) y dentro
	 * de este busca un componente de firma. Si lo encuentra, lo almacena en un
	 * objeto PDSignature, el cuál permite entre otras cosas extraer la información
	 * de la firma
	 * 
	 * @param doc El documento cargado
	 * @return PDSignature un objeto firma que contendrá los datos de una firma
	 *         digital. Null si no encuentra un campo de firma
	 */
	public static PDAcroForm buscarFirmaDocumento(PDDocument doc) {
		PDSignature signature = null;
		PDAcroForm acroForm = doc.getDocumentCatalog().getAcroForm(null);

		if (acroForm == null)
			return null;
		return acroForm;
	}
}