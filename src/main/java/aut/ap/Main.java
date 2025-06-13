package aut.ap;

import jakarta.persistence.Persistence;

import java.util.List;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        var emf = Persistence.createEntityManagerFactory("default");
        var service = new EmailService(emf);
        var scanner = new Scanner(System.in);

        User currentUser = null;

        while (true) {
            System.out.println("\n=== Email System ===");
            if (currentUser == null) {
                System.out.print("[L]ogin, [S]ign up, [0] Exit: ");
                String choice = scanner.nextLine().trim().toLowerCase();

                switch (choice) {
                    case "s":
                    case "sign up":
                        System.out.print("Enter your name: ");
                        String name = scanner.nextLine();
                        System.out.print("Enter your email (without @milou.com): ");
                        String email = scanner.nextLine();
                        System.out.print("Enter your password: ");
                        String password = scanner.nextLine();

                        if (service.signUp(name, email, password)) {
                            System.out.println("✅ Sign-up successful!");
                        } else {
                            System.out.println("❌ Sign-up failed. Email might exist or password is too short.");
                        }
                        break;

                    case "l":
                    case "login":
                        System.out.print("Enter your email (without @milou.com): ");
                        email = scanner.nextLine();
                        System.out.print("Enter your password: ");
                        password = scanner.nextLine();

                        currentUser = service.login(email, password);
                        if (currentUser != null) {
                            System.out.println("Welcome back, " + currentUser.getName() + "!");
                        } else {
                            System.out.println("❌ Invalid email or password.");
                        }
                        break;

                    case "0":
                        System.out.println("Goodbye!");
                        emf.close();
                        return;

                    default:
                        System.out.println("Invalid input. Please enter [L], [S], or [0].");
                }
            } else {
                System.out.println("1. Inbox");
                System.out.println("2. Sent Emails");
                System.out.println("3. Send Email");
                System.out.println("4. Reply");
                System.out.println("5. Forward");
                System.out.println("6. Logout");
                System.out.print("Choose an option: ");
                String choice = scanner.nextLine();

                switch (choice) {
                    case "1":
                        List<Email> inbox = service.getInbox(currentUser, false);
                        if (inbox.isEmpty()) {
                            System.out.println("📭 Inbox is empty.");
                        } else {
                            System.out.println("📥 Your inbox:");
                            for (Email email : inbox) {
                                System.out.println("[" + email.getCode() + "] From: " + email.getSender().getEmail());
                                System.out.println("Subject: " + email.getSubject());
                                System.out.println("Body: " + email.getBody());
                                System.out.println("--------------------");
                            }
                        }
                        break;

                    case "2":
                        List<Email> sent = service.getSent(currentUser);
                        if (sent.isEmpty()) {
                            System.out.println("✉️ You haven't sent any emails.");
                        } else {
                            System.out.println("📤 Sent emails:");
                            for (Email email : sent) {
                                System.out.println("To: " + email.getReceiver().getEmail());
                                System.out.println("Subject: " + email.getSubject());
                                System.out.println("Body: " + email.getBody());
                                System.out.println("--------------------");
                            }
                        }
                        break;

                    case "3":
                        System.out.print("Enter recipient email(s) separated by commas (without @milou.com): ");
                        String recipientInput = scanner.nextLine();
                        List<String> recipients = List.of(recipientInput.split(","));
                        System.out.print("Enter subject: ");
                        String subject = scanner.nextLine();
                        System.out.print("Enter body: ");
                        String body = scanner.nextLine();

                        String result = service.sendEmail(currentUser, recipients, subject, body);
                        System.out.println(result);
                        break;

                    case "4":
                        System.out.print("Enter code of the email to reply to: ");
                        String replyCode = scanner.nextLine();
                        System.out.print("Enter your reply: ");
                        String replyBody = scanner.nextLine();

                        String replyResult = service.replyEmail(currentUser, replyCode, replyBody);
                        if (replyResult != null)
                            System.out.println(replyResult);
                        else
                            System.out.println("❌ Could not find email to reply.");
                        break;

                    case "5":
                        System.out.print("Enter code of the email to forward: ");
                        String forwardCode = scanner.nextLine();
                        System.out.print("Enter new recipient(s) (without @milou.com), separated by commas: ");
                        String forwardRecipientsInput = scanner.nextLine();
                        List<String> forwardRecipients = List.of(forwardRecipientsInput.split(","));

                        String forwardResult = service.forwardEmail(currentUser, forwardCode, forwardRecipients);
                        if (forwardResult != null)
                            System.out.println(forwardResult);
                        else
                            System.out.println("❌ Could not find email to forward.");
                        break;

                    case "6":
                        currentUser = null;
                        System.out.println("🔒 Logged out.");
                        break;

                    default:
                        System.out.println("Invalid choice.");
                }
            }
        }
    }
}
