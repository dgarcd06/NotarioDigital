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
import java.io.File;
import java.io.IOException;

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
import javax.swing.TransferHandler;
import javax.swing.UIManager;
import javax.swing.filechooser.FileNameExtensionFilter;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.icepdf.ri.common.SwingController;

import com.formdev.flatlaf.FlatLightLaf;

import modelo.FirmaDigital;
import vista.FrameVisual;
import vista.VisorPDF;

/**
 * Frame principal de la aplicación. Aquí se coordinan las funciones lógicas de
 * Dilithium con el visor PDF.
 */
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
	private static VisorPDF visor;
	private final static String dir = System.getProperty("user.dir");
	private JPanel panel;

	public NotarioDigital() {
		try {
			UIManager.setLookAndFeel(new FlatLightLaf());
			this.setTitle("Notario Digital");
			this.setSize(650, 500);
			this.setLocation(dim.width / 2 - this.getSize().width / 2, dim.height / 2 - this.getSize().height / 2);
			this.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
			this.setIconImage(Toolkit.getDefaultToolkit().getImage(dir + "\\recursos\\icono_jframe.png"));
			
			JLabel label = new JLabel("Arrastra un archivo aquí", SwingConstants.CENTER);
			label.setPreferredSize(new Dimension(300, 200));
			  
			panel = new JPanel(new BorderLayout()); 
			panel.setTransferHandler(new FileTransferHandler());
			panel.setBorder(BorderFactory.createLineBorder(Color.BLACK));
			panel.add(label,BorderLayout.CENTER); 
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
				remove(panel);	//El panel de arrastrar archivos
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
						visor = new VisorPDF(new SwingController(), rutaPDF);

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
		visual2.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (pdf_cargado == 1) {

					FrameVisual panelFirma = new FrameVisual(visor.getWidth(), visor.getHeight(), getX() + 7,
							getY() + 55);
					if (panelFirma.getFirmaDeseada()) {
						llamadaFirma(2);
					}
				} else {
					JOptionPane.showMessageDialog(null, "No se ha cargado ningún PDF.", "Error",
							JOptionPane.ERROR_MESSAGE);
				}
			}

		});
		visual3.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (pdf_cargado == 1) {

					FrameVisual panelFirma = new FrameVisual(visor.getWidth(), visor.getHeight(), getX() + 7,
							getY() + 55);
					if (panelFirma.getFirmaDeseada()) {
						llamadaFirma(3);
					}
				} else {
					JOptionPane.showMessageDialog(null, "No se ha cargado ningún PDF.", "Error",
							JOptionPane.ERROR_MESSAGE);
				}
			}

		});
		visual5.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (pdf_cargado == 1) {

					FrameVisual panelFirma = new FrameVisual(visor.getWidth(), visor.getHeight(), getX() + 7,
							getY() + 55);
					if (panelFirma.getFirmaDeseada()) {
						llamadaFirma(5);
					}
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
		firmaDigital = new FirmaDigital(doc, nivelSeguridad, rutaPDF.getAbsolutePath(), visor);
		pdf_cargado = 2; // Modificado(para que pregunte por guardar)
	}
	/**
	 * Función para cargar un archivo arrastrado hacia la pantalla.
	 * Similar a la funcionalidad del JMenuItem Abrir
	 * @param rutaPDF La ruta del archivo que se arrastre hacia la pantalla
	 */
	public void abrirArchivoArrastrado(File ruta) {
		remove(panel);
		rutaPDF = ruta;
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
                java.util.List<File> fileList = (java.util.List<File>) transferable.getTransferData(DataFlavor.javaFileListFlavor);
                for (File file : fileList) {
                    if (file.getName().toLowerCase().endsWith(".pdf")) {
                        // Procesar el archivo PDF
                        System.out.println("Ruta del archivo PDF: " + file.getAbsolutePath());
                        abrirArchivoArrastrado(file);
                        break;
                    } else {
                        // Mostrar un JOptionPane de error
                        JOptionPane.showMessageDialog(null, "Error: Sólo pueden cargarse archivos PDF.", "Error", JOptionPane.ERROR_MESSAGE);
                    }
                }
            } catch (UnsupportedFlavorException | IOException e) {
                e.printStackTrace();
                return false;
            }
            return true;
        }
    }
}