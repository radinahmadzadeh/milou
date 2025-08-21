package aut.ap;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import jakarta.persistence.TypedQuery;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Scanner;
import java.util.Set;

public class Main {
    public static void main(String[] args) {
        EntityManagerFactory emf = Persistence.createEntityManagerFactory("default");
        EmailService service = new EmailService(emf);
        Scanner scanner = new Scanner(System.in);

        User currentUser = null;

        while (true) {
            if (currentUser == null) {
                System.out.print("[L]ogin, [S]ign up, [0] Exit: ");
                String choice = scanner.nextLine().trim().toLowerCase();

                switch (choice) {
                    case "s":
                    case "sign up":
                        System.out.print("Name: ");
                        String name = scanner.nextLine().trim();
                        if (name.equalsIgnoreCase("b")) break;
                        System.out.print("Email: ");
                        String email = scanner.nextLine().trim();
                        if (email.equalsIgnoreCase("b")) break;
                        System.out.print("Password: ");
                        String password = scanner.nextLine().trim();
                        if (password.equalsIgnoreCase("b")) break;

                        if (service.signUp(name, email, password)) {
                            System.out.println("Your new account is created. \n" +
                                    "Go ahead and login!");
                        } else {
                            System.out.println("Sign-up failed. Email might exist or password is too short.");
                        }
                        break;

                    case "l":
                    case "login":
                        System.out.print("Email: ");
                        String emailLogin = scanner.nextLine().trim();
                        if (emailLogin.equalsIgnoreCase("b")) break;
                        System.out.print("Password: ");
                        String passwordLogin = scanner.nextLine().trim();
                        if (passwordLogin.equalsIgnoreCase("b")) break;

                        currentUser = service.login(emailLogin, passwordLogin);
                        if (currentUser != null) {
                            System.out.println("Welcome back, " + currentUser.getName() + "!");
                            List<Email> unread = service.getInbox(currentUser, true);
                            System.out.println("\nUnread Emails:\n");
                            if (unread.isEmpty()) {
                                System.out.println("0 unread emails.");
                            } else {
                                System.out.println(unread.size() + " unread emails:");
                                for (Email e : unread) {
                                    System.out.println("+ " + e.getSender().getEmail() + " - " + e.getSubject() + " (" + e.getCode() + ")");
                                }
                            }
                            System.out.println("[S]end, [V]iew, [R]eply, [F]orward, [L]ogout:");
                        } else {
                            System.out.println("Invalid email or password.");
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
                String choice = scanner.nextLine().trim().toLowerCase();

                switch (choice) {
                    case "v":
                    case "view":
                        System.out.println("[A]ll emails, [U]nread emails, [S]ent emails, Read by [C]ode, [B]ack:");
                        String sub = scanner.nextLine().trim().toLowerCase();
                        if (sub.equals("b") || sub.equals("back")) {
                            System.out.println("[S]end, [V]iew, [R]eply, [F]orward, [L]ogout:");
                            break;
                        }

                        if (sub.equals("a") || sub.equals("all")) {
                            List<Email> all = service.getInbox(currentUser, false);
                            System.out.println("All Emails:");
                            for (Email e : all) {
                                System.out.println("+ " + e.getSender().getEmail() + " - " + e.getSubject() + " (" + e.getCode() + ")");
                            }
                        } else if (sub.equals("u") || sub.equals("unread")) {
                            List<Email> unread = service.getInbox(currentUser, true);
                            System.out.println("Unread Emails:");
                            for (Email e : unread) {
                                System.out.println("+ " + e.getSender().getEmail() + " - " + e.getSubject() + " (" + e.getCode() + ")");
                            }
                        } else if (sub.equals("s") || sub.equals("sent")) {
                            List<Email> sent = service.getSent(currentUser);
                            System.out.println("Sent Emails:");
                            boolean[] visited = new boolean[sent.size()];
                            for (int i = 0; i < sent.size(); i++) {
                                if (visited[i]) continue;
                                Email e = sent.get(i);
                                List<String> recipients = new ArrayList<>();
                                TypedQuery<EmailRecipient> query = emf.createEntityManager().createQuery(
                                        "SELECT er FROM EmailRecipient er WHERE er.email = :email", EmailRecipient.class);
                                query.setParameter("email", e);
                                for (EmailRecipient er : query.getResultList()) {
                                    recipients.add(er.getUser().getEmail());
                                }
                                visited[i] = true;
                                for (int j = i + 1; j < sent.size(); j++) {
                                    Email ej = sent.get(j);
                                    if (ej.getSubject() != null && ej.getSubject().equals(e.getSubject()) && ej.getTime() != null && ej.getTime().equals(e.getTime())) {
                                        TypedQuery<EmailRecipient> query2 = emf.createEntityManager().createQuery(
                                                "SELECT er FROM EmailRecipient er WHERE er.email = :email", EmailRecipient.class);
                                        query2.setParameter("email", ej);
                                        for (EmailRecipient er : query2.getResultList()) {
                                            recipients.add(er.getUser().getEmail());
                                        }
                                        visited[j] = true;
                                    }
                                }
                                String recStr = String.join(", ", recipients);
                                System.out.println("+ " + recStr + " - " + e.getSubject() + " (" + e.getCode() + ")");
                            }
                        } else if (sub.equals("c") || sub.equals("code")) {
                            System.out.print("Code (or [B] to back): ");
                            String code = scanner.nextLine().trim();
                            if (code.equalsIgnoreCase("b")) {
                                System.out.println("[S]end, [V]iew, [R]eply, [F]orward, [L]ogout:");
                                break;
                            }
                            Email emailObj = service.getEmailByCode(currentUser, code);
                            if (emailObj == null) {
                                System.out.println("You cannot read this email.");
                            } else {
                                EntityManager em = emf.createEntityManager();
                                try {
                                    TypedQuery<EmailRecipient> query = em.createQuery(
                                            "SELECT er FROM EmailRecipient er WHERE er.email = :email", EmailRecipient.class);
                                    query.setParameter("email", emailObj);
                                    Set<String> recSet = new HashSet<>();
                                    for (EmailRecipient er : query.getResultList()) {
                                        recSet.add(er.getUser().getEmail());
                                    }
                                    String recipients = String.join(", ", recSet);
                                    DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd");
                                    System.out.println("Code: " + emailObj.getCode());
                                    System.out.println("Recipient(s): " + recipients);
                                    System.out.println("Subject: " + emailObj.getSubject());
                                    System.out.println("Date: " + emailObj.getTime().format(fmt));
                                    System.out.println();
                                    System.out.println(emailObj.getBody());

                                    TypedQuery<EmailRecipient> updateQuery = em.createQuery(
                                            "SELECT er FROM EmailRecipient er WHERE er.email.id = :emailId AND er.user.id = :userId", EmailRecipient.class);
                                    updateQuery.setParameter("emailId", emailObj.getId());
                                    updateQuery.setParameter("userId", currentUser.getId());
                                    List<EmailRecipient> results = updateQuery.getResultList();
                                    if (!results.isEmpty()) {
                                        em.getTransaction().begin();
                                        EmailRecipient er = results.get(0);
                                        er.setRead(true);
                                        em.merge(er);
                                        em.getTransaction().commit();
                                    }
                                } finally {
                                    em.close();
                                }
                            }
                        } else {
                            System.out.println("Invalid choice.");
                        }
                        System.out.println("[S]end, [V]iew, [R]eply, [F]orward, [L]ogout:");
                        break;

                    case "s":
                    case "send":
                        System.out.print("Recipient(s) ([B] to back): ");
                        String recipientInput = scanner.nextLine().trim();
                        if (recipientInput.equalsIgnoreCase("b")) {
                            System.out.println("[S]end, [V]iew, [R]eply, [F]orward, [L]ogout:");
                            break;
                        }
                        List<String> recipients = List.of(recipientInput.split(","));
                        System.out.print("Subject ([B] to back): ");
                        String subject = scanner.nextLine().trim();
                        if (subject.equalsIgnoreCase("b")) {
                            System.out.println("[S]end, [V]iew, [R]eply, [F]orward, [L]ogout:");
                            break;
                        }
                        System.out.print("Body ([B] to back): ");
                        String body = scanner.nextLine().trim();
                        if (body.equalsIgnoreCase("b")) {
                            System.out.println("[S]end, [V]iew, [R]eply, [F]orward, [L]ogout:");
                            break;
                        }

                        String result = service.sendEmail(currentUser, recipients, subject, body);
                        if (result != null && result.startsWith("Error")) {
                            System.out.println(result);
                        } else {
                            System.out.println("Successfully sent your email.");
                            EntityManager em = emf.createEntityManager();
                            try {
                                TypedQuery<Email> query = em.createQuery("SELECT e FROM Email e WHERE e.sender = :sender ORDER BY e.time DESC", Email.class);
                                query.setParameter("sender", currentUser);
                                List<Email> emails = query.setMaxResults(1).getResultList();
                                if (!emails.isEmpty()) {
                                    System.out.println("Code: " + emails.get(0).getCode());
                                }
                            } finally {
                                em.close();
                            }
                        }
                        System.out.println("[S]end, [V]iew, [R]eply, [F]orward, [L]ogout:");
                        break;

                    case "r":
                    case "reply":
                        System.out.print("Code: ");
                        String replyCode = scanner.nextLine().trim();
                        if (replyCode.equalsIgnoreCase("b")) {
                            System.out.println("[S]end, [V]iew, [R]eply, [F]orward, [L]ogout:");
                            break;
                        }
                        System.out.print("Body: ");
                        String replyBody = scanner.nextLine().trim();
                        if (replyBody.equalsIgnoreCase("b")) {
                            System.out.println("[S]end, [V]iew, [R]eply, [F]orward, [L]ogout:");
                            break;
                        }

                        String replyResult = service.replyEmail(currentUser, replyCode, replyBody);
                        if (replyResult != null && replyResult.contains("Code:")) {
                            String newCode = replyResult.split("Code:")[1].trim();
                            System.out.println("Successfully sent your reply to email " + replyCode + ".");
                            System.out.println("Code: " + newCode);
                        } else {
                            System.out.println("Could not find email to reply.");
                        }
                        System.out.println("[S]end, [V]iew, [R]eply, [F]orward, [L]ogout:");
                        break;

                    case "f":
                    case "forward":
                        System.out.print("Code: ");
                        String forwardCode = scanner.nextLine().trim();
                        if (forwardCode.equalsIgnoreCase("b")) {
                            System.out.println("[S]end, [V]iew, [R]eply, [F]orward, [L]ogout:");
                            break;
                        }
                        System.out.print("Recipient(s): ");
                        String forwardRecipientsInput = scanner.nextLine().trim();
                        if (forwardRecipientsInput.equalsIgnoreCase("b")) {
                            System.out.println("[S]end, [V]iew, [R]eply, [F]orward, [L]ogout:");
                            break;
                        }
                        List<String> forwardRecipients = List.of(forwardRecipientsInput.split(","));

                        String forwardResult = service.forwardEmail(currentUser, forwardCode, forwardRecipients);
                        if (forwardResult != null && forwardResult.contains("Code:")) {
                            String newCode = forwardResult.split("Code:")[1].trim();
                            System.out.println("Successfully forwarded your email.");
                            System.out.println("Code: " + newCode);
                        } else {
                            System.out.println("Could not find email to forward.");
                        }
                        System.out.println("[S]end, [V]iew, [R]eply, [F]orward, [L]ogout:");
                        break;

                    case "l":
                    case "logout":
                        currentUser = null;
                        System.out.println("Logged out.");
                        break;

                    default:
                        System.out.println("Invalid choice.");
                }
            }
        }
    }
}