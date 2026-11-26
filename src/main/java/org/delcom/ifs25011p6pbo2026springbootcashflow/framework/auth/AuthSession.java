package org.delcom.ifs25011p6pbo2026springbootcashflow.framework.auth;

import org.delcom.ifs25011p6pbo2026springbootcashflow.domain.entity.User;
import org.delcom.ifs25011p6pbo2026springbootcashflow.domain.repository.IUserRepository;
import com.vaadin.flow.server.VaadinRequest;
import com.vaadin.flow.server.VaadinResponse;
import com.vaadin.flow.server.VaadinService;
import com.vaadin.flow.server.VaadinSession;
import jakarta.servlet.http.Cookie;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.HexFormat;
import java.util.Optional;
import java.util.UUID;

/**
 * Sesi login: user disimpan di VaadinSession + cookie bertanda tangan (cashflow_uid).
 * LOGGED_OUT_KEY mencegah sesi dipulihkan otomatis setelah logout.
 */
public final class AuthSession {

    public static final String LOGGED_OUT_KEY = "cashflow_logged_out";
    private static final String USER_KEY = "cashflow_user_id";
    private static final String COOKIE = "cashflow_uid";
    /** Rahasia penanda tangan cookie; berganti tiap aplikasi start (sesi lama otomatis tidak berlaku). */
    private static final String SECRET = UUID.randomUUID().toString();

    private AuthSession() {
    }

    public static void login(User user) {
        VaadinSession session = VaadinSession.getCurrent();
        session.setAttribute(USER_KEY, user.id());
        session.setAttribute(LOGGED_OUT_KEY, Boolean.FALSE);
        writeCookie(user.id() + "." + sign(user.id()), 60 * 60 * 8);
    }

    public static void logout() {
        VaadinSession session = VaadinSession.getCurrent();
        session.setAttribute(LOGGED_OUT_KEY, Boolean.TRUE);
        session.setAttribute(USER_KEY, null);
        writeCookie("", 0);
    }

    /** User yang sedang login, atau null. Memulihkan sesi dari cookie setelah refresh. */
    public static User currentUser(IUserRepository users) {
        VaadinSession session = VaadinSession.getCurrent();
        if (session == null || Boolean.TRUE.equals(session.getAttribute(LOGGED_OUT_KEY))) {
            return null;
        }
        Object id = session.getAttribute(USER_KEY);
        if (id == null) {
            id = readCookie();
        }
        if (id == null) {
            return null;
        }
        Optional<User> user = users.findById((Integer) id);
        user.ifPresent(u -> session.setAttribute(USER_KEY, u.id()));
        return user.orElse(null);
    }

    private static Integer readCookie() {
        VaadinRequest request = VaadinService.getCurrentRequest();
        if (request == null || request.getCookies() == null) {
            return null;
        }
        for (Cookie cookie : request.getCookies()) {
            if (COOKIE.equals(cookie.getName())) {
                String[] parts = cookie.getValue().split("\\.");
                if (parts.length == 2 && parts[0].matches("\\d+") && sign(Integer.parseInt(parts[0])).equals(parts[1])) {
                    return Integer.parseInt(parts[0]);
                }
            }
        }
        return null;
    }

    private static void writeCookie(String value, int maxAgeSeconds) {
        VaadinResponse response = VaadinService.getCurrentResponse();
        if (response == null) {
            return;
        }
        Cookie cookie = new Cookie(COOKIE, value);
        cookie.setHttpOnly(true);
        cookie.setPath("/");
        cookie.setMaxAge(maxAgeSeconds);
        response.addCookie(cookie);
    }

    private static String sign(int id) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(SECRET.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            return HexFormat.of().formatHex(mac.doFinal(Integer.toString(id).getBytes(StandardCharsets.UTF_8)));
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }
}
