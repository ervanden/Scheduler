package scheduler;

import java.awt.Color;
import java.awt.Component;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JRadioButton;
import javax.swing.JTextPane;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableCellRenderer;

public class Scheduler extends JPanel implements ActionListener, ListSelectionListener {

    MatrixTableModel tm;

    JTable table;
    JTextPane msgPane;
    JRadioButton onceButton;
    JRadioButton alwaysButton;

    boolean onceMode = false;

    PiClient piClient = new PiClient();

    
    @Override
    public void valueChanged(ListSelectionEvent e) {
        if (!e.getValueIsAdjusting()) {
            int[] selectedRows = table.getSelectedRows();
            int[] selectedColumns = table.getSelectedColumns();
            for (int r = 0; r < selectedRows.length; r++) {
                for (int c = 0; c < selectedColumns.length; c++) {
                    int row = selectedRows[r];
                    int col = selectedColumns[c];
                    tm.setCyclic(row, col, !tm.getCyclic(row, col));
                    tm.setOnce(row, col, onceMode);
                }
            }
            table.clearSelection();

            if (selectedRows.length * selectedRows.length > 0) {
                tm.sendScheduleToServer(piClient, selectedColumns);
            }
        }
    }

    public void actionPerformed(ActionEvent e) {
        String action = e.getActionCommand();
        System.out.println("action " + action);
        executeAction(action);
    }

    void executeAction(String action) {
        if (action.equals("once")) {
            onceMode = true;
        }
        if (action.equals("always")) {
            onceMode = false;
        }
    }

    public Scheduler() {
        super();

        BoxLayout box = new BoxLayout(this, BoxLayout.PAGE_AXIS);
        this.setLayout(box);

        tm = new MatrixTableModel(piClient);

        table = new JTable(tm);
        table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        table.setFillsViewportHeight(true);
        table.setCellSelectionEnabled(true);
        table.getSelectionModel().addListSelectionListener(this);
        table.setDefaultRenderer(String.class, new MyRenderer());

        JScrollPane tableScrollPane = new JScrollPane(table);

        msgPane = new JTextPane();
        JScrollPane msgScrollPane = new JScrollPane(msgPane);

        int width = 300;
        int height = 100;
        Dimension minimumDimension = new Dimension(width, 50);
        Dimension preferredDimension = new Dimension(width, height);
        Dimension maximumDimension = new Dimension(10000, 10000);
        msgPane.setMinimumSize(minimumDimension);
        msgPane.setPreferredSize(preferredDimension);
        msgPane.setMaximumSize(maximumDimension);
        msgScrollPane.setMinimumSize(minimumDimension);
        msgScrollPane.setPreferredSize(preferredDimension);
        msgScrollPane.setMaximumSize(maximumDimension);

        onceButton = new JRadioButton("Set once");
        onceButton.setActionCommand("once");
        onceButton.addActionListener(this);
        alwaysButton = new JRadioButton("Set always");
        alwaysButton.setActionCommand("always");
        alwaysButton.addActionListener(this);
        alwaysButton.setSelected(true);

        //Group the radio buttons.
        ButtonGroup group = new ButtonGroup();
        group.add(onceButton);
        group.add(alwaysButton);

        JPanel ioPane = new JPanel();
        ioPane.setLayout(new BoxLayout(ioPane, BoxLayout.LINE_AXIS));
        ioPane.setBorder(BorderFactory.createEmptyBorder(0, 10, 10, 10));
        ioPane.add(alwaysButton);
        ioPane.add(Box.createRigidArea(new Dimension(10, 0)));
        ioPane.add(onceButton);

        add(tableScrollPane);
        add(ioPane);
        add(msgScrollPane);
    }

    class MyRenderer extends DefaultTableCellRenderer {

        Color backgroundColor = getBackground();

        @Override
        public Component getTableCellRendererComponent(
                JTable table, Object value, boolean isSelected,
                boolean hasFocus, int row, int column) {
            Component c = super.getTableCellRendererComponent(
                    table, value, isSelected, hasFocus, row, column);

            if (tm.getCyclic(row, column)) {
                if (tm.getOnce(row, column)) {
                    c.setBackground(Color.red);
                } else {
                    c.setBackground(Color.pink);
                }
            } else {
                if (tm.getOnce(row, column)) {
                    c.setBackground(Color.blue);
                } else {
                    c.setBackground(Color.cyan);
                }
            }
            return c;
        }
    }

    private static void createAndShowGUI() {
        JFrame frame = new JFrame("Active hours");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        Scheduler newContentPane = new Scheduler();
        newContentPane.setOpaque(true); //content panes must be opaque
        frame.setContentPane(newContentPane);
        frame.pack();
        frame.setVisible(true);
    }

    private static void usage() {
        System.out.println("Usage :");
        System.out.println(" Scheduler.jar client server_ip server_port");
        System.out.println(" Scheduler.jar server");
        System.out.println();
    }

    public static void main(String[] args) {

        if (args.length == 0) {
            usage();
        } else if (args[0].equals("client")) {
            if (args.length != 3) {
                usage();
            } else {
                PiClient.setServerAddress(args[1], Integer.parseInt(args[2]));

                javax.swing.SwingUtilities.invokeLater(new Runnable() {
                    public void run() {
                        createAndShowGUI();
                    }
                });
            }

        } else if (args[0].equals("server")) {
            PiServer srv = new PiServer();
            srv.runServer();
        }
    }
}
