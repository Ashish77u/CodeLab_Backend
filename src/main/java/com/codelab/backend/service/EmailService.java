package com.codelab.backend.service;

import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${app.frontend-url}")
    private String frontendUrl;

    @Value("${spring.mail.username}")
    private String fromEmail;

    // ── Send Verification Email ───────────────────────────

    @Async
    public void sendVerificationEmail(String toEmail,
                                      String username, String token) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper =
                    new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(toEmail);
            helper.setSubject("Verify your CodeLab account");

            String verifyUrl = frontendUrl
                    + "/verify-email?token=" + token;

            String html = """
                <!DOCTYPE html>
                <html>
                <body style="margin:0;padding:0;background:#0d1117;
                    font-family:'DM Sans',Arial,sans-serif;">
                  <div style="max-width:560px;margin:40px auto;
                      background:#161b22;border-radius:12px;
                      border:1px solid #21262d;overflow:hidden;">

                    <!-- Header -->
                    <div style="background:#161b22;padding:32px;
                        text-align:center;border-bottom:1px solid #21262d;">
                      <span style="font-size:24px;font-weight:800;
                          color:#00ff88;font-family:Arial;">
                        CODE<span style="color:#e6edf3;">LAB</span>
                      </span>
                    </div>

                    <!-- Body -->
                    <div style="padding:32px;">
                      <h2 style="color:#e6edf3;margin:0 0 8px;
                          font-size:22px;">
                        Verify your email address
                      </h2>
                      <p style="color:#7d8590;line-height:1.7;
                          margin:0 0 24px;">
                        Hi <strong style="color:#e6edf3;">%s</strong>,
                        thanks for registering on CodeLab!
                        Please verify your email to activate your account.
                      </p>

                      <a href="%s"
                        style="display:inline-block;padding:13px 28px;
                        background:#00ff88;color:#0d1117;
                        text-decoration:none;border-radius:8px;
                        font-weight:700;font-size:15px;">
                        Verify Email Address →
                      </a>

                      <p style="color:#7d8590;font-size:13px;
                          margin:24px 0 0;line-height:1.7;">
                        This link expires in
                        <strong style="color:#e6edf3;">24 hours</strong>.
                        If you did not create an account, ignore this email.
                      </p>

                      <p style="color:#444;font-size:12px;margin:16px 0 0;">
                        Or copy this link:<br/>
                        <span style="color:#7d8590;word-break:break-all;">
                          %s
                        </span>
                      </p>
                    </div>

                    <!-- Footer -->
                    <div style="padding:20px 32px;
                        border-top:1px solid #21262d;
                        text-align:center;">
                      <p style="color:#444;font-size:12px;margin:0;">
                        © 2026 CodeLab. All rights reserved.
                      </p>
                    </div>
                  </div>
                </body>
                </html>
                """.formatted(username, verifyUrl, verifyUrl);

            helper.setText(html, true);
            mailSender.send(message);
            log.info("Verification email sent to: {}", toEmail);

        } catch (Exception e) {
            log.error("Failed to send verification email to {}: {}",
                    toEmail, e.getMessage());
        }
    }

    // ── Send Welcome Email ────────────────────────────────

    @Async
    public void sendWelcomeEmail(String toEmail, String username) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper =
                    new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(toEmail);
            helper.setSubject("Welcome to CodeLab! 🎉");

            String html = """
                <!DOCTYPE html>
                <html>
                <body style="margin:0;padding:0;background:#0d1117;
                    font-family:'DM Sans',Arial,sans-serif;">
                  <div style="max-width:560px;margin:40px auto;
                      background:#161b22;border-radius:12px;
                      border:1px solid #21262d;overflow:hidden;">

                    <!-- Header -->
                    <div style="background:#161b22;padding:32px;
                        text-align:center;
                        border-bottom:1px solid #21262d;">
                      <span style="font-size:24px;font-weight:800;
                          color:#00ff88;font-family:Arial;">
                        CODE<span style="color:#e6edf3;">LAB</span>
                      </span>
                    </div>

                    <!-- Body -->
                    <div style="padding:32px;">
                      <div style="font-size:40px;
                          margin-bottom:16px;">🎉</div>
                      <h2 style="color:#e6edf3;margin:0 0 8px;
                          font-size:22px;">
                        Welcome to CodeLab, %s!
                      </h2>
                      <p style="color:#7d8590;line-height:1.7;
                          margin:0 0 24px;">
                        Your account is now active. You can now
                        browse projects, upload your own source code,
                        and connect with other developers.
                      </p>

                      <!-- Features -->
                      <div style="background:#0d1117;border-radius:8px;
                          padding:20px;margin-bottom:24px;">
                        <div style="margin-bottom:12px;">
                          <span style="color:#00ff88;">✓</span>
                          <span style="color:#e6edf3;margin-left:8px;">
                            Browse thousands of source code projects
                          </span>
                        </div>
                        <div style="margin-bottom:12px;">
                          <span style="color:#00ff88;">✓</span>
                          <span style="color:#e6edf3;margin-left:8px;">
                            Upload and share your own projects
                          </span>
                        </div>
                        <div style="margin-bottom:12px;">
                          <span style="color:#00ff88;">✓</span>
                          <span style="color:#e6edf3;margin-left:8px;">
                            Download ready-to-use source code
                          </span>
                        </div>
                        <div>
                          <span style="color:#00ff88;">✓</span>
                          <span style="color:#e6edf3;margin-left:8px;">
                            Build your developer profile
                          </span>
                        </div>
                      </div>

                      <a href="%s"
                        style="display:inline-block;padding:13px 28px;
                        background:#00ff88;color:#0d1117;
                        text-decoration:none;border-radius:8px;
                        font-weight:700;font-size:15px;">
                        Explore CodeLab →
                      </a>
                    </div>

                    <!-- Footer -->
                    <div style="padding:20px 32px;
                        border-top:1px solid #21262d;
                        text-align:center;">
                      <p style="color:#444;font-size:12px;margin:0;">
                        © 2026 CodeLab. All rights reserved.
                      </p>
                    </div>
                  </div>
                </body>
                </html>
                """.formatted(username, frontendUrl);

            helper.setText(html, true);
            mailSender.send(message);
            log.info("Welcome email sent to: {}", toEmail);

        } catch (Exception e) {
            log.error("Failed to send welcome email to {}: {}",
                    toEmail, e.getMessage());
        }
    }
}