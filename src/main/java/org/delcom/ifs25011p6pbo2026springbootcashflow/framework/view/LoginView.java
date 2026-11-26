package org.delcom.ifs25011p6pbo2026springbootcashflow.framework.view;

import org.delcom.ifs25011p6pbo2026springbootcashflow.domain.entity.User;
import org.delcom.ifs25011p6pbo2026springbootcashflow.domain.exception.DomainException;
import org.delcom.ifs25011p6pbo2026springbootcashflow.domain.repository.IUserRepository;
import org.delcom.ifs25011p6pbo2026springbootcashflow.framework.auth.AuthSession;
import org.delcom.ifs25011p6pbo2026springbootcashflow.usecase.AuthUseCase;
import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.progressbar.ProgressBar;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouterLink;

@Route("login")
@PageTitle("Masuk | Cashflow")
public class LoginView extends VerticalLayout implements BeforeEnterObserver {

    private final IUserRepository userRepository;
    private final AuthUseCase auth;
    private final TextField username = new TextField("Username");
    private final PasswordField password = new PasswordField("Password");
    private final ProgressBar progress = new ProgressBar();

    public LoginView(IUserRepository userRepository, AuthUseCase auth) {
        this.userRepository = userRepository;
        this.auth = auth;
        setSizeFull();
        setAlignItems(FlexComponent.Alignment.CENTER);
        setJustifyContentMode(FlexComponent.JustifyContentMode.CENTER);

        username.setWidthFull();
        password.setWidthFull();
        ViewSupport.nativeInputId(username, "login-username");
        ViewSupport.nativeInputId(password, "login-password");

        progress.setIndeterminate(true);
        progress.setVisible(false);

        Button submit = new Button("Masuk", e -> doLogin());
        submit.setId("login-submit");
        submit.setWidthFull();
        submit.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        submit.addClickShortcut(Key.ENTER);

        PageCard card = new PageCard();
        card.setWidth("380px");
        card.setMaxWidth("94vw");
        card.add(new H2("Masuk ke Cashflow"), username, password, progress, submit,
                new RouterLink("Belum punya akun? Daftar", RegisterView.class));
        add(card);
    }

    private void doLogin() {
        progress.setVisible(true);
        try {
            User user = auth.login(username.getValue(), password.getValue());
            AuthSession.login(user);
            UI.getCurrent().navigate(FinanceView.class);
        } catch (DomainException e) {
            ViewSupport.error(e.getMessage());
        } finally {
            progress.setVisible(false);
        }
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        if (AuthSession.currentUser(userRepository) != null) {
            event.forwardTo(FinanceView.class);
        }
    }
}
