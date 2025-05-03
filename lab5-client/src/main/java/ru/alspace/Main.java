package ru.alspace;

import javax.swing.*;

public class Main {
    public static void main(String[] args) {
        // 1. адрес/порт
        JTextField hostField = new JTextField();
        JTextField portField = new JTextField();
        Object[] msg = {
                "Host:", hostField,
                "Port:", portField
        };
        if (JOptionPane.showConfirmDialog(null, msg, "Server address",
                JOptionPane.OK_CANCEL_OPTION) != JOptionPane.OK_OPTION) {
            return;
        }
        String host = hostField.getText().trim();
        int port;
        try {
            port = Integer.parseInt(portField.getText().trim());
        } catch (Exception e) {
            return;
        }

        // 2. имя
        String userName = JOptionPane.showInputDialog("Введите имя пользователя:");
        if (userName == null || userName.trim().isEmpty()) {
            return;
        }

        // 3. выбор протокола
        String[] protocols = {"XML", "Serialization"};
        int choice = JOptionPane.showOptionDialog(null, "Протокол:", "Protocol",
                JOptionPane.DEFAULT_OPTION, JOptionPane.QUESTION_MESSAGE,
                null, protocols, protocols[0]);
        boolean useSer = (choice == 1);

        SwingUtilities.invokeLater(() -> {
            ChatClient client = new ChatClient(host, port, userName, useSer);
            client.setVisible(true);
        });
    }
}
