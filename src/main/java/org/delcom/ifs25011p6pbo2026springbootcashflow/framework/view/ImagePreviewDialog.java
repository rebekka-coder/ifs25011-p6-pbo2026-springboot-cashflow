package org.delcom.ifs25011p6pbo2026springbootcashflow.framework.view;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Image;

/** Modal pratinjau gambar. Klik gambar untuk toggle zoom 1.0x <-> 1.8x. */
public class ImagePreviewDialog {

    private static final double MIN = 0.4;
    private static final double MAX = 3.0;

    private final Dialog dialog;
    private final Image image;
    private double zoom = 1.0;

    public ImagePreviewDialog(String src) {
        image = new Image(src, "Pratinjau gambar");
        image.getStyle().set("cursor", "zoom-in").set("max-width", "none").set("display", "block");
        image.addClickListener(e -> apply(zoom == 1.0 ? 1.8 : 1.0));
        apply(1.0);

        Div scroller = new Div(image);
        scroller.getStyle().set("overflow", "auto").set("max-height", "65vh");

        Button zoomOut = new Button("Perkecil", e -> apply(Math.max(MIN, zoom - 0.4)));
        Button zoomIn = new Button("Perbesar", e -> apply(Math.min(MAX, zoom + 0.4)));
        Button close = new Button("Tutup", e -> dialog().close());
        dialog = ViewSupport.modal("Bukti Transaksi", "Pratinjau", scroller, zoomOut, zoomIn, close);
    }

    private Dialog dialog() {
        return dialog;
    }

    private void apply(double value) {
        zoom = value;
        image.setWidth((zoom * 100) + "%");
    }

    public void open() {
        dialog.open();
    }
}
