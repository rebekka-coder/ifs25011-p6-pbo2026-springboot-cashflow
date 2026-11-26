package org.delcom.ifs25011p6pbo2026springbootcashflow.framework.view;

import org.delcom.ifs25011p6pbo2026springbootcashflow.adapter.storage.FileStorage;
import org.delcom.ifs25011p6pbo2026springbootcashflow.domain.entity.User;
import org.delcom.ifs25011p6pbo2026springbootcashflow.domain.exception.DomainException;
import org.delcom.ifs25011p6pbo2026springbootcashflow.domain.repository.IUserRepository;
import org.delcom.ifs25011p6pbo2026springbootcashflow.framework.auth.AuthSession;
import org.delcom.ifs25011p6pbo2026springbootcashflow.usecase.UserUseCase;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.progressbar.ProgressBar;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.upload.Upload;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.streams.UploadHandler;

@Route(value = "profile", layout = MainLayout.class)
@PageTitle("Profil | Cashflow")
public class ProfileView extends VerticalLayout {

    private byte[] photo;
    private String photoName;

    public ProfileView(IUserRepository userRepository, UserUseCase users, FileStorage storage) {
        User user = AuthSession.currentUser(userRepository);
        if (user == null) {
            return;
        }
        TextField fullName = new TextField("Nama lengkap", user.fullName(), "");
        fullName.setWidthFull();
        PasswordField password = new PasswordField("Password baru (kosongkan jika tidak diubah)");
        password.setWidthFull();

        Upload upload = new Upload(UploadHandler.inMemory((meta, data) -> {
            photo = data;
            photoName = meta.fileName();
        }));
        upload.setAcceptedFileTypes(".png", ".jpg", ".jpeg", ".gif", ".webp");
        upload.setMaxFiles(1);
        upload.setMaxFileSize(5 * 1024 * 1024);

        ProgressBar progress = new ProgressBar();
        progress.setIndeterminate(true);
        progress.setVisible(false);

        Button save = new Button("Simpan");
        save.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        save.addClickListener(e -> {
            progress.setVisible(true);
            try {
                users.updateProfile(user.id(), fullName.getValue(), password.getValue());
                if (photo != null) {
                    users.updatePhoto(user.id(), storage.save(photoName, photo));
                    photo = null;
                }
                ViewSupport.success("Profil disimpan");
                UI.getCurrent().getPage().reload(); // segarkan avatar & nama di header
            } catch (DomainException ex) {
                ViewSupport.error(ex.getMessage());
            } finally {
                progress.setVisible(false);
            }
        });

        PageCard card = new PageCard();
        card.setMaxWidth("520px");
        card.add(new H2("Profil"), new Span("Username: " + user.username()), fullName, password,
                new Span("Foto profil"), upload, progress, save);
        add(card);
    }
}
