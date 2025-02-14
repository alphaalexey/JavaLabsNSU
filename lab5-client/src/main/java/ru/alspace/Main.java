package ru.alspace;

import javax.swing.*;

public class Main {
    public static void main(String[] args) {
        String host;
        int port;

        JTextField hostField = new JTextField();
        JTextField portField = new JTextField();
        Object[] message = {
                "Host:", hostField,
                "Port:", portField
        };

        // Запрос адреса сервера
        int option = JOptionPane.showConfirmDialog(
                null,
                message,
                "Server address",
                JOptionPane.OK_CANCEL_OPTION
        );
        if (option == JOptionPane.OK_OPTION) {
            host = hostField.getText().trim();
            try {
                port = Integer.parseUnsignedInt(portField.getText());
            } catch (NumberFormatException e) {
                port = -1;
            }

            if (host.isEmpty() || port == -1) {
                System.exit(1);
            }
        } else {
            System.exit(1);
            return;
        }

        String userName = JOptionPane.showInputDialog("Введите имя пользователя:");
        if (userName == null || userName.trim().isEmpty()) {
            return;
        }

        int finalPort = port;
        SwingUtilities.invokeLater(() -> {
            ChatClient client = new ChatClient(host, finalPort, userName);
            client.setVisible(true);
        });
    }
}
