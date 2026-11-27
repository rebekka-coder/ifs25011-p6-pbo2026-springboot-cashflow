package org.delcom.ifs25011p6pbo2026springbootcashflow.framework.view;

import org.delcom.ifs25011p6pbo2026springbootcashflow.adapter.storage.FileStorage;
import org.delcom.ifs25011p6pbo2026springbootcashflow.domain.entity.User;
import org.delcom.ifs25011p6pbo2026springbootcashflow.domain.repository.IUserRepository;
import org.delcom.ifs25011p6pbo2026springbootcashflow.framework.auth.AuthSession;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.applayout.DrawerToggle;
import com.vaadin.flow.component.avatar.Avatar;
import com.vaadin.flow.component.contextmenu.ContextMenu;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.sidenav.SideNav;
import com.vaadin.flow.component.sidenav.SideNavItem;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;

/** Layout: sidebar, menu avatar (Profil / Keluar), dan guard login global. */
public class MainLayout extends AppLayout implements BeforeEnterObserver {

    private final IUserRepository userRepository;
    private final HorizontalLayout header = new HorizontalLayout();
    private final SideNav nav = new SideNav();
    private final DrawerToggle toggle = new DrawerToggle();
    private Integer builtFor;

    public MainLayout(IUserRepository userRepository) {
        this.userRepository = userRepository;
        setPrimarySection(Section.DRAWER);
        header.setWidthFull();
        header.setAlignItems(FlexComponent.Alignment.CENTER);
        header.addClassName("cf-header");
        header.getElement().setAttribute("role", "banner");
        addToNavbar(header);
        Div brand = new Div("Cashflow");
        brand.addClassName("cf-brand");
        Div menu = new Div(brand, nav);
        menu.getElement().setAttribute("role", "navigation");
        menu.getElement().setAttribute("aria-label", "Menu utama");
        addToDrawer(menu);
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        User user = AuthSession.currentUser(userRepository);
        if (user == null) {
            event.forwardTo(LoginView.class);
            return;
        }
        if (!Integer.valueOf(user.id()).equals(builtFor)) {
            populate(user);
            builtFor = user.id();
        }
    }

    private void populate(User user) {
        nav.removeAll();
        nav.addItem(new SideNavItem("Cashflow", FinanceView.class, VaadinIcon.WALLET.create()));
        nav.addItem(new SideNavItem("Profil", ProfileView.class, VaadinIcon.USER.create()));
        if (user.isAdmin()) {
            nav.addItem(new SideNavItem("Pengguna", UsersView.class, VaadinIcon.USERS.create()));
            nav.addItem(new SideNavItem("Transaksi Pengguna", UserTransactionsView.class, VaadinIcon.LIST.create()));
        }

        Avatar avatar = new Avatar(user.fullName());
        if (user.photoPath() != null) {
            avatar.setImage(FileStorage.urlOf(user.photoPath()));
        }
        avatar.getStyle().set("cursor", "pointer");
        ContextMenu menu = new ContextMenu(avatar);
        menu.setOpenOnClick(true);
        menu.addItem("Profil", e -> UI.getCurrent().navigate(ProfileView.class));
        menu.addItem("Keluar", e -> {
            AuthSession.logout();
            UI.getCurrent().navigate(LoginView.class);
        });

        Span spacer = new Span();
        header.removeAll();
        header.add(toggle, spacer, new Span(user.fullName() + " (" + user.role() + ")"), avatar);
        header.expand(spacer);
    }
}
