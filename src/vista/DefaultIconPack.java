package vista;

import javax.swing.*;

import vista.Images.IconSize;

import java.net.URL;

/**
 * Clase auxiliar para poder modificar el código de SwingController y SwingViewBuilder.
 * @author Alexander Leithner
 */
public class DefaultIconPack extends IconPack {

    @Override
    public VariantPool getProvidedVariants() {
        return new VariantPool(Variant.DISABLED, Variant.ROLLOVER, Variant.PRESSED, Variant.SELECTED);
    }

    @Override
    public Icon getIcon(String name, Variant variant, Images.IconSize size) throws RuntimeException {
        String iconSize;
        switch (size) {
            case HUGE:
                iconSize = "_lg";
                break;
            case LARGE:
                iconSize = "_32";
                break;
            case SMALL:
                iconSize = "_24";
                break;
            case MINI:
                iconSize = "_20";
                break;
            default:
                iconSize = "_16";
                break;
        }

        String iconVariant;
        switch (variant) {
            case NORMAL:
                iconVariant = "_a";
                break;
            case PRESSED:
            case DISABLED:
                iconVariant = "_i";
                break;
            case ROLLOVER:
                iconVariant = "_r";
                break;
            case SELECTED:
                iconVariant = "_selected_a";
                break;
            case NONE:
            default:
                iconVariant = "";
                break;
        }

        URL url = Images.class.getResource(name + iconVariant + iconSize + ".png");
        if (url == null) {
            throw new NullPointerException("Icon " + name + " not found with variant " + variant + ", size " + size + " on classpath; NULL URL returned");
        }

        return new ImageIcon(url);
    }

}
