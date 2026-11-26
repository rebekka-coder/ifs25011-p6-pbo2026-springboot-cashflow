package org.delcom.ifs25011p6pbo2026springbootcashflow.framework.view;

import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;

/** Gaya grid yang konsisten. */
public final class GridStyles {

    private GridStyles() {
    }

    public static void apply(Grid<?> grid) {
        grid.addThemeVariants(GridVariant.LUMO_ROW_STRIPES, GridVariant.LUMO_NO_BORDER);
        grid.addClassName("cf-grid");
        grid.setSizeFull();
    }
}
