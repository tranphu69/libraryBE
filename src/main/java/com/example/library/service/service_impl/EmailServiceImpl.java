package com.example.library.service.service_impl;

import com.example.library.exception.BusinessException;
import com.example.library.exception.ErrorCode;
import com.example.library.service.EmailService;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
@Slf4j
public class EmailServiceImpl implements EmailService {
    private final JavaMailSender mailSender;
    @Value("${spring.mail.username}")
    private String fromEmail;
    private static final int OTP_VALID_MINUTES = 2;

    private String buildOtpHtml(String otp) {
        return """
                <div style="font-family: Arial, sans-serif; max-width: 480px; margin: auto;">
                    <h2>Mã xác thực đăng nhập</h2>
                    <p>Mã OTP của bạn là:</p>
                    <div style="font-size: 32px; font-weight: bold; letter-spacing: 6px; margin: 16px 0;">
                        %s
                    </div>
                    <p>Mã có hiệu lực trong <b>%d phút</b>. Vui lòng không chia sẻ mã này với bất kỳ ai.</p>
                    <p style="color: #888; font-size: 12px;">Nếu bạn không thực hiện yêu cầu đăng nhập này, vui lòng bỏ qua email.</p>
                </div>
                """.formatted(otp, OTP_VALID_MINUTES);
    }

    @Override
    public void sendOtpEmail(String toEmail, String otp) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom(fromEmail);
            helper.setTo(toEmail);
            helper.setSubject("Mã xác thực đăng nhập (OTP)");
            helper.setText(buildOtpHtml(otp), true);
            mailSender.send(message);
        } catch (MessagingException e) {
            throw new BusinessException(ErrorCode.FAILED_EMAIL);
        }
    }
}
