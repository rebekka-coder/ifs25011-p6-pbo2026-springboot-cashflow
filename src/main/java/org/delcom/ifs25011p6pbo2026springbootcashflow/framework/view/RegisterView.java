package org.delcom.ifs25011p6pbo2026springbootcashflow.framework.view;

import org.delcom.ifs25011p6pbo2026springbootcashflow.domain.exception.DomainException;
import org.delcom.ifs25011p6pbo2026springbootcashflow.domain.repository.IUserRepository;
import org.delcom.ifs25011p6pbo2026springbootcashflow.framework.auth.AuthSession;
import org.delcom.ifs25011p6pbo2026springbootcashflow.usecase.AuthUseCase;
import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.H1;
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

@Route("register")
@PageTitle("Daftar | Cashflow")
public class RegisterView extends VerticalLayout implements BeforeEnterObserver {

    private final IUserRepository userRepository;
    private final AuthUseCase auth;
    private final TextField fullName = new TextField("Nama lengkap");
    private final TextField username = new TextField("Username");
    private final PasswordField password = new PasswordField("Password");
    private final ProgressBar progress = new ProgressBar();

    public RegisterView(IUserRepository userRepository, AuthUseCase auth) {
        getElement().setAttribute("role", "main");
        this.userRepository = userRepository;
        this.auth = auth;
        setSizeFull();
        setAlignItems(FlexComponent.Alignment.CENTER);
        setJustifyContentMode(FlexComponent.JustifyContentMode.CENTER);

        fullName.setWidthFull();
        username.setWidthFull();
        password.setWidthFull();
        progress.setIndeterminate(true);
        progress.setVisible(false);

        Button submit = new Button("Daftar", e -> doRegister());
        submit.setWidthFull();
        submit.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        submit.addClickShortcut(Key.ENTER);

        PageCard card = new PageCard();
        card.setWidth("380px");
        card.setMaxWidth("94vw");
        card.add(new H1("Daftar Akun"), fullName, username, password, progress, submit,
                new RouterLink("Sudah punya akun? Masuk", LoginView.class));
        add(card);
    }

    private void doRegister() {
        progress.setVisible(true);
        try {
            auth.register(username.getValue(), password.getValue(), fullName.getValue());
            AuthSession.login(auth.login(username.getValue(), password.getValue()));
            ViewSupport.success("Registrasi berhasil");
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
