package com.leyou.sms;

import com.aliyuncs.exceptions.ClientException;
import com.leyou.sms.utils.SmsUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
public class SmsTest {
    @Autowired
    private SmsUtils smsUtils;

    @Test
    public void test(){
        try {
            smsUtils.sendSms("15017246740", "443212", "awe商城", "SMS_179611881");
        } catch (ClientException e) {
            e.printStackTrace();
        }
    }
}
