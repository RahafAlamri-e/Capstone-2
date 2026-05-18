package rahafalamri.github.com.bookshare.Service;

import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class WhatsAppService {

    @Value("${twilio.account.sid}")
    private String accountSid;

    @Value("${twilio.auth.token}")
    private String authToken;

    @Value("${twilio.whatsapp.number}")
    private String fromNumber;

    public void sendWhatsAppMessage(String toMobile, String body) {

        Twilio.init(accountSid, authToken);

        Message.creator(
                new PhoneNumber("whatsapp:+966" + toMobile.substring(1)),
                new PhoneNumber("whatsapp:" + fromNumber),
                body
        ).create();
    }
}
