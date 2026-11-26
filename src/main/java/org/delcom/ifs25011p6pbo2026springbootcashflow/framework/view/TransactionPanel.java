package org.delcom.ifs25011p6pbo2026springbootcashflow.framework.view;

import org.delcom.ifs25011p6pbo2026springbootcashflow.adapter.presenter.FinancePresenter;
import org.delcom.ifs25011p6pbo2026springbootcashflow.adapter.storage.FileStorage;
import org.delcom.ifs25011p6pbo2026springbootcashflow.domain.entity.Balance;
import org.delcom.ifs25011p6pbo2026springbootcashflow.domain.entity.SortOption;
import org.delcom.ifs25011p6pbo2026springbootcashflow.domain.entity.Transaction;
import org.delcom.ifs25011p6pbo2026springbootcashflow.domain.entity.TransactionType;
import org.delcom.ifs25011p6pbo2026springbootcashflow.domain.entity.TransactionTypeFilter;
import org.delcom.ifs25011p6pbo2026springbootcashflow.domain.exception.DomainException;
import org.delcom.ifs25011p6pbo2026springbootcashflow.usecase.FinanceUseCase;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.data.value.ValueChangeMode;

import java.util.Comparator;

/** Panel transaksi (kartu ringkasan + toolbar + grid). Dipakai FinanceView dan UserTransactionsView. */
public class TransactionPanel extends VerticalLayout {

    private final FinanceUseCase finance;
    private final FinancePresenter presenter;
    private final boolean readOnly;

    private final Div summary = new Div();
    private final HorizontalLayout toolbar = new HorizontalLayout();
    private final TextField search = new TextField();
    private final Select<TransactionTypeFilter> typeFilter = new Select<>();
    private final Select<SortOption> sort = new Select<>();
    private final Grid<Transaction> grid = new Grid<>(Transaction.class, false);
    private Integer userId;

    public TransactionPanel(FinanceUseCase finance, FinancePresenter presenter, boolean readOnly) {
        this.finance = finance;
        this.presenter = presenter;
        this.readOnly = readOnly;
        setSizeFull();
        setPadding(false);

        summary.addClassName("cf-summary");

        search.setPlaceholder("Cari keterangan...");
        search.setPrefixComponent(VaadinIcon.SEARCH.create());
        search.setClearButtonVisible(true);
        search.setValueChangeMode(ValueChangeMode.LAZY);
        search.addValueChangeListener(e -> reload());

        typeFilter.setLabel("Tipe");
        typeFilter.setItems(TransactionTypeFilter.values());
        typeFilter.setItemLabelGenerator(TransactionTypeFilter::label);
        typeFilter.setValue(TransactionTypeFilter.ALL);
        typeFilter.addValueChangeListener(e -> reload());

        sort.setLabel("Urutan");
        sort.setItems(SortOption.values());
        sort.setItemLabelGenerator(SortOption::label);
        sort.setValue(SortOption.NEWEST_FIRST);
        sort.addValueChangeListener(e -> reload());

        Button refresh = new Button("Muat ulang", VaadinIcon.REFRESH.create(), e -> reload());
        toolbar.setWidthFull();
        toolbar.setAlignItems(FlexComponent.Alignment.END);
        toolbar.setDefaultVerticalComponentAlignment(FlexComponent.Alignment.END);
        toolbar.add(search, typeFilter, sort, refresh);
        toolbar.addClassName("cf-toolbar");

        configureGrid();
        add(summary, toolbar, grid);
        expand(grid);
    }

    /** Toolbar agar view dapat menambah tombol (Generate AI, tambah, dll.). */
    public HorizontalLayout toolbar() {
        return toolbar;
    }

    public void setUser(Integer userId) {
        this.userId = userId;
        reload();
    }

    public void reload() {
        if (userId == null) {
            grid.setItems();
            summary.removeAll();
            return;
        }
        grid.setItems(finance.listTransactions(userId, search.getValue(), typeFilter.getValue(), sort.getValue()));
        renderSummary(finance.getBalance(userId));
    }

    private void renderSummary(Balance b) {
        summary.removeAll();
        summary.add(card("Total transaksi", String.valueOf(b.count()), ""),
                card("Pemasukan", presenter.money(b.income()), "cf-income"),
                card("Pengeluaran", presenter.money(b.expense()), "cf-expense"),
                card("Saldo", presenter.money(b.balance()), "cf-balance"));
    }

    private Div card(String title, String value, String extraClass) {
        Span t = new Span(title);
        t.addClassName("cf-summary-title");
        Span v = new Span(value);
        v.addClassName("cf-summary-value");
        Div card = new Div(t, v);
        card.addClassNames("cf-summary-card", extraClass.isEmpty() ? "cf-neutral" : extraClass);
        return card;
    }

    private void configureGrid() {
        GridStyles.apply(grid);
        grid.addColumn(Transaction::id).setHeader("ID").setSortable(true).setWidth("80px").setFlexGrow(0);
        grid.addColumn(Transaction::description).setHeader("Keterangan").setSortable(true).setFlexGrow(2);
        grid.addColumn(t -> presenter.money(t.amount())).setHeader("Jumlah")
                .setComparator(Comparator.comparingLong(Transaction::amount)).setTextAlign(
                        com.vaadin.flow.component.grid.ColumnTextAlign.END);
        grid.addColumn(new ComponentRenderer<>(this::typeBadge)).setHeader("Tipe")
                .setComparator(Comparator.comparing(Transaction::type));
        grid.addColumn(new ComponentRenderer<>(this::proofThumb)).setHeader("Bukti");
        grid.addColumn(t -> presenter.dateTime(t.createdAt())).setHeader("Waktu")
                .setComparator(Comparator.comparing(Transaction::createdAt));
        if (!readOnly) {
            grid.addColumn(new ComponentRenderer<>(t -> {
                Button delete = new Button(VaadinIcon.TRASH.create(), e -> confirmDelete(t));
                delete.addThemeVariants(ButtonVariant.LUMO_ERROR, ButtonVariant.LUMO_TERTIARY);
                delete.setAriaLabel("Hapus");
                return delete;
            })).setHeader("Aksi").setWidth("90px").setFlexGrow(0);
        }
    }

    private Span typeBadge(Transaction t) {
        Span badge = new Span(presenter.typeLabel(t.type()));
        badge.addClassNames("cf-type", t.type() == TransactionType.INCOME ? "cf-type-income" : "cf-type-expense");
        return badge;
    }

    private Div proofThumb(Transaction t) {
        Div box = new Div();
        if (t.proofPath() == null) {
            box.setText("-");
            return box;
        }
        Image img = new Image(FileStorage.urlOf(t.proofPath()), "Bukti");
        img.setHeight("40px");
        img.getStyle().set("cursor", "zoom-in").set("border-radius", "4px");
        img.addClickListener(e -> new ImagePreviewDialog(FileStorage.urlOf(t.proofPath())).open());
        box.add(img);
        return box;
    }

    private void confirmDelete(Transaction t) {
        Button cancel = new Button("Batal");
        Button ok = new Button("Hapus");
        ok.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_ERROR);
        var dialog = ViewSupport.modal("Hapus Transaksi", "Konfirmasi",
                new Span("Yakin menghapus transaksi ini? " + presenter.line(t)), cancel, ok);
        cancel.addClickListener(e -> dialog.close());
        ok.addClickListener(e -> {
            try {
                finance.deleteTransaction(userId, t.id());
                dialog.close();
                ViewSupport.success(presenter.deletedMessage(t));
                reload();
            } catch (DomainException ex) {
                ViewSupport.error(ex.getMessage());
            }
        });
        dialog.open();
    }
}
