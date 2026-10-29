package com.proyectocitas.support;

import jakarta.mail.Message;
import jakarta.mail.Session;
import jakarta.mail.internet.MimeMessage;
import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/** Buzón SMTP solo de pruebas: recibe por loopback y nunca reenvía correos. */
public final class LocalSmtpServer implements AutoCloseable {
    private final ServerSocket server;
    private final List<MimeMessage> messages = new ArrayList<>();
    private final ExecutorService workers = Executors.newCachedThreadPool(task -> {
        var thread = new Thread(task, "test-smtp");
        thread.setDaemon(true);
        return thread;
    });

    public LocalSmtpServer() throws IOException {
        server = new ServerSocket();
        server.bind(new InetSocketAddress(InetAddress.getLoopbackAddress(), 0));
        workers.submit(() -> {
            while (!server.isClosed()) {
                try {
                    var socket = server.accept();
                    workers.submit(() -> receive(socket));
                } catch (IOException ex) {
                    if (!server.isClosed()) throw new UncheckedIOException(ex);
                }
            }
        });
    }

    public int port() { return server.getLocalPort(); }

    private void receive(Socket socket) {
        try (socket;
             var reader = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
             var writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8))) {
            socket.setSoTimeout(5000);
            reply(writer, "220 localhost test SMTP");
            for (String command; (command = reader.readLine()) != null;) {
                if (command.startsWith("EHLO") || command.startsWith("HELO")) {
                    reply(writer, "250 localhost");
                } else if (command.startsWith("MAIL FROM") || command.startsWith("RCPT TO") || command.equals("RSET")) {
                    reply(writer, "250 OK");
                } else if (command.equals("DATA")) {
                    reply(writer, "354 End with a dot");
                    var content = new StringBuilder();
                    for (String line; (line = reader.readLine()) != null && !line.equals(".");) {
                        content.append(line.startsWith("..") ? line.substring(1) : line).append("\r\n");
                    }
                    var message = new MimeMessage(Session.getInstance(new Properties()),
                            new ByteArrayInputStream(content.toString().getBytes(StandardCharsets.UTF_8)));
                    synchronized (messages) {
                        messages.add(message);
                        messages.notifyAll();
                    }
                    reply(writer, "250 Accepted");
                } else if (command.equals("QUIT")) {
                    reply(writer, "221 Bye");
                    break;
                } else {
                    reply(writer, "250 OK");
                }
            }
        } catch (Exception ex) {
            // Nunca volcar contenido, direcciones o tokens de mensajes al log de pruebas.
        }
    }

    private void reply(BufferedWriter writer, String text) throws IOException {
        writer.write(text + "\r\n");
        writer.flush();
    }

    public MimeMessage awaitMessage(String recipient, Duration timeout) throws Exception {
        long deadline = System.nanoTime() + timeout.toNanos();
        synchronized (messages) {
            while (true) {
                for (var message : messages) {
                    for (var address : message.getRecipients(Message.RecipientType.TO)) {
                        if (address.toString().equalsIgnoreCase(recipient)) return message;
                    }
                }
                long remaining = deadline - System.nanoTime();
                if (remaining <= 0) throw new AssertionError("No llegó el correo al buzón SMTP local de prueba");
                messages.wait(Math.max(1, remaining / 1_000_000));
            }
        }
    }

    @Override
    public void close() throws IOException {
        server.close();
        workers.shutdownNow();
    }
}
