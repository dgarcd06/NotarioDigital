package vista;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;

@SuppressWarnings("serial")
/**
 * Este frame semitransparente se genera cuando un usuario elige una opción de "Firma Visual".
 * Se trata de un frame semitransparente que espera la selección de un área rectangular por
 * parte del usuario. Este área sirve para decidir la posición y tamaño de la firma digital.
 * @author David García Diez
 */
public class FrameVisual extends JDialog {

    private int startX, startY, ancho, alto;
    private boolean dragging, firmaDeseada;

    public FrameVisual(Frame owner, int width, int height, int posX, int posY) {
    	super(owner, "Select Area", true);
        this.setSize(width, height);
        this.setUndecorated(true);    //Necesario para la transparencia
        this.setBackground(new Color(224, 206, 67, 30));
        this.setLocation(posX, posY);
        
        addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent e) {
                startX = e.getX();
                startY = e.getY();
                dragging = true;
            }

            public void mouseReleased(MouseEvent e) {
                dragging = false;
                ancho = e.getX() - startX;
                alto = e.getY() - startY;
                repaint();
                validarSeleccion();
            }
        });

        addMouseMotionListener(new MouseMotionAdapter() {
            public void mouseDragged(MouseEvent e) {
                if (dragging) {
                	ancho = e.getX() - startX;
                	alto = e.getY() - startY;
                    repaint();
                }
            }
        });
        addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
                	int option = JOptionPane.showOptionDialog(null,
            				"¿Desea cancerlar la operación de firma?",
            				"Selección de área",JOptionPane.YES_NO_OPTION, 
            			      JOptionPane.QUESTION_MESSAGE, 
            			      null, null, null);
                	if(option == 0) {
                		dispose();
                	}
                    
                }
            }
        });
        JOptionPane.showMessageDialog(this, "Selecciona el área donde desees crear la firma.\nPulse ESC si desea cancelar la operación.");
        this.setVisible(true);
    }
    /**
     * Desaparece el frame. Se utiliza este método para así ocultarlo sin afectar al Frame
     * que está por debajo.
     */
    public void cerrarFrame() {
        this.dispose();
    }
    /**
     * Método que define cómo debe funcionar el objeto Graphics a la hora de la selección de area.
     */
    public void paint(Graphics g) {
        super.paint(g);
        g.setColor(Color.BLACK);
        g.drawRect(startX, startY, ancho, alto);
    }
    /**
     * Método que sirve para comprobar si la selección del usuario es la deseada.
     * Este valor depende de un JOptionPane que pregunta al usuario sobre su selección.
     * @return true si el usuario está conforme con la selección. false si quiere repetir.
     */
    public boolean getFirmaDeseada() {
    	return this.firmaDeseada;
    }
    /**
     * Recoge el ancho de la firma
     * @return ancho el tamaño de la firma en anchura
     */
    public int getAncho() {
    	return ancho;
    }
    /**
     * Recoge el alto de la firma
     * @return alto el tamaño de la firma en altura
     */
    public int getAlto() {
    	return alto;
    }
    /**
     * Recoge la coordenada X en la que el usuario comienza la selección de la firma
     * @return startX la coordenada en X donde comienza la selección.
     */
    public int getX() {
    	return startX;
    }
    /**
     * Recoge la coordenada Y en la que el usuario comienza la selección de la firma
     * @return startY la coordenada en Y donde comienza la selección.
     */
    public int getY() {
    	return startY;
    }
    /**
     * Este método se encarga de confirmar la selección de firma por parte del usuario.
     * Permite decidir si repetir la firma o mantener la selección y eliminar el frame.
     * Este método tiene una parte asíncrona junto con su parte de código en NotarioDigital,
     * ya que si el usuario decide repetir la selección, no debe llamarse a la función de 
     * firma hasta conocer las coordenadas deseadas.
     */
    public void validarSeleccion() {
    	int option = JOptionPane.showOptionDialog(null,
				"¿Desea confirmar el área seleccionado para la firma?",
				"Selección de área",JOptionPane.YES_NO_OPTION, 
			      JOptionPane.QUESTION_MESSAGE, 
			      null, null, null);
    	if(option == 0) {
    		firmaDeseada = true;
    		this.setVisible(false);
    		synchronized (this) {
                this.notifyAll();
            }
    	}
    }
}
