package org.delcom.ifs25011p6pbo2026springbootcashflow.framework.view;

import org.delcom.ifs25011p6pbo2026springbootcashflow.domain.entity.User;
import org.delcom.ifs25011p6pbo2026springbootcashflow.domain.repository.IUserRepository;
import org.delcom.ifs25011p6pbo2026springbootcashflow.framework.auth.AuthSession;
import org.delcom.ifs25011p6pbo2026springbootcashflow.usecase.UserUseCase;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

/** Daftar pengguna (ADMIN saja). */
@Route(value = "users", layout = MainLayout.class)
@PageTitle("Pengguna | Cashflow")
public class UsersView extends VerticalLayout implements BeforeEnterObserver {

    private final IUserRepository userRepository;

    public UsersView(IUserRepository userRepository, UserUseCase users) {
        this.userRepository = userRepository;
        setSizeFull();
        User current = AuthSession.currentUser(userRepository);
        if (current == null || !current.isAdmin()) {
            return;
        }
        Grid<User> grid = new Grid<>(User.class, false);
        GridStyles.apply(grid);
        grid.addColumn(User::id).setHeader("ID").setSortable(true).setWidth("80px").setFlexGrow(0);
        grid.addColumn(User::username).setHeader("Username").setSortable(true);
        grid.addColumn(User::fullName).setHeader("Nama").setSortable(true);
        grid.addColumn(u -> u.role().name()).setHeader("Role").setSortable(true);
        grid.addColumn(new ComponentRenderer<>(u -> new Button("Lihat transaksi",
                e -> UI.getCurrent().navigate(UserTransactionsView.class, u.id())))).setHeader("Aksi");
        grid.setItems(users.listUsers());

        add(new H2("Daftar Pengguna"), grid);
        expand(grid);
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        User current = AuthSession.currentUser(userRepository);
        if (current != null && !current.isAdmin()) {
            event.forwardTo(FinanceView.class);
        }
    }
}
