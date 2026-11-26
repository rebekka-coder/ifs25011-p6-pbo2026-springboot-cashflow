package org.delcom.ifs25011p6pbo2026springbootcashflow.framework.view;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.dialog.DialogVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;

/** Helper UI bersama: notifikasi, modal berwarna, ID elemen input native. */
final class ViewSupport {

    private ViewSupport() {
    }

    static void error(String message) {
        Notification.show(message, 4000, Notification.Position.TOP_END)
                .addThemeVariants(NotificationVariant.LUMO_ERROR);
    }

    static void success(String message) {
        Notification.show(message, 3000, Notification.Position.TOP_END)
                .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
    }

    /** Menetapkan id ke <input> native di dalam komponen web Vaadin (untuk Selenium/Cypress). */
    static void nativeInputId(Component field, String id) {
        field.getElement().executeJs(
                "customElements.whenDefined(this.localName).then(() => setTimeout(() => {"
                        + " if (this.inputElement) { this.inputElement.id = $0; } }, 100));", id);
    }

    /** Modal: header primer #2563EB, footer abu-abu #F3F4F6 dengan tombol di kanan. */
    static Dialog modal(String title, String badge, Component body, Button... actions) {
        Dialog dialog = new Dialog();
        dialog.addThemeVariants(DialogVariant.LUMO_NO_PADDING);
        dialog.setWidth("560px");
        dialog.setMaxWidth("96vw");
        Span titleSpan = new Span(title);
        Span badgeSpan = new Span(badge);
        badgeSpan.addClassName("cf-badge");
        Div header = new Div(titleSpan, badgeSpan);
        header.addClassName("cf-modal-header");
        Div content = new Div(body);
        content.addClassName("cf-modal-body");
        Div footer = new Div(actions);
        footer.addClassName("cf-modal-footer");
        dialog.add(header, content, footer);
        return dialog;
    }
}
