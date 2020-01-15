package com.leyou.sms.listen;

import com.leyou.sms.utils.MailUtil;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.Map;

@Component
public class EmailListen {
    private final MailUtil mailUtil;

    @Autowired
    public EmailListen(MailUtil mailUtil) {
        this.mailUtil = mailUtil;
    }

    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(value = "awe.email.queue", durable = "true"),
            exchange = @Exchange(value = "awe.email.exchange",
                    ignoreDeclarationExceptions = "true"),
            key = {"email.verify.code"}))
    public void listenSms(Map<String, String> msg) {
        if (msg == null || msg.isEmpty()) {
            // 放弃处理
            return;
        }
        String email = msg.get("email");
        String code = msg.get("code");

        if (StringUtils.isEmpty(email) || StringUtils.isEmpty(code)) {
            // 放弃处理
            return;
        }
        try {
            mailUtil.sendMail(email,code);
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}
