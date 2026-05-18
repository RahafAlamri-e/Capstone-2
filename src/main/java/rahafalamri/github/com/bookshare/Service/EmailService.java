package rahafalamri.github.com.bookshare.Service;

import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.FileSystemResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import rahafalamri.github.com.bookshare.Model.Book;
import rahafalamri.github.com.bookshare.Model.User;

import java.io.File;
import java.io.FileOutputStream;

@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

    public void sendSimpleEmail(String to, String subject, String message) {

        try {

            MimeMessage mimeMessage = mailSender.createMimeMessage();

            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage);

            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(message);

            mailSender.send(mimeMessage);

        } catch (MessagingException e) {
            throw new RuntimeException(e);
        }
    }


    public void sendEmailWithPdf(String to, String subject, String message, String fileName) {

        try {

            MimeMessage mimeMessage = mailSender.createMimeMessage();

            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true);

            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(message);

            FileSystemResource file = new FileSystemResource(new File(fileName));

            helper.addAttachment(fileName, file);

            mailSender.send(mimeMessage);

        } catch (MessagingException e) {
            throw new RuntimeException(e);
        }
    }

    public void sendBorrowingTicket(User borrower, Book book, String returnDate) {

        String fileName = "Borrowing_Contract_" + ".pdf";

        createBorrowingPdf(borrower, book, returnDate, fileName);

        String message =
                "Hello " + borrower.getName() + ",\n\n"
                        + "Your borrowing request has been completed successfully.\n\n"
                        + "Book Title: " + book.getTitle() + "\n"
                        + "Rental Price: " + book.getRentalPrice() + " SAR\n"
                        + "Deposit Amount: " + book.getDepositAmount() + " SAR\n"
                        + "Total Paid: " + (book.getRentalPrice() + book.getDepositAmount()) + " SAR\n"
                        + "Return Date: " + returnDate + "\n\n"
                        + "Attached is your borrowing contract PDF.\n\n"
                        + "Thank you for using BookShare.";

        sendEmailWithPdf(
                borrower.getEmail(),
                "Borrowing Contract - " + book.getTitle(),
                message,
                fileName
        );
    }

    private void createBorrowingPdf(User borrower, Book book, String returnDate, String fileName) {

        Double totalPaid = book.getRentalPrice() + book.getDepositAmount();

        String html =
                "<html>" +
                        "<head>" +
                        "<style>" +
                        "body {" +
                        "font-family: Arial, sans-serif;" +
                        "padding: 35px;" +
                        "color: #1f2937;" +
                        "background-color: #ffffff;" +
                        "}" +

                        ".header {" +
                        "background-color: #5b2c83;" +
                        "color: white;" +
                        "padding: 22px;" +
                        "border-radius: 10px;" +
                        "margin-bottom: 25px;" +
                        "}" +

                        "h1 {" +
                        "margin: 0;" +
                        "font-size: 26px;" +
                        "}" +

                        ".subtitle {" +
                        "margin-top: 6px;" +
                        "font-size: 13px;" +
                        "}" +

                        ".section {" +
                        "border: 1px solid #dddddd;" +
                        "border-radius: 10px;" +
                        "padding: 16px;" +
                        "margin-bottom: 18px;" +
                        "}" +

                        ".section-title {" +
                        "font-size: 18px;" +
                        "font-weight: bold;" +
                        "color: #5b2c83;" +
                        "margin-bottom: 12px;" +
                        "}" +

                        "p {" +
                        "font-size: 14px;" +
                        "margin: 7px 0;" +
                        "}" +

                        ".amount {" +
                        "font-weight: bold;" +
                        "color: #d35400;" +
                        "}" +

                        ".terms p {" +
                        "font-size: 13px;" +
                        "line-height: 1.5;" +
                        "}" +

                        ".footer {" +
                        "margin-top: 30px;" +
                        "font-size: 12px;" +
                        "color: #6b7280;" +
                        "text-align: center;" +
                        "}" +
                        "</style>" +
                        "</head>" +

                        "<body>" +

                        "<div class='header'>" +
                        "<h1>BookShare Borrowing Contract</h1>" +
                        "<div class='subtitle'>Official borrowing agreement generated by BookShare system</div>" +
                        "</div>" +

                        "<div class='section'>" +
                        "<div class='section-title'>Borrower Details</div>" +
                        "<p><b>Name:</b> " + borrower.getName() + "</p>" +
                        "<p><b>Email:</b> " + borrower.getEmail() + "</p>" +
                        "<p><b>Phone:</b> " + borrower.getPhoneNumber() + "</p>" +
                        "</div>" +

                        "<div class='section'>" +
                        "<div class='section-title'>Book Details</div>" +
                        "<p><b>Book Title:</b> " + book.getTitle() + "</p>" +
                        "<p><b>Category:</b> " + book.getCategory() + "</p>" +
                        "<p><b>Description:</b> " + book.getDescription() + "</p>" +
                        "</div>" +

                        "<div class='section'>" +
                        "<div class='section-title'>Payment Details</div>" +
                        "<p><b>Rental Price:</b> <span class='amount'>" + book.getRentalPrice() + " SAR</span></p>" +
                        "<p><b>Deposit Amount:</b> <span class='amount'>" + book.getDepositAmount() + " SAR</span></p>" +
                        "<p><b>Total Paid:</b> <span class='amount'>" + totalPaid + " SAR</span></p>" +
                        "<p><b>Return Date:</b> " + returnDate + "</p>" +
                        "</div>" +

                        "<div class='section terms'>" +
                        "<div class='section-title'>Terms and Conditions</div>" +
                        "<p>1. The book must be returned on or before the return date.</p>" +
                        "<p>2. The deposit will be refunded if the book is returned in good condition.</p>" +
                        "<p>3. If the book is damaged, a damage fee may be deducted from the deposit.</p>" +
                        "<p>4. If the book is lost, the full deposit will be transferred to the book owner.</p>" +
                        "<p>5. Late return may cause temporary or permanent account blocking according to platform policy.</p>" +
                        "</div>" +

                        "<div class='footer'>" +
                        "This PDF is considered a borrowing contract between the borrower and the book owner." +
                        "</div>" +

                        "</body>" +
                        "</html>";

        try {

            PdfRendererBuilder builder = new PdfRendererBuilder();

            builder.withHtmlContent(html, null);
            builder.toStream(new FileOutputStream(fileName));
            builder.run();

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}