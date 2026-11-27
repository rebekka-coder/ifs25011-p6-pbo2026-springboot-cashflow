package org.delcom.ifs25011p6pbo2026springbootcashflow.framework.view;

import org.delcom.ifs25011p6pbo2026springbootcashflow.adapter.presenter.FinancePresenter;
import org.delcom.ifs25011p6pbo2026springbootcashflow.domain.entity.User;
import org.delcom.ifs25011p6pbo2026springbootcashflow.domain.repository.IUserRepository;
import org.delcom.ifs25011p6pbo2026springbootcashflow.framework.auth.AuthSession;
import org.delcom.ifs25011p6pbo2026springbootcashflow.usecase.FinanceUseCase;
import org.delcom.ifs25011p6pbo2026springbootcashflow.usecase.UserUseCase;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.BeforeEvent;
import com.vaadin.flow.router.HasUrlParameter;
import com.vaadin.flow.router.OptionalParameter;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

import java.util.List;

/** Peninjauan transaksi pengguna lain, read-only (ADMIN saja). */
@Route(value = "user-transactions", layout = MainLayout.class)
@PageTitle("Transaksi Pengguna | Cashflow")
public class UserTransactionsView extends VerticalLayout implements HasUrlParameter<Integer>, BeforeEnterObserver {

    private final IUserRepository userRepository;
    private ComboBox<User> picker;
    private List<User> allUsers = List.of();

    public UserTransactionsView(IUserRepository userRepository, UserUseCase users,
                                FinanceUseCase finance, FinancePresenter presenter) {
        getElement().setAttribute("role", "main");
        this.userRepository = userRepository;
        setSizeFull();
        User current = AuthSession.currentUser(userRepository);
        if (current == null || !current.isAdmin()) {
            return;
        }
        allUsers = users.listUsers();
        TransactionPanel panel = new TransactionPanel(finance, presenter, true);
        panel.setVisible(false);

        picker = new ComboBox<>("Pilih pengguna");
        picker.setItems(allUsers);
        picker.setItemLabelGenerator(u -> u.fullName() + " (" + u.username() + ")");
        picker.setWidth("320px");
        picker.addValueChangeListener(e -> {
            panel.setVisible(e.getValue() != null);
            panel.setUser(e.getValue() == null ? null : e.getValue().id());
        });
        add(new H1("Transaksi Pengguna (read-only)"), picker, panel);
        expand(panel);
    }

    @Override
    public void setParameter(BeforeEvent event, @OptionalParameter Integer userId) {
        if (picker != null && userId != null) {
            allUsers.stream().filter(u -> u.id() == userId).findFirst().ifPresent(picker::setValue);
        }
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        User current = AuthSession.currentUser(userRepository);
        if (current != null && !current.isAdmin()) {
            event.forwardTo(FinanceView.class);
        }
    }
}
