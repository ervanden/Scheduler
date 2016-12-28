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
import javax.swing.JButton;
import javax.swing.JRadioButton;
import javax.swing.JTextPane;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableCellRenderer;

public class Scheduler extends JPanel implements ActionListener, ListSelectionListener {

    MatrixTableModel tm;

    JTable table;
    JTextPane msgPane;
    JButton connectedButton;
    JButton saveButton;
    JButton loadButton;
    JRadioButton onceButton;
    JRadioButton alwaysButton;
    
    boolean onceMode=false;

    piClient piClient = new piClient();

    ArrayList<String> days = new ArrayList<>();
    ArrayList<String> timeslots = new ArrayList<>();

    public void valueChanged(ListSelectionEvent e) {
        if (!e.getValueIsAdjusting()) {
            int[] selectedrows = table.getSelectedRows();
            int[] selectedcolumns = table.getSelectedColumns();
            for (int r = 0; r < selectedrows.length; r++) {
                for (int c = 0; c < selectedcolumns.length; c++) {
                    int row = selectedrows[r];
                    int col = selectedcolumns[c];
                    tm.setCyclic(row, col, !tm.getCyclic(row, col));
                    tm.setOnce(row,col,onceMode);
                }
            }
            table.clearSelection();
        }
    }

    public void actionPerformed(ActionEvent e) {
        String action = e.getActionCommand();
        System.out.println("action " + action);
        executeAction(action);
    }

    void executeAction(String action) {

        if (action.equals("Connected?")) {
            Color buttonColor = connectedButton.getBackground();
            String buttonText = connectedButton.getText();
            connectedButton.setText("pinging pi");
            String reply = piClient.ping("ping");
            if (reply.equals("goed ontvangen: <ping>")) {
                connectedButton.setBackground(Color.green);
            } else {
                connectedButton.setBackground(Color.red);
            }

            connectedButton.setText(buttonText);
            new resetButtonColorThread(connectedButton, buttonColor, buttonText).start();
        }
        if (action.equals("once")) {
onceMode=true;
        }
        if (action.equals("always")) {
onceMode=false;
        }
        if (action.equals("Save")) {

        }
        if (action.equals("Load")) {

        }
    }

    class resetButtonColorThread extends Thread {

        JButton b;
        Color c;
        String t;

        public resetButtonColorThread(JButton b, Color c, String t) {
            super("reset button Thread");
            this.b = b;
            this.c = c;
            this.t = t;
        }

        public void run() {
            try {
                Thread.sleep(2000);
            } catch (InterruptedException ie) {
            };
            b.setBackground(c);
            b.setText(t);
        }
    }

    public Scheduler() {
        super();
        /*
         TimeValue t = new TimeValue();
         t.print();
         for (int i = 0; i < 200; i++) {
         t.add(Calendar.DAY_OF_MONTH, 1);
         t.print();
         }
         */
        BoxLayout box = new BoxLayout(this, BoxLayout.PAGE_AXIS);
        this.setLayout(box);

        tm = new MatrixTableModel();

        table = new JTable(tm);
        table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        //       table.setFillsViewportHeight(true);
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

        connectedButton = new JButton("Connected?");
        connectedButton.addActionListener(this);
        connectedButton.setActionCommand("Connected?");
        saveButton = new JButton("Save");
        saveButton.addActionListener(this);
        saveButton.setActionCommand("Save");
        loadButton = new JButton("Load");
        loadButton.addActionListener(this);
        loadButton.setActionCommand("Load");

        onceButton = new JRadioButton("once");
        onceButton.setActionCommand("once");
        onceButton.addActionListener(this);
        alwaysButton = new JRadioButton("always");
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
        ioPane.add(Box.createRigidArea(new Dimension(10, 0)));
        ioPane.add(saveButton);
        ioPane.add(Box.createRigidArea(new Dimension(10, 0)));
        ioPane.add(loadButton);
        ioPane.add(Box.createRigidArea(new Dimension(10, 0)));
        ioPane.add(connectedButton);

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

        return c ;
    }
}

private static void createAndShowGUI() {
        //Create and set up the window.
        JFrame frame = new JFrame("Active hours");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        //Create and set up the content pane.
        Scheduler newContentPane = new Scheduler();
        newContentPane.setOpaque(true); //content panes must be opaque
        frame.setContentPane(newContentPane);
        frame.pack();
        frame.setVisible(true);
    }

    public static void main(String[] args) {
        javax.swing.SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                createAndShowGUI();
            }
        });
    }
}
