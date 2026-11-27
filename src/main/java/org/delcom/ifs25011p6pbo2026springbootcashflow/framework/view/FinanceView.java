package org.delcom.ifs25011p6pbo2026springbootcashflow.framework.view;

import org.delcom.ifs25011p6pbo2026springbootcashflow.adapter.presenter.FinancePresenter;
import org.delcom.ifs25011p6pbo2026springbootcashflow.adapter.storage.FileStorage;
import org.delcom.ifs25011p6pbo2026springbootcashflow.domain.entity.AiGeneratedTransaction;
import org.delcom.ifs25011p6pbo2026springbootcashflow.domain.entity.Transaction;
import org.delcom.ifs25011p6pbo2026springbootcashflow.domain.entity.TransactionType;
import org.delcom.ifs25011p6pbo2026springbootcashflow.domain.entity.User;
import org.delcom.ifs25011p6pbo2026springbootcashflow.domain.exception.DomainException;
import org.delcom.ifs25011p6pbo2026springbootcashflow.domain.repository.IUserRepository;
import org.delcom.ifs25011p6pbo2026springbootcashflow.framework.auth.AuthSession;
import org.delcom.ifs25011p6pbo2026springbootcashflow.usecase.FinanceAiUseCase;
import org.delcom.ifs25011p6pbo2026springbootcashflow.usecase.FinanceUseCase;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.progressbar.ProgressBar;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.upload.Upload;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.streams.UploadHandler;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.atomic.AtomicReference;

@Route(value = "", layout = MainLayout.class)
@PageTitle("Cashflow")
public class FinanceView extends VerticalLayout {

    private final FinanceUseCase finance;
    private final FinanceAiUseCase ai;
    private final FinancePresenter presenter;
    private final FileStorage storage;
    private final User user;
    private final TransactionPanel panel;

    public FinanceView(IUserRepository userRepository, FinanceUseCase finance, FinanceAiUseCase ai,
                       FinancePresenter presenter, FileStorage storage) {
        getElement().setAttribute("role", "main");
        this.finance = finance;
        this.ai = ai;
        this.presenter = presenter;
        this.storage = storage;
        this.user = AuthSession.currentUser(userRepository);
        this.panel = new TransactionPanel(finance, presenter, false);
        setSizeFull();
        if (user == null) {
            return; // MainLayout akan mengarahkan ke /login
        }

        Button aiButton = new Button("Generate AI", VaadinIcon.MAGIC.create(), e -> openAiDialog());
        Button income = new Button("Pemasukan", VaadinIcon.PLUS.create(), e -> openForm(TransactionType.INCOME));
        Button expense = new Button("Pengeluaran", VaadinIcon.MINUS.create(), e -> openForm(TransactionType.EXPENSE));
        income.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_SUCCESS);
        expense.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_ERROR);
        panel.toolbar().add(aiButton, income, expense);
        add(new H1("Catatan Keuangan"), panel);
        expand(panel);
        panel.setUser(user.id());
    }

    // ---------------- Form tambah pemasukan / pengeluaran ----------------
    private void openForm(TransactionType type) {
        TextField description = new TextField("Keterangan");
        description.setWidthFull();
        TextField amount = new TextField("Jumlah (Rp)");
        amount.setPlaceholder("contoh: 150000");
        amount.setWidthFull();

        byte[][] file = new byte[1][];
        String[] fileName = new String[1];
        Upload upload = new Upload(UploadHandler.inMemory((meta, data) -> {
            file[0] = data;
            fileName[0] = meta.fileName();
        }));
        upload.setAcceptedFileTypes(".png", ".jpg", ".jpeg", ".gif", ".webp");
        upload.setMaxFiles(1);
        upload.setMaxFileSize(5 * 1024 * 1024);
        upload.getElement().addEventListener("file-remove", e -> {
            file[0] = null;
            fileName[0] = null;
        });

        ProgressBar progress = new ProgressBar();
        progress.setIndeterminate(true);
        progress.setVisible(false);

        VerticalLayout body = new VerticalLayout(description, amount, new com.vaadin.flow.component.html.Span(
                "Bukti transaksi (opsional)"), upload, progress);
        body.setPadding(true);

        Button cancel = new Button("Batal");
        Button save = new Button("Simpan");
        save.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        var dialog = ViewSupport.modal("Tambah " + type.label(), type.label(), body, cancel, save);
        cancel.addClickListener(e -> dialog.close());
        save.addClickListener(e -> {
            progress.setVisible(true);
            save.setEnabled(false);
            try {
                String proof = file[0] == null ? null : storage.save(fileName[0], file[0]);
                Transaction t = finance.addTransaction(user.id(), description.getValue(), amount.getValue(), type, proof);
                dialog.close();
                ViewSupport.success(presenter.addedMessage(t));
                panel.reload();
            } catch (DomainException ex) {
                ViewSupport.error(ex.getMessage());
            } finally {
                progress.setVisible(false);
                save.setEnabled(true);
            }
        });
        dialog.open();
    }

    // ---------------- Generate AI (async + ProgressBar) ----------------
    private void openAiDialog() {
        TextArea prompt = new TextArea("Catatan keuangan");
        prompt.setPlaceholder("contoh: Gajian 5 juta, bayar kos 1,2 juta, makan siang 35 ribu");
        prompt.setWidthFull();
        prompt.setMinHeight("110px");

        ProgressBar progress = new ProgressBar();
        progress.setIndeterminate(true);
        progress.setVisible(false);

        Grid<AiGeneratedTransaction> preview = new Grid<>(AiGeneratedTransaction.class, false);
        preview.addColumn(AiGeneratedTransaction::description).setHeader("Keterangan").setFlexGrow(2);
        preview.addColumn(t -> presenter.money(t.amount())).setHeader("Jumlah");
        preview.addColumn(t -> presenter.typeLabel(t.type())).setHeader("Tipe");
        preview.setAllRowsVisible(true);
        preview.setVisible(false);

        VerticalLayout body = new VerticalLayout(prompt, progress, preview);
        body.setPadding(true);

        Button cancel = new Button("Tutup");
        Button process = new Button("Proses AI", VaadinIcon.MAGIC.create());
        process.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        Button add = new Button("Tambahkan ke Catatan");
        add.addThemeVariants(ButtonVariant.LUMO_SUCCESS, ButtonVariant.LUMO_PRIMARY);
        add.setEnabled(false);
        var dialog = ViewSupport.modal("Generate AI", "AI", body, cancel, process, add);
        AtomicReference<List<AiGeneratedTransaction>> result = new AtomicReference<>(List.of());

        cancel.addClickListener(e -> dialog.close());
        process.addClickListener(e -> {
            UI ui = UI.getCurrent();
            String text = prompt.getValue();
            progress.setVisible(true);
            process.setEnabled(false);
            ui.setPollInterval(500); // agar hasil async tampil tanpa @Push
            CompletableFuture.supplyAsync(() -> {
                try {
                    return ai.generateTransactions(text);
                } catch (DomainException ex) {
                    throw new CompletionException(ex);
                }
            }).whenComplete((items, error) -> ui.access(() -> {
                ui.setPollInterval(-1);
                progress.setVisible(false);
                process.setEnabled(true);
                if (error != null) {
                    ViewSupport.error(error.getCause() != null ? error.getCause().getMessage() : error.getMessage());
                    return;
                }
                result.set(items);
                preview.setItems(items);
                preview.setVisible(true);
                add.setEnabled(!items.isEmpty());
                if (items.isEmpty()) {
                    ViewSupport.error("AI tidak menghasilkan transaksi yang valid");
                }
            }));
        });
        add.addClickListener(e -> {
            try {
                int count = ai.saveGeneratedTransactions(user.id(), result.get());
                dialog.close();
                ViewSupport.success(presenter.aiSavedMessage(count));
                panel.reload();
            } catch (DomainException ex) {
                ViewSupport.error(ex.getMessage());
            }
        });
        dialog.open();
    }
}
