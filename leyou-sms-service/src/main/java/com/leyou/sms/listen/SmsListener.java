package com.leyou.sms.listen;

import com.leyou.sms.config.SmsProperties;
import com.leyou.sms.utils.SmsUtils;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.Map;

@Component
@EnableConfigurationProperties(SmsProperties.class)
@Deprecated
/**
 * 短信监听器，由于没钱买短信服务，所以暂时只能停用
 */
public class SmsListener {

    private final SmsUtils smsUtils;

    private final SmsProperties prop;

    @Autowired
    public SmsListener(SmsUtils smsUtils, SmsProperties prop) {
        this.smsUtils = smsUtils;
        this.prop = prop;
    }

    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(value = "leyou.sms.queue", durable = "true"),
            exchange = @Exchange(value = "leyou.sms.exchange",
                    ignoreDeclarationExceptions = "true"),
            key = {"sms.verify.code"}))
    public void listenSms(Map<String, String> msg) throws Exception {
        if (msg == null || msg.isEmpty()) {
            // 放弃处理
            return;
        }
        String phone = msg.get("phone");
        String code = msg.get("code");

        if (StringUtils.isEmpty(phone) || StringUtils.isEmpty(code)) {
            // 放弃处理
            return;
        }
        // 发送消息
        this.smsUtils.sendSms(phone, code,
                prop.getSignName(),
                prop.getVerifyCodeTemplate());

    }
}