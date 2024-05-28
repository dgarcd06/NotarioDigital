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
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.Security;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.Calendar;
import java.util.Collection;

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
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.cms.CMSException;
import org.bouncycastle.cms.CMSSignedData;
import org.bouncycastle.cms.SignerInformation;
import org.bouncycastle.cms.SignerInformationStore;
import org.bouncycastle.cms.SignerInformationVerifier;
import org.bouncycastle.cms.jcajce.JcaSignerInfoVerifierBuilder;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.operator.ContentVerifierProvider;
import org.bouncycastle.operator.jcajce.JcaContentVerifierProviderBuilder;
import org.bouncycastle.pqc.jcajce.provider.BouncyCastlePQCProvider;
import org.bouncycastle.util.Store;
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
 */
@SuppressWarnings("serial")
public class NotarioDigital extends JFrame {
	private Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
	private JMenuBar menu; // Menú de opciones para la pantalla
	private JMenu archivo, editar, ayuda, firmaVisual, firmaRapida;
	private JMenuItem abrir, guardar, salir, verificar, visual2, visual3, visual5, rapida2, rapida3, rapida5,
			comoUsar, acercaDe;
	private int pdfCargado = 0; // 0 = No cargado | 1 = Cargado | 2 = Modificado
	private static File rutaPDF; // Objeto que usaremos para cargar despues el pdf
	private static PDDocument doc;
	private static FirmaDigital firmaDigital;
	private static VisorPDF visor;
	private final static String dir = System.getProperty("user.dir");
	private JPanel panel;
	private static String archivo_output;
	static {
        Security.addProvider(new BouncyCastleProvider());
    }

	public NotarioDigital() {
		Security.addProvider(new BouncyCastlePQCProvider());
		try {
			UIManager.setLookAndFeel(new FlatLightLaf());
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
			firmaRapida = new JMenu("Firma Automática");
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

		// ARCHIVO
		abrir.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				// Si ya hay un pdf cargado SIN GUARDAR (pdf_cargado == 2 es que está
				// modificado) hay que cerrarlo para abrir otro
				Boolean borrar = false;
				if (pdfCargado == 2) {
					int option = JOptionPane.showConfirmDialog(null,
							"Un PDF ha sido modificado sin guardar cambios. ¿Desea guardar antes de cerrarlo?");
					if (option == JOptionPane.YES_OPTION) {
						try {
							guardar(pdfCargado);
							setPDFCargado(0);
						} catch (Exception ex) {
							JOptionPane.showMessageDialog(null,
									"No se ha podido guardar el PDF. Comprueba que tiene permisos para escribir en la ruta indicada.\n"
											+ ex.getMessage(),
									"Error", JOptionPane.ERROR_MESSAGE);
						}
					} else if (option == JOptionPane.NO_OPTION) {
						borrar = true;
					}
				}

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

							// Agregar el visor al Frame principal
							getContentPane().add(visor, BorderLayout.CENTER);
							visor.cargarPDF();
							revalidate();
							repaint();
							// Si el PDF anterior estaba firmado (pdfCargado == 2) y no se quiere guardar se
							// borra
							if (borrar) {
								File eliminarArchivo = new File(archivo_output);
								if (eliminarArchivo.delete()) {
									System.out.println("El archivo ha sido eliminado exitosamente.");
								} else {
									System.out.println(
											"No se pudo eliminar el archivo. Verifica que el archivo exista y que tengas los permisos necesarios.");
								}
							}
						}

					} catch (IOException ex) {
						ex.printStackTrace();
					}
				}
			}
		});

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

		salir.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				System.exit(0);
			}
		});

		// ACCIONES DE EDITAR
		visual2.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (pdfCargado == 1) {

					FrameVisual panelFirma = new FrameVisual(visor.getWidth(), visor.getHeight(), getX() + 7,
							getY() + 55);
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

				} else {
					JOptionPane.showMessageDialog(null, "No se ha cargado ningún PDF.", "Error",
							JOptionPane.ERROR_MESSAGE);
				}
			}

		});
		visual3.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (pdfCargado == 1) {

					FrameVisual panelFirma = new FrameVisual(visor.getWidth(), visor.getHeight(), getX() + 7,
							getY() + 55);
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

				} else {
					JOptionPane.showMessageDialog(null, "No se ha cargado ningún PDF.", "Error",
							JOptionPane.ERROR_MESSAGE);
				}
			}

		});
		visual5.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (pdfCargado == 1) {

					FrameVisual panelFirma = new FrameVisual(visor.getWidth(), visor.getHeight(), getX() + 7,
							getY() + 55);
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

				} else {
					JOptionPane.showMessageDialog(null, "No se ha cargado ningún PDF.", "Error",
							JOptionPane.ERROR_MESSAGE);
				}
			}

		});

		rapida2.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {
					firmaDocumento(2, 100, 100, 350, 100);
					setPDFCargado(2); // Modificado(para que pregunte por guardar)
				} catch (IOException e1) {
					e1.printStackTrace();
				}
				revalidate();
			}
		});
		rapida3.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {
					firmaDocumento(3, 100, 100, 350, 100);
					setPDFCargado(2); // Modificado(para que pregunte por guardar)
				} catch (IOException e1) {
					e1.printStackTrace();
				}
				revalidate();
			}
		});
		rapida5.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {
					firmaDocumento(5, 100, 100, 350, 100);
					setPDFCargado(2); // Modificado(para que pregunte por guardar)
				} catch (IOException e1) {
					e1.printStackTrace();
				}
				revalidate();
			}
		});

		verificar.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				PDSignature firmaDocumento;
				if (pdfCargado == 1) {
						//Se busca firma en el documento. Si no hay, no se puede verificar
						//La verificacion se hace en la clase FirmaDigital
						//Después se crea un frame con la información de la verificación
						try {
							firmaDocumento = buscarFirmaDocumento(doc);
							if(firmaDocumento != null) {
								FirmaDigital verificador = new FirmaDigital();
								Boolean firmaVerificada = verificador.verificarFirmaDocumento(doc, firmaDocumento);
								new FrameVerificacion(firmaVerificada,verificador.getFirma(),firmaDocumento.getContents(),verificador.getCertificado());
							}else {
								JOptionPane.showMessageDialog(null, "No se ha encontrado una firma en el documento", "Error",
										JOptionPane.ERROR_MESSAGE);
							}
							
						} catch (IOException e1) {
							e1.printStackTrace();
						}
						
				}else if(pdfCargado == 2) { 
					try {
						Boolean firmaVerificada = firmaDigital.verificarFirmaDocumento(doc, doc.getLastSignatureDictionary());
						new FrameVerificacion(firmaVerificada,firmaDigital.getFirma(),doc.getLastSignatureDictionary().getContents(),firmaDigital.getCertificado());
					} catch (IOException e1) {
						e1.printStackTrace();
					}
				}else {
					JOptionPane.showMessageDialog(null, "No se ha cargado ningún PDF.", "Error",
							JOptionPane.ERROR_MESSAGE);
				}
			}
		});

		// ACCIONES DE AYUDA
		comoUsar.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				JOptionPane.showMessageDialog(null,
						  "Para utilizar las funcionalidades de firma y verificación deberá haberse cargado un PDF.\n"
						+ "Puede elegir entre Firma Automática, de manera que la firma se incluirá automáticamente en el documento, o bien seleccionar el área de firma con Firma Visual.\n"
						+ "Si selecciona Firma Visual, confirme el área seleccionado para añadir la firma en el documento.\n"
						+ "Dentro de las dos opciones, se puede seleccionar el nivel de seguridad de Dilithium, entre las opciones 2, 3 y 5.\n"
						+ "Al pulsar Verificación, se buscará una firma en el documento. En el caso de existir la firma, se comprobará su veracidad a partir de la recogida de sus datos del documento.",
						"Como usar la aplicación", JOptionPane.INFORMATION_MESSAGE);

			}
		});

		acercaDe.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				JOptionPane.showMessageDialog(null,
						  "Notario Digital es una aplicación de escritorio destinada a la firma y verificación de documentos digitales con el uso del algoritmo Dilithium, finalista del proceso del NIST \"Post-Quantum Cryptography\".\n"
						+ "Esta aplicación ha sido desarrollada por David García Diez, como parte de su Trabajo de Fin de Grado en la Universidad de León.\n"
						+ "El desarrollo de la aplicación tiene fines de investigación; si bien su desarrollo cumple con la seguridad del algoritmo, el certificado digital es emitido por una autoridad de certificación. No se recomienda su uso profesional.",
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
	 * @return 0 si se ha podido guardar correctamente, 1 si ha habido errores
	 * @throws IOException
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

	public static void firmaDocumento(int nivelSeguridad, int x, int y, int width, int height) throws IOException {
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
			visor.setDocumento(docFirmado);
		}
	}

	/**
	 * Función para cargar un archivo arrastrado hacia la pantalla. Similar a la
	 * funcionalidad del JMenuItem Abrir
	 * 
	 * @param rutaPDF La ruta del archivo que se arrastre hacia la pantalla
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

			// Agregar el componente de visualización al marco

			getContentPane().add(visor, BorderLayout.CENTER);
			visor.cargarPDF();
		} catch (IOException ex) {
			ex.printStackTrace();
		}
	}

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
						System.out.println("Ruta del archivo PDF: " + file.getAbsolutePath());
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

	public int getPDFCargado() {
		return pdfCargado;
	}
	
	public PDDocument getDoc() {
		return doc;
	}

	public static PDSignature buscarFirmaDocumento(PDDocument doc) throws IOException {
		PDSignature signature = null;
		PDAcroForm acroForm = doc.getDocumentCatalog().getAcroForm(null);

		if (acroForm == null)
			return null;
		for (PDField field : acroForm.getFields()) {
			if (field instanceof PDSignatureField) {
				PDSignatureField signatureField = (PDSignatureField) field;
				signature = signatureField.getSignature();
				if (signature != null) {
					return signature;
				}
			}
		}
		return null;
	}
}