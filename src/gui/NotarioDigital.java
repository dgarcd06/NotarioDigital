package gui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Base64;
import java.awt.event.ActionEvent;

import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenuBar;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.WindowConstants;
import javax.swing.filechooser.FileNameExtensionFilter;

import org.apache.pdfbox.io.IOUtils;
import org.apache.pdfbox.pdmodel.PDDocument;

import javafx.application.Platform;
import javafx.concurrent.Worker;
import javafx.embed.swing.JFXPanel;
import javafx.scene.Scene;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;

public class NotarioDigital extends JFrame {
	private Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
	private JMenuBar menu; // Menú de opciones para la pantalla
	private JMenu archivo, editar, ayuda;
	private JMenuItem abrir, guardar,salir, verificar, firmar, como_firmar, acerca_de;
	private static int pdf_cargado = 0; // Variable para comprobar si un pdf ha sido modificado/guardado
	private static File rutaPDF;	//Objeto que usaremos para cargar despues el pdf
	private static PDDocument doc;
	private JFXPanel panel;
	
	public NotarioDigital() {
		this.setTitle("Notario Digital");
		this.setSize(300, 200);
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
		
		
		
		/* ACCIONES DE LOS BOTONES */

		// ARCHIVO
		abrir.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				try {
					JFileChooser selector = new JFileChooser();
					FileNameExtensionFilter filter = new FileNameExtensionFilter("Archivos PDF", "pdf");
					selector.setFileFilter(filter);
					int result = selector.showOpenDialog(null);
	                if (result == JFileChooser.APPROVE_OPTION) {
	                    rutaPDF = selector.getSelectedFile();
	                    doc = PDDocument.load(new File(rutaPDF.getAbsolutePath()));
	                    if(doc != null) {
	                    	pdf_cargado = 1;
	                    }
	                    //Codigo para cargar el visor web
	                    Platform.startup(() -> {
							Platform.runLater(()-> {
			                    WebView webView = new WebView();
			                    WebEngine webEngine = webView.getEngine();
			                    panel = new JFXPanel();
			            		getContentPane().add(panel,BorderLayout.CENTER);
			                    Scene scene = new Scene(webView);
								try {
				                    webEngine.setUserStyleSheetLocation(getClass().getResource("/web/viewer.css").toURI().toString());
				                    webEngine.setJavaScriptEnabled(true);
				                    webEngine.load(getClass().getResource("/web/viewer.html").toExternalForm());
								} catch (URISyntaxException e1) {
									e1.printStackTrace();
								}

			                    webEngine.getLoadWorker().stateProperty().addListener((obs, oldV, newV) -> {
			                        if (Worker.State.SUCCEEDED == newV) {
			                            try {

			                                byte[] bytes = IOUtils.toByteArray(new FileInputStream(rutaPDF));
			                                String base64 = Base64.getEncoder().encodeToString(bytes);
			                                webEngine.executeScript("openFileFromBase64('" + base64 + "')");
			                                
			                            } catch (Exception exc) {
			                                exc.printStackTrace();
			                            }
			                        }
			                    });
			                    panel.setScene(scene);
		                    });
						});
	                }
				} catch (IOException ex) {
					ex.printStackTrace();
				}
			}
		});

		guardar.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if(pdf_cargado == 0) {
					JOptionPane.showMessageDialog(null, "No se ha cargado ningún PDF","Guardar",JOptionPane.INFORMATION_MESSAGE);
				}else {
					try {
						guardar();
					}catch(Exception ex)
					{
						JOptionPane.showMessageDialog(null,"No se ha podido guardar el PDF. Comprueba que tiene permisos para escribir en la ruta indicada.","Error",JOptionPane.ERROR_MESSAGE);
					}
				}
			}
		});

		salir.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				// Si hay un PDF cargado sin guardar, se preguntará antes de salir
				if (pdf_cargado == 1) {
					int option = JOptionPane.showConfirmDialog(null,"Un PDF ha sido modificado sin guardar cambios. ¿Desea guardar antes de salir?");
					if (option == 0) {
						try {
							guardar();
							System.exit(0);
						}catch(Exception ex)
						{
							JOptionPane.showMessageDialog(null,"No se ha podido guardar el PDF. Comprueba que tiene permisos para escribir en la ruta indicada.","Error",JOptionPane.ERROR_MESSAGE);
						}
					} else if (option == 1) {
						System.exit(0);
					}
				} else {
					System.exit(0);
				}
			}
		});
		
		//ACCIONES DE EDITAR
		firmar.addActionListener(new ActionListener() {
			//TODO
			public void actionPerformed(ActionEvent e) {
				if(pdf_cargado == 1) {
					
				}else {
					JOptionPane.showMessageDialog(null, "No se ha cargado ningún PDF.","Error",JOptionPane.ERROR_MESSAGE);
				}
				
			}
			
		});
		
		verificar.addActionListener(new ActionListener() {
			//TODO	
			public void actionPerformed(ActionEvent e) {
				if(pdf_cargado == 1) {
					
				}else {
					JOptionPane.showMessageDialog(null, "No se ha cargado ningún PDF.","Error",JOptionPane.ERROR_MESSAGE);
				}
				
			}
			
		});
		
		//ACCIONES DE AYUDA
		como_firmar.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				//TODO Escribir texto Como firmar
				JOptionPane.showMessageDialog(null, "Como firmar","Como firmar",JOptionPane.INFORMATION_MESSAGE);
				
			}
			
		});
		
		acerca_de.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				// TODO Escribir texto Acerca De....
				JOptionPane.showMessageDialog(null, "Acerca de...","Acerca de...",JOptionPane.INFORMATION_MESSAGE);
			}
			
		});

		this.setJMenuBar(menu);
		this.setVisible(true);
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
        }else {
        	return 1;
        }
	}

}
