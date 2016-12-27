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
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JTextPane;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableColumnModel;
import javax.swing.table.DefaultTableModel;

public class Scheduler extends JPanel implements ActionListener, ListSelectionListener {

    JTable table;
    MatrixTableModel tm;
    DefaultTableColumnModel cm;
    JTextPane msgPane;
    JLabel connectedFlag = new JLabel("isConnected?");

    piClient piClient=new piClient();
    
    ArrayList<String> days = new ArrayList<>();
    ArrayList<String> timeslots = new ArrayList<>();
    ArrayList<Float> totals = new ArrayList<>();
    ActiveHoursMap activehours = new ActiveHoursMap();

    public void actionPerformed(ActionEvent e) {
        String action = e.getActionCommand();
        System.out.println("action " + action);
        executeAction(action);
    }

    public void valueChanged(ListSelectionEvent e) {
        if (!e.getValueIsAdjusting()) {
            int[] selectedrows = table.getSelectedRows();
            int[] selectedcolumns = table.getSelectedColumns();
            for (int r = 0; r < selectedrows.length; r++) {
                for (int c = 0; c < selectedcolumns.length; c++) {
//                    System.out.println("Selected row=" + selectedrows[r] + " col=" + selectedcolumns[c]);
                    activehours.toggle(selectedrows[r], selectedcolumns[c]);
                }
            }
            table.clearSelection();
        }
    }

    void executeAction(String action) {

        if (action.equals("Save")) {

        }
        if (action.equals("Load")) {

        }
    }
    
    class testConnectionThread extends Thread {
    JLabel result;
    public testConnectionThread(JLabel result) {
        super("testConnection Thread");
        this.result = result;
    }

    public void run() {
        while (true){
            System.out.println("pinging pi");
            String reply=piClient.ping("ping");
            if (reply.equals("goed ontvangen: <ping>")) {
                result.setText("Connected");
                result.setForeground(Color.GREEN);
            } else {
                result.setText("Disconnected");
                                result.setForeground(Color.RED);
            }
            try {Thread.sleep(1000);} catch (InterruptedException ie){};
        }
    }
}

    public Scheduler() {
        super();

         new testConnectionThread(connectedFlag).start();
         
        BoxLayout box = new BoxLayout(this, BoxLayout.PAGE_AXIS);
        this.setLayout(box);

        tm = new MatrixTableModel();

        for (int hr = 0; hr <= 23; hr++) {
            for (int min = 0; min <= 45; min = min + 15) {
                timeslots.add(hr + ":" + min);
            }
        }

        days.add("monday");
        days.add("tuesday");
        days.add("wednesday");
        days.add("thursday");
        days.add("friday");
        days.add("saturday");
        days.add("sunday");

        table = new JTable(tm);
        table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        //       table.setFillsViewportHeight(true);
        table.setCellSelectionEnabled(true);
        table.getSelectionModel().addListSelectionListener(this);
        table.setDefaultRenderer(String.class, new MyRenderer());

        for (int row = 0; row < days.size(); row++) {
            for (int col = 0; col < timeslots.size(); col++) {
                activehours.put(row, col, false);
            }
        }

        JScrollPane tableScrollPane = new JScrollPane(table);
        
        msgPane=new JTextPane();
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

        JButton saveButton = new JButton("Save");
        saveButton.addActionListener(this);
        saveButton.setActionCommand("Save");
        JButton loadButton = new JButton("Load");
        loadButton.addActionListener(this);
        loadButton.setActionCommand("Load");

        JPanel ioPane = new JPanel();
        ioPane.setLayout(new BoxLayout(ioPane, BoxLayout.LINE_AXIS));
        ioPane.setBorder(BorderFactory.createEmptyBorder(0, 10, 10, 10));
//        buttonPane.add(Box.createHorizontalGlue());
        ioPane.add(connectedFlag);
                ioPane.add(Box.createRigidArea(new Dimension(10, 0)));
        ioPane.add(saveButton);
        ioPane.add(Box.createRigidArea(new Dimension(10, 0)));
        ioPane.add(loadButton);

        add(tableScrollPane);
        add(ioPane);
        add(msgScrollPane);
    }

    class ActiveHoursMap {

        HashMap<String, Boolean> m = new HashMap<>();

        public ActiveHoursMap() {
            m.clear();
        }

        public void put(int row, int col, boolean value) {
            m.put(row + "|" + col, value);
        }

        public void toggle(int row, int col) {
            m.put(row + "|" + col, !m.get(row + "|" + col));
        }

        public boolean get(int row, int col) {
            return m.get(row + "|" + col);
        }
    }

    class MatrixTableModel extends DefaultTableModel {

        public int getColumnCount() {
            return timeslots.size();
        }

        public int getRowCount() {
            return days.size();
        }

        public String getColumnName(int col) {
            return null; //days.get(col);
        }

        public Object getValueAt(int row, int col) {
            return timeslots.get(col);
        }

        public Class getColumnClass(int c) {
            String s = "";
            return s.getClass();
        }

        public boolean isCellEditable(int row, int col) {
            return false;
        }

        public void setValueAt(Object value, int row, int col) {

            // fireTableDataChanged();
        }
    }

    class MyRenderer extends DefaultTableCellRenderer {

        Color backgroundColor = getBackground();

        @Override
        public Component getTableCellRendererComponent(
                JTable table, Object value, boolean isSelected,
                boolean hasFocus, int row, int column) {
            Component c = super.getTableCellRendererComponent(
                    table, value, isSelected, hasFocus, row, column);

            if (activehours.get(row, column)) {
                c.setBackground(Color.green.darker());
            } else if (!isSelected) {
                c.setBackground(backgroundColor);
            }
            return c;
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
