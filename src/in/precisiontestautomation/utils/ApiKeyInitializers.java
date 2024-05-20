package in.precisiontestautomation.utils;

import in.precisiontestautomation.scriptlessautomation.core.utils.CoreKeyInitializers;
import io.restassured.response.Response;
import lombok.Getter;
import in.precisiontestautomation.apifactory.MailingServices;

import java.util.HashMap;
import java.util.Map;

/**
 * <p>KeyInitializers class.</p>
 *
 * @author PTA-dev
 * @version 1.2
 * @since 2024-05-02
 */
public class ApiKeyInitializers extends CoreKeyInitializers {

    protected ApiKeyInitializers() {
    }

    @Getter
    private static final ThreadLocal<MailingServices> mailingServices = new ThreadLocal<>();

    @Getter
    private static final ThreadLocal<Map<String, Object>> globalVariables = ThreadLocal.withInitial(HashMap::new);

    @Getter
    private static ThreadLocal<Response> response = new ThreadLocal<>();
}
