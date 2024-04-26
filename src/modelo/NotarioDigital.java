package modelo;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Base64;
import java.awt.event.ActionEvent;

import javax.imageio.ImageIO;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenuBar;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.UIManager;
import javax.swing.WindowConstants;
import javax.swing.filechooser.FileNameExtensionFilter;

import org.apache.pdfbox.io.IOUtils;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.interactive.digitalsignature.visible.PDVisibleSigProperties;
import org.apache.pdfbox.pdmodel.interactive.digitalsignature.visible.PDVisibleSignDesigner;
import org.icepdf.core.exceptions.PDFException;
import org.icepdf.core.exceptions.PDFSecurityException;
import org.icepdf.core.pobjects.Document;
import org.icepdf.core.pobjects.Page;
import org.icepdf.core.util.GraphicsRenderingHints;
import org.icepdf.ri.common.SwingController;
import org.icepdf.ri.common.SwingViewBuilder;
import org.icepdf.ri.common.views.DocumentViewController;
import org.icepdf.ri.common.views.DocumentViewControllerImpl;
import org.icepdf.ri.common.views.DocumentViewModel;
import org.icepdf.core.pobjects.annotations.Annotation;
import org.icepdf.core.pobjects.annotations.SquareAnnotation;

import com.formdev.flatlaf.FlatLightLaf;

import controlador.Controlador;
import netscape.javascript.JSObject;
import vista.VisorPDF;

@SuppressWarnings("serial")
public class NotarioDigital extends JFrame {
	private Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
	private JMenuBar menu; // Menú de opciones para la pantalla
	private JMenu archivo, editar, ayuda;
	private JMenuItem abrir, guardar, salir, verificar, firmar, como_firmar, acerca_de;
	private static int pdf_cargado = 0; // Variable para comprobar si un pdf ha sido modificado/guardado
	private static File rutaPDF; // Objeto que usaremos para cargar despues el pdf
	private static PDDocument doc;
	private static boolean javafx_inicializado = false;
	private static Controlador controlador;
	private double startX, startY, endX, endY;
	JPanel viewerComponentPanel;
	private SwingController controller;
	VisorPDF visor;

	public NotarioDigital() {
		try {
			UIManager.setLookAndFeel(new FlatLightLaf());
			this.setTitle("Notario Digital");
			this.setSize(650, 500);
			this.setLocation(dim.width / 2 - this.getSize().width / 2, dim.height / 2 - this.getSize().height / 2);
			this.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

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
			firmar = new JMenuItem("Firmar");
			verificar.setEnabled(false);
			firmar.setEnabled(false);
			editar.add(firmar);
			editar.add(verificar);

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
					controlador = new Controlador(rutaPDF);
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
						firmar.setEnabled(true);

						// Inicializar el controlador de Swing
						controller = new SwingController();
						visor = new VisorPDF(controller,rutaPDF);
						
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
				// Si hay un PDF modificado sin guardar, se preguntará antes de salir
				if (pdf_cargado == 2) {
					int option = JOptionPane.showConfirmDialog(null,
							"Un PDF ha sido modificado sin guardar cambios. ¿Desea guardar antes de salir?");
					if (option == 0) {
						try {
							guardar();
							System.exit(0);
						} catch (Exception ex) {
							JOptionPane.showMessageDialog(null,
									"No se ha podido guardar el PDF. Comprueba que tiene permisos para escribir en la ruta indicada.\n"
											+ ex.getMessage(),
									"Error", JOptionPane.ERROR_MESSAGE);
						}
					} else if (option == 1) {
						System.exit(0);
					}
				} else {
					System.exit(0);
				}
			}
		});

		// ACCIONES DE EDITAR
		firmar.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (pdf_cargado == 1) {
					/**
					 * TODO Aqui se puede usar el objeto scene para pintar la firma Se usa el objeto
					 * "controlador" que debe estar inicializado en "abrir" No debería haber error
					 * porque sólo se ejecuta este código cuando se abre un pdf-> COMPROBAR!
					 */
					
					/*try {
						BufferedImage image = ImageIO.read(new File("C:\\Users\\David\\Pictures\\GBKuCm5b0AAgHOA.jpg"));
						String keystorePath = "ruta/al/almacen_de_claves.p12";
						String keystorePassword = "contraseña_del_almacén";
						String keyAlias = "alias_de_la_clave";
						String keyPassword = "contraseña_de_la_clave";
						PDVisibleSignDesigner visibleSignDesigner;
						visibleSignDesigner = new PDVisibleSignDesigner(rutaPDF.toString(),image,1);
						PDVisibleSigProperties visibleSigProperties = new PDVisibleSigProperties();
						visibleSigProperties.signerName("Nombre del firmante").signerLocation("Ubicación del firmante")
								.signatureReason("Razón de la firma").preferredSize(0).page(1).visualSignEnabled(true)
								.setPdVisibleSignature(visibleSignDesigner).buildSignature();
					} catch (IOException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}*/
					
					if(controlador != null) {
						controlador.firmar();
						controlador.setArchivoFirma();
						controlador.leerArchivoFirma();
						
					}
				} else {
					JOptionPane.showMessageDialog(null, "No se ha cargado ningún PDF.", "Error",
							JOptionPane.ERROR_MESSAGE);
				}
			}
		});

		verificar.addActionListener(new ActionListener() {
			// TODO
			public void actionPerformed(ActionEvent e) {
				if (pdf_cargado == 1) {
					if(controlador != null) {
						System.out.println(controlador.verificar());
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
	}

	/**
	 * Guarda cambios en un PDF. Escribe el PDF en la ruta indicada
	 * 
	 * @return 0 si se ha podido guardar correctamente, 1 si ha habido errores
	 * @throws IOException
	 */
	public static int guardar() throws IOException {
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
	}
}